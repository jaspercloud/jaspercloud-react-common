package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.mono.AsyncMono;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public interface InstanceChooseRule {

    AsyncMono<ServiceInstance> choose(String serviceId, List<ServiceInstance> instanceList);
}
