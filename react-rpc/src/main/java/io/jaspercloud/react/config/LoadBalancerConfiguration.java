package io.jaspercloud.react.config;

import io.jaspercloud.react.http.client.HttpConfig;
import io.jaspercloud.react.loadbalancer.LoadBalancerRequestInterceptor;
import io.jaspercloud.react.loadbalancer.RandomServiceInstanceChooser;
import io.jaspercloud.react.loadbalancer.ReactiveServiceInstanceChooser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(DiscoveryClient.class)
@Configuration
public class LoadBalancerConfiguration {

    @ConditionalOnMissingBean(ReactiveServiceInstanceChooser.class)
    @Bean
    public RandomServiceInstanceChooser reactServiceInstanceChooser(DiscoveryClient discoveryClient, HttpConfig httpConfig) {
        return new RandomServiceInstanceChooser(discoveryClient, httpConfig.getDiscoveryTimeout());
    }

    @Bean
    public LoadBalancerRequestInterceptor loadBalancerRequestInterceptor(RandomServiceInstanceChooser instanceChooser) {
        return new LoadBalancerRequestInterceptor(instanceChooser);
    }
}
