package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SoapMockResponseTestStepParserTest extends RecipeParserTestBase {
    @Ignore
    @Test
    public void parsesRecipeWithWsdlMockResponseTestStep() throws Exception {
        WsdlProject wsdlProject = buildProjectFromRecipe("wsdl-mock-response-test-step.json");
        WsdlMockResponseTestStep testStep = getSingleTestStepIn(wsdlProject, WsdlMockResponseTestStep.class);
        assertThat(testStep.getPath(), is("/myweatherservice"));
        assertThat(testStep.getPort(), is(6091));
        assertThat(testStep.getOperation().getName(), is("GetCitiesByCountry"));
        assertThat(testStep.getInterface().getName(), is("GlobalWeatherSoap12"));
    }
}