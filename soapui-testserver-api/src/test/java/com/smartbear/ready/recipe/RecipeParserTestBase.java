package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Base class for unit test classes exercising the test recipe functionality, containing basic test functionality.
 */
public abstract class RecipeParserTestBase {
    protected JsonRecipeParser parser = new JsonRecipeParser();

    protected WsdlProject buildProjectFromRecipe(String recipeFileName, ResourceReplacement... replacements) throws Exception {
        InputStream recipeAsStream = RestRequestParsingTest.class.getResourceAsStream("/test-recipes/" + recipeFileName);
        String recipe = IOUtils.toString(recipeAsStream);
        for (ResourceReplacement replacement : replacements) {
            recipe = replacement.apply(recipe);
        }
        return parser.parse(recipe);
    }

    protected RestTestRequestStep getSingleRestRequestStepIn(WsdlProject project) {
        return getSingleTestStepIn(project, RestTestRequestStep.class);
    }

    protected <T extends WsdlTestStep> T getSingleTestStepIn(WsdlProject project, Class<T> clazz) {
        assertThat(project.getTestSuiteCount(), is(1));
        assertThat(project.getTestSuiteAt(0).getTestCaseCount(), is(1));
        WsdlTestStep singleTestStep = project.getTestSuiteAt(0).getTestCaseAt(0).getTestStepAt(0);
        assertTrue(clazz.isAssignableFrom(singleTestStep.getClass()));
        return (T) singleTestStep;
    }

    protected static class ResourceReplacement {
        final String token;
        final String resourceToInsert;

        public ResourceReplacement(String token, String resourceToInsert) throws URISyntaxException, MalformedURLException {
            this.token = token;
            this.resourceToInsert = RecipeParserTestBase.class.getResource(resourceToInsert).toURI().toURL().toString();
        }

        String apply(String input) {
            return input.replaceAll(token, resourceToInsert);
        }
    }
}
