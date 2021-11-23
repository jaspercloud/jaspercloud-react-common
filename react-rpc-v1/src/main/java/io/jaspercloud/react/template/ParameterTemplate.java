package io.jaspercloud.react.template;

public class ParameterTemplate {

    private boolean required;
    private String name;
    private String defaultValue;
    private Integer index;

    public boolean isRequired() {
        return required;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Integer getIndex() {
        return index;
    }

    public ParameterTemplate(boolean required, String name, String defaultValue, Integer index) {
        this.required = required;
        this.name = name;
        this.defaultValue = defaultValue;
        this.index = index;
    }
}
