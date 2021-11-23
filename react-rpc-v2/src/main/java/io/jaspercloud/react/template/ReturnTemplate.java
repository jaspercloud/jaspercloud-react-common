package io.jaspercloud.react.template;

import java.lang.reflect.Type;

public class ReturnTemplate {

    private Class<?> returnClass;
    private Class<?> rawType;
    private Type genericReturnType;

    public Class<?> getReturnClass() {
        return returnClass;
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public Type getGenericReturnType() {
        return genericReturnType;
    }

    public ReturnTemplate(Class<?> returnClass, Class<?> rawType, Type genericReturnType) {
        this.returnClass = returnClass;
        this.rawType = rawType;
        this.genericReturnType = genericReturnType;
    }
}
