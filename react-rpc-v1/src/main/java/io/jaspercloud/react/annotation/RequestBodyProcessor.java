package io.jaspercloud.react.annotation;

import io.jaspercloud.react.template.RequestTemplate;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class RequestBodyProcessor implements AnnotationProcessor {

    @Override
    public void process(Class<?> clazz, Method method, RequestTemplate template) {
        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            RequestBody requestBody = AnnotatedElementUtils.findMergedAnnotation(parameter, RequestBody.class);
            if (null == requestBody) {
                continue;
            }
            if (null != template.getBodyIndex()) {
                throw new IllegalArgumentException("found body > 1");
            }
            template.setBodyIndex(i);
        }
    }
}
