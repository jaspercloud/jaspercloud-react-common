package io.jaspercloud.react.loadbalancer;

import io.jaspercloud.react.mono.AsyncMono;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public interface ReactDiscoveryClient {

    AsyncMono<List<ServiceInstance>> getInstances(String serviceId);
}
