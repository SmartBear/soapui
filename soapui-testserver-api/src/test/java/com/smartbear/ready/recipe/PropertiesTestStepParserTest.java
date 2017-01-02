package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlPropertiesTestStep;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PropertiesTestStepParserTest extends RecipeParserTestBase {
    @Test
    public void parsesPropertiesTestStepFromRecipe() throws Exception {
        WsdlProject wsdlProject = buildProjectFromRecipe("properties-test-step.json");
        WsdlPropertiesTestStep propertiesTestStep = (WsdlPropertiesTestStep) wsdlProject.getTestSuiteAt(0)
                .getTestCaseAt(0).getTestStepAt(0);
        assertThat(propertiesTestStep.getPropertyCount(), is(2));
        assertThat(propertiesTestStep.getProperty("property1").getValue(), is("value1"));
        assertThat(propertiesTestStep.getProperty("property2").getValue(), is("value2"));
    }
}
