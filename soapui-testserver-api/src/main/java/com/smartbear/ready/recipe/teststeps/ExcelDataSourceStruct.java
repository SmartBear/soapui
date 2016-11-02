package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;
import static org.apache.commons.lang3.StringUtils.defaultString;

@ApiModel(value = "ExcelDataSource", description = "Excel data source definition")
public class ExcelDataSourceStruct {
    public String file;
    public String worksheet;
    public String startAtCell;
    public boolean ignoreEmpty;

    @JsonCreator
    public ExcelDataSourceStruct(
            @JsonProperty("file") String file,
            @JsonProperty("worksheet") String worksheet,
            @JsonProperty("startAtCell") String startAtCell,
            @JsonProperty("ignoreEmpty") boolean ignoreEmpty) {
        checkNotNull(file, "file");

        this.file = file;
        this.worksheet = worksheet;
        this.startAtCell = defaultString(startAtCell, "A1");
        this.ignoreEmpty = ignoreEmpty;
    }

    public ExcelDataSourceStruct() {
        startAtCell = "A1";
    }
}
