package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.settings.HttpSettings;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestCaseOptionsParsingTest extends RecipeParserTestBase {
    @Test
    public void parsesTestcaseOptions() throws Exception {
        WsdlProject project = buildProjectFromRecipe("test-case-level-options.json");

        WsdlTestCase testCase = project.getTestSuiteAt(0).getTestCaseAt(0);

        assertThat(testCase.getName(), is("RecipeTestCase"));
        assertThat(testCase.getSearchProperties(), is(true));
        assertThat(testCase.getKeepSession(), is(true));
        assertThat(testCase.getFailOnError(), is(true));
        assertThat(testCase.getFailTestCaseOnErrors(), is(true));
        assertThat(testCase.getDiscardOkResults(), is(true));
        assertThat(testCase.getSettings().getString(HttpSettings.SOCKET_TIMEOUT, "-1"), is("0"));
        assertThat(testCase.getTimeout(), is(0L));
        assertThat(testCase.getMaxResults(), is(0));
        assertThat(testCase.getProperties().size(), is(2));
        assertThat(testCase.getProperties().get("test").getValue(), is("test"));
        assertThat(testCase.getProperties().get("test2").getValue(), is("test2"));
    }

    @Test
    public void parsesTestcaseOptionsWithFalseValues() throws Exception {
        WsdlProject project = buildProjectFromRecipe("test-case-options-with-false-values.json");

        WsdlTestCase testCase = project.getTestSuiteAt(0).getTestCaseAt(0);

        assertThat(testCase.getSearchProperties(), is(false));
        assertThat(testCase.getKeepSession(), is(false));
        assertThat(testCase.getFailOnError(), is(false));
        assertThat(testCase.getFailTestCaseOnErrors(), is(false));
        assertThat(testCase.getDiscardOkResults(), is(false));
        assertThat(testCase.getSettings().getString(HttpSettings.SOCKET_TIMEOUT, "-1"), is("100"));
        assertThat(testCase.getTimeout(), is(1000L));
        assertThat(testCase.getMaxResults(), is(0));
        assertThat(testCase.getProperties().size(), is(0));
    }
}
