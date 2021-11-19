package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.exception.ReactRpcException;
import io.jaspercloud.react.mono.AsyncMono;
import io.jaspercloud.react.mono.ReactAsyncCall;
import io.jaspercloud.react.mono.ReactSink;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public class DiscoveryInstanceChooser implements ReactiveServiceInstanceChooser {

    private ReactiveDiscoveryClient discoveryClient;
    private InstanceChooseRule chooserRule;

    public DiscoveryInstanceChooser(ReactiveDiscoveryClient discoveryClient, InstanceChooseRule chooserRule) {
        this.discoveryClient = discoveryClient;
        this.chooserRule = chooserRule;
    }

    @Override
    public AsyncMono<ServiceInstance> chooseAsync(String serviceId) {
        AsyncMono<ServiceInstance> asyncMono = discoveryClient.getInstances(serviceId)
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
