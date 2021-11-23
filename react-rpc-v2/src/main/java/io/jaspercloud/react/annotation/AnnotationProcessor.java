package io.jaspercloud.react.annotation;

import io.jaspercloud.react.template.RequestTemplate;

import java.lang.reflect.Method;

public interface AnnotationProcessor {

    void process(Class<?> clazz, Method method, RequestTemplate template);
}
