package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.config.ReactProperties;
import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import org.springframework.cloud.client.ServiceInstance;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CacheDiscoveryClient implements ReactiveDiscoveryClient {

    private ReactiveDiscoveryClient discoveryClient;
    private Map<String, List<ServiceInstance>> cache = new ConcurrentHashMap<>();

    public CacheDiscoveryClient(ReactiveDiscoveryClient discoveryClient,
                                ReactProperties reactProperties) {
        this.discoveryClient = discoveryClient;
        new ScheduledThreadPoolExecutor(1, new ThreadFactory() {

            private AtomicLong counter = new AtomicLong();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "CacheDiscoveryClient-" + counter.incrementAndGet());
            }
        }).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (String serviceId : cache.keySet()) {
                    List<ServiceInstance> list = cache.get(serviceId);
                    List<ServiceInstance> tmp = new ArrayList<>();
                    AtomicInteger counter = new AtomicInteger(list.size());
                    for (ServiceInstance instance : list) {
                        HealthPing.check(instance.getHost(), instance.getPort(), reactProperties.getHealthTimeout())
                                .subscribe(new BaseSubscriber<Boolean>() {
                                    @Override
                                    protected void hookOnNext(Boolean status) {
                                        if (status) {
                                            synchronized (tmp) {
                                                tmp.add(instance);
                                            }
                                        }
                                    }

                                    @Override
                                    protected void hookFinally(SignalType type) {
                                        int ret = counter.decrementAndGet();
                                        if (0 != ret) {
                                            return;
                                        }
                                        if (tmp.isEmpty()) {
                                            cache.remove(serviceId);
                                            return;
                                        }
                                        cache.put(serviceId, tmp);
                                    }
                                });
                    }
                }
            }
        }, 0, reactProperties.getHealthTime(), TimeUnit.MILLISECONDS);
    }

    @Override
    public AsyncMono<List<ServiceInstance>> getInstances(String serviceId) {
        List<ServiceInstance> instances = cache.get(serviceId);
        if (null != instances) {
            return new AsyncMono<>(instances);
        }
        AsyncMono<List<ServiceInstance>> asyncMono = discoveryClient.getInstances(serviceId)
                .then(new ReactAsyncCall<List<ServiceInstance>, List<ServiceInstance>>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, List<ServiceInstance> result, ReactSink<? super List<ServiceInstance>> sink) throws Throwable {
                        if (hasError) {
                            sink.finish();
                            return;
                        }
                        cache.put(serviceId, result);
                        sink.success(result);
                    }
                });
        return asyncMono;
    }
}
