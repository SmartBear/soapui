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
import com.eviware.soapui.impl.wsdl.loadtest.log.LoadTestLog;
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
 * LoadTestAssertion for asserting the maximum number of total assertion errors
 *
 * @author Ole.Matzura
 */

public class MaxErrorsAssertion extends AbstractLoadTestAssertion implements Configurable {
    private static final String NAME_FIELD = "Name";
    private static final String NAME_ELEMENT = "name";
    private static final String MAX_ABSOLUTE_ERRORS_ELEMENT = "max-absolute-errors";
    private static final String MAX_ABSOLUTE_ERRORS_FIELD = "Max Absolute Errors";
    private static final String MAX_RELATIVE_ERRORS_ELEMENT = "max-relative-errors";
    private static final String MAX_RELATIVE_ERRORS_FIELD = "Max Relative Errors";

    private float maxRelativeErrors;
    private int maxAbsoluteErrors;
    private XFormDialog dialog;
    public static final String MAX_ERRORS_TYPE = "Max Errors";

    public MaxErrorsAssertion(LoadTestAssertionConfig assertionConfig, WsdlLoadTest loadTest) {
        super(assertionConfig, loadTest);

        init(assertionConfig);
        initIcon("/errors_loadtest_assertion.gif");
    }

    private void init(LoadTestAssertionConfig assertionConfig) {
        XmlObject configuration = assertionConfig.getConfiguration();
        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(configuration);

        setName(reader.readString(MaxErrorsAssertion.NAME_ELEMENT, "Max Errors"));
        maxAbsoluteErrors = reader.readInt(MAX_ABSOLUTE_ERRORS_ELEMENT, 100);
        maxRelativeErrors = reader.readFloat(MAX_RELATIVE_ERRORS_ELEMENT, (float) 0.2);
        setTargetStep(reader.readString(TEST_STEP_ELEMENT, ALL_TEST_STEPS));
    }

    public String getDescription() {
        return "testStep: " + getTargetStep() + ", maxAbsoluteErrors: " + maxAbsoluteErrors + ", maxRelativeErrors; "
                + maxRelativeErrors;
    }

    public String assertResult(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestStepResult result,
                               TestCaseRunner testRunner, TestCaseRunContext runContext) {
        TestStep step = result.getTestStep();
        if (targetStepMatches(step)) {
            WsdlLoadTest loadTest = (WsdlLoadTest) loadTestRunner.getLoadTest();
            LoadTestLog loadTestLog = loadTest.getLoadTestLog();

            int errorCount = loadTestLog.getErrorCount(step.getName());
            if (maxAbsoluteErrors >= 0 && errorCount > maxAbsoluteErrors) {
                loadTestRunner.fail("Maximum number of errors [" + maxAbsoluteErrors + "] exceeded for step ["
                        + step.getName() + "]");
            }

            int index = step.getTestCase().getIndexOfTestStep(step);

            LoadTestStatistics statisticsModel = loadTest.getStatisticsModel();
            long totalSteps = statisticsModel.getStatistic(index, Statistic.COUNT);
            float relativeErrors = (float) errorCount / (float) totalSteps;

            if (maxRelativeErrors > 0 && relativeErrors > maxRelativeErrors) {
                loadTestRunner.fail("Maximum relative number of errors [" + maxRelativeErrors + "] exceeded for step ["
                        + step.getName() + "]");
            }
        }

        return null;
    }

    public String assertResults(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                                TestCaseRunContext runContext) {
        if (ALL_TEST_STEPS.equals(getTargetStep())) {
            WsdlLoadTest loadTest = (WsdlLoadTest) loadTestRunner.getLoadTest();
            LoadTestLog loadTestLog = loadTest.getLoadTestLog();

            int errorCount = loadTestLog.getErrorCount(null);
            if (maxAbsoluteErrors >= 0 && errorCount > maxAbsoluteErrors) {
                loadTestRunner.fail("Maximum number of errors [" + maxAbsoluteErrors + "] exceeded");
            }

            LoadTestStatistics statisticsModel = loadTest.getStatisticsModel();
            long totalSteps = statisticsModel.getStatistic(LoadTestStatistics.TOTAL, Statistic.COUNT);
            float relativeErrors = (float) errorCount / (float) totalSteps;

            if (maxRelativeErrors > 0 && relativeErrors > maxRelativeErrors) {
                loadTestRunner.fail("Maximum relative number of errors [" + maxRelativeErrors + "] exceeded");
            }
        }

        return null;
    }

    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        StringToStringMap values = new StringToStringMap();

        values.put(NAME_FIELD, getName());
        values.put(MAX_ABSOLUTE_ERRORS_FIELD, String.valueOf(maxAbsoluteErrors));
        values.put(MAX_RELATIVE_ERRORS_FIELD, String.valueOf(maxRelativeErrors));
        values.put(TEST_STEP_FIELD, getTargetStep());

        dialog.setOptions(TEST_STEP_FIELD, getTargetStepOptions(true));
        values = dialog.show(values);

        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            try {
                maxAbsoluteErrors = Integer.parseInt(values.get(MAX_ABSOLUTE_ERRORS_FIELD));
                maxRelativeErrors = Float.parseFloat(values.get(MAX_RELATIVE_ERRORS_FIELD));
                setTargetStep(values.get(TEST_STEP_FIELD));
                setName(values.get(NAME_FIELD));
            } catch (Exception e) {
                UISupport.showErrorMessage(e.getMessage());
            }

            updateConfiguration();

            return true;
        }

        return false;
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Max Errors Assertion");
        XForm form = builder.createForm("Basic");

        form.addTextField(NAME_FIELD, "Name of this assertion", FieldType.TEXT);
        form.addTextField(MAX_ABSOLUTE_ERRORS_FIELD, "Maximum number of errors before failing", FieldType.TEXT);
        form.addTextField(MAX_RELATIVE_ERRORS_FIELD, "Relative maximum number of errors before failing (0-1)",
                FieldType.TEXT);
        form.addComboBox(TEST_STEP_FIELD, new String[0], "TestStep to assert");

        dialog = builder.buildDialog(
                builder.buildOkCancelHelpActions(HelpUrls.MAX_ERRORS_LOAD_TEST_ASSERTION_HELP_URL),
                "Specify options for this Max Errors Assertion", UISupport.OPTIONS_ICON);
    }

    protected void updateConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();

        builder.add(NAME_ELEMENT, getName());
        builder.add(MAX_ABSOLUTE_ERRORS_ELEMENT, maxAbsoluteErrors);
        builder.add(MAX_RELATIVE_ERRORS_ELEMENT, maxRelativeErrors);
        builder.add(TEST_STEP_ELEMENT, getTargetStep());

        setConfiguration(builder.finish());
    }
}
