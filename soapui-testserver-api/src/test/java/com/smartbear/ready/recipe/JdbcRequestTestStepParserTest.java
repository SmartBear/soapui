package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.jdbc.JdbcTimeoutAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.registry.JdbcRequestTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class JdbcRequestTestStepParserTest extends RecipeParserTestBase {

    @BeforeClass
    public static void registerFactory() {
        WsdlTestStepRegistry.getInstance().addFactory(new JdbcRequestTestStepFactory());
    }

    @Test
    public void parsesJdbcTestStep() throws Exception {
        WsdlProject project = buildProjectFromRecipe("jdbc-request.json");

        JdbcRequestTestStep testStep = getSingleTestStepIn(project, JdbcRequestTestStep.class);

        assertThat(testStep.getName(), is("A JDBC request test step"));
        assertThat(testStep.getConnectionString(), is("someuri"));
        assertThat(testStep.getDriver(), is("some.Driver"));
        assertThat(testStep.getPassword(), is(nullValue()));
        assertThat(testStep.getQuery(), is("select * from whatever"));
        assertThat(testStep.isStoredProcedure(), is(false));
        assertThat(testStep.getAssertionCount(), is(3));

        //ResponseAsXml is set by default.
        assertThat(testStep.getPropertyCount(), is(3));
    }

    @Test
    public void setsAssertionTimeout() throws Exception {
        WsdlProject project = buildProjectFromRecipe("jdbc-request.json");
        JdbcRequestTestStep testStep = getSingleTestStepIn(project, JdbcRequestTestStep.class);

        JdbcTimeoutAssertion assertion = (JdbcTimeoutAssertion) testStep.getAssertionAt(2);
        assertThat(assertion.getName(), is("JDBC Timeout"));
        assertThat(assertion.getQueryTimeoutProperty(), is(1000L));
    }
}
