package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGroovyScriptTestStep;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for groovy script recipe parsing.
 */
public class GroovyScriptParsingTest extends RecipeParserTestBase {

    @Test
    public void parsesSimpleGroovyScriptTestStep() throws Exception {
        WsdlProject project = buildProjectFromRecipe("groovy-script.json");

        WsdlGroovyScriptTestStep groovyScriptTestStep = getSingleTestStepIn(project, WsdlGroovyScriptTestStep.class);
        assertThat(groovyScriptTestStep.getName(), is("Groovy Script"));
        assertThat(groovyScriptTestStep.getScript(), is("println 'Hello World!'"));
    }
}
