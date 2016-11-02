package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbear.ready.recipe.assertions.AssertionStruct;
import io.swagger.annotations.ApiModel;

import java.util.Map;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Struct representing a JDBC Request test step.
 */
@ApiModel(value = "JdbcRequestTestStep", description = "JDBC Request Test step definition")
public class JdbcRequestTestStepStruct extends TestStepStruct {

    public String driver;
    //Contains username and password etc.
    public String connectionString;
    public String sqlQuery;
    public boolean storedProcedure;
    public Map<String, Object> properties;
    public AssertionStruct[] assertions;

    @JsonCreator
    public JdbcRequestTestStepStruct(
            @JsonProperty("type") String type,
            @JsonProperty("name") String name,
            @JsonProperty("driver") String driver,
            @JsonProperty("connectionString") String connectionString,
            @JsonProperty("sqlQuery") String sqlQuery,
            @JsonProperty("storedProcedure") boolean storedProcedure,
            @JsonProperty("properties") Map<String, Object> properties,
            @JsonProperty("assertions") AssertionStruct[] assertions) {
        super(type, name);

        checkNotNull(driver, "driver");
        checkNotNull(connectionString, "connectionString");
        checkNotNull(sqlQuery, "sqlQuery");

        this.properties = properties;
        this.driver = driver;
        this.connectionString = connectionString;
        this.sqlQuery = sqlQuery;
        this.storedProcedure = storedProcedure;
        this.assertions = assertions;
    }
}
