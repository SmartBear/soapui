package io.swagger.inflector.examples.models;

public class IntegerExample extends AbstractExample {
    private Integer value;

    public IntegerExample() {
        super.setTypeName("integer");
    }

    public IntegerExample value(Integer value) {
        this.setValue(value);
        return this;
    }

    public Integer getValue() {
        return this.value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
