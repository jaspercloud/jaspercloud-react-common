package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.mono.AsyncMono;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.ServiceInstanceChooser;

public interface ReactiveServiceInstanceChooser extends ServiceInstanceChooser {

    AsyncMono<ServiceInstance> chooseAsync(String serviceId);
}
