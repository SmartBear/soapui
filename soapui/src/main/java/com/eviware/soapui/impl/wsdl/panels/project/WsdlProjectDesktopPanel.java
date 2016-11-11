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

package com.eviware.soapui.impl.wsdl.panels.project;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.ProjectMetrics;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.util.ModelItemIconFactory;
import com.eviware.soapui.security.panels.ProjectSensitiveInformationPanel;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.GroovyEditorComponent;
import com.eviware.soapui.support.components.GroovyEditorInspector;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JFocusableComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.MetricsPanel;
import com.eviware.soapui.support.components.MetricsPanel.MetricType;
import com.eviware.soapui.support.components.MetricsPanel.MetricsSection;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class WsdlProjectDesktopPanel extends ModelItemDesktopPanel<WsdlProject> {
    // These final strings are used both as keys for counters in the MetricsPanel and as
    // the actual VISIBLE label in the user interface. They all have to be different.
    protected static final String MOCKRESPONSES_STATISTICS = "WsdlMockResponses";
    protected static final String MOCKOPERATIONS_STATISTICS = "WsdlMockOperations";
    protected static final String MOCKSERVICES_STATISTICS = "WsdlMockServices";
    protected static final String REST_MOCKRESPONSES_STATISTICS = "RestMockResponses";
    protected static final String REST_MOCKACTIONS_STATISTICS = "RestMockActions";
    protected static final String REST_MOCKSERVICES_STATISTICS = "RestMockServices";
    protected static final String LOADTESTS_STATISTICS = "LoadTests";
    protected static final String ASSERTIONS_STATISTICS = "Assertions";
    protected static final String TESTSTEPS_STATISTICS = "TestSteps";
    protected static final String TESTCASES_STATISTICS = "TestCases";
    protected static final String TESTSUITES_STATISTICS = "TestSuites";

    private PropertyHolderTable propertiesTable;
    private JUndoableTextArea descriptionArea;
    private InternalTreeModelListener treeModelListener;
    private Set<String> interfaceNameSet = new HashSet<String>();
    private WSSTabPanel wssTabPanel;
    protected MetricsPanel metrics;
    private GroovyEditorComponent loadScriptGroovyEditor;
    private GroovyEditorComponent saveScriptGroovyEditor;
    private JInspectorPanel inspectorPanel;
    private WsdlProjectTestSuitesTabPanel testSuitesPanel;
    private ProjectSensitiveInformationPanel sensitiveInfoPanel;

    public WsdlProjectDesktopPanel(WsdlProject modelItem) {
        super(modelItem);

        add(buildTabbedPane(), BorderLayout.CENTER);

        setPreferredSize(new Dimension(600, 600));
    }

    private Component buildTabbedPane() {
        JTabbedPane mainTabs = new JTabbedPane();
        addTabs(mainTabs);
        return UISupport.createTabPanel(mainTabs, true);
    }

    protected void addTabs(JTabbedPane mainTabs) {
        mainTabs.addTab("Overview", null, buildOverviewTab(), "Shows General Project information and metrics");
        mainTabs.addTab("TestSuites", null, testSuitesPanel = buildTestSuitesTab(),
                "Shows and runs all TestSuites in project");
        mainTabs.addTab("WS-Security Configurations", null, buildWSSTab(), "Manages Security-related configurations");
        mainTabs.addTab("Security Scan Defaults", null, buildSecConfigTab(), "Manages Security related configurations");
    }

    private Component buildSecConfigTab() {
        sensitiveInfoPanel = new ProjectSensitiveInformationPanel(getModelItem().getConfig());

        return sensitiveInfoPanel.getMainPanel();
    }

    public WsdlProjectTestSuitesTabPanel getTestSuitesPanel() {
        return testSuitesPanel;
    }

    protected WsdlProjectTestSuitesTabPanel buildTestSuitesTab() {
        return new WsdlProjectTestSuitesTabPanel(getModelItem());
    }

    protected Component buildWSSTab() {
        wssTabPanel = new WSSTabPanel(getModelItem().getWssContainer());
        return wssTabPanel;
    }

    protected Component buildOverviewTab() {
        inspectorPanel = JInspectorPanelFactory.build(buildProjectOverview());

        addOverviewInspectors(inspectorPanel);

        inspectorPanel.setCurrentInspector("Properties");

        if (StringUtils.hasContent(getModelItem().getDescription())
                && getModelItem().getSettings().getBoolean(UISettings.SHOW_DESCRIPTIONS)) {
            inspectorPanel.setCurrentInspector("Description");
        }

        treeModelListener = new InternalTreeModelListener();
        SoapUI.getNavigator().getMainTree().getModel().addTreeModelListener(treeModelListener);

        updateStatistics();

        return inspectorPanel.getComponent();
    }

    protected void addOverviewInspectors(JInspectorPanel inspectorPanel) {
        inspectorPanel.addInspector(new JFocusableComponentInspector<JPanel>(buildDescriptionPanel(), descriptionArea,
                "Description", "Project description", true));
        inspectorPanel.addInspector(new JComponentInspector<JComponent>(buildPropertiesPanel(), "Properties",
                "Project level properties", true));
        inspectorPanel.addInspector(new GroovyEditorInspector(buildLoadScriptPanel(), "Load Script",
                "Script to run after loading the project"));
        inspectorPanel.addInspector(new GroovyEditorInspector(buildSaveScriptPanel(), "Save Script",
                "Script to run before saving the project"));
    }

    private void updateStatistics() {
        ProjectMetrics projectMetrics = new ProjectMetrics(getModelItem());

        metrics.setMetric("File Path", getModelItem().getPath());

        Set<String> newNames = new HashSet<String>();
        boolean rebuilt = false;
        for (Interface iface : getModelItem().getInterfaceList()) {
            if (!metrics.hasMetric(iface.getName())) {
                MetricsSection section = metrics.getSection("Interface Summary");
                buildInterfaceSummary(section.clear());
                rebuilt = true;
                break;
            }

            newNames.add(iface.getName());
            interfaceNameSet.remove(iface.getName());
        }

        if (!rebuilt) {
            if (!interfaceNameSet.isEmpty()) {
                MetricsSection section = metrics.getSection("Interface Summary");
                buildInterfaceSummary(section.clear());
            }

            interfaceNameSet = newNames;
        }

        metrics.setMetric(TESTSUITES_STATISTICS, getModelItem().getTestSuiteCount());

        metrics.setMetric(TESTCASES_STATISTICS, projectMetrics.getTestCaseCount());
        metrics.setMetric(TESTSTEPS_STATISTICS, projectMetrics.getTestStepCount());
        metrics.setMetric(ASSERTIONS_STATISTICS, projectMetrics.getAssertionCount());
        metrics.setMetric(LOADTESTS_STATISTICS, projectMetrics.getLoadTestCount());

        metrics.setMetric(MOCKSERVICES_STATISTICS, getModelItem().getMockServiceCount());
        metrics.setMetric(MOCKOPERATIONS_STATISTICS, projectMetrics.getMockOperationCount());
        metrics.setMetric(MOCKRESPONSES_STATISTICS, projectMetrics.getMockResponseCount());

        metrics.setMetric(REST_MOCKSERVICES_STATISTICS, getModelItem().getRestMockServiceCount());
        metrics.setMetric(REST_MOCKACTIONS_STATISTICS, projectMetrics.getRestMockActionCount());
        metrics.setMetric(REST_MOCKRESPONSES_STATISTICS, projectMetrics.getRestMockResponseCount());
    }

    private JComponent buildProjectOverview() {
        metrics = new MetricsPanel();

        JXToolBar toolbar = buildOverviewToolbar();
        metrics.add(toolbar, BorderLayout.NORTH);

        MetricsSection section = metrics.addSection("Project Summary");
        section.addMetric(ModelItemIconFactory.getIcon(Project.class), "File Path", MetricType.URL);
        section.finish();

        section = metrics.addSection("Interface Summary");
        buildInterfaceSummary(section);

        section = metrics.addSection("Test Summary");
        section.addMetric(ModelItemIconFactory.getIcon(TestSuite.class), TESTSUITES_STATISTICS);
        section.addMetric(ModelItemIconFactory.getIcon(TestCase.class), TESTCASES_STATISTICS);
        section.addMetric(ModelItemIconFactory.getIcon(TestStep.class), TESTSTEPS_STATISTICS);
        section.addMetric(ModelItemIconFactory.getIcon(TestAssertion.class), ASSERTIONS_STATISTICS);
        section.addMetric(ModelItemIconFactory.getIcon(LoadTest.class), LOADTESTS_STATISTICS);
        section.finish();

        section = metrics.addSection("SOAP Mock Summary");
        section.addMetric(ModelItemIconFactory.getIcon(MockService.class), MOCKSERVICES_STATISTICS);
        section.addMetric(ModelItemIconFactory.getIcon(MockOperation.class), MOCKOPERATIONS_STATISTICS);
        section.addMetric(ModelItemIconFactory.getIcon(MockResponse.class), MOCKRESPONSES_STATISTICS);
        section.finish();

        section = metrics.addSection("REST Mock Summary");
        section.addMetric(ModelItemIconFactory.getIcon(RestMockService.class), REST_MOCKSERVICES_STATISTICS);
        section.addMetric(ModelItemIconFactory.getIcon(RestMockAction.class), REST_MOCKACTIONS_STATISTICS);
        section.addMetric(ModelItemIconFactory.getIcon(RestMockResponse.class), REST_MOCKRESPONSES_STATISTICS);
        section.finish();
        return new JScrollPane(metrics);
    }

    protected JXToolBar buildOverviewToolbar() {
        JXToolBar toolbar = UISupport.createSmallToolbar();
        toolbar.addGlue();
        toolbar
                .addFixed(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.PROJECT_OVERVIEW_HELP_URL)));
        return toolbar;
    }

    protected void buildInterfaceSummary(MetricsSection section) {
        interfaceNameSet.clear();
        for (Interface ic : getModelItem().getInterfaceList()) {
            if (ic instanceof WsdlInterface) {
                WsdlInterface iface = (WsdlInterface) ic;
                section.addMetric(iface.getIcon(), iface.getName(), MetricType.URL).set(iface.getDefinition());
            } else if (ic instanceof RestService) {
                RestService iface = (RestService) ic;
                section.addMetric(iface.getIcon(), iface.getName(), MetricType.URL).set(iface.getWadlUrl());
            }

            interfaceNameSet.add(ic.getName());
        }

        section.finish();
    }

    private JPanel buildDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        descriptionArea = new JUndoableTextArea(getModelItem().getDescription());
        descriptionArea.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            @Override
            public void update(Document document) {
                getModelItem().setDescription(descriptionArea.getText());
            }
        });

        panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        panel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        UISupport.addTitledBorder(panel, "Project Description");

        return panel;
    }

    protected GroovyEditorComponent buildLoadScriptPanel() {
        loadScriptGroovyEditor = new GroovyEditorComponent(new LoadScriptGroovyEditorModel(), null);
        return loadScriptGroovyEditor;
    }

    protected GroovyEditorComponent buildSaveScriptPanel() {
        saveScriptGroovyEditor = new GroovyEditorComponent(new SaveScriptGroovyEditorModel(), null);
        return saveScriptGroovyEditor;
    }

    private JComponent buildPropertiesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        propertiesTable = new PropertyHolderTable(getModelItem());
        if (getModelItem() instanceof WsdlProject) {
            ((WsdlProject) getModelItem()).addProjectListener(propertiesTable.getProjectListener());
        }
        panel.add(propertiesTable, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public boolean dependsOn(ModelItem modelItem) {
        return modelItem == getModelItem();
    }

    public boolean onClose(boolean canCancel) {
        propertiesTable.release();
        loadScriptGroovyEditor.getEditor().release();
        saveScriptGroovyEditor.getEditor().release();

        SoapUI.getNavigator().getMainTree().getModel().removeTreeModelListener(treeModelListener);
        wssTabPanel.release();
        sensitiveInfoPanel.release();

        inspectorPanel.release();
        testSuitesPanel.release();
        return release();
    }

    private final class InternalTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            updateStatistics();
        }

        public void treeNodesInserted(TreeModelEvent e) {
            updateStatistics();
        }

        public void treeNodesRemoved(TreeModelEvent e) {
            updateStatistics();
        }

        public void treeStructureChanged(TreeModelEvent e) {
            updateStatistics();
        }
    }

    private class LoadScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        public LoadScriptGroovyEditorModel() {
            super(new String[]{"log", "project"}, WsdlProjectDesktopPanel.this.getModelItem(), "Load");
        }

        @Override
        public String getScript() {
            return WsdlProjectDesktopPanel.this.getModelItem().getAfterLoadScript();
        }

        @Override
        public void setScript(String text) {
            WsdlProjectDesktopPanel.this.getModelItem().setAfterLoadScript(text);
        }

        @Override
        public Action getRunAction() {
            return new AfterLoadScriptRunAction();
        }

        private final class AfterLoadScriptRunAction extends AbstractAction {
            public AfterLoadScriptRunAction() {
                putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
                putValue(SHORT_DESCRIPTION, "Runs this script");
            }

            public void actionPerformed(ActionEvent e) {
                try {
                    WsdlProjectDesktopPanel.this.getModelItem().runAfterLoadScript();
                } catch (Exception e1) {
                    UISupport.showErrorMessage(e1);
                }
            }
        }
    }

    private class SaveScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        public SaveScriptGroovyEditorModel() {
            super(new String[]{"log", "project"}, WsdlProjectDesktopPanel.this.getModelItem(), "Save");
        }

        @Override
        public String getScript() {
            return WsdlProjectDesktopPanel.this.getModelItem().getBeforeSaveScript();
        }

        @Override
        public void setScript(String text) {
            WsdlProjectDesktopPanel.this.getModelItem().setBeforeSaveScript(text);
        }

        @Override
        public Action getRunAction() {
            return new BeforeSaveScriptRunAction();
        }

        private final class BeforeSaveScriptRunAction extends AbstractAction {
            public BeforeSaveScriptRunAction() {
                putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
                putValue(SHORT_DESCRIPTION, "Runs this script");
            }

            public void actionPerformed(ActionEvent e) {
                try {
                    WsdlProjectDesktopPanel.this.getModelItem().runBeforeSaveScript();
                } catch (Exception e1) {
                    UISupport.showErrorMessage(e1);
                }
            }
        }
    }

}
