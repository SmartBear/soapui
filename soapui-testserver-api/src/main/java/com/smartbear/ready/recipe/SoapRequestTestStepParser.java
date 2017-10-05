package com.smartbear.ready.recipe;

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.soapui.support.xml.XmlUtils;
import com.smartbear.ready.recipe.teststeps.AuthenticationStruct;
import com.smartbear.ready.recipe.teststeps.SoapParamStruct;
import com.smartbear.ready.recipe.teststeps.SoapTestRequestStepStruct;
import com.smartbear.ready.recipe.teststeps.TestStepStruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import static com.smartbear.ready.recipe.TestStepNames.createUniqueName;
import static com.smartbear.ready.recipe.WsdlExtractor.getWsdlInterface;

/**
 * Parses a JSON Object describing a SOAP Request test step.
 */
class SoapRequestTestStepParser extends HttpRequestTestStepParser {

    public void createTestStep(WsdlTestCase testCase, TestStepStruct testStepStruct, StringToObjectMap context) throws ParseException {
        SoapTestRequestStepStruct requestTestStepElement = (SoapTestRequestStepStruct) testStepStruct;

        WsdlInterface wsdlInterface = getWsdlInterface(testCase, requestTestStepElement.wsdl, requestTestStepElement.binding);
        if (wsdlInterface == null) {
            throw new ParseException("Failed to find specified binding [" + requestTestStepElement.binding + "] in WSDL");
        }

        WsdlOperation operation = wsdlInterface.getOperationByName(requestTestStepElement.operation);
        if (operation == null) {
            throw new ParseException("Failed to find specified operation [" + requestTestStepElement +
                    "] in binding [" + wsdlInterface.getBindingName().toString() + "]");
        }

        String testStepName = createUniqueName(testCase, testStepStruct.name, operation.getName());
        WsdlTestRequestStep requestTestStep = (WsdlTestRequestStep) testCase.addTestStep(
                WsdlTestRequestStepFactory.createConfig(operation, testStepName));

        WsdlTestRequest testRequest = requestTestStep.getTestRequest();
        addProperties(requestTestStepElement, testRequest);
        addParameters(requestTestStepElement, testRequest);
        addHeaders(requestTestStepElement, testRequest);
        addAuthentication(requestTestStepElement, testRequest);
        addAssertions(requestTestStepElement, testRequest);
        addAttachments(requestTestStepElement, testRequest);
    }

    private void addProperties(SoapTestRequestStepStruct testStepStruct, WsdlTestRequest testRequest) {
        super.addProperties(testStepStruct, testRequest);

        if (testStepStruct.requestBody != null) {
            testRequest.setRequestContent(testStepStruct.requestBody);
        } else {
            testRequest.setRequestContent(testRequest.getOperation().createRequest(true));
            testRequest.setRemoveEmptyContent(true);
        }

        if (StringUtils.isNotBlank(testStepStruct.URI)) {
            testRequest.setEndpoint(testStepStruct.URI);
        }
    }

    private void addParameters(SoapTestRequestStepStruct testStepStruct, WsdlTestRequest testRequest) throws ParseException {

        try {
            XmlObject xml = XmlUtils.createXmlObject(testRequest.getRequestContent(),
                    new XmlOptions().setLoadStripComments());

            SoapParamStruct[] parameterArray = testStepStruct.parameters;
            if (parameterArray != null) {
                for (SoapParamStruct parameter : parameterArray) {
                    if (StringUtils.isNotBlank(parameter.name)) {
                        parameter.path = "//*[local-name() = '" + parameter.name + "']";
                    }

                    XmlObject[] nodes = xml.selectPath(parameter.path);
                    if (nodes.length > 0) {
                        XmlUtils.setNodeValue(nodes[0].getDomNode(), parameter.value);
                    }
                }
            }

            testRequest.setRequestContent(xml.xmlText().replaceAll(">\\?</", "></"));

        } catch (Exception e) {
            throw new ParseException("Failed to parse request body [" + testRequest.getRequestContent() + "]", e);
        }
    }

    private void addAuthentication(SoapTestRequestStepStruct testStepStruct, WsdlTestRequest testRequest) {
        AuthenticationStruct authentication = testStepStruct.authentication;
        if (authentication != null) {
            String authenticationType = authentication.type;
            testRequest.setSelectedAuthProfileAndAuthType("Auth", CredentialsConfig.AuthType.Enum.forString(authenticationType));
            testRequest.setUsername(authentication.username);
            testRequest.setPassword(authentication.password);
        }
    }

}
