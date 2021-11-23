package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.mono.AsyncMono;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public class RandomInstanceChooseRule implements InstanceChooseRule {

    @Override
    public AsyncMono<ServiceInstance> choose(String serviceId, List<ServiceInstance> instanceList) {
        int i = RandomUtils.nextInt(0, instanceList.size());
        ServiceInstance instance = instanceList.get(i);
        return new AsyncMono<>(instance);
    }
}
