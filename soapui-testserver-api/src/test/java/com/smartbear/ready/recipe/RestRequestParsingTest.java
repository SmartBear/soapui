package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.FileAttachment;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.model.iface.Attachment;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for basic recipe parsing.
 */
public class RestRequestParsingTest extends RecipeParserTestBase {


    @Test
    public void parsesSingleRestRequestRecipe() throws Exception {
        WsdlProject project = buildProjectFromRecipe("single-get-request.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        assertThat(restRequestStep.getInterface().getEndpoints()[0], is("http://google.com"));
        assertThat(restRequestStep.getResource().getFullPath(), is("/"));
        RestTestRequest testRequest = restRequestStep.getTestRequest();

        assertThat(testRequest.getParams().getProperty("q").getValue(), is("Madonna"));
        assertThat(testRequest.getParams().getProperty("usr").getValue(), is("Kalle"));
    }

    @Test
    public void parsesRequestWithDifferentParameterTypes() throws Exception {
        WsdlProject project = buildProjectFromRecipe("request-with-different-parameter-types.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        RestTestRequest testRequest = restRequestStep.getTestRequest();

        assertThat(testRequest.getParams().getProperty("carId").getStyle(),
                is(RestParamsPropertyHolder.ParameterStyle.TEMPLATE));
        assertThat(testRequest.getParams().getProperty("X-Car-Auth").getStyle(),
                is(RestParamsPropertyHolder.ParameterStyle.HEADER));
        assertThat(testRequest.getParams().getProperty("session").getStyle(),
                is(RestParamsPropertyHolder.ParameterStyle.MATRIX));
    }

    @Test
    public void parsesPostRequestWithBodyRecipe() throws Exception {
        WsdlProject project = buildProjectFromRecipe("single-post-request.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        assertThat(restRequestStep.getInterface().getEndpoints()[0], is("http://our.api.com"));
        assertThat(restRequestStep.getResource().getFullPath(), is("/register"));
        RestTestRequest postRequest = restRequestStep.getTestRequest();
        assertThat(postRequest.getMethod(), is(RestRequestInterface.HttpMethod.POST));
        assertThat(postRequest.getRequestContent(), is("Smokin' hot body"));
    }

    @Test
    public void parsesRequestHeaders() throws Exception {
        WsdlProject project = buildProjectFromRecipe("request-with-headers-and-properties.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        RestTestRequest postRequest = restRequestStep.getTestRequest();
        assertThat(postRequest.getRequestHeaders().get("Pragma"), is(Collections.singletonList("nocache")));
        assertThat(postRequest.getRequestHeaders().get("X-Custom"), is(Arrays.asList("first", "second")));
    }

    @Test
    public void parsesRequestProperties() throws Exception {
        WsdlProject project = buildProjectFromRecipe("request-with-headers-and-properties.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        RestTestRequest postRequest = restRequestStep.getTestRequest();
        assertThat(postRequest.getEncoding(), is("ISO-8859-1"));
        assertThat(postRequest.isFollowRedirects(), is(false));
        assertThat(postRequest.getTimeout(), is("60"));
        assertThat(postRequest.isEntitizeProperties(), is(true));
        assertThat(postRequest.isPostQueryString(), is(false));
        assertThat(postRequest.getMediaType(), is("application/pdf"));
    }

    @Test
    public void parsesRequestWithPostQueryString() throws Exception {
        WsdlProject project = buildProjectFromRecipe("request-with-post-query-string.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        RestTestRequest postRequest = restRequestStep.getTestRequest();
        assertThat(postRequest.isPostQueryString(), is(true));
        assertThat(postRequest.getMediaType(), is("application/x-www-form-urlencoded"));
    }

    @Test
    public void parsesRecipeWithTwoRequestsForSameResource() throws Exception {
        WsdlProject project = buildProjectFromRecipe("two-get-requests.json");

        assertThat(project.getTestSuiteAt(0).getTestCaseAt(0).getTestStepCount(), is(2));
        assertThat(project.getInterfaceCount(), is(1));
        assertThat(((RestService) project.getInterfaceAt(0)).getResourceList().size(), is(1));
    }

    @Test
    public void parsesQueryParametersInUri() throws Exception {
        WsdlProject project = buildProjectFromRecipe("get-request-with-parameters-in-uri.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        assertThat(restRequestStep.getProperty("address").getValue(), is("1600 Amphitheatre Parkway, Mountain View, CA"));
        assertThat(restRequestStep.getProperty("sensor").getValue(), is("false"));
        assertThat(restRequestStep.getRestMethod().getProperty("address").getParamLocation(), is(NewRestResourceActionBase.ParamLocation.METHOD));
        assertThat(restRequestStep.getRestMethod().getProperty("sensor").getParamLocation(), is(NewRestResourceActionBase.ParamLocation.METHOD));
        assertThat(restRequestStep.getRestMethod().getProperty("address").getStyle(), is(RestParamsPropertyHolder.ParameterStyle.QUERY));
        assertThat(restRequestStep.getRestMethod().getProperty("sensor").getStyle(), is(RestParamsPropertyHolder.ParameterStyle.QUERY));
    }

    @Test
    public void parsesMatrixParametersInUri() throws Exception {
        WsdlProject project = buildProjectFromRecipe("get-request-with-parameters-in-uri.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        assertThat(restRequestStep.getProperty("mat1").getValue(), is("val1"));
        assertThat(restRequestStep.getProperty("mat2").getValue(), is("val2"));
        assertThat(restRequestStep.getRestMethod().getProperty("mat1").getParamLocation(), is(NewRestResourceActionBase.ParamLocation.METHOD));
        assertThat(restRequestStep.getRestMethod().getProperty("mat2").getParamLocation(), is(NewRestResourceActionBase.ParamLocation.METHOD));
        assertThat(restRequestStep.getRestMethod().getProperty("mat1").getStyle(), is(RestParamsPropertyHolder.ParameterStyle.MATRIX));
        assertThat(restRequestStep.getRestMethod().getProperty("mat2").getStyle(), is(RestParamsPropertyHolder.ParameterStyle.MATRIX));
    }

    @Test
    public void parsesRequestAttachment() throws Exception {
        WsdlProject project = buildProjectFromRecipe("request-with-base64-attachment.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        RestTestRequest testRequest = restRequestStep.getTestRequest();
        Attachment[] attachments = testRequest.getAttachments();
        assertThat(attachments.length, is(1));
        Attachment attachment = attachments[0];
        assertThat(attachment.getName(), is("A Name"));
        assertThat(attachment.getContentType(), is("text/plain"));
        assertThat(attachment.getContentID(), is("An id"));
        assertThat(((FileAttachment) attachment).getData(), is("Content".getBytes()));
    }
}
