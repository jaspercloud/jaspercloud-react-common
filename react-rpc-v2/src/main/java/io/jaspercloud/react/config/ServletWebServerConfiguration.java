package io.jaspercloud.react.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ServletWebServerConfiguration implements InitializingBean {

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

    @Bean
    public TomcatConnectorCustomizer tomcatConnectorHttp2Customizer() {
        return new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.addUpgradeProtocol(new Http2Protocol());
            }
        };
    }
}
