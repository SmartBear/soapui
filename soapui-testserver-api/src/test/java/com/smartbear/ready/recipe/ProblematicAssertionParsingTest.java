package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ProblematicAssertionParsingTest extends RecipeParserTestBase {
    @Test
    public void canParseRecipeWithMultipleAssertionsOfSameType() throws Exception {
        RestTestRequestStep requestTestStepForRecipe = getRequestTestStepForRecipe("multiple-assertions-with-same-type.json");

        assertThat(requestTestStepForRecipe.getAssertions().size(), is(2));

        SimpleContainsAssertion fooContainsAssertion = (SimpleContainsAssertion) requestTestStepForRecipe.getAssertionAt(0);
        assertThat(fooContainsAssertion.getName(), is("Contains"));
        assertThat(fooContainsAssertion.getToken(), is("Foo"));

        SimpleContainsAssertion barContainsAssertion = (SimpleContainsAssertion) requestTestStepForRecipe.getAssertionAt(1);
        assertThat(barContainsAssertion.getName(), is("Contains 1"));
        assertThat(barContainsAssertion.getToken(), is("Bar"));
    }

    private RestTestRequestStep getRequestTestStepForRecipe(String recipeFileName) throws Exception {
        WsdlProject project = buildProjectFromRecipe(recipeFileName);

        return getSingleRestRequestStepIn(project);
    }
}
