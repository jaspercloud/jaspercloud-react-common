package io.jaspercloud.react;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RpcClient {

    @AliasFor("name")
    String value() default "";

    // TODO: 2021/11/22
    String contextId() default "";

    @AliasFor("value")
    String name() default "";

    String qualifier() default "";

    String[] qualifiers() default {};

    String url() default "";

    // TODO: 2021/11/22
    boolean decode404() default false;

    // TODO: 2021/11/22
    Class<?>[] configuration() default {};

    // TODO: 2021/11/22
    Class<?> fallback() default void.class;

    // TODO: 2021/11/22
    Class<?> fallbackFactory() default void.class;

    String path() default "";

    boolean primary() default true;
}
