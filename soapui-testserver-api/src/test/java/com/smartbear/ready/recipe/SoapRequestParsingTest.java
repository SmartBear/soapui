package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.filters.RemoveEmptyContentRequestFilter;
import com.eviware.soapui.impl.wsdl.support.FileAttachment;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.NotSoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapFaultAssertion;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for basic SOAP recipe parsing.
 */
public class SoapRequestParsingTest extends RecipeParserTestBase {

    @Test
    public void parsesSimpleSoapRequestRecipe() throws Exception {
        String jsonFile = "simple-soap-request.json";
        WsdlProject project = buildProjectWithWsdlReference(jsonFile);

        WsdlTestRequestStep restRequestStep = getSingleTestStepIn(project, WsdlTestRequestStep.class);
        WsdlTestRequest request = restRequestStep.getTestRequest();
        assertEquals("GetCitiesByCountry", request.getOperationName());

        XmlObject xml = XmlUtils.createXmlObject(request.getRequestContent());

        XmlObject node = xml.selectPath("//*[local-name() = 'CountryName']")[0];
        assertEquals("Sweden", XmlUtils.getNodeValue(node.getDomNode()));
    }

    @Test
    public void parsesSimpleSoapRequestRecipeWithEndpoint() throws Exception {
        WsdlProject project = buildProjectWithWsdlReference("simple-soap-request-with-endpoint.json");

        WsdlTestRequestStep restRequestStep = getSingleTestStepIn(project, WsdlTestRequestStep.class);
        WsdlTestRequest request = restRequestStep.getTestRequest();
        assertEquals("http://some.endpoint.com", request.getEndpoint());
    }

    @Test
    public void parsesSimpleSoapRequestRecipeWithAssertions() throws Exception {
        WsdlProject project = buildProjectWithWsdlReference("simple-soap-request-with-soap-assertions.json");

        WsdlTestRequestStep restRequestStep = getSingleTestStepIn(project, WsdlTestRequestStep.class);
        WsdlTestRequest request = restRequestStep.getTestRequest();

        assertEquals(3, request.getAssertionCount());
        assertTrue(request.getAssertionAt(0) instanceof SchemaComplianceAssertion);
        assertTrue(request.getAssertionAt(1) instanceof SoapFaultAssertion);
        assertTrue(request.getAssertionAt(2) instanceof NotSoapFaultAssertion);
    }

    @Test
    public void parsesSoapRequestRecipeWithXPathParameter() throws Exception {
        WsdlProject project = buildProjectWithWsdlReference("soap-request-with-xpath-parameter.json");

        WsdlTestRequestStep restRequestStep = getSingleTestStepIn(project, WsdlTestRequestStep.class);
        WsdlTestRequest request = restRequestStep.getTestRequest();
        assertEquals("GetWeather", request.getOperationName());

        XmlObject xml = XmlUtils.createXmlObject(request.getRequestContent());
        XmlObject node = xml.selectPath("//*[local-name() = 'CountryName']")[0];
        assertEquals("Sweden", XmlUtils.getNodeValue(node.getDomNode()));

        node = xml.selectPath("//*[local-name() = 'CityName']")[0];
        assertEquals("Stockholm", XmlUtils.getNodeValue(node.getDomNode()));
    }

    @Test
    public void parsesSoapRequestRecipeWithEmptyParameters() throws Exception {
        WsdlProject project = buildProjectWithWsdlReference("soap-request-with-empty-parameters.json");

        WsdlTestRequestStep restRequestStep = getSingleTestStepIn(project, WsdlTestRequestStep.class);
        WsdlTestRequest request = restRequestStep.getTestRequest();
        assertEquals("GetWeather", request.getOperationName());

        assertTrue(request.isRemoveEmptyContent());

        String soapNamespace = request.getOperation().getInterface().getSoapVersion()
                .getEnvelopeNamespace();

        String content = RemoveEmptyContentRequestFilter.removeEmptyContent(request.getRequestContent(), soapNamespace, false);

        assertFalse(content.contains("CityName"));
        assertFalse(content.contains("CountryName"));
    }

    @Test
    public void parsesRequestAttachment() throws Exception {
        WsdlProject project = buildProjectWithWsdlReference("simple-soap-request-with-base64-attachment.json");
        WsdlTestRequestStep restRequestStep = getSingleTestStepIn(project, WsdlTestRequestStep.class);
        WsdlTestRequest request = restRequestStep.getTestRequest();
        Attachment[] attachments = request.getAttachments();
        assertThat(attachments.length, is(1));
        Attachment attachment = attachments[0];
        assertThat(attachment.getName(), is("A Name"));
        assertThat(attachment.getContentType(), is("text/plain"));
        assertThat(attachment.getContentID(), is("An id"));
        assertThat(((FileAttachment) attachment).getData(), is("Content".getBytes()));
    }

    private WsdlProject buildProjectWithWsdlReference(String jsonFile) throws Exception {
        return buildProjectFromRecipe(jsonFile, new ResourceReplacement("globalweather.wsdl", "/wsdl/globalweather.wsdl"));
    }
}
