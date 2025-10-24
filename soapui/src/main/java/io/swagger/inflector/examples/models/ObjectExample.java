package io.swagger.inflector.examples.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.inflector.examples.ExampleDeserializer;

import java.util.HashMap;

@JsonDeserialize(using = ExampleDeserializer.class)
public class ObjectExample extends HashMap<String, Example> implements Example {
    private String name;
    private String namespace;
    private String prefix;
    private Boolean attribute;
    private Boolean wrapped;
    private Object example;

    public ObjectExample() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Boolean getAttribute() {
        return this.attribute;
    }

    public void setAttribute(Boolean attribute) {
        this.attribute = attribute;
    }

    public Boolean getWrapped() {
        return this.wrapped;
    }

    public void setWrapped(Boolean wrapped) {
        this.wrapped = wrapped;
    }

    public String getTypeName() {
        return "object";
    }

    public String asString() {
        return this.toString();
    }

    public Object getExample() {
        return this.example;
    }

    public void setExample(Object example) {
        this.example = example;
    }
}
