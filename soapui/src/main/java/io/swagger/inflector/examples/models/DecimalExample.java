package io.swagger.inflector.examples.models;

import java.math.BigDecimal;

public class DecimalExample extends AbstractExample {
    private BigDecimal value;

    public DecimalExample() {
        super.setTypeName("number");
    }

    public DecimalExample value(BigDecimal value) {
        this.setValue(value);
        return this;
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
