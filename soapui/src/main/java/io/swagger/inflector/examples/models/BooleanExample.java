package io.swagger.inflector.examples.models;

public class BooleanExample extends AbstractExample {
    private Boolean value;

    public BooleanExample() {
        super.setTypeName("boolean");
    }

    public BooleanExample value(Boolean value) {
        this.setValue(value);
        return this;
    }

    public Boolean getValue() {
        return this.value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
