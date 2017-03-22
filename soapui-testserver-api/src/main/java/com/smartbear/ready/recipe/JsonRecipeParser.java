package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smartbear.ready.recipe.assertions.AssertionStruct;
import com.smartbear.ready.recipe.assertions.AssertionStructAdapter;
import com.smartbear.ready.recipe.teststeps.TestCaseStruct;
import com.smartbear.ready.recipe.teststeps.TestStepStruct;
import com.smartbear.ready.recipe.teststeps.TestStepStructAdapter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for building SoapUI projects from Test recipes in JSON format.
 */
public class JsonRecipeParser implements ObjectRecipeParser {

    public static final String CURRENT_TEST_STEP_INDEX = "CURRENT_TEST_STEP_INDEX";
    private static Logger logger = LoggerFactory.getLogger(JsonRecipeParser.class);

    protected static Map<String, TestStepJsonParser> testStepParsers = new HashMap<>();

    static {
        testStepParsers.put(TestStepNames.REST_REQUEST_TYPE, new RestRequestTestStepParser());
        testStepParsers.put(TestStepNames.SOAP_REQUEST_TYPE, new SoapRequestTestStepParser());
        testStepParsers.put(TestStepNames.PROPERTY_TRANSFER_TYPE, new PropertyTransferTestStepParser());
        testStepParsers.put(TestStepNames.GROOVY_SCRIPT_TYPE, new GroovyScriptTestStepParser());
        testStepParsers.put(TestStepNames.JDBC_REQUEST_TYPE, new JdbcRequestTestStepParser());
        testStepParsers.put(TestStepNames.DELAY_TYPE, new DelayTestStepParser());
        testStepParsers.put(TestStepNames.PROPERTIES_TYPE, new PropertiesTestStepParser());
        testStepParsers.put(TestStepNames.SOAP_MOCK_RESPONSE_TYPE, new SoapMockResponseTestStepParser());
    }

    private Gson gson = new GsonBuilder().
            registerTypeAdapter(AssertionStruct.class, new AssertionStructAdapter()).
            registerTypeAdapter(TestStepStruct.class, new TestStepStructAdapter()).
            create();

    @Override
    public WsdlProject parse(String recipeJson) throws Exception {
        TestCaseStruct testCaseStruct = parseToTestCaseStruct(recipeJson);
        return parse(testCaseStruct);
    }

    @Override
    public TestCaseStruct parseToTestCaseStruct(String recipeJson) throws Exception {
        validateSchema(recipeJson);
        return gson.fromJson(recipeJson, TestCaseStruct.class);
    }

    @Override
    public WsdlProject parse(TestCaseStruct testCaseStruct) throws ParseException {
        WsdlProject project;
        try {
            project = new WsdlProject();
        } catch (Exception e) {
            throw new Error("Unexpected error while creating project", e);
        }
        project.setName("Recipe REST Project");
        if (testCaseStruct.testSteps != null) {
            WsdlTestSuite testSuite = project.addNewTestSuite("Recipe Test Suite");
            WsdlTestCase testCase = testSuite.addNewTestCase("Recipe Test Case");
            addTestCaseOptions(testCase, testCaseStruct);
            addTestCaseProperties(testCase, testCaseStruct);
            addTestSteps(testCase, testCaseStruct.testSteps, new StringToObjectMap());
        }
        return project;
    }

    private void addTestCaseProperties(WsdlTestCase testCase, TestCaseStruct testCaseStruct) {
        Map<String, String> properties = testCaseStruct.properties;
        if (properties != null) {
            for (Map.Entry<String, String> property : properties.entrySet()) {
                testCase.setPropertyValue(property.getKey(), property.getValue());
            }
        }
    }

    private void addTestCaseOptions(WsdlTestCase testCase, TestCaseStruct testCaseStruct) {
        testCase.setSearchProperties(testCaseStruct.isSearchProperties());
        testCase.setKeepSession(testCaseStruct.maintainSession);
        testCase.setFailOnError(testCaseStruct.abortOnError);
        if (testCaseStruct.name != null) {
            testCase.setName(testCaseStruct.name);
        }
        testCase.setFailTestCaseOnErrors(testCaseStruct.isFailTestCaseOnError());
        testCase.setDiscardOkResults(testCaseStruct.isDiscardOkResults());
        testCase.setTimeout(testCaseStruct.testCaseTimeout);
        testCase.setMaxResults(testCaseStruct.getMaxResults());
        if (StringUtils.isNotEmpty(testCaseStruct.socketTimeout)) {
            testCase.getSettings().setString(HttpSettings.SOCKET_TIMEOUT, testCaseStruct.socketTimeout);
        }
    }

    private void validateSchema(String recipeJson) throws Exception {
        JsonSchema schema = getTestRecipeSchema();
        JsonNode data = JsonLoader.fromString(recipeJson);
        ProcessingReport report = schema.validate(data);

        if (report == null) {
            logger.error("JSON schema validation error: validation report is null");
            throw new Exception("JSON schema validation error");
        } else if (!report.isSuccess()) {
            StringBuilder errorMessageBuilder = new StringBuilder();
            for (ProcessingMessage processingMessage : report) {
                errorMessageBuilder.append(processingMessage.asJson().get("message").asText())
                        .append(System.getProperty("line.separator"));
            }
            logger.error("Could not validate json schema: " + errorMessageBuilder.toString());
            throw new Exception("JSON schema validation error: " + errorMessageBuilder.toString());
        }
    }

    private JsonSchema getTestRecipeSchema() throws IOException, ProcessingException {
        String jsonSchema = loadJsonSchema();
        JsonNode schemaNode = JsonLoader.fromString(jsonSchema);
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        return factory.getJsonSchema(schemaNode);
    }

    private String loadJsonSchema() throws IOException {
        InputStream recipeAsStream = JsonRecipeParser.class.getResourceAsStream("/test-recipe-schema.json");
        return IOUtils.toString(recipeAsStream);
    }

    static void addTestSteps(WsdlTestCase testCase, TestStepStruct[] testSteps, StringToObjectMap context) throws ParseException {
        addTestStepsExcludingPropertyTransferTestSteps(testCase, testSteps, context); //First we need to create all referenced test steps
        addPropertyTransferTestSteps(testCase, testSteps, context);
    }

    private static void addTestStepsExcludingPropertyTransferTestSteps(WsdlTestCase testCase, TestStepStruct[] testSteps, StringToObjectMap context) throws ParseException {
        for (TestStepStruct testStepObject : testSteps) {
            updateCurrentTestStepIndex(context, testStepObject);
            if (TestStepNames.PROPERTY_TRANSFER_TYPE.equals(testStepObject.type)) {
                continue;
            }
            TestStepJsonParser parserToUse = testStepParsers.get(testStepObject.type);
            if (parserToUse != null) {
                parserToUse.createTestStep(testCase, testStepObject, context);
            }
        }
    }

    private static void addPropertyTransferTestSteps(WsdlTestCase testCase, TestStepStruct[] testSteps, StringToObjectMap context) throws ParseException {
        for (TestStepStruct testStepObject : testSteps) {
            if (TestStepNames.PROPERTY_TRANSFER_TYPE.equals(testStepObject.type)) {
                TestStepJsonParser parserToUse = testStepParsers.get(TestStepNames.PROPERTY_TRANSFER_TYPE);
                parserToUse.createTestStep(testCase, testStepObject, context);
            }
        }
    }

    static void updateCurrentTestStepIndex(HashMap<String, Object> context, TestStepStruct testStepObject) {
        Integer currentTestStepIndex = (Integer) context.get(CURRENT_TEST_STEP_INDEX);
        if (currentTestStepIndex == null) {
            currentTestStepIndex = 0;
        }
        if (testStepObject != null) {
            testStepObject.index = currentTestStepIndex;
        }
        context.put(CURRENT_TEST_STEP_INDEX, currentTestStepIndex + 1);
    }

}
