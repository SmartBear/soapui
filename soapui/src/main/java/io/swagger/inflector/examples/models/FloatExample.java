package io.swagger.inflector.examples.models;

public class FloatExample extends AbstractExample {
    private Float value;

    public FloatExample() {
        super.setTypeName("number");
    }

    public FloatExample value(Float value) {
        this.setValue(value);
        return this;
    }

    public Float getValue() {
        return this.value;
    }

    public void setValue(Float value) {
        this.value = value;
    }
}
