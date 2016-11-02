package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Struct capturing values from JSON for building a Data source test step.
 */
@ApiModel(value = "DataSourceTestStep", description = "Data source test step definition")
public class DataSourceTestStepStruct extends TestStepStruct {

    public DataSourceStruct dataSource;
    public TestStepStruct[] testSteps;
    public Boolean restartOnRun;
    public boolean preload;
    public boolean failOnEmpty;
    public boolean skipLoopOnEmpty;
    public boolean trimValues;
    public boolean entitizeValues;
    public boolean expandProperties;
    public boolean shared;
    public boolean restartShared;
    public String startRow;
    public String endRow;

    @JsonCreator
    public DataSourceTestStepStruct(
            @JsonProperty("type") String type, @JsonProperty("name") String name,
            @JsonProperty("dataSource") DataSourceStruct dataSource, @JsonProperty("testSteps") TestStepStruct[] testSteps,
            @JsonProperty("restartOnRun") Boolean restartOnRun, @JsonProperty("preload") boolean preload,
            @JsonProperty("failOnEmpty") boolean failOnEmpty, @JsonProperty("skipLoopOnEmpty") boolean skipLoopOnEmpty,
            @JsonProperty("trimValues") boolean trimValues, @JsonProperty("entitizeValues") boolean entitizeValues,
            @JsonProperty("expandProperties") boolean expandProperties, @JsonProperty("shared") boolean shared,
            @JsonProperty("restartShared") boolean restartShared, @JsonProperty("startRow") String startRow,
            @JsonProperty("endRow") String endRow) {
        super(type, name);

        checkNotNull(dataSource, "dataSource");
        checkNotNull(testSteps, "testSteps");

        this.dataSource = dataSource;
        this.testSteps = testSteps;
        this.restartOnRun = restartOnRun;
        this.preload = preload;
        this.failOnEmpty = failOnEmpty;
        this.skipLoopOnEmpty = skipLoopOnEmpty;
        this.trimValues = trimValues;
        this.entitizeValues = entitizeValues;
        this.expandProperties = expandProperties;
        this.shared = shared;
        this.restartShared = restartShared;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    public boolean isRestartOnRun() {
        return restartOnRun == null ? true : restartOnRun;
    }
}
