package io.jaspercloud.react.config;

import io.jaspercloud.react.annotation.PathVariableProcessor;
import io.jaspercloud.react.annotation.RequestBodyProcessor;
import io.jaspercloud.react.annotation.RequestHeaderProcessor;
import io.jaspercloud.react.annotation.RequestMappingProcessor;
import io.jaspercloud.react.annotation.RequestParamProcessor;
import io.jaspercloud.react.annotation.ReturnProcessor;
import io.jaspercloud.react.http.client.ReactHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactRpcConfiguration {

    @ConditionalOnMissingBean(ReactHttpClient.class)
    @Bean
    public ReactHttpClient reactHttpClient() {
        return new ReactHttpClient();
    }

    @Bean
    public ReturnProcessor returnProcessor() {
        return new ReturnProcessor();
    }

    @Bean
    public RequestMappingProcessor requestMappingProcessor() {
        return new RequestMappingProcessor();
    }

    @Bean
    public PathVariableProcessor pathVariableProcessor() {
        return new PathVariableProcessor();
    }

    @Bean
    public RequestParamProcessor requestParamProcessor() {
        return new RequestParamProcessor();
    }

    @Bean
    public RequestHeaderProcessor requestHeaderProcessor() {
        return new RequestHeaderProcessor();
    }

    @Bean
    public RequestBodyProcessor requestBodyProcessor() {
        return new RequestBodyProcessor();
    }
}
