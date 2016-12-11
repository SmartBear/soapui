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
 * LoadTestAssertion for asserting the average step time
 *
 * @author Ole.Matzura
 */

public class TestStepAverageAssertion extends AbstractLoadTestAssertion implements Configurable {
    private static final String NAME_FIELD = "Name";
    private static final String NAME_ELEMENT = "name";
    private static final String SAMPLE_INTERVAL_ELEMENT = "sample-interval";
    private static final String SAMPLE_INTERVAL_FIELD = "Sample Interval";
    private static final String MAX_AVERAGE_ELEMENT = "max-average";
    private static final String MAX_ERRORS_ELEMENT = "max-errors";
    private static final String MAX_ERRORS_FIELD = "Max Errors";
    private static final String MIN_REQUESTS_ELEMENT = "min-requests";
    private static final String MAX_AVERAGE_FIELD = "Max Average";
    private static final String MINIMUM_REQUESTS_FIELD = "Minimum Requests";

    private int minRequests;
    private int maxAverage;
    private int maxErrors;
    private int sampleInterval;
    private XFormDialog dialog;
    public static final String STEP_AVERAGE_TYPE = "Step Average";

    public TestStepAverageAssertion(LoadTestAssertionConfig assertionConfig, WsdlLoadTest loadTest) {
        super(assertionConfig, loadTest);

        init(assertionConfig);
        initIcon("/average_loadtest_assertion.gif");
    }

    private void init(LoadTestAssertionConfig assertionConfig) {
        XmlObject configuration = assertionConfig.getConfiguration();

        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(configuration);
        setName(reader.readString(NAME_ELEMENT, "Step Average"));
        minRequests = reader.readInt(MIN_REQUESTS_ELEMENT, 100);
        maxAverage = reader.readInt(MAX_AVERAGE_ELEMENT, 1000);
        setTargetStep(reader.readString(TEST_STEP_ELEMENT, ANY_TEST_STEP));
        maxErrors = reader.readInt(MAX_ERRORS_ELEMENT, -1);
        sampleInterval = reader.readInt(SAMPLE_INTERVAL_ELEMENT, 20);
    }

    public String assertResult(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestStepResult result,
                               TestCaseRunner testRunner, TestCaseRunContext runContext) {
        WsdlLoadTest loadTest = (WsdlLoadTest) loadTestRunner.getLoadTest();
        LoadTestStatistics statisticsModel = loadTest.getStatisticsModel();

        TestStep step = result.getTestStep();
        if (targetStepMatches(step)) {
            int index = step.getTestCase().getIndexOfTestStep(step);

            long average = statisticsModel.getStatistic(index, Statistic.AVERAGE);
            long count = statisticsModel.getStatistic(index, Statistic.AVERAGE);
            if (count > minRequests && (count % sampleInterval == 0) && average >= maxAverage) {
                return returnErrorOrFail("Average [" + average + "] exceeds limit [" + maxAverage + "]", maxErrors,
                        loadTestRunner, context);
            }
        } else if (ALL_TEST_STEPS.equals(getTargetStep())) {
            long average = statisticsModel.getStatistic(LoadTestStatistics.TOTAL, Statistic.AVERAGE);
            long count = statisticsModel.getStatistic(LoadTestStatistics.TOTAL, Statistic.COUNT);
            if (count > minRequests && (count % sampleInterval == 0) && average >= maxAverage) {
                return returnErrorOrFail("Average [" + average + "] exceeds limit [" + maxAverage + "]", maxErrors,
                        loadTestRunner, context);
            }
        }

        return null;
    }

    public String assertResults(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                                TestCaseRunContext runContext) {
        return null;
    }

    public String getDescription() {
        return "testStep: " + getTargetStep() + ", minRequests: " + minRequests + ", maxAverage: " + maxAverage
                + ", maxErrors: " + maxErrors + ", sampleInterval: " + sampleInterval;
    }

    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        StringToStringMap values = new StringToStringMap();

        values.put(NAME_FIELD, getName());
        values.put(MINIMUM_REQUESTS_FIELD, String.valueOf(minRequests));
        values.put(MAX_AVERAGE_FIELD, String.valueOf(maxAverage));
        values.put(TEST_STEP_FIELD, getTargetStep());
        values.put(MAX_ERRORS_FIELD, String.valueOf(maxErrors));
        values.put(SAMPLE_INTERVAL_FIELD, String.valueOf(sampleInterval));

        dialog.setOptions(TEST_STEP_FIELD, getTargetStepOptions(true));
        values = dialog.show(values);

        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            try {
                minRequests = Integer.parseInt(values.get(MINIMUM_REQUESTS_FIELD));
                maxAverage = Integer.parseInt(values.get(MAX_AVERAGE_FIELD));
                maxErrors = Integer.parseInt(values.get(MAX_ERRORS_FIELD));
                sampleInterval = Integer.parseInt(values.get(SAMPLE_INTERVAL_FIELD));
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

    protected void updateConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();

        builder.add(NAME_ELEMENT, getName());
        builder.add(MIN_REQUESTS_ELEMENT, minRequests);
        builder.add(MAX_AVERAGE_ELEMENT, maxAverage);
        builder.add(TEST_STEP_ELEMENT, getTargetStep());
        builder.add(MAX_ERRORS_ELEMENT, maxErrors);
        builder.add(SAMPLE_INTERVAL_ELEMENT, sampleInterval);

        setConfiguration(builder.finish());
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Step Average Assertion");
        XForm form = builder.createForm("Basic");

        form.addTextField(NAME_FIELD, "Name of this assertion", FieldType.TEXT);
        form.addTextField(MINIMUM_REQUESTS_FIELD, "Minimum number of steps before asserting", FieldType.TEXT);
        form.addTextField(MAX_AVERAGE_FIELD, "Maximum allowed average step time", FieldType.TEXT);
        form.addTextField(MAX_ERRORS_FIELD, "Maximum number of allowed errors before failing loadtest (-1 = unlimited)",
                FieldType.TEXT);
        form.addTextField(SAMPLE_INTERVAL_FIELD, "Step count interval between sampling", FieldType.TEXT);
        form.addComboBox(TEST_STEP_FIELD, new String[0], "TestStep to assert");

        dialog = builder.buildDialog(
                builder.buildOkCancelHelpActions(HelpUrls.STEP_AVERAGE_LOAD_TEST_ASSERTION_HELP_URL),
                "Specify options for this Step Average Assertion", UISupport.OPTIONS_ICON);
    }
}
