package io.jaspercloud.react.annotation;

import io.jaspercloud.react.template.ParameterTemplate;
import io.jaspercloud.react.template.RequestTemplate;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class RequestParamProcessor implements AnnotationProcessor {

    @Override
    public void process(Class<?> clazz, Method method, RequestTemplate template) {
        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            RequestParam requestParam = AnnotatedElementUtils.findMergedAnnotation(parameter, RequestParam.class);
            if (null == requestParam) {
                continue;
            }
            String defaultValue = ValueConstants.DEFAULT_NONE.equals(requestParam.defaultValue()) ? null : requestParam.defaultValue();
            template.getParams().add(new ParameterTemplate(requestParam.required(), requestParam.value(), defaultValue, i));
        }
    }
}
