package io.jaspercloud.react.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("react")
public class ReactProperties {

    /**
     * loadbalancer
     */
    private long discoveryTimeout = 10 * 1000;
    private int healthTime = 15 * 1000;
    private int healthTimeout = 5 * 1000;

    public long getDiscoveryTimeout() {
        return discoveryTimeout;
    }

    public void setDiscoveryTimeout(long discoveryTimeout) {
        this.discoveryTimeout = discoveryTimeout;
    }

    public int getHealthTime() {
        return healthTime;
    }

    public void setHealthTime(int healthTime) {
        this.healthTime = healthTime;
    }

    public int getHealthTimeout() {
        return healthTimeout;
    }

    public void setHealthTimeout(int healthTimeout) {
        this.healthTimeout = healthTimeout;
    }
}
