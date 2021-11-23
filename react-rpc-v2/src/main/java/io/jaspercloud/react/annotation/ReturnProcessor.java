package io.jaspercloud.react.annotation;

import io.jaspercloud.react.template.RequestTemplate;
import io.jaspercloud.react.template.ReturnTemplate;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReturnProcessor implements AnnotationProcessor {

    @Override
    public void process(Class<?> clazz, Method method, RequestTemplate template) {
        Class<?> returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
            Type type = parameterizedType.getActualTypeArguments()[0];
            if (type instanceof ParameterizedType) {
                Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
                ReturnTemplate returnTemplate = new ReturnTemplate(returnType, rawType, genericReturnType);
                template.setReturnTemplate(returnTemplate);
            } else if (type instanceof Class<?>) {
                Class<?> rawType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                ReturnTemplate returnTemplate = new ReturnTemplate(returnType, rawType, genericReturnType);
                template.setReturnTemplate(returnTemplate);
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            ReturnTemplate returnTemplate = new ReturnTemplate(returnType, null, genericReturnType);
            template.setReturnTemplate(returnTemplate);
        }
    }
}
