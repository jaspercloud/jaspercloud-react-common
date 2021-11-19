package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.exception.ReactRpcException;
import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class DiscoveryInstanceChooser implements ReactiveServiceInstanceChooser {

    private DiscoveryClient discoveryClient;
    private long timeout;
    private InstanceChooseRule chooserRule;

    public DiscoveryInstanceChooser(DiscoveryClient discoveryClient, long timeout, InstanceChooseRule chooserRule) {
        this.discoveryClient = discoveryClient;
        this.timeout = timeout;
        this.chooserRule = chooserRule;
    }

    @Override
    public AsyncMono<ServiceInstance> chooseAsync(String serviceId) {
        AsyncMono<ServiceInstance> asyncMono = AsyncMono.<List<ServiceInstance>>create(sink -> {
            List<ServiceInstance> instanceList = discoveryClient.getInstances(serviceId);
            sink.success(instanceList);
        }).subscribeOn(Schedulers.boundedElastic()).timeout(timeout)
                .then(new ReactAsyncCall<List<ServiceInstance>, ServiceInstance>() {
                    @Override
                    public void process(boolean hasError, Throwable throwable, List<ServiceInstance> instances, ReactSink<? super ServiceInstance> sink) throws Throwable {
                        if (hasError) {
                            sink.finish();
                            return;
                        }
                        if (instances.isEmpty()) {
                            sink.error(new ReactRpcException("not found instances"));
                            return;
                        }
                        chooserRule.choose(serviceId, instances).subscribe(sink);
                    }
                });
        return asyncMono;
    }
}
