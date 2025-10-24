package io.swagger.inflector.examples.models;

public class StringExample extends AbstractExample {
    private String value;

    public StringExample() {
        super.setTypeName("string");
    }

    public StringExample value(String value) {
        this.setValue(value);
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
