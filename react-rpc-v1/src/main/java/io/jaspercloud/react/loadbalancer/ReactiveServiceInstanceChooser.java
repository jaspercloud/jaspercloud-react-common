package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.mono.AsyncMono;
import org.springframework.cloud.client.ServiceInstance;

public interface ReactiveServiceInstanceChooser {

    AsyncMono<ServiceInstance> choose(String serviceId);
}
