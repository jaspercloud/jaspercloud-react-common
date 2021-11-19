package io.jaspercloud.react.config;

import io.jaspercloud.react.http.client.HttpConfig;
import io.jaspercloud.react.loadbalancer.DiscoveryInstanceChooser;
import io.jaspercloud.react.loadbalancer.InstanceChooseRule;
import io.jaspercloud.react.loadbalancer.LoadBalancerRequestInterceptor;
import io.jaspercloud.react.loadbalancer.ReactiveServiceInstanceChooser;
import io.jaspercloud.react.loadbalancer.RoundRobinInstanceChooseRule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(DiscoveryClient.class)
@Configuration
public class LoadBalancerConfiguration {

    @ConditionalOnMissingBean(InstanceChooseRule.class)
    @Bean
    public RoundRobinInstanceChooseRule roundRobinInstanceChooseRule() {
        return new RoundRobinInstanceChooseRule();
    }

    @ConditionalOnMissingBean(ReactiveServiceInstanceChooser.class)
    @Bean
    public DiscoveryInstanceChooser reactServiceInstanceChooser(DiscoveryClient discoveryClient,
                                                                HttpConfig httpConfig,
                                                                InstanceChooseRule chooseRule) {
        return new DiscoveryInstanceChooser(discoveryClient, httpConfig.getDiscoveryTimeout(), chooseRule);
    }

    @Bean
    public LoadBalancerRequestInterceptor loadBalancerRequestInterceptor(DiscoveryInstanceChooser instanceChooser) {
        return new LoadBalancerRequestInterceptor(instanceChooser);
    }
}
