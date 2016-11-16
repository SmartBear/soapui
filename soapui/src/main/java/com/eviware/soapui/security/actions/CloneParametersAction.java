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

package com.eviware.soapui.security.actions;

import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.scan.AbstractSecurityScan;
import com.eviware.soapui.security.scan.AbstractSecurityScanWithProperties;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormMultiSelectList;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.SwingXFormDialog;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class CloneParametersAction extends AbstractAction {

    private XFormDialog dialog;
    protected DefaultActionList actionList;
    private AbstractSecurityScanWithProperties securityScan;

    public CloneParametersAction() {
        super("Clone SecurityScan Parameters");
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/clone_parameters.png"));
        putValue(Action.SHORT_DESCRIPTION, "Clones parameter");
        setEnabled(false);
    }

    public CloneParametersAction(AbstractSecurityScanWithProperties securityScan) {
        super("Clone SecurityScan Parameters");
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/clone_parameters.png"));
        putValue(Action.SHORT_DESCRIPTION, "Clones parameter");
        this.securityScan = securityScan;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        XFormDialog dialog = createCloneParameterDialog();
        dialog.show();
        // model.fireTableDataChanged();
    }

    public void setSecurityScan(AbstractSecurityScanWithProperties securityScan) {
        this.securityScan = securityScan;
    }

    private class OkAction extends AbstractAction {

        private XFormDialog dialog;

        public OkAction() {
            super("OK");
        }

        public void setDialog(XFormDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (dialog != null) {
                ((SwingXFormDialog) dialog).setReturnValue(XFormDialog.OK_OPTION);
                List<ModelItem> items = performClone(true);
                UISupport.showInfoMessage("Updated " + items.size() + " scans");
                dialog.setVisible(false);
            }
        }
    }

    private String[] getSecurableTestStepsNames(TestCase testCase) {
        List<TestStep> testStepList = testCase.getTestStepList();
        List<String> namesList = new ArrayList<String>();
        for (TestStep testStep : testStepList) {
            if (AbstractSecurityScan.isSecurable(testStep)) {
                namesList.add(testStep.getName());
            }
        }
        String[] names = new String[namesList.size()];
        for (int c = 0; c < namesList.size(); c++) {
            names[c] = namesList.get(c);
        }
        return names;
    }

    public List<ModelItem> performClone(boolean showErrorMessage) {
        List<ModelItem> items = new ArrayList<ModelItem>();
        String targetTestSuiteName = dialog.getValue(CloneParameterDialog.TARGET_TESTSUITE);
        String targetTestCaseName = dialog.getValue(CloneParameterDialog.TARGET_TESTCASE);
        String targetSecurityTestName = dialog.getValue(CloneParameterDialog.TARGET_SECURITYTEST);
        String targetSecurityTestStepName = dialog.getValue(CloneParameterDialog.TARGET_TESTSTEP);
        String[] targetSecurityScans = StringUtils.toStringArray(((XFormMultiSelectList) dialog
                .getFormField(CloneParameterDialog.TARGET_SECURITYSCAN)).getSelectedOptions());

        if (targetSecurityScans.length == 0) {
            if (showErrorMessage) {
                UISupport.showErrorMessage("No SecurityScans selected..");
            }
            return items;
        }

        int[] indexes = ((XFormOptionsField) dialog.getFormField(CloneParameterDialog.PARAMETERS))
                .getSelectedIndexes();
        if (indexes.length == 0) {
            if (showErrorMessage) {
                UISupport.showErrorMessage("No Parameters selected..");
            }
            return items;
        }

        Project project = securityScan.getTestStep().getTestCase().getTestSuite().getProject();
        TestSuite targetTestSuite = project.getTestSuiteByName(targetTestSuiteName);
        TestCase targetTestCase = targetTestSuite.getTestCaseByName(targetTestCaseName);
        SecurityTest targetSecurityTest = targetTestCase.getSecurityTestByName(targetSecurityTestName);
        TestStep targetTestStep = targetTestCase.getTestStepByName(targetSecurityTestStepName);

        boolean overwrite = dialog.getBooleanValue(CloneParameterDialog.OVERWRITE);

        for (String scanName : targetSecurityScans) {
            AbstractSecurityScanWithProperties targetSecurityScan = (AbstractSecurityScanWithProperties) targetSecurityTest
                    .getTestStepSecurityScanByName(targetTestStep.getId(), scanName);

            for (int i : indexes) {
                SecurityCheckedParameter scanParameter = securityScan.getParameterAt(i);
                String newParameterLabel = scanParameter.getLabel();
                if (securityScan.getParameterByLabel(scanParameter.getLabel()) != null) {
                    if (securityScan.equals(targetSecurityScan)) {
                        newParameterLabel = "Copy of " + scanParameter.getLabel();
                    }
                }
                if (targetSecurityScan.importParameter(scanParameter, overwrite, newParameterLabel)
                        && !items.contains(targetSecurityScan)) {
                    items.add(targetSecurityScan);
                }
            }
        }
        return items;
    }

    private class CancelAction extends AbstractAction {

        private XFormDialog dialog;

        public CancelAction() {
            super("Cancel");
        }

        public void setDialog(XFormDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (dialog != null) {
                ((SwingXFormDialog) dialog).setReturnValue(XFormDialog.CANCEL_OPTION);
                dialog.setVisible(false);
            }
        }
    }

    private class ApplyAction extends AbstractAction {

        private XFormDialog dialog;

        public ApplyAction() {
            super("Apply");
        }

        public void setDialog(XFormDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (dialog != null) {
                List<ModelItem> items = performClone(true);
                UISupport.showInfoMessage("Updated " + items.size() + " scans");

                if (items.size() > 0) {
                    ((XFormMultiSelectList) dialog.getFormField(CloneParameterDialog.TARGET_SECURITYSCAN))
                            .setSelectedOptions(new String[0]);
                    ((XFormMultiSelectList) dialog.getFormField(CloneParameterDialog.PARAMETERS))
                            .setSelectedOptions(new String[0]);
                }
            }
        }
    }

    protected XFormDialog createCloneParameterDialog() {
        actionList = new DefaultActionList();
        OkAction okAction = new OkAction();
        actionList.addAction(okAction, true);
        CancelAction cancelAction = new CancelAction();
        actionList.addAction(cancelAction);
        ApplyAction applyAction = new ApplyAction();
        actionList.addAction(applyAction);

        dialog = ADialogBuilder.buildDialog(CloneParameterDialog.class, actionList, false);

        okAction.setDialog(dialog);
        cancelAction.setDialog(dialog);
        applyAction.setDialog(dialog);

        final TestCase testCase = securityScan.getTestStep().getTestCase();
        final Project project = testCase.getTestSuite().getProject();

        dialog.getFormField(CloneParameterDialog.TARGET_TESTSUITE).addFormFieldListener(new XFormFieldListener() {
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                TestSuite testSuite = project.getTestSuiteByName(newValue);
                String[] testCaseNames = ModelSupport.getNames(testSuite.getTestCaseList());
                dialog.setOptions(CloneParameterDialog.TARGET_TESTCASE, testCaseNames);

                if (testCaseNames.length > 0) {
                    dialog.setValue(CloneParameterDialog.TARGET_TESTCASE, testCaseNames[0]);
                    TestCase testCase = testSuite.getTestCaseByName(testCaseNames[0]);

                    String[] testStepNames = new String[0];
                    String[] securityTestNames = ModelSupport.getNames(testCase.getSecurityTestList());
                    dialog.setOptions(CloneParameterDialog.TARGET_SECURITYTEST, securityTestNames);
                    if (securityTestNames.length > 0) {
                        testStepNames = getSecurableTestStepsNames(testCase);
                    }
                    dialog.setOptions(CloneParameterDialog.TARGET_TESTSTEP, testStepNames);

                    if (securityTestNames.length > 0) {
                        dialog.setValue(CloneParameterDialog.TARGET_SECURITYTEST, securityTestNames[0]);
                        if (testStepNames.length > 0) {
                            dialog.setValue(CloneParameterDialog.TARGET_TESTSTEP, testStepNames[0]);
                        } else {
                            dialog.setOptions(CloneParameterDialog.TARGET_TESTSTEP, new String[0]);
                        }

                        String securityTestName = dialog.getValue(CloneParameterDialog.TARGET_SECURITYTEST);
                        SecurityTest securityTest = testCase.getSecurityTestByName(securityTestName);
                        String testStepName = dialog.getValue(CloneParameterDialog.TARGET_TESTSTEP);
                        TestStep testStep = testCase.getTestStepByName(testStepName);
                        String[] securityScanNames = ModelSupport.getNames(securityTest.getTestStepSecurityScanByType(
                                testStep.getId(), AbstractSecurityScanWithProperties.class));
                        dialog.setOptions(CloneParameterDialog.TARGET_SECURITYSCAN, securityScanNames);
                    } else {
                        dialog.setOptions(CloneParameterDialog.TARGET_SECURITYTEST, new String[0]);
                        dialog.setOptions(CloneParameterDialog.TARGET_TESTSTEP, new String[0]);
                        dialog.setOptions(CloneParameterDialog.TARGET_SECURITYSCAN, new String[0]);
                    }
                } else {
                    dialog.setOptions(CloneParameterDialog.TARGET_SECURITYTEST, new String[0]);
                    dialog.setOptions(CloneParameterDialog.TARGET_TESTSTEP, new String[0]);
                }
            }
        });
        dialog.getFormField(CloneParameterDialog.TARGET_TESTCASE).addFormFieldListener(new XFormFieldListener() {
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                String testSuiteName = dialog.getValue(CloneParameterDialog.TARGET_TESTSUITE);
                TestSuite testSuite = project.getTestSuiteByName(testSuiteName);
                TestCase testCase = testSuite.getTestCaseByName(newValue);

                String[] testStepNames = new String[0];
                String[] securityTestNames = ModelSupport.getNames(testCase.getSecurityTestList());
                dialog.setOptions(CloneParameterDialog.TARGET_SECURITYTEST, securityTestNames);
                if (securityTestNames.length > 0) {
                    testStepNames = getSecurableTestStepsNames(testCase);
                }
                dialog.setOptions(CloneParameterDialog.TARGET_TESTSTEP, testStepNames);

                if (securityTestNames.length > 0) {
                    dialog.setValue(CloneParameterDialog.TARGET_SECURITYTEST, securityTestNames[0]);
                    if (testStepNames.length > 0) {
                        dialog.setValue(CloneParameterDialog.TARGET_TESTSTEP, testStepNames[0]);
                    } else {
                        dialog.setOptions(CloneParameterDialog.TARGET_TESTSTEP, new String[0]);
                    }

                    String securityTestName = dialog.getValue(CloneParameterDialog.TARGET_SECURITYTEST);
                    SecurityTest securityTest = testCase.getSecurityTestByName(securityTestName);
                    String testStepName = dialog.getValue(CloneParameterDialog.TARGET_TESTSTEP);
                    TestStep testStep = testCase.getTestStepByName(testStepName);
                    String[] securityScanNames = ModelSupport.getNames(securityTest.getTestStepSecurityScanByType(
                            testStep.getId(), AbstractSecurityScanWithProperties.class));
                    dialog.setOptions(CloneParameterDialog.TARGET_SECURITYSCAN, securityScanNames);
                } else {
                    dialog.setOptions(CloneParameterDialog.TARGET_SECURITYTEST, new String[0]);
                    dialog.setOptions(CloneParameterDialog.TARGET_TESTSTEP, new String[0]);
                    dialog.setOptions(CloneParameterDialog.TARGET_SECURITYSCAN, new String[0]);
                }
            }
        });
        dialog.getFormField(CloneParameterDialog.TARGET_TESTSTEP).addFormFieldListener(new XFormFieldListener() {
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                String testSuiteName = dialog.getValue(CloneParameterDialog.TARGET_TESTSUITE);
                TestSuite testSuite = project.getTestSuiteByName(testSuiteName);
                String testCaseName = dialog.getValue(CloneParameterDialog.TARGET_TESTCASE);
                TestCase testCase = testSuite.getTestCaseByName(testCaseName);
                String securityTestName = dialog.getValue(CloneParameterDialog.TARGET_SECURITYTEST);
                SecurityTest securityTest = testCase.getSecurityTestByName(securityTestName);
                TestStep testStep = testCase.getTestStepByName(newValue);

                String[] securityScanNames = ModelSupport.getNames(securityTest.getTestStepSecurityScanByType(
                        testStep.getId(), AbstractSecurityScanWithProperties.class));
                dialog.setOptions(CloneParameterDialog.TARGET_SECURITYSCAN, securityScanNames);
            }
        });
        dialog.getFormField(CloneParameterDialog.TARGET_SECURITYTEST).addFormFieldListener(new XFormFieldListener() {
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                String testSuiteName = dialog.getValue(CloneParameterDialog.TARGET_TESTSUITE);
                TestSuite testSuite = project.getTestSuiteByName(testSuiteName);
                String testCaseName = dialog.getValue(CloneParameterDialog.TARGET_TESTCASE);
                TestCase testCase = testSuite.getTestCaseByName(testCaseName);
                SecurityTest securityTest = testCase.getSecurityTestByName(newValue);
                String testStepName = dialog.getValue(CloneParameterDialog.TARGET_TESTSTEP);
                TestStep testStep = testCase.getTestStepByName(testStepName);

                String[] securityScanNames = ModelSupport.getNames(securityTest.getTestStepSecurityScanByType(
                        testStep.getId(), AbstractSecurityScanWithProperties.class));
                dialog.setOptions(CloneParameterDialog.TARGET_SECURITYSCAN, securityScanNames);
            }
        });

        WsdlTestCase wsdlTestCase = (WsdlTestCase) securityScan.getTestStep().getTestCase();

        dialog.setOptions(CloneParameterDialog.TARGET_TESTSUITE,
                ModelSupport.getNames(wsdlTestCase.getTestSuite().getProject().getTestSuiteList()));
        dialog.setValue(CloneParameterDialog.TARGET_TESTSUITE, wsdlTestCase.getTestSuite().getName());

        List<TestCase> wsdlTestCaseList = wsdlTestCase.getTestSuite().getTestCaseList();
        dialog.setOptions(CloneParameterDialog.TARGET_TESTCASE, ModelSupport.getNames(wsdlTestCaseList));
        dialog.setValue(CloneParameterDialog.TARGET_TESTCASE, wsdlTestCase.getName());

        dialog.setOptions(CloneParameterDialog.TARGET_TESTSTEP, getSecurableTestStepsNames(wsdlTestCase));
        dialog.setOptions(CloneParameterDialog.TARGET_SECURITYTEST,
                ModelSupport.getNames(wsdlTestCase.getSecurityTestList()));

        String securityTestName = dialog.getValue(CloneParameterDialog.TARGET_SECURITYTEST);
        SecurityTest securityTest = wsdlTestCase.getSecurityTestByName(securityTestName);
        String testStepName = dialog.getValue(CloneParameterDialog.TARGET_TESTSTEP);
        TestStep testStep = wsdlTestCase.getTestStepByName(testStepName);

        String[] securityScanNames = ModelSupport.getNames(securityTest.getTestStepSecurityScanByType(testStep.getId(),
                AbstractSecurityScanWithProperties.class));
        dialog.setOptions(CloneParameterDialog.TARGET_SECURITYSCAN, securityScanNames);

        dialog.setOptions(CloneParameterDialog.PARAMETERS, securityScan.getParameterHolder().getParameterLabels());

        ((JFormDialog) dialog).getDialog().setResizable(false);

        return dialog;
    }

    @AForm(description = "Specify target TestSuite/TestCase/Security Test(s)/Security Scan(s) and select Parameters to clone", name = "Clone Parameters", icon = UISupport.TOOL_ICON_PATH, helpUrl = HelpUrls.SECURITY_SCANS_OVERVIEW)
    private interface CloneParameterDialog {
        @AField(name = "Parameters", description = "The Parameters to clone", type = AFieldType.MULTILIST)
        public final static String PARAMETERS = "Parameters";

        @AField(name = "Target TestSuite", description = "The target TestSuite for the cloned Parameter(s)", type = AFieldType.ENUMERATION)
        public final static String TARGET_TESTSUITE = "Target TestSuite";

        @AField(name = "Target TestCase", description = "The target TestCase for the cloned Parameter(s)", type = AFieldType.ENUMERATION)
        public final static String TARGET_TESTCASE = "Target TestCase";

        @AField(name = "Target SecurityTest", description = "The target SecurityTest for the cloned Parameter(s)", type = AFieldType.ENUMERATION)
        public final static String TARGET_SECURITYTEST = "Target SecurityTest";

        @AField(name = "Target TestStep", description = "The target TestStep for the cloned Parameter(s)", type = AFieldType.ENUMERATION)
        public final static String TARGET_TESTSTEP = "Target TestStep";

        @AField(name = "Target SecurityScans", description = "The SecurityScans to clone to", type = AFieldType.MULTILIST)
        public final static String TARGET_SECURITYSCAN = "Target SecurityScans";

        @AField(name = "Overwrite", description = "Overwrite existing parameters", type = AFieldType.BOOLEAN)
        public final static String OVERWRITE = "Overwrite";
    }

}
