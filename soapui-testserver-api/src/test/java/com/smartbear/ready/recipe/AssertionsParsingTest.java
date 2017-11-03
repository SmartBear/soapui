package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.ResponseSLAAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleNotContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XQueryContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.json.JsonPathContentAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.json.JsonPathCountAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.json.JsonPathExistenceAssertion;
import com.eviware.soapui.security.assertion.InvalidHttpStatusCodesAssertion;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for basic recipe parsing.
 */
public class AssertionsParsingTest extends RecipeParserTestBase {

    private static final String EXPECTED_GROOVY_SCRIPT =
            "if (messageExchange.response.contentType != 'text/plain') fail('Wrong content type!')";

    private static JsonPathContentAssertion.Factory jsonPathAssertionFactory;
    private static JsonPathCountAssertion.Factory jsonPathCountAssertionFactory;

    private RestTestRequestStep restRequestStep;

    @BeforeClass
    public static void registerAssertionFactories() {
        jsonPathAssertionFactory = new JsonPathContentAssertion.Factory();
        jsonPathCountAssertionFactory = new JsonPathCountAssertion.Factory();
        TestAssertionRegistry.getInstance().addAssertion(jsonPathAssertionFactory);
        TestAssertionRegistry.getInstance().addAssertion(jsonPathCountAssertionFactory);
    }

    @AfterClass
    public static void unregisterFactories() {
        TestAssertionRegistry.getInstance().removeFactory(jsonPathAssertionFactory);
        TestAssertionRegistry.getInstance().removeFactory(jsonPathCountAssertionFactory);
    }

    @Before
    public void setUp() throws Exception {
        restRequestStep = getRequestTestStepForRecipe("request-with-assertions.json");
    }

    @Test
    public void parsesValidHttpStatusesAssertion() throws Exception {
        WsdlMessageAssertion assertion = restRequestStep.getAssertionAt(0);
        assertTrue("Wrong assertion class: " + assertion.getClass(), assertion instanceof ValidHttpStatusCodesAssertion);
        ValidHttpStatusCodesAssertion statusCodesAssertion = (ValidHttpStatusCodesAssertion) assertion;
        assertThat(statusCodesAssertion.getName(), is("ValidStatusCodeAssertion"));
        assertThat(statusCodesAssertion.getCodes(), is("200,201"));
    }

    @Test
    public void parsesInvalidHttpStatusesAssertion() throws Exception {
        WsdlMessageAssertion assertion = restRequestStep.getAssertionAt(1);
        assertTrue("Wrong assertion class: " + assertion.getClass(), assertion instanceof InvalidHttpStatusCodesAssertion);
        InvalidHttpStatusCodesAssertion statusCodesAssertion = (InvalidHttpStatusCodesAssertion) assertion;
        assertThat(statusCodesAssertion.getName(), is("InvalidStatusCodeAssertion"));
        assertThat(statusCodesAssertion.getCodes(), is("500,404"));
    }

    @Test
    public void parsesSimpleContainsAssertion() throws Exception {
        WsdlMessageAssertion assertion = restRequestStep.getAssertionAt(2);
        assertTrue("Wrong assertion class: " + assertion.getClass(), assertion instanceof SimpleContainsAssertion);
        SimpleContainsAssertion containsAssertion = (SimpleContainsAssertion) assertion;
        assertThat(containsAssertion.getName(), is("ContainsAssertion"));
        assertThat(containsAssertion.getToken(), is("Blaha"));
        assertTrue("Wrong value for ignoreCase", containsAssertion.isIgnoreCase());
        assertTrue("Wrong value for useRegexp", containsAssertion.isUseRegEx());
    }

    @Test
    public void parsesXPathContainsAssertion() throws Exception {
        WsdlMessageAssertion assertion = restRequestStep.getAssertionAt(3);
        assertTrue("Wrong assertion class: " + assertion.getClass(), assertion instanceof XPathContainsAssertion);
        XPathContainsAssertion containsAssertion = (XPathContainsAssertion) assertion;
        assertThat(containsAssertion.getName(), is("XPathMatchAssertion"));
        assertThat(containsAssertion.getPath(), is("//name"));
        assertThat(containsAssertion.getExpectedContent(), is("Waheed"));
        assertTrue("Wrong value for allowWildcards", containsAssertion.isAllowWildcards());
        assertTrue("Wrong value for ignoreNamespaces", containsAssertion.isIgnoreNamespaceDifferences());
        assertTrue("Wrong value for ignoreComments", containsAssertion.isIgnoreComments());
    }

    @Test
    public void parsesXQueryContainsAssertion() throws Exception {
        WsdlMessageAssertion assertion = restRequestStep.getAssertionAt(4);
        assertTrue("Wrong assertion class: " + assertion.getClass(), assertion instanceof XQueryContainsAssertion);
        XQueryContainsAssertion containsAssertion = (XQueryContainsAssertion) assertion;
        assertThat(containsAssertion.getName(), is("XQueryMatchAssertion"));
        assertThat(containsAssertion.getPath(), is(".//actors[0]"));
        assertThat(containsAssertion.getExpectedContent(), is("Branagh"));
        assertTrue("Wrong value for allowWildcards", containsAssertion.isAllowWildcards());
    }

    @Test
    public void parsesJsonPathContentAssertion() throws Exception {

        WsdlMessageAssertion assertion = restRequestStep.getAssertionAt(5);
        assertTrue("Wrong assertion class: " + assertion.getClass(), assertion instanceof JsonPathContentAssertion);
        JsonPathContentAssertion jsonPathContentAssertion = (JsonPathContentAssertion) assertion;
        assertThat(jsonPathContentAssertion.getName(), is("JsonPathMatchAssertion"));
        assertThat(jsonPathContentAssertion.getPath(), is("$.actors[0]"));
        assertThat(jsonPathContentAssertion.getExpectedContent(), is("Branagh"));
        assertTrue("Wrong value for allowWildcards", jsonPathContentAssertion.isAllowWildcards());
    }

    @Test
    public void parsesJsonPathCountAssertion() throws Exception {

        WsdlMessageAssertion assertion = restRequestStep.getAssertionAt(6);
        assertTrue("Wrong assertion class: " + assertion.getClass(), assertion instanceof JsonPathCountAssertion);
        JsonPathCountAssertion jsonPathCountAssertion = (JsonPathCountAssertion) assertion;
        assertThat(jsonPathCountAssertion.getName(), is("JsonPathCountAssertion"));
        assertThat(jsonPathCountAssertion.getPath(), is("$.actors"));
        assertThat(jsonPathCountAssertion.getExpectedContent(), is("5"));
        assertTrue("Wrong value for allowWildcards", jsonPathCountAssertion.isAllowWildcards());
    }

    @Test
    public void parsesJsonPathExistenceAssertion() throws Exception {

        WsdlMessageAssertion assertion = restRequestStep.getAssertionAt(7);
        assertTrue("Wrong assertion class: " + assertion.getClass(), assertion instanceof JsonPathExistenceAssertion);
        JsonPathExistenceAssertion jsonPathExistenceAssertion = (JsonPathExistenceAssertion) assertion;
        assertThat(jsonPathExistenceAssertion.getName(), is("JsonPathExistenceAssertion"));
        assertThat(jsonPathExistenceAssertion.getPath(), is("$.actors"));
        assertThat(jsonPathExistenceAssertion.getExpectedContent(), is("true"));
    }

    @Test
    public void parsesScriptAssertion() throws Exception {
        WsdlMessageAssertion assertion = restRequestStep.getAssertionAt(8);
        assertTrue("Wrong assertion class: " + assertion.getClass(), assertion instanceof GroovyScriptAssertion);
        GroovyScriptAssertion groovyScriptAssertion = (GroovyScriptAssertion) assertion;
        assertThat(groovyScriptAssertion.getName(), is("ScriptAssertion"));
        assertThat(groovyScriptAssertion.getScriptText(), is(EXPECTED_GROOVY_SCRIPT));

    }

    @Test
    public void parsesResponseSlaAssertion() throws Exception {
        WsdlMessageAssertion assertion = restRequestStep.getAssertionAt(9);
        assertTrue("Wrong assertion class: " + assertion.getClass(), assertion instanceof ResponseSLAAssertion);
        ResponseSLAAssertion responseSLAAssertion = (ResponseSLAAssertion) assertion;
        assertThat(responseSLAAssertion.getName(), is("ResponseSlaAssertion"));
        assertThat(responseSLAAssertion.getSLA(), is("500"));

    }

    @Test
    public void parsesNotContainsAssertion() throws Exception {
        WsdlMessageAssertion assertion = restRequestStep.getAssertionAt(10);
        assertTrue("Wrong assertion class: " + assertion.getClass(), assertion instanceof SimpleNotContainsAssertion);
        SimpleNotContainsAssertion notContainsAssertion = (SimpleNotContainsAssertion) assertion;
        assertThat(notContainsAssertion.getName(), is("NotContainsAssertion"));
        assertThat(notContainsAssertion.getToken(), is("Silent Force"));
        assertTrue("Wrong value for ignoreCase", notContainsAssertion.isIgnoreCase());
        assertTrue("Wrong value for useRegexp", notContainsAssertion.isUseRegEx());
    }

    private RestTestRequestStep getRequestTestStepForRecipe(String recipeFileName) throws Exception {
        WsdlProject project = buildProjectFromRecipe(recipeFileName);

        return getSingleRestRequestStepIn(project);
    }


}
