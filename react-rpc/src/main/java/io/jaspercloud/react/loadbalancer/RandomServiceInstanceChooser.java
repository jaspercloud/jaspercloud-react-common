package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.mono.AsyncMono;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class RandomServiceInstanceChooser implements ReactiveServiceInstanceChooser {

    private DiscoveryClient discoveryClient;
    private long timeout;

    public RandomServiceInstanceChooser(DiscoveryClient discoveryClient, long timeout) {
        this.discoveryClient = discoveryClient;
        this.timeout = timeout;
    }

    @Override
    public AsyncMono<ServiceInstance> chooseAsync(String serviceId) {
        AsyncMono<ServiceInstance> asyncMono = AsyncMono.<ServiceInstance>create(sink -> {
            sink.success(choose(serviceId));
        }).subscribeOn(Schedulers.boundedElastic()).timeout(timeout);
        return asyncMono;
    }

    @Override
    public ServiceInstance choose(String serviceId) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        int i = RandomUtils.nextInt(0, instances.size());
        ServiceInstance instance = instances.get(i);
        return instance;
    }
}
