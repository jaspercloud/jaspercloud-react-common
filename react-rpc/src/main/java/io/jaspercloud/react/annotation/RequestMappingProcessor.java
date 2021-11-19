package io.jaspercloud.react.annotation;

import io.jaspercloud.react.template.RequestTemplate;
import io.jaspercloud.react.template.UriTemplate;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;

public class RequestMappingProcessor implements AnnotationProcessor {

    @Override
    public void process(Class<?> clazz, Method method, RequestTemplate template) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        if (null == requestMapping) {
            throw new IllegalArgumentException("not found RequestMapping");
        }
        if (requestMapping.value().length == 0) {
            throw new IllegalArgumentException("not found RequestMapping.value");
        }
        if (requestMapping.value().length > 1) {
            throw new IllegalArgumentException("RequestMapping.value > 1");
        }
        if (requestMapping.method().length <= 0) {
            throw new IllegalArgumentException("not found RequestMapping.method");
        }
        if (requestMapping.method().length > 1) {
            throw new IllegalArgumentException("not found RequestMapping.method > 1");
        }
        RequestMethod requestMethod = requestMapping.method()[0];
        String path = requestMapping.value()[0];
        template.setMethod(requestMethod.name());
        UriTemplate uriTemplate = new UriTemplate(path);
        template.setUriTemplate(uriTemplate);
    }
}
