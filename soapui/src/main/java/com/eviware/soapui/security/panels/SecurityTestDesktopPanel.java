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

package com.eviware.soapui.security.panels;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.support.MockSecurityTestRunner;
import com.eviware.soapui.impl.wsdl.panels.testcase.actions.SetCredentialsAction;
import com.eviware.soapui.impl.wsdl.panels.testcase.actions.SetEndpointAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.actions.SecurityTestOptionsAction;
import com.eviware.soapui.security.log.JFunctionalTestRunLog;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.result.SecurityResult.ResultStatus;
import com.eviware.soapui.security.result.SecurityScanRequestResult;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.support.ProgressBarSecurityTestAdapter;
import com.eviware.soapui.security.support.SecurityTestRunListenerAdapter;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DateUtil;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.GroovyEditorComponent;
import com.eviware.soapui.support.components.GroovyEditorInspector;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JFocusableComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.dnd.JListDragAndDropable;
import com.eviware.soapui.support.swing.ComponentBag;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.soapui.ui.support.KeySensitiveModelItemDesktopPanel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;

/**
 * SecurityTest desktop panel
 * <p/>
 * <p/>
 * this is just first rough version created by copy-pasting from
 * WsdlTestCaseDesktoppanel therefore a lot of variables have not been renamed
 * yet, and a lot of code my be unused, or missing
 *
 * @author dragica.soldo
 */

@SuppressWarnings("serial")
public class SecurityTestDesktopPanel extends KeySensitiveModelItemDesktopPanel<SecurityTest> {
    private JSecurityTestTestStepList testStepList;
    private JProgressBar progressBar;
    private JButton runButton;
    private JButton cancelButton;
    private SecurityTestRunner runner;
    private JButton setEndpointButton;
    private JButton setCredentialsButton;
    private JButton optionsButton;
    private JSecurityTestRunLog securityTestLog;
    private JFunctionalTestRunLog functionalTestLog;
    // private JToggleButton loopButton;
    private ProgressBarSecurityTestAdapter progressBarAdapter;
    private ComponentBag stateDependantComponents = new ComponentBag();
    private boolean canceled;
    private JTextArea descriptionArea;
    private PropertyHolderTable propertiesTable;
    private GroovyEditorComponent tearDownGroovyEditor;
    private GroovyEditorComponent setupGroovyEditor;
    private JInspectorPanel testStepListInspectorPanel;
    private JInspectorPanel inspectorPanel;
    private SecurityTestRunner lastRunner;
    private SecurityTest securityTest;
    private JXToolBar toolbar;
    private InternalSecurityTestRunListener securityTestRunListener = new InternalSecurityTestRunListener();
    private JLabel cntLabel;
    private JComponentInspector<?> securityLogInspector;
    private JComponentInspector<?> functionalLogInspector;
    private ResultStatus securityStatus;
    private ResultStatus functionalStatus;
    private boolean startStepLogEntryAdded;

    public SecurityTestDesktopPanel(SecurityTest securityTest) {
        super(securityTest);

        buildUI();

        setPreferredSize(new Dimension(400, 550));
        this.securityTest = securityTest;
        securityTest.addSecurityTestRunListener(securityTestRunListener);
        progressBarAdapter = new ProgressBarSecurityTestAdapter(progressBar, securityTest, cntLabel);
    }

    protected JSecurityTestTestStepList getTestStepList() {
        return testStepList;
    }

    protected JSecurityTestRunLog getSecurityTestLog() {
        return securityTestLog;
    }

    protected void setTestStepList(JSecurityTestTestStepList testStepList) {
        this.testStepList = testStepList;
    }

    private void buildUI() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(buildToolbar(), BorderLayout.PAGE_START);
        panel.add(buildRunnerBar(), BorderLayout.CENTER);

        add(panel, BorderLayout.NORTH);

        JComponent securityLog = buildSecurityLog();
        inspectorPanel = JInspectorPanelFactory.build(buildContent());
        securityLogInspector = new JComponentInspector<JComponent>(securityLog, "Security Log",
                "Security Execution Log", true);
        inspectorPanel.addInspector(securityLogInspector);
        JComponent functionalLog = buildFunctionalLog();
        functionalLogInspector = new JComponentInspector<JComponent>(functionalLog, "TestCase Log",
                "Functional Execution Log", true);
        inspectorPanel.addInspector(functionalLogInspector);
        inspectorPanel.setDefaultDividerLocation(0.7F);
        inspectorPanel.setCurrentInspector("Security Log");

        if (StringUtils.hasContent(getModelItem().getDescription())
                && getModelItem().getSettings().getBoolean(UISettings.SHOW_DESCRIPTIONS)) {
            testStepListInspectorPanel.setCurrentInspector("Description");
        }
        initializeStatusIcons();

        add(inspectorPanel.getComponent(), BorderLayout.CENTER);
    }

    private void initializeStatusIcons() {
        securityStatus = ResultStatus.UNKNOWN;
        updateStatusIcon(securityStatus, securityLogInspector);

        functionalStatus = ResultStatus.UNKNOWN;
        updateStatusIcon(functionalStatus, functionalLogInspector);
    }

    private void updateStatusIcon(ResultStatus status, JComponentInspector<?> logInspector) {
        switch (status) {
            case FAILED: {
                logInspector.setIcon(UISupport.createImageIcon("/failed_securitytest.gif"));
                // inspectorPanel.activate( logInspector );
                break;
            }
            case UNKNOWN: {
                logInspector.setIcon(UISupport.createImageIcon("/unknown_securitytest.gif"));
                break;
            }
            case OK: {
                logInspector.setIcon(UISupport.createImageIcon("/valid_securitytest.gif"));
                // inspectorPanel.deactivate();
                break;
            }
        }
    }

    private Component buildRunnerBar() {
        progressBar = new JProgressBar(0, getModelItem().getSecurityScanCount());

        JPanel progressPanel = new JPanel(new BorderLayout(10, 0));

        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setIndeterminate(false);

        progressBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));

        progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        progressPanel.add(progressBar);

        progressBar.setMinimumSize(new Dimension(0, 200));
        progressBar.setBackground(Color.WHITE);

        cntLabel = new JLabel("");
        cntLabel.setForeground(Color.white);
        cntLabel.setPreferredSize(new Dimension(50, 18));
        cntLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        cntLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cntLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        progressPanel.add(cntLabel, BorderLayout.EAST);

        return progressPanel;
    }

    private JComponent buildSecurityLog() {
        securityTestLog = new JSecurityTestRunLog(getModelItem());
        stateDependantComponents.add(securityTestLog);
        return securityTestLog;
    }

    private JComponent buildFunctionalLog() {
        functionalTestLog = new JFunctionalTestRunLog(getModelItem());
        stateDependantComponents.add(functionalTestLog);
        return functionalTestLog;
    }

    private JComponent buildContent() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        testStepListInspectorPanel = JInspectorPanelFactory.build(buildTestStepList(), SwingConstants.BOTTOM);

        tabs.addTab("TestSteps", testStepListInspectorPanel.getComponent());

        addTabs(tabs, testStepListInspectorPanel);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return UISupport.createTabPanel(tabs, true);
    }

    protected JComponent buildTestStepList() {
        testStepList = new JSecurityTestTestStepList(getModelItem(), securityTestLog);
        return testStepList;
    }

    protected void addTabs(JTabbedPane tabs, JInspectorPanel inspectorPanel) {
        inspectorPanel.addInspector(new JFocusableComponentInspector<JPanel>(buildDescriptionPanel(), descriptionArea,
                "Description", "SecurityTest Description", true));
        inspectorPanel.addInspector(new JComponentInspector<JComponent>(buildPropertiesPanel(), "Properties",
                "SecurityTest level properties", true));
        inspectorPanel.addInspector(new GroovyEditorInspector(buildSetupScriptPanel(), "Setup Script",
                "Script to run before tunning a SecurityTest"));
        inspectorPanel.addInspector(new GroovyEditorInspector(buildTearDownScriptPanel(), "TearDown Script",
                "Script to run after a SecurityTest Run"));
    }

    protected GroovyEditorComponent buildTearDownScriptPanel() {
        tearDownGroovyEditor = new GroovyEditorComponent(new TearDownScriptGroovyEditorModel(), null);
        return tearDownGroovyEditor;
    }

    protected GroovyEditorComponent buildSetupScriptPanel() {
        setupGroovyEditor = new GroovyEditorComponent(new SetupScriptGroovyEditorModel(), null);
        return setupGroovyEditor;
    }

    protected JComponent buildPropertiesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        propertiesTable = buildPropertiesTable();
        panel.add(propertiesTable, BorderLayout.CENTER);
        return panel;
    }

    protected PropertyHolderTable buildPropertiesTable() {
        return new PropertyHolderTable(getModelItem());
    }

    private JPanel buildDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        descriptionArea = new JUndoableTextArea(getModelItem().getDescription());
        descriptionArea.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            public void update(Document document) {
                getModelItem().setDescription(descriptionArea.getText());
            }
        });

        panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        panel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        UISupport.addTitledBorder(panel, "SecurityTest Description");

        return panel;
    }

    protected Component buildToolbar() {
        toolbar = UISupport.createToolbar();

        runButton = UISupport.createToolbarButton(new RunSecurityTestAction());
        optionsButton = UISupport.createToolbarButton(SwingActionDelegate.createDelegate(
                SecurityTestOptionsAction.SOAPUI_ACTION_ID, getModelItem(), null, "/preferences.png"));
        optionsButton.setText(null);
        cancelButton = UISupport.createToolbarButton(new CancelRunSecuritytestAction(), false);

        // loopButton = new JToggleButton( UISupport.createImageIcon( "/loop.gif"
        // ) );
        // loopButton.setPreferredSize( UISupport.getPreferredButtonSize() );
        // loopButton.setToolTipText( "Loop TestCase continuously" );

        setCredentialsButton = UISupport.createToolbarButton(new SetCredentialsAction(getModelItem().getTestCase()));
        setEndpointButton = UISupport.createToolbarButton(new SetEndpointAction(getModelItem().getTestCase()));

        stateDependantComponents.add(runButton);
        stateDependantComponents.add(optionsButton);
        stateDependantComponents.add(cancelButton);
        stateDependantComponents.add(setCredentialsButton);
        stateDependantComponents.add(setEndpointButton);

        addToolbarActions(toolbar);

        toolbar.addSeparator();
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.SECURITYTESTEDITOR_HELP_URL)));

        return toolbar;
    }

    protected JButton getSetEndpointButton() {
        return setEndpointButton;
    }

    public JButton getOptionsButton() {
        return optionsButton;
    }

    public void setOptionsButton(JButton optionsButton) {
        this.optionsButton = optionsButton;
    }

    protected void addToolbarActions(JToolBar toolbar) {
        toolbar.add(runButton);
        toolbar.add(cancelButton);
        // toolbar.add( loopButton );
        toolbar.addSeparator();
        toolbar.add(setCredentialsButton);
        toolbar.add(setEndpointButton);
        toolbar.addSeparator();
        toolbar.add(optionsButton);

    }

    protected void runSecurityTest() {
        initializeStatusIcons();
        if (canceled) {

            // make sure state is correct
            runButton.setEnabled(true);
            cancelButton.setEnabled(false);
            return;
        }

        runButton.setEnabled(false);
        cancelButton.setEnabled(true);
        StringToObjectMap properties = new StringToObjectMap();
        // properties.put( "loopButton", loopButton );
        properties.put(TestCaseRunContext.INTERACTIVE, Boolean.TRUE);
        lastRunner = null;

        runner = getModelItem().run(properties, true);
    }

    public class RunSecurityTestAction extends AbstractAction {
        public RunSecurityTestAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
            putValue(Action.SHORT_DESCRIPTION, "Runs this securitytest");
        }

        public void actionPerformed(ActionEvent e) {
            canceled = false;
            // shouldRun is indicator is there any security scan that can be run
            // meaning security scan have at least one scan and it is not disabled.

            boolean shouldRun = false;
            for (List<SecurityScan> scanList : securityTest.getSecurityScansMap().values()) {
                for (SecurityScan scan : scanList) {
                    if (!scan.isDisabled()) {
                        shouldRun = true;
                    }
                }
            }
            if (shouldRun) {
                Analytics.trackAction(SoapUIActions.RUN_SECURITY_TEST.getActionName());
                runSecurityTest();
            } else {
                UISupport.showInfoMessage("No Security Scans available to run.", "Security Test Warning");
            }
        }
    }

    public class CancelRunSecuritytestAction extends AbstractAction {
        public CancelRunSecuritytestAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/stop.png"));
            putValue(Action.SHORT_DESCRIPTION, "Stops running this securitytest");
        }

        public void actionPerformed(ActionEvent e) {
            if (runner != null) {
                runner.cancel("canceled in UI");
            }

            canceled = true;
        }
    }

    @Override
    protected boolean release() {
        testStepList.release();
        setupGroovyEditor.release();
        tearDownGroovyEditor.release();
        functionalTestLog.release();
        return super.release();
    }

    public boolean onClose(boolean canCancel) {
        if (canCancel) {
            if (runner != null && runner.getStatus() == TestCaseRunner.Status.RUNNING) {
                Boolean retval = UISupport.confirmOrCancel("Cancel running SecurityTest?", "Cancel Run");

                if (retval == null) {
                    return false;
                }
                if (retval.booleanValue()) {
                    if (runner != null) {
                        runner.cancel(null);
                    }
                }
            }
        } else {
            if (runner != null && runner.getStatus() == TestCaseRunner.Status.RUNNING) {
                if (runner != null) {
                    runner.cancel(null);
                }
            }
        }

        getModelItem().removeSecurityTestRunListener(securityTestRunListener);
        progressBarAdapter.release();
        propertiesTable.release();
        inspectorPanel.release();

        setupGroovyEditor.getEditor().release();
        tearDownGroovyEditor.getEditor().release();

        securityTestLog.release();
        lastRunner = null;

        return release();
    }

    protected void beforeRun() {
    }

    protected void afterRun() {
        runButton.setEnabled(true);
        cancelButton.setEnabled(false);
        testStepList.setEnabled(true);
    }

    private class SetupScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        @Override
        public Action createRunAction() {
            return new AbstractAction() {

                public void actionPerformed(ActionEvent e) {

                    MockSecurityTestRunner securityTestRunner = new MockSecurityTestRunner(SecurityTestDesktopPanel.this
                            .getModelItem());
                    try {
                        SecurityTestDesktopPanel.this.getModelItem().runStartupScript(
                                (SecurityTestRunContext) securityTestRunner.getRunContext(), securityTestRunner);
                    } catch (Exception e1) {
                        UISupport.showErrorMessage(e1);
                    }

                }
            };
        }

        public SetupScriptGroovyEditorModel() {
            super(new String[]{"log", "testCase", "context", "testRunner"}, SecurityTestDesktopPanel.this
                    .getModelItem(), "Setup");
        }

        public String getScript() {
            return SecurityTestDesktopPanel.this.getModelItem().getStartupScript();
        }

        public void setScript(String text) {
            SecurityTestDesktopPanel.this.getModelItem().setStartupScript(text);
        }
    }

    private class TearDownScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        @Override
        public Action createRunAction() {
            return new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        MockSecurityTestRunner securityTestRunner = new MockSecurityTestRunner(SecurityTestDesktopPanel.this
                                .getModelItem());
                        SecurityTestDesktopPanel.this.getModelItem().runTearDownScript(
                                (SecurityTestRunContext) securityTestRunner.getRunContext(), securityTestRunner);
                    } catch (Exception e1) {
                        UISupport.showErrorMessage(e1);
                    }
                }
            };
        }

        public TearDownScriptGroovyEditorModel() {
            super(new String[]{"log", "securityTest", "context", "testRunner"}, SecurityTestDesktopPanel.this
                    .getModelItem(), "TearDown");
        }

        public String getScript() {
            return SecurityTestDesktopPanel.this.getModelItem().getTearDownScript();
        }

        public void setScript(String text) {
            SecurityTestDesktopPanel.this.getModelItem().setTearDownScript(text);
        }
    }

    public static class ModelItemListDragAndDropable extends JListDragAndDropable<JList> {
        public ModelItemListDragAndDropable(JList list, WsdlTestCase testCase) {
            super(list, testCase);
        }

        @Override
        public ModelItem getModelItemAtRow(int row) {
            return (ModelItem) getList().getModel().getElementAt(row);
        }

        @Override
        public int getModelItemRow(ModelItem modelItem) {
            ListModel model = getList().getModel();

            for (int c = 0; c < model.getSize(); c++) {
                if (model.getElementAt(c) == modelItem) {
                    return c;
                }
            }

            return -1;
        }

        public Component getRenderer(ModelItem modelItem) {
            return getList().getCellRenderer().getListCellRendererComponent(getList(), modelItem,
                    getModelItemRow(modelItem), true, true);
        }

        @Override
        public void setDragInfo(String dropInfo) {
            super.setDragInfo(dropInfo == null || dropInfo.length() == 0 ? null : dropInfo);
        }
    }

    public SecurityTestRunner getSecurityTestRunner() {
        return runner == null ? lastRunner : runner;
    }

    public boolean dependsOn(ModelItem modelItem) {
        SecurityTest securityTest = getModelItem();

        return modelItem == securityTest || modelItem == securityTest.getTestCase()
                || modelItem == securityTest.getTestCase().getTestSuite()
                || modelItem == securityTest.getTestCase().getTestSuite().getProject();
    }

    protected SecurityTest getSecurityTest() {
        return securityTest;
    }

    public class InternalSecurityTestRunListener extends SecurityTestRunListenerAdapter {

        public InternalSecurityTestRunListener() {
        }

        public void beforeRun(TestCaseRunner testRunner, SecurityTestRunContext runContext) {
            securityTestLog.clear();
            functionalTestLog.clear();

            runButton.setEnabled(false);
            cancelButton.setEnabled(true);
            testStepList.setEnabled(false);
            // testStepList.setSelectedIndex( -1 );
            String start = DateUtil.formatExtraFull(new Date());
            securityTestLog.addText("SecurityTest started at " + start);
            functionalTestLog.addText("Test started at " + start);

            SecurityTestDesktopPanel.this.beforeRun();

            progressBar.setValue(0);
            progressBar.setString("");

            if (runner == null) {
                runner = (SecurityTestRunnerImpl) testRunner;
            }

            securityStatus = ResultStatus.UNKNOWN;
        }

        public void afterRun(TestCaseRunner testRunner, SecurityTestRunContext runContext) {
            SecurityTestRunnerImpl securityRunner = (SecurityTestRunnerImpl) testRunner;

            if (testRunner.getStatus() == SecurityTestRunner.Status.CANCELED) {
                securityTestLog.addText("SecurityTest canceled [" + testRunner.getReason() + "], time taken = "
                        + securityRunner.getTimeTaken());
                functionalTestLog.addText("FunctionalTest canceled [" + testRunner.getReason() + "], time taken = "
                        + securityRunner.getFunctionalTimeTaken());
            } else if (testRunner.getStatus() == SecurityTestRunner.Status.FAILED) {
                String msg = securityRunner.getReason();
                if (securityRunner.getError() != null) {
                    if (msg != null) {
                        msg += ":";
                    }

                    msg += securityRunner.getError();
                }

                securityTestLog
                        .addText("SecurityTest failed [" + msg + "], time taken = " + securityRunner.getTimeTaken());
                if (functionalStatus == ResultStatus.OK) {
                    functionalTestLog.addText("FunctionalTest finished with status [" + functionalStatus
                            + "], time taken = " + securityRunner.getFunctionalTimeTaken());
                } else if (functionalStatus == ResultStatus.FAILED) {
                    functionalTestLog.addText("FunctionalTest failed [Failing due to failed test step], time taken = "
                            + securityRunner.getFunctionalTimeTaken());
                }
            } else {
                securityTestLog.addText("SecurityTest finished with status [" + testRunner.getStatus()
                        + "], time taken = " + securityRunner.getTimeTaken());
                if (functionalStatus == ResultStatus.OK) {
                    functionalTestLog.addText("FunctionalTest finished with status [" + functionalStatus
                            + "], time taken = " + securityRunner.getFunctionalTimeTaken());
                } else if (functionalStatus == ResultStatus.FAILED) {
                    functionalTestLog.addText("FunctionalTest failed [Failing due to failed test step], time taken = "
                            + securityRunner.getFunctionalTimeTaken());
                }
            }

            lastRunner = runner;
            runner = null;

            // JToggleButton loopButton = ( JToggleButton )runContext.getProperty(
            // "loopButton" );
            // if( loopButton != null && loopButton.isSelected()
            // && testRunner.getStatus() == SecurityTestRunner.Status.FINISHED )
            // {
            // SwingUtilities.invokeLater( new Runnable()
            // {
            // public void run()
            // {
            // runSecurityTest();
            // }
            // } );
            // }
            // else
            // {
            SecurityTestDesktopPanel.this.afterRun();
            // }

            if (testRunner.getStatus() == Status.FAILED) {
                securityStatus = ResultStatus.FAILED;
            } else if (testRunner.getStatus() == Status.FINISHED || testRunner.getStatus() == Status.CANCELED) {
                if (securityStatus != ResultStatus.FAILED) {
                    securityStatus = ResultStatus.OK;
                }
            }
            updateStatusIcon(securityStatus, securityLogInspector);
            updateStatusIcon(functionalStatus, functionalLogInspector);
        }

        @Override
        public void beforeSecurityScan(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                       SecurityScan securityCheck) {
            securityTestLog.addSecurityScanResult(securityCheck);
        }

        @Override
        public void afterSecurityScan(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                      SecurityScanResult securityCheckResult) {
            securityTestLog.updateSecurityScanResult(securityCheckResult);

            if (securityCheckResult.getStatus() == ResultStatus.CANCELED
                    && securityCheckResult.isHasRequestsWithWarnings()) {
                securityStatus = ResultStatus.FAILED;
            } else if (securityCheckResult.getStatus() == ResultStatus.FAILED) {
                securityStatus = ResultStatus.FAILED;
            } else if (securityCheckResult.getStatus() == ResultStatus.OK) {
                if (securityStatus != ResultStatus.FAILED) {
                    securityStatus = ResultStatus.OK;
                }
            }

        }

        @Override
        public void afterOriginalStep(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                      SecurityTestStepResult result) {
            functionalTestLog.addSecurityTestFunctionalStepResult(result.getOriginalTestStepResult());
            if (result.getOriginalTestStepResult().getStatus() == TestStepStatus.FAILED) {
                functionalStatus = ResultStatus.FAILED;
            } else if (result.getOriginalTestStepResult().getStatus() == TestStepStatus.OK
                    && functionalStatus != ResultStatus.FAILED) {
                functionalStatus = ResultStatus.OK;
            }
        }

        @Override
        public void afterSecurityScanRequest(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                             SecurityScanRequestResult securityCheckReqResult) {
            securityTestLog.addSecurityScanRequestResult(securityCheckReqResult);
        }

        @Override
        public void beforeStep(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                               TestStepResult testStepResult) {
            startStepLogEntryAdded = securityTestLog.addSecurityTestStepResult(testStepResult.getTestStep());
        }

        @Override
        public void afterStep(TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result) {
            boolean hasChecksToProcess = securityTest.getTestStepSecurityScansCount(result.getTestStep().getId()) > 0;
            securityTestLog.updateSecurityTestStepResult(result, hasChecksToProcess, startStepLogEntryAdded);
            if (result.getStatus() == ResultStatus.FAILED) {
                securityStatus = ResultStatus.FAILED;
            } else if (result.getStatus() == ResultStatus.OK) {
                if (securityStatus != ResultStatus.FAILED) {
                    securityStatus = ResultStatus.OK;
                }
            }
        }
    }

    @Override
    protected void renameModelItem() {
        SoapUI.getActionRegistry().performAction("RenameSecurityTestAction", getModelItem(), null);
    }

    @Override
    protected void cloneModelItem() {
        SoapUI.getActionRegistry().performAction("CloneSecurityTestAction", getModelItem(), null);
    }
}
