/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wsdl.loadtest.assertions;

import com.eviware.soapui.config.LoadTestAssertionConfig;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.data.LoadTestStatistics;
import com.eviware.soapui.impl.wsdl.loadtest.data.LoadTestStatistics.Statistic;
import com.eviware.soapui.impl.wsdl.support.Configurable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XForm.FieldType;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import org.apache.xmlbeans.XmlObject;

import java.util.Arrays;

/**
 * LoadTestAssertion for asserting the status of a teststep
 *
 * @author Ole.Matzura
 */

public class TestStepStatusAssertion extends AbstractLoadTestAssertion implements Configurable {
    private static final String NAME_FIELD = "Name";
    private static final String NAME_ELEMENT = "name";
    private static final String MINIMUM_REQUESTS_FIELD = "Minimum Requests";
    private static final String MIN_REQUESTS_ELEMENT = "min-requests";
    private static final String MAX_ERRORS_ELEMENT = "max-errors";
    private static final String MAX_ERRORS_FIELD = "Max Errors";

    private int minRequests;
    private int maxErrors;
    private XFormDialog dialog;
    public static final String STEP_STATUS_TYPE = "Step Status";

    public TestStepStatusAssertion(LoadTestAssertionConfig assertionConfig, WsdlLoadTest loadTest) {
        super(assertionConfig, loadTest);

        init(assertionConfig);
        initIcon("/status_loadtest_assertion.png");
    }

    private void init(LoadTestAssertionConfig assertionConfig) {
        XmlObject configuration = assertionConfig.getConfiguration();
        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(configuration);

        setName(reader.readString(TestStepStatusAssertion.NAME_ELEMENT, "Step Status"));
        minRequests = reader.readInt(TestStepStatusAssertion.MIN_REQUESTS_ELEMENT, 0);
        setTargetStep(reader.readString(TestStepStatusAssertion.TEST_STEP_ELEMENT, ANY_TEST_STEP));
        maxErrors = reader.readInt(MAX_ERRORS_ELEMENT, -1);
    }

    public String getDescription() {
        return "testStep: " + getTargetStep() + ", minRequests: " + minRequests + ", maxErrors: " + maxErrors;
    }

    public String assertResult(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestStepResult result,
                               TestCaseRunner testRunner, TestCaseRunContext runContext) {
        WsdlLoadTest loadTest = (WsdlLoadTest) loadTestRunner.getLoadTest();
        LoadTestStatistics statisticsModel = loadTest.getStatisticsModel();

        TestStep step = result.getTestStep();

        if (targetStepMatches(step)) {
            int index = step.getTestCase().getIndexOfTestStep(step);

            if (statisticsModel.getStatistic(index, Statistic.COUNT) >= minRequests
                    && result.getStatus() == TestStepStatus.FAILED) {
                return returnErrorOrFail("TestStep [" + step.getName() + "] result status is "
                        + result.getStatus().toString() + "; " + Arrays.toString(result.getMessages()), maxErrors,
                        loadTestRunner, context);
            } else {
                return null;
            }
        }

        return null;
    }

    public String assertResults(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                                TestCaseRunContext runContext) {
        return null;
    }

    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        StringToStringMap values = new StringToStringMap();

        values.put(NAME_FIELD, getName());
        values.put(MINIMUM_REQUESTS_FIELD, String.valueOf(minRequests));
        values.put(TEST_STEP_FIELD, getTargetStep());
        values.put(MAX_ERRORS_FIELD, String.valueOf(maxErrors));

        dialog.setOptions(TestStepStatusAssertion.TEST_STEP_FIELD, getTargetStepOptions(false));
        values = dialog.show(values);

        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            try {
                minRequests = Integer.parseInt(values.get(MINIMUM_REQUESTS_FIELD));
                maxErrors = Integer.parseInt(values.get(MAX_ERRORS_FIELD));
                setName(values.get(NAME_FIELD));
                setTargetStep(values.get(TEST_STEP_FIELD));
            } catch (Exception e) {
                UISupport.showErrorMessage(e.getMessage());
            }

            updateConfiguration();
            return true;
        }

        return false;
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Step Status Assertion");
        XForm form = builder.createForm("Basic");

        form.addTextField(NAME_FIELD, "Name of this assertion", FieldType.TEXT);
        form.addTextField(MINIMUM_REQUESTS_FIELD, "Minimum number of runs before asserting", FieldType.TEXT);
        form.addTextField(MAX_ERRORS_FIELD, "Maximum number of errors before failing", FieldType.TEXT);
        form.addComboBox(TEST_STEP_FIELD, new String[0], "TestStep to assert");

        dialog = builder.buildDialog(
                builder.buildOkCancelHelpActions(HelpUrls.STEP_STATUS_LOAD_TEST_ASSERTION_HELP_URL),
                "Specify options for this Step Status Assertion", UISupport.OPTIONS_ICON);
    }

    protected void updateConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();

        builder.add(NAME_ELEMENT, getName());
        builder.add(MIN_REQUESTS_ELEMENT, minRequests);
        builder.add(TEST_STEP_ELEMENT, getTargetStep());
        builder.add(MAX_ERRORS_ELEMENT, maxErrors);

        setConfiguration(builder.finish());
    }
}
