package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.mono.AsyncMono;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinInstanceChooseRule implements InstanceChooseRule {

    private AtomicInteger counter = new AtomicInteger();

    @Override
    public AsyncMono<ServiceInstance> choose(String serviceId, List<ServiceInstance> instanceList) {
        int pos = Math.abs(counter.incrementAndGet());
        ServiceInstance instance = instanceList.get(pos % instanceList.size());
        return new AsyncMono<>(instance);
    }
}
