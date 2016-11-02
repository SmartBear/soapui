package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import java.util.List;
import java.util.Map;

@ApiModel(value = "DataSource", description = "Data source definition")
public class DataSourceStruct {
    public String[] properties;
    public Map<String, List<String>> grid;
    public DataGeneratorDataSourceStruct dataGen;
    public ExcelDataSourceStruct excel;
    public FileDataSourceStruct file;

    @JsonCreator
    public DataSourceStruct(
            @JsonProperty("properties") String[] properties,
            @JsonProperty("grid") Map<String, List<String>> grid,
            @JsonProperty("excel") ExcelDataSourceStruct excel,
            @JsonProperty("file") FileDataSourceStruct file,
            @JsonProperty("dataGen") DataGeneratorDataSourceStruct dataGen) {
        if (countNotNull(grid) + countNotNull(excel) + countNotNull(file) + countNotNull(dataGen) != 1) {
            throw new IllegalArgumentException("Exactly one of 'grid', 'excel', 'file' or 'grid' is expected");
        }
        this.properties = properties;
        this.grid = grid;
        this.excel = excel;
        this.file = file;
        this.dataGen = dataGen;
    }

    private int countNotNull(Object object) {
        return object == null ? 0 : 1;
    }

    public DataSourceStruct() {
    }
}
