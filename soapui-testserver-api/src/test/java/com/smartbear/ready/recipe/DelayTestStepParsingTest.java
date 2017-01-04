package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlDelayTestStep;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DelayTestStepParsingTest extends RecipeParserTestBase {
    @Test
    public void parsesDelayTestStep() throws Exception {
        WsdlProject project = buildProjectFromRecipe("delay-test-step.json");
        WsdlDelayTestStep delayTestStep = getSingleTestStepIn(project, WsdlDelayTestStep.class);
        assertThat(delayTestStep.getDelay(), is(3000));
    }
}