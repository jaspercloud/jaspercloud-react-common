package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.config.ReactProperties;
import io.jaspercloud.react.mono.AsyncMono;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class DefaultReactiveDiscoveryClient implements ReactDiscoveryClient {

    private ReactiveDiscoveryClient discoveryClient;
    private ReactProperties reactProperties;

    public DefaultReactiveDiscoveryClient(ReactiveDiscoveryClient discoveryClient, ReactProperties reactProperties) {
        this.discoveryClient = discoveryClient;
        this.reactProperties = reactProperties;
    }

    @Override
    public AsyncMono<List<ServiceInstance>> getInstances(String serviceId) {
        AsyncMono<List<ServiceInstance>> asyncMono = AsyncMono.<List<ServiceInstance>>create(sink -> {
            discoveryClient.getInstances(serviceId).collectList()
                    .subscribe(new BaseSubscriber<List<ServiceInstance>>() {
                        @Override
                        protected void hookOnNext(List<ServiceInstance> list) {
                            sink.success(list);
                        }

                        @Override
                        protected void hookOnError(Throwable throwable) {
                            sink.error(throwable);
                        }
                    });
        }).subscribeOn(Schedulers.boundedElastic()).timeout(reactProperties.getDiscoveryTimeout());
        return asyncMono;
    }
}
