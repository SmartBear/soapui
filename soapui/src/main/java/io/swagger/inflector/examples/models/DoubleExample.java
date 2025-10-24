package io.swagger.inflector.examples.models;

public class DoubleExample extends AbstractExample {
    private Double value;

    public DoubleExample() {
        super.setTypeName("number");
    }

    public DoubleExample value(Double value) {
        this.setValue(value);
        return this;
    }

    public Double getValue() {
        return this.value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
