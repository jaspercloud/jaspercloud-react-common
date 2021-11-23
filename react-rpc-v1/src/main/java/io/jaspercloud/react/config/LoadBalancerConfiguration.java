package io.jaspercloud.react.config;

import io.jaspercloud.react.loadbalancer.CacheDiscoveryClient;
import io.jaspercloud.react.loadbalancer.DefaultDiscoveryClient;
import io.jaspercloud.react.loadbalancer.DiscoveryInstanceChooser;
import io.jaspercloud.react.loadbalancer.InstanceChooseRule;
import io.jaspercloud.react.loadbalancer.LoadBalancerRequestInterceptor;
import io.jaspercloud.react.loadbalancer.ReactiveDiscoveryClient;
import io.jaspercloud.react.loadbalancer.ReactiveServiceInstanceChooser;
import io.jaspercloud.react.loadbalancer.RoundRobinInstanceChooseRule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(ReactProperties.class)
@ConditionalOnClass(DiscoveryClient.class)
@Configuration
public class LoadBalancerConfiguration {

    @ConditionalOnMissingBean(ReactiveDiscoveryClient.class)
    @Bean
    public ReactiveDiscoveryClient defaultDiscoveryClient(DiscoveryClient discoveryClient, ReactProperties reactProperties) {
        return new DefaultDiscoveryClient(discoveryClient, reactProperties);
    }

    @Bean
    public CacheDiscoveryClient cacheDiscoveryClient(ReactiveDiscoveryClient discoveryClient, ReactProperties reactProperties) {
        return new CacheDiscoveryClient(discoveryClient, reactProperties);
    }

    @ConditionalOnMissingBean(InstanceChooseRule.class)
    @Bean
    public InstanceChooseRule roundRobinInstanceChooseRule() {
        return new RoundRobinInstanceChooseRule();
    }

    @ConditionalOnMissingBean(ReactiveServiceInstanceChooser.class)
    @Bean
    public ReactiveServiceInstanceChooser reactServiceInstanceChooser(CacheDiscoveryClient discoveryClient,
                                                                      InstanceChooseRule chooseRule) {
        return new DiscoveryInstanceChooser(discoveryClient, chooseRule);
    }

    @Bean
    public LoadBalancerRequestInterceptor loadBalancerRequestInterceptor(ReactiveServiceInstanceChooser instanceChooser) {
        return new LoadBalancerRequestInterceptor(instanceChooser);
    }
}
