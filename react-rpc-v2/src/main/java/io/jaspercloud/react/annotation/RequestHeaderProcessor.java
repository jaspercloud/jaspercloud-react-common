package io.jaspercloud.react.annotation;

import io.jaspercloud.react.template.ParameterTemplate;
import io.jaspercloud.react.template.RequestTemplate;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class RequestHeaderProcessor implements AnnotationProcessor {

    @Override
    public void process(Class<?> clazz, Method method, RequestTemplate template) {
        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            RequestHeader requestHeader = AnnotatedElementUtils.findMergedAnnotation(parameter, RequestHeader.class);
            if (null == requestHeader) {
                continue;
            }
            String defaultValue = ValueConstants.DEFAULT_NONE.equals(requestHeader.defaultValue()) ? null : requestHeader.defaultValue();
            template.getHeaders().add(new ParameterTemplate(requestHeader.required(), requestHeader.value(), defaultValue, i));
        }
    }
}
