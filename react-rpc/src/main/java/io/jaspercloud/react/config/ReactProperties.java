package io.jaspercloud.react.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("react")
public class ReactProperties {

    /**
     * loadbalancer
     */
    private long discoveryTimeout = 10 * 1000;

    public long getDiscoveryTimeout() {
        return discoveryTimeout;
    }

    public void setDiscoveryTimeout(long discoveryTimeout) {
        this.discoveryTimeout = discoveryTimeout;
    }
}
