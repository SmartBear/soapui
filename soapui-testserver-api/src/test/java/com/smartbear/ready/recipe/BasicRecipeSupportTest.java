package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests of the most fundamental parts of the test recipe functionality – error handling etc.
 */
public class BasicRecipeSupportTest extends RecipeParserTestBase {

    @Test
    public void parsesEmptyRecipe() throws Exception {
        WsdlProject project = parser.parse("{ \"testSteps\": [] }");

        assertThat(project, is(not(nullValue())));
    }
}
