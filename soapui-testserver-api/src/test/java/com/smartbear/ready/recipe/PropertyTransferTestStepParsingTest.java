package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.PathLanguage;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class PropertyTransferTestStepParsingTest extends RecipeParserTestBase {
    @Test
    public void parsesSinglePropertyTransferTestStepRecipe() throws Exception {
        WsdlProject project = buildProjectFromRecipe("property-transfer-test-step.json");

        WsdlTestCase testCase = project.getTestSuiteAt(0).getTestCaseAt(0);
        assertThat(testCase.getTestStepList().size(), is(1));

        PropertyTransfersTestStep transfersTestStep = (PropertyTransfersTestStep) testCase.getTestStepAt(0);
        assertTestStepNameAndNumberOfTransfers(transfersTestStep);

        assertPropertyTransfer(transfersTestStep.getTransferAt(0), false, "Transfer 1", "Username");
        assertPropertyTransfer(transfersTestStep.getTransferAt(1), false, "TransferPassword", "Password");
    }

    @Test
    public void parsesPropertyTransferTestStepRecipeWhenSourceAndTargetStepsAreDefined() throws Exception {
        WsdlProject project = buildProjectFromRecipe("property-transfer-test-step-with-source-and-target-steps.json");

        WsdlTestCase testCase = project.getTestSuiteAt(0).getTestCaseAt(0);
        assertThat(testCase.getTestStepList().size(), is(3));
        assertThat(testCase.getTestStepList().get(0), is(instanceOf(RestTestRequestStep.class))); //Source step
        assertThat(testCase.getTestStepList().get(2), is(instanceOf(RestTestRequestStep.class))); //Target step

        PropertyTransfersTestStep transfersTestStep = (PropertyTransfersTestStep) testCase.getTestStepAt(1);
        assertTestStepNameAndNumberOfTransfers(transfersTestStep);

        assertPropertyTransfer(transfersTestStep.getTransferAt(0), true, "TransferUserName", "Username");
        assertPropertyTransfer(transfersTestStep.getTransferAt(1), true, "TransferPassword", "Password");
    }

    @Test
    public void parsesWhenPathAndPathLanguageAreMissing() throws Exception {
        WsdlProject project = buildProjectFromRecipe("property-transfer-test-step-without-path-language.json");
        WsdlTestCase testCase = project.getTestSuiteAt(0).getTestCaseAt(0);
        PropertyTransfersTestStep transfersTestStep = (PropertyTransfersTestStep) testCase.getTestStepAt(0);

        PropertyTransfer propertyTransfer = transfersTestStep.getTransferAt(0);
        assertThat(propertyTransfer.getSourcePathLanguage(), is(PathLanguage.XPATH));
        assertThat(propertyTransfer.getTargetPathLanguage(), is(PathLanguage.XPATH));
    }

    private void assertPropertyTransfer(PropertyTransfer propertyTransfer, boolean sourceAndTargetTestStepsPresent, String transferUserName, String targetPropertyName) {
        assertSourceAndTargetTestStep(propertyTransfer, sourceAndTargetTestStepsPresent);
        assertTransferNameAndSourceDetails(propertyTransfer, transferUserName);
        assertTargetDetails(propertyTransfer, targetPropertyName);
        assertTransferOptions(propertyTransfer);
    }

    private void assertTransferOptions(PropertyTransfer propertyTransfer) {
        assertThat(propertyTransfer.getFailOnError(), is(true));
        assertThat(propertyTransfer.getSetNullOnMissingSource(), is(true));
        assertThat(propertyTransfer.getTransferTextContent(), is(true));
        assertThat(propertyTransfer.getIgnoreEmpty(), is(false));
        assertThat(propertyTransfer.getTransferToAll(), is(false));
        assertThat(propertyTransfer.getTransferChildNodes(), is(false));
        assertThat(propertyTransfer.getEntitize(), is(false));
    }

    private void assertTestStepNameAndNumberOfTransfers(PropertyTransfersTestStep transfersTestStep) {
        assertThat(transfersTestStep.getName(), is("Property Transfers"));
        assertThat(transfersTestStep.getTransferCount(), is(2));
    }

    private void assertSourceAndTargetTestStep(PropertyTransfer propertyTransfer1, boolean sourceAndTargetTestStepsPresent) {
        if (sourceAndTargetTestStepsPresent) {
            assertThat(propertyTransfer1.getSourceStep(), is(not(nullValue())));
            assertThat(propertyTransfer1.getTargetStep(), is(not(nullValue())));
        } else {
            assertThat(propertyTransfer1.getSourceStep(), is(nullValue()));
            assertThat(propertyTransfer1.getTargetStep(), is(nullValue()));
        }
    }

    private void assertTransferNameAndSourceDetails(PropertyTransfer propertyTransfer, String transferName) {
        assertThat(propertyTransfer.getName(), is(transferName));
        assertThat(propertyTransfer.getSourceStepName(), is("REST Request Test Step"));
        assertThat(propertyTransfer.getSourcePropertyName(), is("Response"));
        assertThat(propertyTransfer.getSourcePath(), containsString("declare namespace sam='http://www.soapui.org/sample/'; //sam:response/"));
        assertThat(propertyTransfer.getSourcePathLanguage(), is(PathLanguage.XPATH));
    }

    private void assertTargetDetails(PropertyTransfer propertyTransfer, String targetPropertyName) {
        assertThat(propertyTransfer.getTargetStepName(), is("REST Request Test Step 2"));
        assertThat(propertyTransfer.getTargetPropertyName(), is(targetPropertyName));
        assertThat(propertyTransfer.getTargetPath(), is("declare namespace sam='http://www.soapui.org/sample/'; //sam:login/" + targetPropertyName));
        assertThat(propertyTransfer.getTargetPathLanguage(), is(PathLanguage.XPATH));
    }
}
