package io.jaspercloud.react.annotation;

import io.jaspercloud.react.template.ParameterTemplate;
import io.jaspercloud.react.template.RequestTemplate;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class PathVariableProcessor implements AnnotationProcessor {

    @Override
    public void process(Class<?> clazz, Method method, RequestTemplate template) {
        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            PathVariable pathVariable = AnnotatedElementUtils.findMergedAnnotation(parameter, PathVariable.class);
            if (null == pathVariable) {
                continue;
            }
            template.getPathVariables().add(new ParameterTemplate(pathVariable.required(), pathVariable.value(), null, i));
        }
    }
}
