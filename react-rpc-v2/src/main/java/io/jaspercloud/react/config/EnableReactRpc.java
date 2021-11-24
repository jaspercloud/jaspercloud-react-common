package io.jaspercloud.react.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ReactRpcRegistrar.class})
@ImportAutoConfiguration({
        ReactRpcConfiguration.class,
        LoadBalancerConfiguration.class,
        ServletWebServerConfiguration.class,
        ReactiveWebServerConfiguration.class
})
public @interface EnableReactRpc {

    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    // TODO: 2021/11/22
    Class<?>[] defaultConfiguration() default {};

    // TODO: 2021/11/22
    Class<?>[] clients() default {};
}
