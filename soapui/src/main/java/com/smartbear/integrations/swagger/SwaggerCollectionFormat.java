package com.smartbear.integrations.swagger;

public enum SwaggerCollectionFormat {
    CSV("csv", ","),
    SSV("ssv", " "),
    TSV("tsv", "\t"),
    PIPES("pipes", "|"),
    MULTI("multi", "&");

    private final String type;
    private String delimiter;

    private SwaggerCollectionFormat(String type, String delimiter) {
        this.type = type;
        this.delimiter = delimiter;
    }

    public String toString() {
        return type;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
}
