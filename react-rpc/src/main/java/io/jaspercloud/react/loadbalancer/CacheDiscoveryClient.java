package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.config.ReactProperties;
import io.jaspercloud.react.mono.AsyncMono;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class CacheDiscoveryClient implements ReactiveDiscoveryClient {

    private DiscoveryClient discoveryClient;
    private ReactProperties reactProperties;

    public CacheDiscoveryClient(DiscoveryClient discoveryClient, ReactProperties reactProperties) {
        this.discoveryClient = discoveryClient;
        this.reactProperties = reactProperties;
    }

    @Override
    public AsyncMono<List<ServiceInstance>> getInstances(String serviceId) {
        AsyncMono<List<ServiceInstance>> asyncMono = AsyncMono.<List<ServiceInstance>>create(sink -> {
            List<ServiceInstance> instanceList = discoveryClient.getInstances(serviceId);
            sink.success(instanceList);
        }).subscribeOn(Schedulers.boundedElastic()).timeout(reactProperties.getDiscoveryTimeout());
        return asyncMono;
    }
}
