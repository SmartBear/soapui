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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.ManualTestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.DefaultTestStepProperty;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JFormDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Dialog.ModalityType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nebojsa.tasic
 */

public class ManualTestStep extends WsdlTestStepWithProperties implements PropertyExpansionContainer

{
    @SuppressWarnings("unused")
    private final static Logger log = LogManager.getLogger(WsdlTestRequestStep.class);
    protected ManualTestStepConfig manualTestStepConfig;
    private ManualTestStepResult testStepResult;
    public final static String MANUAL_STEP = ManualTestStep.class.getName() + "@manualstep";
    public static final String STATUS_PROPERTY = WsdlTestRequest.class.getName() + "@status";
    private final boolean forLoadTest;

    public ManualTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        super(testCase, config, true, forLoadTest);
        this.forLoadTest = forLoadTest;

        if (!forLoadTest) {
            setIcon(UISupport.createImageIcon("/manual_step.png"));
        }

        if (getConfig().getConfig() != null) {
            manualTestStepConfig = (ManualTestStepConfig) getConfig().getConfig().changeType(ManualTestStepConfig.type);
        } else {
            manualTestStepConfig = (ManualTestStepConfig) getConfig().addNewConfig().changeType(
                    ManualTestStepConfig.type);
        }

        addProperty(new DefaultTestStepProperty("Result", true, new DefaultTestStepProperty.PropertyHandlerAdapter() {
            @Override
            public String getValue(DefaultTestStepProperty property) {
                return getLastResult() == null ? null : getLastResult().getResult();
            }
        }, this));

        addProperty(new TestStepBeanProperty("ExpectedResult", false, this, "expectedResult", this));
    }

    protected ManualTestStepResult getLastResult() {
        return testStepResult;
    }

    public ManualTestStepConfig getManualTestStepConfig() {
        return manualTestStepConfig;
    }

    @Override
    public WsdlTestStep clone(WsdlTestCase targetTestCase, String name) {
        beforeSave();

        TestStepConfig config = (TestStepConfig) getConfig().copy();
        ManualTestStep result = (ManualTestStep) targetTestCase.addTestStep(config);

        return result;
    }

    @Override
    public void release() {
        super.release();
    }

    public TestStepResult run(TestCaseRunner runner, TestCaseRunContext runContext) {
        testStepResult = new ManualTestStepResult(this);
        testStepResult.startTimer();

        if (!forLoadTest && SoapUI.usingGraphicalEnvironment()) {
            XFormDialog dialog = ADialogBuilder.buildDialog(Form.class);
            dialog.setSize(450, 550);
            ((JFormDialog) dialog).getDialog().setModalityType(ModalityType.MODELESS);

            dialog.setValue(Form.DESCRIPTION, runContext.expand(getDescription()));
            dialog.setValue(Form.EXPECTED_DESULT, runContext.expand(getExpectedResult()));
            dialog.setValue(Form.STATUS, "Unknown");

            UISupport.select(this);

            while (!dialog.show()) {
                if (UISupport.confirm("Are you sure? This will stop the entire test", "Cancel TestStep")) {
                    testStepResult.setStatus(TestStepStatus.CANCELED);
                    runner.cancel("Canceled by user");
                    break;
                }
            }

            if (dialog.getValue(Form.STATUS).equals("Pass")) {
                testStepResult.setStatus(TestStepStatus.OK);
            } else if (dialog.getValue(Form.STATUS).equals("Fail")) {
                testStepResult.setStatus(TestStepStatus.FAILED);
            }

            String result = dialog.getValue(Form.RESULT);
            if (StringUtils.hasContent(result)) {
                testStepResult.setResult(result);
            }

            testStepResult.setUrls(((XFormOptionsField) dialog.getFormField(Form.URLS)).getOptions());

            dialog.release();
        }

        testStepResult.stopTimer();
        // FIXME This should not be hard coded
        return testStepResult;
    }

    @Override
    public boolean cancel() {
        return true;
    }

    @Override
    public String getDefaultSourcePropertyName() {
        return "Result";
    }

    @Override
    public String getDefaultTargetPropertyName() {
        return "ExpectedResult";
    }

    public PropertyExpansion[] getPropertyExpansions() {
        List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this, this, "description"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this, this, "expectedResult"));
        return result.toArray(new PropertyExpansion[result.size()]);
    }

    public String getExpectedResult() {
        return manualTestStepConfig.getExpectedResult();
    }

    public void setExpectedResult(String expectedResult) {
        String old = getExpectedResult();
        if (String.valueOf(old).equals(expectedResult)) {
            return;
        }

        manualTestStepConfig.setExpectedResult(expectedResult);
        notifyPropertyChanged("expectedResult", old, expectedResult);
        firePropertyValueChanged("ExpectedResult", old, expectedResult);
    }

    public void resetConfigOnMove(TestStepConfig config) {
        super.resetConfigOnMove(config);
        manualTestStepConfig = (ManualTestStepConfig) config.getConfig().changeType(ManualTestStepConfig.type);
    }

    @AForm(description = "", name = "Run Manual TestStep", helpUrl = HelpUrls.MANUALTESTSTEP_HELP_URL)
    protected interface Form {
        @AField(name = "Description", description = "Describes the actions to perform", type = AFieldType.INFORMATION)
        public final static String DESCRIPTION = "Description";

        @AField(name = "Expected Result", description = "Describes the actions to perform", type = AFieldType.INFORMATION)
        public final static String EXPECTED_DESULT = "Expected Result";

        @AField(name = "Result", description = "an optional result description or value", type = AFieldType.STRINGAREA)
        public final static String RESULT = "Result";

        @AField(name = "URLs", description = "A list of URLs related to the result", type = AFieldType.STRINGLIST)
        public final static String URLS = "URLs";

        @AField(name = "Result Status", description = "The result status", type = AFieldType.ENUMERATION, values = {
                "Pass", "Fail", "Unknown"})
        public final static String STATUS = "Result Status";
    }
}
