package io.jaspercloud.react.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.reactive.server.ConfigurableReactiveWebServerFactory;
import org.springframework.boot.web.server.Http2;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class ReactiveWebServerConfiguration implements InitializingBean {

    @Autowired(required = false)
    private Registration registration;

    @Override
    public void afterPropertiesSet() {
        if (null == registration) {
            return;
        }
        Map<String, String> metadata = registration.getMetadata();
        metadata.put("http2", "true");
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @Bean
    public WebServerFactoryCustomizer<ConfigurableReactiveWebServerFactory> http2WebServerFactoryCustomizer() {
        return new WebServerFactoryCustomizer<ConfigurableReactiveWebServerFactory>() {
            @Override
            public void customize(ConfigurableReactiveWebServerFactory factory) {
                Http2 http2 = new Http2();
                http2.setEnabled(true);
                factory.setHttp2(http2);
            }
        };
    }

    @ConditionalOnMissingBean(StringHttpMessageConverter.class)
    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        return new StringHttpMessageConverter();
    }

    @ConditionalOnMissingBean(ByteArrayHttpMessageConverter.class)
    @Bean
    public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
        return new ByteArrayHttpMessageConverter();
    }

    @ConditionalOnMissingBean(MappingJackson2HttpMessageConverter.class)
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }

    @Bean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }
}
