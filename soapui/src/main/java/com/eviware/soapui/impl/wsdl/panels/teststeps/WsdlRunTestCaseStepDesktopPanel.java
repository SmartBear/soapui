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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.config.RunTestCaseRunModeTypeConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.panels.testcase.JTestRunLog;
import com.eviware.soapui.impl.wsdl.panels.testcase.TestRunLogTestRunListener;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormMultiSelectList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class WsdlRunTestCaseStepDesktopPanel extends ModelItemDesktopPanel<WsdlRunTestCaseTestStep> implements
        PropertyChangeListener {
    private WsdlProject project;
    private TitledBorder titledBorder;
    private OptionsAction optionsAction;
    private RunAction runAction;
    private OpenTestCaseAction openTestCaseAction;
    private JTestRunLog testRunLog;
    private CancelRunTestCaseAction cancelAction;
    private XFormDialog optionsDialog;
    private JInspectorPanel inspectorPanel;
    private PropertyHolderTable propertiesTable;

    public WsdlRunTestCaseStepDesktopPanel(WsdlRunTestCaseTestStep modelItem) {
        super(modelItem);

        project = getModelItem().getTestCase().getTestSuite().getProject();

        getModelItem().addPropertyChangeListener(WsdlRunTestCaseTestStep.TARGET_TESTCASE, this);
        WsdlTestCase targetTestCase = getModelItem().getTargetTestCase();
        if (targetTestCase != null) {
            targetTestCase.addPropertyChangeListener(WsdlTestCase.NAME_PROPERTY, this);
            targetTestCase.getTestSuite().addPropertyChangeListener(WsdlTestCase.NAME_PROPERTY, this);
        }

        buildUI();
        setEnabledState();

        if (modelItem.getTargetTestCase() == null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    optionsAction.actionPerformed(null);
                }
            });
        }

        setPreferredSize(new Dimension(400, 600));
    }

    private void setEnabledState() {
        runAction.setEnabled(getModelItem().getTargetTestCase() != null);
        openTestCaseAction.setEnabled(getModelItem().getTargetTestCase() != null);
    }

    private void buildUI() {
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private Component buildContent() {
        inspectorPanel = JInspectorPanelFactory.build(createPropertiesTable());

        inspectorPanel.addInspector(new JComponentInspector<JComponent>(buildLog(), "TestCase Log",
                "log output from testcase run", true));

        return inspectorPanel.getComponent();
    }

    private JComponent buildLog() {
        testRunLog = new JTestRunLog(getModelItem().getSettings());
        return testRunLog;
    }

    protected PropertyHolderTable createPropertiesTable() {
        propertiesTable = new PropertyHolderTable(getModelItem());

        titledBorder = BorderFactory.createTitledBorder(createTitleForBorder());
        propertiesTable.setBorder(titledBorder);

        return propertiesTable;
    }

    private String createTitleForBorder() {
        WsdlTestCase targetTestCase = getModelItem().getTargetTestCase();
        return "TestCase ["
                + (targetTestCase == null ? "- none selected -" : targetTestCase.getTestSuite().getName() + ":"
                + targetTestCase.getName()) + "] Run Properties";
    }

    private Component buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        toolbar.add(UISupport.createToolbarButton(runAction = new RunAction()));
        toolbar.add(UISupport.createToolbarButton(cancelAction = new CancelRunTestCaseAction(), false));
        toolbar.add(UISupport.createToolbarButton(optionsAction = new OptionsAction()));
        toolbar.add(UISupport.createToolbarButton(openTestCaseAction = new OpenTestCaseAction()));

        toolbar.addGlue();
        toolbar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.RUNTESTCASESTEP_HELP_URL)));

        return toolbar;
    }

    @Override
    public boolean dependsOn(ModelItem modelItem) {
        WsdlRunTestCaseTestStep callStep = getModelItem();

        return modelItem == callStep || modelItem == callStep.getTestCase()
                || modelItem == callStep.getTestCase().getTestSuite()
                || modelItem == callStep.getTestCase().getTestSuite().getProject();
    }

    public boolean onClose(boolean canCancel) {
        getModelItem().removePropertyChangeListener(WsdlRunTestCaseTestStep.TARGET_TESTCASE, this);

        WsdlTestCase targetTestCase = getModelItem().getTargetTestCase();
        if (targetTestCase != null) {
            targetTestCase.removePropertyChangeListener(WsdlTestCase.NAME_PROPERTY, this);
            targetTestCase.getTestSuite().removePropertyChangeListener(WsdlTestCase.NAME_PROPERTY, this);
        }

        testRunLog.release();
        if (optionsDialog != null) {
            optionsDialog.release();
            optionsDialog = null;
        }

        propertiesTable.release();
        inspectorPanel.release();

        return release();
    }

    private class RunAction extends AbstractAction {
        public RunAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
            putValue(Action.SHORT_DESCRIPTION, "Runs the selected TestCases");
        }

        public void actionPerformed(ActionEvent e) {
            Analytics.trackAction(SoapUIActions.RUN_TEST_STEP.getActionName(), "StepType", "RunTestCase");

            runAction.setEnabled(false);
            cancelAction.setEnabled(true);

            new Thread(new Runnable() {

                public void run() {
                    WsdlRunTestCaseTestStep testStep = getModelItem();
                    InternalTestRunListener testRunListener = new InternalTestRunListener();
                    testStep.addTestRunListener(testRunListener);

                    try {
                        testRunLog.clear();
                        MockTestRunner mockTestRunner = new MockTestRunner(testStep.getTestCase(), SoapUI.ensureGroovyLog());
                        WsdlTestStepResult result = (WsdlTestStepResult) testStep.run(mockTestRunner,
                                new MockTestRunContext(mockTestRunner, testStep));

                        Throwable er = result.getError();
                        if (er != null) {
                            UISupport.showErrorMessage(er.toString());
                        }
                    } catch (Throwable t) {
                        UISupport.showErrorMessage(t);
                    } finally {
                        testStep.removeTestRunListener(testRunListener);
                        runAction.setEnabled(true);
                        cancelAction.setEnabled(false);
                    }
                }
            }).start();
        }
    }

    private class OpenTestCaseAction extends AbstractAction {
        public OpenTestCaseAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/testcase.png"));
            putValue(Action.SHORT_DESCRIPTION, "Opens the target TestCases editor");
        }

        public void actionPerformed(ActionEvent e) {
            WsdlTestCase targetTestCase = getModelItem().getTargetTestCase();
            if (targetTestCase == null) {
                UISupport.showErrorMessage("No target TestCase selected");
            } else {
                UISupport.showDesktopPanel(targetTestCase);
            }
        }
    }

    private class OptionsAction extends AbstractAction {
        public OptionsAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/preferences.png"));
            putValue(Action.SHORT_DESCRIPTION, "Sets Options");
        }

        public void actionPerformed(ActionEvent e) {
            if (optionsDialog == null) {
                optionsDialog = ADialogBuilder.buildDialog(OptionsForm.class);
                optionsDialog.getFormField(OptionsForm.TESTSUITE).addFormFieldListener(new XFormFieldListener() {

                    public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                        List<TestCase> testCaseList = project.getTestSuiteByName(newValue).getTestCaseList();
                        testCaseList.remove(getModelItem().getTestCase());
                        optionsDialog.setOptions(OptionsForm.TESTCASE, ModelSupport.getNames(testCaseList));

                        if (testCaseList.size() > 0) {
                            WsdlTestCase testCase = project.getTestSuiteByName(newValue).getTestCaseAt(0);
                            optionsDialog.setOptions(OptionsForm.RETURN_PROPERTIES, testCase.getPropertyNames());
                            ((XFormMultiSelectList) optionsDialog.getFormField(OptionsForm.RETURN_PROPERTIES))
                                    .setSelectedOptions(getModelItem().getReturnProperties().toStringArray());
                        }
                    }
                });
                optionsDialog.getFormField(OptionsForm.TESTCASE).addFormFieldListener(new XFormFieldListener() {

                    public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                        WsdlTestSuite testSuite = project.getTestSuiteByName(optionsDialog.getValue(OptionsForm.TESTSUITE));
                        WsdlTestCase testCase = testSuite.getTestCaseByName(newValue);
                        optionsDialog.setOptions(OptionsForm.RETURN_PROPERTIES, testCase.getPropertyNames());
                        ((XFormMultiSelectList) optionsDialog.getFormField(OptionsForm.RETURN_PROPERTIES))
                                .setSelectedOptions(getModelItem().getReturnProperties().toStringArray());
                    }
                });
            }

            WsdlTestCase targetTestCase = getModelItem().getTargetTestCase();

            optionsDialog.setOptions(OptionsForm.TESTSUITE, ModelSupport.getNames(project.getTestSuiteList()));
            if (targetTestCase != null) {
                optionsDialog.setValue(OptionsForm.TESTSUITE, targetTestCase.getTestSuite().getName());

                List<TestCase> testCaseList = targetTestCase.getTestSuite().getTestCaseList();
                testCaseList.remove(getModelItem().getTestCase());

                optionsDialog.setOptions(OptionsForm.TESTCASE, ModelSupport.getNames(testCaseList));
                optionsDialog.setValue(OptionsForm.TESTCASE, targetTestCase.getName());

                optionsDialog.setOptions(OptionsForm.RETURN_PROPERTIES, targetTestCase.getPropertyNames());
                ((XFormMultiSelectList) optionsDialog.getFormField(OptionsForm.RETURN_PROPERTIES))
                        .setSelectedOptions(getModelItem().getReturnProperties().toStringArray());
            } else {
                if (project.getTestSuiteCount() == 0) {
                    optionsDialog.setOptions(OptionsForm.TESTCASE, new String[0]);
                    optionsDialog.setOptions(OptionsForm.RETURN_PROPERTIES, new String[0]);
                } else {
                    List<TestCase> testCaseList = project.getTestSuiteAt(0).getTestCaseList();
                    testCaseList.remove(getModelItem().getTestCase());
                    optionsDialog.setOptions(OptionsForm.TESTCASE, ModelSupport.getNames(testCaseList));

                    if (testCaseList.isEmpty()) {
                        optionsDialog.setOptions(OptionsForm.RETURN_PROPERTIES, new String[0]);
                    } else {
                        optionsDialog.setOptions(OptionsForm.RETURN_PROPERTIES, testCaseList.get(0).getPropertyNames());
                    }
                }
            }

            switch (getModelItem().getRunMode().intValue()) {
                case RunTestCaseRunModeTypeConfig.INT_PARALLELL:
                    optionsDialog.setValue(OptionsForm.RUN_MODE, OptionsForm.CREATE_ISOLATED_COPY_FOR_EACH_RUN);
                    break;
                case RunTestCaseRunModeTypeConfig.INT_SINGLETON_AND_FAIL:
                    optionsDialog.setValue(OptionsForm.RUN_MODE, OptionsForm.RUN_PRIMARY_TEST_CASE);
                    break;
                case RunTestCaseRunModeTypeConfig.INT_SINGLETON_AND_WAIT:
                    optionsDialog.setValue(OptionsForm.RUN_MODE, OptionsForm.RUN_SYNCHRONIZED_TESTCASE);
                    break;
            }

            optionsDialog.setBooleanValue(OptionsForm.COPY_HTTP_SESSION, getModelItem().isCopyHttpSession());
            optionsDialog
                    .setBooleanValue(OptionsForm.COPY_LOADTEST_PROPERTIES, getModelItem().isCopyLoadTestProperties());
            optionsDialog.setBooleanValue(OptionsForm.IGNORE_EMPTY_PROPERTIES, getModelItem().isIgnoreEmptyProperties());

            if (optionsDialog.show()) {
                WsdlTestSuite testSuite = project.getTestSuiteByName(optionsDialog.getValue(OptionsForm.TESTSUITE));
                getModelItem().setTargetTestCase(
                        testSuite == null ? null
                                : testSuite.getTestCaseByName(optionsDialog.getValue(OptionsForm.TESTCASE)));
                getModelItem().setReturnProperties(
                        new StringList(
                                ((XFormMultiSelectList) optionsDialog.getFormField(OptionsForm.RETURN_PROPERTIES))
                                        .getSelectedOptions()));

                switch (optionsDialog.getValueIndex(OptionsForm.RUN_MODE)) {
                    case 0:
                        getModelItem().setRunMode(RunTestCaseRunModeTypeConfig.PARALLELL);
                        break;
                    case 1:
                        getModelItem().setRunMode(RunTestCaseRunModeTypeConfig.SINGLETON_AND_FAIL);
                        break;
                    case 2:
                        getModelItem().setRunMode(RunTestCaseRunModeTypeConfig.SINGLETON_AND_WAIT);
                        break;
                }

                getModelItem().setCopyHttpSession(optionsDialog.getBooleanValue(OptionsForm.COPY_HTTP_SESSION));
                getModelItem().setCopyLoadTestProperties(
                        optionsDialog.getBooleanValue(OptionsForm.COPY_LOADTEST_PROPERTIES));
                getModelItem().setIgnoreEmptyProperties(
                        optionsDialog.getBooleanValue(OptionsForm.IGNORE_EMPTY_PROPERTIES));

                titledBorder.setTitle(createTitleForBorder());
            }
        }
    }

    @AForm(name = "Run TestCase Options", description = "Set options for the Run TestCase Step below", helpUrl = HelpUrls.RUNTESTCASESTEP_HELP_URL)
    private static interface OptionsForm {
        public static final String RUN_PRIMARY_TEST_CASE = "Run primary TestCase (fail if already running)";
        public static final String CREATE_ISOLATED_COPY_FOR_EACH_RUN = "Create isolated copy for each run (Thread-Safe)";
        public static final String RUN_SYNCHRONIZED_TESTCASE = "Run primary TestCase (wait for running to finish, Thread-Safe)";

        @AField(name = "Target TestCase", description = "Selects the TestCase to run", type = AFieldType.ENUMERATION)
        public static final String TESTCASE = "Target TestCase";

        @AField(name = "Target TestSuite", description = "Selects the containing TestSuite to run", type = AFieldType.ENUMERATION)
        public static final String TESTSUITE = "Target TestSuite";

        @AField(name = "Return Properties", description = "Selects the properties that are return values", type = AFieldType.MULTILIST)
        public static final String RETURN_PROPERTIES = "Return Properties";

        @AField(name = "Run Mode", description = "Sets how to run the target TestCase", type = AFieldType.RADIOGROUP, values = {
                CREATE_ISOLATED_COPY_FOR_EACH_RUN, RUN_PRIMARY_TEST_CASE, RUN_SYNCHRONIZED_TESTCASE})
        public static final String RUN_MODE = "Run Mode";

        @AField(name = "Copy LoadTest Properties", description = "Copies LoadTest related properties to target context", type = AFieldType.BOOLEAN)
        public static final String COPY_LOADTEST_PROPERTIES = "Copy LoadTest Properties";

        @AField(name = "Copy HTTP Session", description = "Copies HTTP Session to and from the target TestCase", type = AFieldType.BOOLEAN)
        public static final String COPY_HTTP_SESSION = "Copy HTTP Session";

        @AField(name = "Ignore Empty Properties", description = "Does not set empty TestCase property values", type = AFieldType.BOOLEAN)
        public static final String IGNORE_EMPTY_PROPERTIES = "Ignore Empty Properties";
    }

    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);

        if (evt.getPropertyName().equals(WsdlRunTestCaseTestStep.TARGET_TESTCASE)) {
            WsdlTestCase targetTestCase = (WsdlTestCase) evt.getOldValue();
            if (targetTestCase != null) {
                targetTestCase.removePropertyChangeListener(WsdlTestCase.NAME_PROPERTY, this);
                targetTestCase.getTestSuite().removePropertyChangeListener(WsdlTestCase.NAME_PROPERTY, this);
            }

            targetTestCase = (WsdlTestCase) evt.getNewValue();
            if (targetTestCase != null) {
                targetTestCase.addPropertyChangeListener(WsdlTestCase.NAME_PROPERTY, this);
                targetTestCase.getTestSuite().addPropertyChangeListener(WsdlTestCase.NAME_PROPERTY, this);
            }
        }

        setEnabledState();
        titledBorder.setTitle(createTitleForBorder());
        repaint();
    }

    public class InternalTestRunListener extends TestRunLogTestRunListener {
        public InternalTestRunListener() {
            super(testRunLog, true);
        }

        public void beforeRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
            runAction.setEnabled(false);
            cancelAction.setEnabled(true);
        }

        public void afterRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
            runAction.setEnabled(true);
            cancelAction.setEnabled(false);
        }
    }

    public class CancelRunTestCaseAction extends AbstractAction {
        public CancelRunTestCaseAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/stop.png"));
            putValue(Action.SHORT_DESCRIPTION, "Stops running this testcase");
        }

        public void actionPerformed(ActionEvent e) {
            WsdlTestCaseRunner testCaseRunner = getModelItem().getTestCaseRunner();
            if (testCaseRunner != null) {
                testCaseRunner.cancel("Canceled from RunTestCase UI");
            }
        }
    }
}
