/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XForm.FieldType;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlObject;

import java.util.List;

/**
 * LoadTestAssertion for asserting the maximum step time
 *
 * @author Ole.Matzura
 */

public class TestStepMaxAssertion extends AbstractLoadTestAssertion implements Configurable {
    private static final String NAME_FIELD = "Name";
    private static final String NAME_ELEMENT = "name";
    private static final String MAX_VALUE_ELEMENT = "max-value";
    private static final String MIN_REQUESTS_ELEMENT = "min-requests";
    private static final String MAX_VALUE_FIELD = "Max Time";
    private static final String MINIMUM_REQUESTS_FIELD = "Minimum Requests";
    private static final String MAX_ERRORS_ELEMENT = "max-errors";
    private static final String MAX_ERRORS_FIELD = "Max Errors";

    private int minRequests;
    private int maxValue;
    private int maxErrors;
    private XFormDialog dialog;
    public static final String STEP_MAXIMUM_TYPE = "Step Maximum";
    private final static Logger log = LogManager.getLogger(TestStepMaxAssertion.class);

    public TestStepMaxAssertion(LoadTestAssertionConfig assertionConfig, WsdlLoadTest loadTest) {
        super(assertionConfig, loadTest);

        init(assertionConfig);
        initIcon("/max_loadtest_assertion.gif");
    }

    private void init(LoadTestAssertionConfig assertionConfig) {
        XmlObject configuration = assertionConfig.getConfiguration();

        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(configuration);
        setName(reader.readString(TestStepMaxAssertion.NAME_ELEMENT, "Step Maximum"));
        minRequests = reader.readInt(TestStepMaxAssertion.MIN_REQUESTS_ELEMENT, 100);
        maxValue = reader.readInt(TestStepMaxAssertion.MAX_VALUE_ELEMENT, 1000);
        setTargetStep(reader.readString(TestStepMaxAssertion.TEST_STEP_ELEMENT, TestStepMaxAssertion.ANY_TEST_STEP));
        maxErrors = reader.readInt(MAX_ERRORS_ELEMENT, -1);
    }

    public String assertResult(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestStepResult result,
                               TestCaseRunner testRunner, TestCaseRunContext runContext) {
        TestStep step = result.getTestStep();
        if (targetStepMatches(step)) {
            WsdlLoadTest loadTest = (WsdlLoadTest) loadTestRunner.getLoadTest();
            LoadTestStatistics statisticsModel = loadTest.getStatisticsModel();

            int index = step.getTestCase().getIndexOfTestStep(step);

            long maximum = result.getTimeTaken();
            if (statisticsModel.getStatistic(index, Statistic.COUNT) > minRequests && maximum >= maxValue) {
                return returnErrorOrFail("Time [" + maximum + "] exceeds limit [" + maxValue + "]", maxErrors,
                        loadTestRunner, context);
            }
        }

        return null;
    }

    public String assertResults(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                                TestCaseRunContext runContext) {
        if (ALL_TEST_STEPS.equals(getTargetStep())) {
            WsdlLoadTest loadTest = (WsdlLoadTest) loadTestRunner.getLoadTest();
            LoadTestStatistics statisticsModel = loadTest.getStatisticsModel();

            long sum = 0;
            List<TestStepResult> results = testRunner.getResults();
            for (int c = 0; c < results.size(); c++) {
                TestStepResult result = results.get(c);
                if (result == null) {
                    log.warn("Result [" + c + "] is null in TestCase [" + testRunner.getTestCase().getName() + "]");
                    continue;
                }

                sum += result.getTimeTaken();
            }

            if (statisticsModel.getStatistic(LoadTestStatistics.TOTAL, Statistic.COUNT) >= minRequests
                    && sum >= maxValue) {
                return returnErrorOrFail("Time [" + sum + "] exceeds limit [" + maxValue + "]", maxErrors, loadTestRunner,
                        context);
            }
        }

        return null;
    }

    public String getDescription() {
        return "testStep: " + getTargetStep() + ", minRequests: " + minRequests + ", maxValue: " + maxValue
                + ", maxErrors: " + maxErrors;
    }

    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        StringToStringMap values = new StringToStringMap();

        values.put(TestStepMaxAssertion.NAME_FIELD, getName());
        values.put(TestStepMaxAssertion.MINIMUM_REQUESTS_FIELD, String.valueOf(minRequests));
        values.put(TestStepMaxAssertion.MAX_VALUE_FIELD, String.valueOf(maxValue));
        values.put(TestStepMaxAssertion.TEST_STEP_FIELD, getTargetStep());
        values.put(TestStepMaxAssertion.MAX_ERRORS_FIELD, String.valueOf(maxErrors));

        dialog.setOptions(TestStepMaxAssertion.TEST_STEP_FIELD, getTargetStepOptions(true));
        values = dialog.show(values);

        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            try {
                minRequests = Integer.parseInt(values.get(TestStepMaxAssertion.MINIMUM_REQUESTS_FIELD));
                maxValue = Integer.parseInt(values.get(TestStepMaxAssertion.MAX_VALUE_FIELD));
                maxErrors = Integer.parseInt(values.get(TestStepMaxAssertion.MAX_ERRORS_FIELD));
                setTargetStep(values.get(TestStepMaxAssertion.TEST_STEP_FIELD));
                setName(values.get(TestStepMaxAssertion.NAME_FIELD));
            } catch (Exception e) {
                UISupport.showErrorMessage(e.getMessage());
            }

            updateConfiguration();

            return true;
        }

        return false;
    }

    protected void updateConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();

        builder.add(TestStepMaxAssertion.NAME_ELEMENT, getName());
        builder.add(TestStepMaxAssertion.MIN_REQUESTS_ELEMENT, minRequests);
        builder.add(TestStepMaxAssertion.MAX_VALUE_ELEMENT, maxValue);
        builder.add(TestStepMaxAssertion.TEST_STEP_ELEMENT, getTargetStep());
        builder.add(TestStepMaxAssertion.MAX_ERRORS_ELEMENT, maxErrors);

        setConfiguration(builder.finish());
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Step Maximum Assertion");
        XForm form = builder.createForm("Basic");

        form.addTextField(TestStepMaxAssertion.NAME_FIELD, "Name of this assertion", FieldType.TEXT);
        form.addTextField(TestStepMaxAssertion.MINIMUM_REQUESTS_FIELD, "Minimum steps before asserting", FieldType.TEXT);
        form.addTextField(TestStepMaxAssertion.MAX_VALUE_FIELD, "Maximum allowed step time", FieldType.TEXT);
        form.addTextField(TestStepMaxAssertion.MAX_ERRORS_FIELD, "Maximum number of errors before failing",
                FieldType.TEXT);
        form.addComboBox(TestStepMaxAssertion.TEST_STEP_FIELD, new String[0], "TestStep to assert");

        dialog = builder.buildDialog(
                builder.buildOkCancelHelpActions(HelpUrls.STEP_MAXIMUM_LOAD_TEST_ASSERTION_HELP_URL),
                "Specify options for this Step Maximum Assertion", UISupport.OPTIONS_ICON);
    }
}
