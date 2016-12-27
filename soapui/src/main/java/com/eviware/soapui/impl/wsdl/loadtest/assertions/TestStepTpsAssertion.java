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

/**
 * LoadTestAssertion for asserting the TPS of a teststep
 *
 * @author Ole.Matzura
 */

public class TestStepTpsAssertion extends AbstractLoadTestAssertion implements Configurable {
    private static final String NAME_FIELD = "Name";
    private static final String NAME_ELEMENT = "name";
    private static final String MIN_VALUE_ELEMENT = "min-value";
    private static final String MIN_REQUESTS_ELEMENT = "min-requests";
    private static final String MIN_VALUE_FIELD = "Minimum TPS";
    private static final String MINIMUM_REQUESTS_FIELD = "Minimum Requests";
    private static final String MAX_ERRORS_ELEMENT = "max-errors";
    private static final String MAX_ERRORS_FIELD = "Max Errors";

    private int minRequests;
    private int minValue;
    private int maxErrors;
    private XFormDialog dialog;
    public static final String STEP_TPS_TYPE = "Step TPS";

    public TestStepTpsAssertion(LoadTestAssertionConfig assertionConfig, WsdlLoadTest loadTest) {
        super(assertionConfig, loadTest);

        init(assertionConfig);
        initIcon("/tps_loadtest_assertion.png");
    }

    private void init(LoadTestAssertionConfig assertionConfig) {
        XmlObject configuration = assertionConfig.getConfiguration();

        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(configuration);
        setName(reader.readString(TestStepTpsAssertion.NAME_ELEMENT, "Step TPS"));
        minRequests = reader.readInt(TestStepTpsAssertion.MIN_REQUESTS_ELEMENT, 100);
        minValue = reader.readInt(TestStepTpsAssertion.MIN_VALUE_ELEMENT, 10);
        setTargetStep(reader.readString(TestStepTpsAssertion.TEST_STEP_ELEMENT, TestStepTpsAssertion.ANY_TEST_STEP));
        maxErrors = reader.readInt(MAX_ERRORS_ELEMENT, -1);
    }

    public String assertResult(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestStepResult result,
                               TestCaseRunner testRunner, TestCaseRunContext runContext) {
        TestStep step = result.getTestStep();
        if (targetStepMatches(step)) {
            WsdlLoadTest loadTest = (WsdlLoadTest) loadTestRunner.getLoadTest();
            LoadTestStatistics statisticsModel = loadTest.getStatisticsModel();

            int index = step.getTestCase().getIndexOfTestStep(step);

            long tps = statisticsModel.getStatistic(index, Statistic.TPS);
            if (statisticsModel.getStatistic(index, Statistic.COUNT) >= minRequests && tps < minValue) {
                return returnErrorOrFail("TPS [" + tps + "] is less than limit [" + minValue + "]", maxErrors,
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

            long tps = statisticsModel.getStatistic(LoadTestStatistics.TOTAL, Statistic.TPS);

            if (statisticsModel.getStatistic(LoadTestStatistics.TOTAL, Statistic.COUNT) > minRequests && tps < minValue) {
                return returnErrorOrFail("TPS [" + tps + "] is less than limit [" + minValue + "]", maxErrors,
                        loadTestRunner, context);
            }
        }

        return null;
    }

    public String getDescription() {
        return "testStep: " + getTargetStep() + ", minRequests: " + minRequests + ", minValue: " + minValue
                + ", maxErrors: " + maxErrors;
    }

    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        StringToStringMap values = new StringToStringMap();

        values.put(TestStepTpsAssertion.NAME_FIELD, getName());
        values.put(TestStepTpsAssertion.MINIMUM_REQUESTS_FIELD, String.valueOf(minRequests));
        values.put(TestStepTpsAssertion.MIN_VALUE_FIELD, String.valueOf(minValue));
        values.put(TestStepTpsAssertion.TEST_STEP_FIELD, getTargetStep());
        values.put(TestStepTpsAssertion.MAX_ERRORS_FIELD, String.valueOf(maxErrors));

        dialog.setOptions(TestStepTpsAssertion.TEST_STEP_FIELD, getTargetStepOptions(true));
        values = dialog.show(values);

        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            try {
                minRequests = Integer.parseInt(values.get(TestStepTpsAssertion.MINIMUM_REQUESTS_FIELD));
                minValue = Integer.parseInt(values.get(TestStepTpsAssertion.MIN_VALUE_FIELD));
                maxErrors = Integer.parseInt(values.get(TestStepTpsAssertion.MAX_ERRORS_FIELD));
                setTargetStep(values.get(TestStepTpsAssertion.TEST_STEP_FIELD));
                setName(values.get(TestStepTpsAssertion.NAME_FIELD));
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

        builder.add(TestStepTpsAssertion.NAME_ELEMENT, getName());
        builder.add(TestStepTpsAssertion.MIN_REQUESTS_ELEMENT, minRequests);
        builder.add(TestStepTpsAssertion.MIN_VALUE_ELEMENT, minValue);
        builder.add(TestStepTpsAssertion.TEST_STEP_ELEMENT, getTargetStep());
        builder.add(TestStepTpsAssertion.MAX_ERRORS_ELEMENT, maxErrors);

        setConfiguration(builder.finish());
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Step TPS Assertion");
        XForm form = builder.createForm("Basic");

        form.addTextField(TestStepTpsAssertion.NAME_FIELD, "Name of this assertion", FieldType.TEXT);
        form.addTextField(TestStepTpsAssertion.MINIMUM_REQUESTS_FIELD, "Minimum steps before asserting", FieldType.TEXT);
        form.addTextField(TestStepTpsAssertion.MIN_VALUE_FIELD, "Minimum required step TPS", FieldType.TEXT);
        form.addTextField(TestStepTpsAssertion.MAX_ERRORS_FIELD, "Maximum number of errors before failing",
                FieldType.TEXT);
        form.addComboBox(TestStepTpsAssertion.TEST_STEP_FIELD, new String[0], "TestStep to assert");

        dialog = builder.buildDialog(builder.buildOkCancelHelpActions(HelpUrls.STEP_TPS_LOAD_TEST_ASSERTION_HELP_URL),
                "Specify options for this Step TPS Assertion", UISupport.OPTIONS_ICON);
    }
}
