package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Used to deserialize the top level JSON object in a test recipe to Java.
 */
@ApiModel(value = "TestCase", description = "Test case definition")
public class TestCaseStruct {
    public String name;
    public Boolean searchProperties;
    public boolean maintainSession;
    public boolean abortOnError;
    public Boolean failTestCaseOnError;
    public Boolean discardOkResults;
    public String socketTimeout;
    public int testCaseTimeout;
    public String clientCertFileName;
    public String clientCertPassword;
    public Map<String, String> properties;

    /**
     * Adding only getter for 'maxResults' test case option as it must NOT be exposed to the Runtime Service user (hence not supported in JSON schema),
     * since we use this property to save the test steps results to create the status report for executions in the Ready! API TestServer.
     * 0 means it will save all the results (0=UNLIMITED).
     */
    @ApiModelProperty(hidden = true)
    public int getMaxResults() {
        return 0;
    }

    public TestStepStruct[] testSteps;

    @JsonCreator
    public TestCaseStruct(
            @JsonProperty("searchProperties") Boolean searchProperties,
            @JsonProperty("maintainSession") boolean maintainSession,
            @JsonProperty("abortOnError") boolean abortOnError,
            @JsonProperty("failTestCaseOnError") Boolean failTestCaseOnError,
            @JsonProperty("name") String name,
            @JsonProperty("discardOkResults") Boolean discardOkResults,
            @JsonProperty("socketTimeout") String socketTimeout,
            @JsonProperty("testCaseTimeout") int testCaseTimeout,
            @JsonProperty("testSteps") TestStepStruct[] testSteps,
            @JsonProperty("clientCertFileName") String clientCertFileName,
            @JsonProperty("clientCertPassword") String clientCertPassword,
            @JsonProperty("properties") Map<String, String> properties) {

        checkNotNull(testSteps, "testSteps");

        this.searchProperties = searchProperties;
        this.maintainSession = maintainSession;
        this.abortOnError = abortOnError;
        this.failTestCaseOnError = failTestCaseOnError;
        this.name = name;
        this.discardOkResults = discardOkResults;
        this.socketTimeout = socketTimeout;
        this.testCaseTimeout = testCaseTimeout;
        this.testSteps = testSteps;
        this.clientCertFileName = clientCertFileName;
        this.clientCertPassword = clientCertPassword;
        this.properties = properties;
    }

    public boolean isSearchProperties() {
        return searchProperties == null ? true : searchProperties;
    }

    public boolean isFailTestCaseOnError() {
        return failTestCaseOnError == null ? true : failTestCaseOnError;
    }

    public boolean isDiscardOkResults() {
        return discardOkResults == null ? true : discardOkResults;
    }
}
