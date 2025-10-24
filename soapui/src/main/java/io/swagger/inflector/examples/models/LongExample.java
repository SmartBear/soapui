package io.swagger.inflector.examples.models;

public class LongExample extends AbstractExample {
    private Long value;

    public LongExample() {
        super.setTypeName("integer");
    }

    public LongExample value(Long value) {
        this.setValue(value);
        return this;
    }

    public Long getValue() {
        return this.value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
