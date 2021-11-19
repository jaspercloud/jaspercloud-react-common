package io.jaspercloud.react.template;

public class ReturnTemplate {

    private Class<?> returnClass;
    private Class<?> rawType;

    public Class<?> getReturnClass() {
        return returnClass;
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public ReturnTemplate(Class<?> returnClass, Class<?> rawType) {
        this.returnClass = returnClass;
        this.rawType = rawType;
    }
}
