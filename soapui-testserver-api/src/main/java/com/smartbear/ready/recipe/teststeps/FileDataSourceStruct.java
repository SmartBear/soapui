package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;
import static org.apache.commons.lang3.StringUtils.defaultString;

@ApiModel(value = "FileDataSource", description = "File Data Source definition")
public class FileDataSourceStruct {
    public String file;
    public String charset;
    public String separator;
    public boolean trim = true;
    public boolean quotedValues;

    public FileDataSourceStruct() {
        charset = System.getProperty("file.encoding");
        separator = ",";
    }

    @JsonCreator
    public FileDataSourceStruct(
            @JsonProperty("file") String file,
            @JsonProperty("charset") String charset,
            @JsonProperty("separator") String separator,
            @JsonProperty("trim") boolean trim,
            @JsonProperty("quotedValues") boolean quotedValues) {
        checkNotNull(file, "file");

        this.file = file;
        this.charset = defaultString(charset, System.getProperty("file.encoding"));
        this.separator = defaultString(separator, ",");
        this.trim = trim;
        this.quotedValues = quotedValues;
    }
}
