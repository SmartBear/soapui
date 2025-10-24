package io.swagger.inflector.examples.models;

public interface Example {
    String getName();

    void setName(String var1);

    String getNamespace();

    void setNamespace(String var1);

    String getPrefix();

    void setPrefix(String var1);

    Boolean getAttribute();

    void setAttribute(Boolean var1);

    Boolean getWrapped();

    void setWrapped(Boolean var1);

    String getTypeName();

    String asString();
}
