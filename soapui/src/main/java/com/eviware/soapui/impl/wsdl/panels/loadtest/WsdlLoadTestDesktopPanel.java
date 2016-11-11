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

package com.eviware.soapui.impl.wsdl.panels.loadtest;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.config.LoadTestLimitTypesConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.actions.loadtest.LoadTestOptionsAction;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTestRunner;
import com.eviware.soapui.impl.wsdl.loadtest.data.actions.ExportStatisticsAction;
import com.eviware.soapui.impl.wsdl.loadtest.log.LoadTestLog;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.LoadStrategy;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.LoadStrategyFactory;
import com.eviware.soapui.impl.wsdl.loadtest.strategy.LoadStrategyRegistry;
import com.eviware.soapui.impl.wsdl.panels.support.MockLoadTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockLoadTestRunner;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.LoadTestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.GroovyEditorComponent;
import com.eviware.soapui.support.components.GroovyEditorInspector;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.support.DesktopListenerAdapter;
import com.eviware.soapui.ui.support.KeySensitiveModelItemDesktopPanel;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Desktop panel for LoadTests
 *
 * @author Ole.Matzura
 */

@SuppressWarnings("serial")
public class WsdlLoadTestDesktopPanel extends KeySensitiveModelItemDesktopPanel<WsdlLoadTest> implements PropertyChangeListener {
    private static final String SECONDS_LIMIT = "Seconds";
    private static final String RUNS_LIMIT = "Total Runs";
    private static final String RUNS_PER_THREAD_LIMIT = "Runs per Thread";
    @SuppressWarnings("unused")
    private JSplitPane mainSplit;
    @SuppressWarnings("unused")
    private JTabbedPane mainTabs;
    @SuppressWarnings("unused")
    private JPanel graphPanel;
    protected JButton runButton;
    protected JButton cancelButton;
    protected JButton statisticsGraphButton;
    private WsdlLoadTestRunner runner;
    protected JSpinner threadsSpinner;
    private LoadTestRunListener internalLoadTestListener = new InternalLoadTestListener();
    protected JComboBox strategyCombo;
    protected JPanel loadStrategyConfigurationPanel;
    protected JButton resetButton;
    private LoadTestLog loadTestLog;
    protected JButton optionsButton;
    protected JButton testTimesGraphButton;
    @SuppressWarnings("unused")
    private Object limit;
    private JSpinner limitSpinner;
    private JComboBox limitTypeCombo;
    private SpinnerNumberModel limitSpinnerModel;
    protected JProgressBar progressBar;
    private StatisticsDesktopPanel statisticsDesktopPanel;
    private StatisticsHistoryDesktopPanel statisticsHistoryDesktopPanel;

    public boolean loadTestIsRunning;
    private InternalDesktopListener desktopListener;
    protected JButton exportButton;
    private JLoadTestAssertionsTable assertionsTable;
    private JStatisticsTable statisticsTable;
    private GroovyEditorComponent tearDownGroovyEditor;
    private GroovyEditorComponent setupGroovyEditor;
    private JInspectorPanel inspectorPanel;

    public WsdlLoadTestDesktopPanel(WsdlLoadTest loadTest) {
        super(loadTest);

        loadTestLog = loadTest.getLoadTestLog();
        loadTest.addPropertyChangeListener(this);
        loadTest.addLoadTestRunListener(internalLoadTestListener);

        desktopListener = new InternalDesktopListener();
        SoapUI.getDesktop().addDesktopListener(desktopListener);

        buildUI();
    }

    private void buildUI() {
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        setPreferredSize(new Dimension(600, 500));
    }

    private JComponent buildContent() {
        inspectorPanel = JInspectorPanelFactory.build(buildStatistics());
        addInspectors(inspectorPanel);
        inspectorPanel.setDefaultDividerLocation(0.6F);
        inspectorPanel.setCurrentInspector("LoadTest Log");

        return inspectorPanel.getComponent();
    }

    protected void addInspectors(JInspectorPanel inspectorPanel) {
        inspectorPanel.addInspector(new JComponentInspector<JComponent>(buildLog(), "LoadTest Log",
                "The current LoadTest execution log", true));
        inspectorPanel.addInspector(new JComponentInspector<JComponent>(buildAssertions(), "LoadTest Assertions",
                "The assertions for this LoadTest", true));
        inspectorPanel.addInspector(new GroovyEditorInspector(buildSetupScriptPanel(), "Setup Script",
                "Script to run before tunning a TestCase"));
        inspectorPanel.addInspector(new GroovyEditorInspector(buildTearDownScriptPanel(), "TearDown Script",
                "Script to run after a TestCase Run"));
    }

    protected GroovyEditorComponent buildTearDownScriptPanel() {
        tearDownGroovyEditor = new GroovyEditorComponent(new TearDownScriptGroovyEditorModel(), null);
        return tearDownGroovyEditor;
    }

    protected GroovyEditorComponent buildSetupScriptPanel() {
        setupGroovyEditor = new GroovyEditorComponent(new SetupScriptGroovyEditorModel(), null);
        return setupGroovyEditor;
    }

    protected JComponent buildStatistics() {
        statisticsTable = new JStatisticsTable(getModelItem());
        return statisticsTable;
    }

    protected JComponent buildLog() {
        JLoadTestLogTable loadTestLogTable = new JLoadTestLogTable(loadTestLog);
        return loadTestLogTable;
    }

    protected JComponent buildAssertions() {
        assertionsTable = new JLoadTestAssertionsTable(getModelItem());
        return assertionsTable;
    }

    protected JComponent buildToolbar() {
        WsdlLoadTest loadTest = getModelItem();

        JXToolBar toolbar = UISupport.createToolbar();

        // ButtonBarBuilder builder = new ButtonBarBuilder();
        runButton = UISupport.createToolbarButton(new RunLoadTestAction());
        cancelButton = UISupport.createToolbarButton(new CancelRunTestCaseAction(), false);
        resetButton = UISupport.createToolbarButton(new ResetAction());
        exportButton = UISupport.createToolbarButton(new ExportStatisticsAction(loadTest.getStatisticsModel()));

        statisticsGraphButton = UISupport.createToolbarButton(new ShowStatisticsGraphAction());
        testTimesGraphButton = UISupport.createToolbarButton(new ShowTestTimesGraphAction());

        statisticsGraphButton.setEnabled(getModelItem().getHistoryLimit() != 0);
        testTimesGraphButton.setEnabled(getModelItem().getHistoryLimit() != 0);

        AbstractAction optionsDelegate = SwingActionDelegate.createDelegate(LoadTestOptionsAction.SOAPUI_ACTION_ID,
                loadTest);
        optionsDelegate.putValue(Action.SMALL_ICON, UISupport.createImageIcon("/preferences.png"));
        optionsButton = UISupport.createToolbarButton(optionsDelegate);

        strategyCombo = new JComboBox(LoadStrategyRegistry.getInstance().getStrategies());
        strategyCombo.setToolTipText("Selects which LoadTest Strategy to use");
        UISupport.setPreferredHeight(strategyCombo, 18);
        strategyCombo.setSelectedItem(loadTest.getLoadStrategy().getType());
        strategyCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Object item = e.getItem();
                if (item == null) {
                    return;
                }

                setLoadStrategy(item.toString());
            }
        });

        toolbar.add(runButton);
        toolbar.add(cancelButton);
        toolbar.add(statisticsGraphButton);
        toolbar.add(testTimesGraphButton);
        toolbar.add(resetButton);
        toolbar.add(exportButton);

        toolbar.add(optionsButton);
        toolbar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.LOADTESTEDITOR_HELP_URL)));
        toolbar.add(Box.createHorizontalGlue());
        buildLimitBar(toolbar);
        toolbar.addSeparator();

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(70, 20));

        toolbar.addFixed(progressBar);

        ButtonBarBuilder builder = new ButtonBarBuilder();

        builder.addFixed(new JLabel("Threads:"));
        builder.addRelatedGap();

        threadsSpinner = new JSpinner(new SpinnerNumberModel(getModelItem().getThreadCount(), 1, 9999, 1));
        threadsSpinner.setToolTipText("Sets the number of threads (\"Virtual Users\") to run this TestCase");
        UISupport.setPreferredHeight(threadsSpinner, 18);
        threadsSpinner.getModel().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                getModelItem().setThreadCount(((SpinnerNumberModel) threadsSpinner.getModel()).getNumber().intValue());
            }
        });

        builder.addFixed(threadsSpinner);
        builder.addUnrelatedGap();

        LoadStrategy loadStrategy = loadTest.getLoadStrategy();

        builder.addFixed(new JLabel("Strategy"));
        builder.addRelatedGap();
        builder.addFixed(strategyCombo);
        builder.addUnrelatedGap();

        loadStrategyConfigurationPanel = new JPanel(new BorderLayout());
        loadStrategyConfigurationPanel.add(loadStrategy.getConfigurationPanel(), BorderLayout.CENTER);

        builder.addFixed(loadStrategyConfigurationPanel);
        builder.setBorder(BorderFactory.createEmptyBorder(2, 3, 3, 3));

        return UISupport.buildPanelWithToolbar(toolbar, builder.getPanel());
    }

    public void buildLimitBar(JXToolBar toolbar) {
        limitSpinnerModel = new SpinnerNumberModel(getModelItem().getTestLimit(), 0, Long.MAX_VALUE, 100);

        limitSpinner = new JSpinner(limitSpinnerModel);
        limitSpinner.setPreferredSize(new Dimension(70, 20));
        limitSpinner.setToolTipText("Sets the limit for this test; total number of requests or seconds to run");
        limitSpinner.getModel().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int intValue = ((SpinnerNumberModel) limitSpinner.getModel()).getNumber().intValue();
                getModelItem().setTestLimit(intValue);
            }
        });

        toolbar.addSeparator();
        toolbar.addFixed(new JLabel("Limit:"));
        toolbar.addSeparator();
        toolbar.addFixed(limitSpinner);
        toolbar.addSeparator();

        limitTypeCombo = new JComboBox(new String[]{WsdlLoadTestDesktopPanel.RUNS_LIMIT,
                WsdlLoadTestDesktopPanel.SECONDS_LIMIT, WsdlLoadTestDesktopPanel.RUNS_PER_THREAD_LIMIT});

        if (getModelItem().getLimitType() == LoadTestLimitTypesConfig.TIME) {
            limitTypeCombo.setSelectedIndex(1);
        } else if (getModelItem().getLimitType() == LoadTestLimitTypesConfig.COUNT_PER_THREAD) {
            limitTypeCombo.setSelectedIndex(2);
        }

        toolbar.addFixed(limitTypeCombo);
        toolbar.addSeparator();

        limitTypeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Object item = e.getItem();
                if (WsdlLoadTestDesktopPanel.RUNS_LIMIT.equals(item)) {
                    getModelItem().setLimitType(LoadTestLimitTypesConfig.COUNT);
                } else if (WsdlLoadTestDesktopPanel.SECONDS_LIMIT.equals(item)) {
                    getModelItem().setLimitType(LoadTestLimitTypesConfig.TIME);
                } else if (WsdlLoadTestDesktopPanel.RUNS_PER_THREAD_LIMIT.equals(item)) {
                    getModelItem().setLimitType(LoadTestLimitTypesConfig.COUNT_PER_THREAD);
                }
            }
        });
    }

    public boolean onClose(boolean canCancel) {
        if (runner != null && runner.getStatus() == Status.RUNNING) {
            if (!UISupport.confirm("Running test will be canceled when closing window. Close anyway?", "Close LoadTest")) {
                return false;
            }
        }

        getModelItem().removeLoadTestRunListener(internalLoadTestListener);
        getModelItem().removePropertyChangeListener(this);
        getModelItem().getStatisticsModel().reset();

        if (runner != null && runner.getStatus() == Status.RUNNING) {
            runner.cancel("closing window");
        }

        if (statisticsDesktopPanel != null) {
            SoapUI.getDesktop().closeDesktopPanel(statisticsDesktopPanel);
        }

        if (statisticsHistoryDesktopPanel != null) {
            SoapUI.getDesktop().closeDesktopPanel(statisticsHistoryDesktopPanel);
        }

        assertionsTable.release();
        loadStrategyConfigurationPanel.removeAll();
        SoapUI.getDesktop().removeDesktopListener(desktopListener);

        statisticsTable.release();
        inspectorPanel.release();

        setupGroovyEditor.release();
        tearDownGroovyEditor.release();

        return release();
    }

    private final class InternalDesktopListener extends DesktopListenerAdapter {
        public void desktopPanelClosed(DesktopPanel desktopPanel) {
            if (desktopPanel == statisticsDesktopPanel) {
                statisticsDesktopPanel = null;
            } else if (desktopPanel == statisticsHistoryDesktopPanel) {
                statisticsHistoryDesktopPanel = null;
            }
        }
    }

    public class RunLoadTestAction extends AbstractAction {
        public RunLoadTestAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
            putValue(Action.SHORT_DESCRIPTION, "Runs this LoadTest");
        }

        public void actionPerformed(ActionEvent e) {

            WsdlLoadTest loadtest = getModelItem();
            if (loadtest.getTestCase().getTestStepCount() == 0) {
                UISupport.showErrorMessage("Missing TestSteps for testing!");
                return;
            }

            if (loadtest.getLimitType() == LoadTestLimitTypesConfig.COUNT
                    && loadtest.getTestLimit() < loadtest.getThreadCount()) {
                if (!UISupport.confirm("The run limit is set to a lower count than number of threads\nRun Anyway?",
                        "Run LoadTest")) {
                    return;
                }
            }

            runButton.setEnabled(false);
            runner = loadtest.run();
            Analytics.trackAction(SoapUIActions.RUN_LOAD_TEST.getActionName());
        }
    }

    public class ResetAction extends AbstractAction {
        public ResetAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/reset_loadtest_statistics.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Resets statistics for this LoadTest");
        }

        public void actionPerformed(ActionEvent e) {
            getModelItem().getStatisticsModel().reset();
        }
    }

    public class ShowStatisticsGraphAction extends AbstractAction {
        public ShowStatisticsGraphAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/stats_graph.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Shows the statistics graph");
        }

        public void actionPerformed(ActionEvent e) {
            if (statisticsDesktopPanel == null) {
                statisticsDesktopPanel = new StatisticsDesktopPanel(getModelItem());
            }

            UISupport.showDesktopPanel(statisticsDesktopPanel);
        }
    }

    public class ShowTestTimesGraphAction extends AbstractAction {
        public ShowTestTimesGraphAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/samples_graph.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Shows the Statistics History graph");
        }

        public void actionPerformed(ActionEvent e) {
            if (statisticsHistoryDesktopPanel == null) {
                statisticsHistoryDesktopPanel = new StatisticsHistoryDesktopPanel(getModelItem());
            }

            UISupport.showDesktopPanel(statisticsHistoryDesktopPanel);
        }
    }

    public class CancelRunTestCaseAction extends AbstractAction {

        public CancelRunTestCaseAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/stop.png"));
            putValue(Action.SHORT_DESCRIPTION, "Stops running this LoadTest");
        }

        public void actionPerformed(ActionEvent e) {
            if (runner != null) {
                runner.cancel("Canceled");
                cancelButton.setEnabled(false);
            }
        }
    }

    public boolean dependsOn(ModelItem modelItem) {
        WsdlLoadTest loadTest = getModelItem();

        return modelItem == loadTest || modelItem == loadTest.getTestCase()
                || modelItem == loadTest.getTestCase().getTestSuite()
                || modelItem == loadTest.getTestCase().getTestSuite().getProject();
    }

    public void setLoadStrategy(String type) {
        LoadStrategyFactory factory = LoadStrategyRegistry.getInstance().getFactory(type);
        LoadStrategy loadStrategy = factory.create(getModelItem());
        getModelItem().setLoadStrategy(loadStrategy);
        loadStrategyConfigurationPanel.removeAll();
        loadStrategyConfigurationPanel.add(loadStrategy.getConfigurationPanel(), BorderLayout.CENTER);
        loadStrategyConfigurationPanel.revalidate();
    }

    private class InternalLoadTestListener extends LoadTestRunListenerAdapter {
        public void beforeLoadTest(LoadTestRunner testRunner, LoadTestRunContext context) {
            loadTestLog.clear();

            loadTestIsRunning = true;
            if (getModelItem().getTestLimit() > 0) {
                progressBar.setValue(0);
                progressBar.setString(null);
            } else {
                progressBar.setString("...");
            }

            progressBar.setStringPainted(true);

            runButton.setEnabled(false);
            cancelButton.setEnabled(true);
            strategyCombo.setEnabled(false);
            limitTypeCombo.setEnabled(false);
            optionsButton.setEnabled(false);
            threadsSpinner.setEnabled(getModelItem().getLoadStrategy().allowThreadCountChangeDuringRun());

            new Thread(new ProgressBarUpdater(), getModelItem().getName() + " ProgressBarUpdater").start();
        }

        public void afterLoadTest(LoadTestRunner testRunner, LoadTestRunContext context) {
            runButton.setEnabled(true);

            cancelButton.setEnabled(false);
            strategyCombo.setEnabled(true);
            limitTypeCombo.setEnabled(true);
            threadsSpinner.setEnabled(true);
            optionsButton.setEnabled(true);

            runner = null;
            loadTestIsRunning = false;

            if (progressBar.isIndeterminate()) {
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
            } else if (testRunner.getStatus() == Status.FINISHED) {
                progressBar.setValue(100);
            }

            if (testRunner.getStatus() == Status.FAILED) {
                UISupport.showErrorMessage("LoadTest failed; " + testRunner.getReason());
            }
        }

    }

    private class ProgressBarUpdater implements Runnable {
        public void run() {
            while (true) {
                if (!loadTestIsRunning) {
                    break;
                }

                if (getModelItem().getTestLimit() == 0) {
                    if (loadTestIsRunning && !progressBar.isIndeterminate()) {
                        progressBar.setIndeterminate(true);
                        progressBar.setString("...");
                    }
                } else {
                    if (loadTestIsRunning && progressBar.isIndeterminate()) {
                        progressBar.setIndeterminate(false);
                        progressBar.setString(null);
                    }

                    progressBar.setValue(runner == null ? 0 : (int) (runner.getProgress() * 100));
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    SoapUI.logError(e);
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(WsdlLoadTest.THREADCOUNT_PROPERTY)) {
            threadsSpinner.setValue(evt.getNewValue());
        } else if (evt.getPropertyName().equals(WsdlLoadTest.HISTORYLIMIT_PROPERTY)) {
            long lng = (Long) evt.getNewValue();

            statisticsGraphButton.setEnabled(lng != 0);
            testTimesGraphButton.setEnabled(lng != 0);
        }

        super.propertyChange(evt);
    }

    private class SetupScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        @Override
        public Action createRunAction() {
            return new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        MockLoadTestRunner mockTestRunner = new MockLoadTestRunner(
                                WsdlLoadTestDesktopPanel.this.getModelItem(), SoapUI.ensureGroovyLog());
                        WsdlLoadTestDesktopPanel.this.getModelItem().runSetupScript(
                                new MockLoadTestRunContext(mockTestRunner), mockTestRunner);
                    } catch (Exception e1) {
                        UISupport.showErrorMessage(e1);
                    }
                }
            };
        }

        public SetupScriptGroovyEditorModel() {
            super(new String[]{"log", "context", "loadTestRunner"}, WsdlLoadTestDesktopPanel.this.getModelItem(),
                    "Setup");
        }

        public String getScript() {
            return WsdlLoadTestDesktopPanel.this.getModelItem().getSetupScript();
        }

        public void setScript(String text) {
            WsdlLoadTestDesktopPanel.this.getModelItem().setSetupScript(text);
        }
    }

    private class TearDownScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        @Override
        public Action createRunAction() {
            return new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        MockLoadTestRunner mockTestRunner = new MockLoadTestRunner(
                                WsdlLoadTestDesktopPanel.this.getModelItem(), SoapUI.ensureGroovyLog());
                        WsdlLoadTestDesktopPanel.this.getModelItem().runTearDownScript(
                                new MockLoadTestRunContext(mockTestRunner), mockTestRunner);
                    } catch (Exception e1) {
                        UISupport.showErrorMessage(e1);
                    }
                }
            };
        }

        public TearDownScriptGroovyEditorModel() {
            super(new String[]{"log", "context", "loadTestRunner"}, WsdlLoadTestDesktopPanel.this.getModelItem(),
                    "TearDown");
        }

        public String getScript() {
            return WsdlLoadTestDesktopPanel.this.getModelItem().getTearDownScript();
        }

        public void setScript(String text) {
            WsdlLoadTestDesktopPanel.this.getModelItem().setTearDownScript(text);
        }
    }

    @Override
    protected void renameModelItem() {
        SoapUI.getActionRegistry().performAction("RenameLoadTestAction", getModelItem(), null);
    }

    @Override
    protected void cloneModelItem() {
        SoapUI.getActionRegistry().performAction("CloneLoadTestAction", getModelItem(), null);
    }
}
