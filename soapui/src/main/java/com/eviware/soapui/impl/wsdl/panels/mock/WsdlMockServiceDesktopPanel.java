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

package com.eviware.soapui.impl.wsdl.panels.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.rest.actions.mock.AddEmptyRestMockResourceAction;
import com.eviware.soapui.impl.rest.actions.mock.RestMockServiceOptionsAction;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.actions.mockservice.AddNewMockOperationAction;
import com.eviware.soapui.impl.wsdl.actions.mockservice.MockServiceOptionsAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.mock.MockServiceListener;
import com.eviware.soapui.model.support.MockRunListenerAdapter;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.GroovyEditorComponent;
import com.eviware.soapui.support.components.GroovyEditorInspector;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JFocusableComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.AbstractListMouseListener;
import com.eviware.soapui.support.swing.ModelItemListKeyListener;
import com.eviware.soapui.support.swing.ModelItemListMouseListener;
import com.eviware.soapui.ui.support.JProgressBarWrapper;
import com.eviware.soapui.ui.support.KeySensitiveModelItemDesktopPanel;
import org.apache.commons.collections.list.TreeList;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DesktopPanel for WsdlMockServices
 *
 * @author ole.matzura
 */


@SuppressWarnings("serial")
public class WsdlMockServiceDesktopPanel<MockServiceType extends MockService>
        extends KeySensitiveModelItemDesktopPanel<MockServiceType> {
    private JButton runButton;
    private WsdlMockRunner mockRunner;
    private JButton stopButton;
    private JProgressBarWrapper progressBarWrapper = new JProgressBarWrapper();
    private LogListModel logListModel;
    private JList testLogList;
    private JCheckBox enableLogCheckBox;
    private JScrollPane logScrollPane;
    private JList operationList;
    private InternalMockRunListener mockRunListener;
    private PropertyHolderTable propertiesTable;
    private JUndoableTextArea descriptionArea;
    private JButton showWsdlButton;
    private JButton optionsButton;
    private JLabel runInfoLabel;
    private GroovyEditorComponent startGroovyEditor;
    private GroovyEditorComponent stopGroovyEditor;
    private GroovyEditorComponent onRequestGroovyEditor;
    private GroovyEditorComponent afterRequestGroovyEditor;
    private JInspectorPanel inspectorPanel;
    private JInspectorPanel contentInspector;

    public WsdlMockServiceDesktopPanel(MockServiceType mockService) {
        super(mockService);
        buildUI();

        setPreferredSize(new Dimension(400, 500));

        mockRunListener = new InternalMockRunListener();
        mockService.addMockRunListener(mockRunListener);
    }

    public boolean onClose(boolean canCancel) {
        if (mockRunner != null && mockRunner.isRunning() && canCancel) {
            if (!UISupport.confirm("Close and stop MockService", "Close MockService")) {
                return false;
            }
        }

        if (mockRunner != null) {
            if (mockRunner.isRunning()) {
                mockRunner.stop();
            }

            mockRunner.release();
        }

        getModelItem().removeMockRunListener(mockRunListener);
        ((OperationListModel) operationList.getModel()).release();

        logListModel.clear();
        propertiesTable.release();

        startGroovyEditor.release();
        stopGroovyEditor.release();
        onRequestGroovyEditor.release();
        afterRequestGroovyEditor.release();

        inspectorPanel.release();
        contentInspector.release();
        return release();
    }

    public boolean dependsOn(ModelItem modelItem) {
        return modelItem == getModelItem() || modelItem == getModelItem().getProject();
    }

    private void buildUI() {
        add(buildToolbar(), BorderLayout.NORTH);

        contentInspector = JInspectorPanelFactory.build(buildContent());
        contentInspector.setDefaultDividerLocation(0.5F);
        contentInspector.addInspector(new JComponentInspector<JComponent>(buildLog(), "Message Log",
                "A log of processed requests and their responses", true));

        contentInspector.setCurrentInspector("Message Log");

        add(contentInspector.getComponent(), BorderLayout.CENTER);
        add(new JLabel("--"), BorderLayout.PAGE_END);
    }

    public boolean logIsEnabled() {
        return enableLogCheckBox.isSelected();
    }

    private JComponent buildContent() {
        JTabbedPane tabs = new JTabbedPane();
        inspectorPanel = JInspectorPanelFactory.build(buildOperationList());

        String title = getModelItem() instanceof RestMockService ? "Actions" : "Operations";
        tabs.addTab(title, inspectorPanel.getComponent());
        addTabs(tabs, inspectorPanel);

        if (StringUtils.hasContent(getModelItem().getDescription())
                && getModelItem().getSettings().getBoolean(UISettings.SHOW_DESCRIPTIONS)) {
            inspectorPanel.setCurrentInspector("Description");
        }

        return UISupport.createTabPanel(tabs, true);
    }

    protected void addTabs(JTabbedPane tabs, JInspectorPanel inspectorPanel) {
        inspectorPanel.addInspector(new JFocusableComponentInspector<JPanel>(buildDescriptionPanel(), descriptionArea,
                "Description", "A description for this MockService", true));
        inspectorPanel.addInspector(new JComponentInspector<JComponent>(buildPropertiesPanel(), "Properties",
                "Properties for this MockService", true));
        inspectorPanel.addInspector(new GroovyEditorInspector(buildStartScriptPanel(), "Start Script",
                "A Groovy script to run when starting the MockService"));
        inspectorPanel.addInspector(new GroovyEditorInspector(buildStopScriptPanel(), "Stop Script",
                "A Groovy script to run when stopping the MockService"));
        inspectorPanel.addInspector(new GroovyEditorInspector(buildOnRequestScriptPanel(), "OnRequest Script",
                "A Groovy script to run when receiving a request before it is dispatched"));
        inspectorPanel.addInspector(new GroovyEditorInspector(buildAfterRequestScriptPanel(), "AfterRequest Script",
                "A Groovy script to run after a request has been dispatched"));
    }

    protected JComponent buildOperationList() {
        operationList = new JList(new OperationListModel());
        operationList.addMouseListener(new ModelItemListMouseListener() {
            private ActionList defaultActions;

            protected ActionList getDefaultActions() {
                if (defaultActions == null) {
                    defaultActions = new DefaultActionList();
                    defaultActions.addAction(createAddMockOperationDelegate());
                }

                return defaultActions;
            }
        });
        operationList.setCellRenderer(new OperationListCellRenderer());
        operationList.addKeyListener(new ModelItemListKeyListener() {

            @Override
            public ModelItem getModelItemAt(int ix) {
                return getModelItem().getMockOperationAt(ix);
            }
        });

        JScrollPane scrollPane = new JScrollPane(operationList);
        return UISupport.buildPanelWithToolbar(buildMockOperationListToolbar(), scrollPane);
    }

    private JComponent buildMockOperationListToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        toolbar.add(UISupport.createToolbarButton(createAddMockOperationDelegate()));

        return toolbar;
    }

    private SwingActionDelegate<?> createAddMockOperationDelegate() {
        String actionId = AddNewMockOperationAction.SOAPUI_ACTION_ID;
        String icon = "/mockOperation.gif";

        if (getModelItem() instanceof RestMockService) {
            actionId = AddEmptyRestMockResourceAction.SOAPUI_ACTION_ID;
            icon = "/addToRestMockAction.gif";
        }

        return SwingActionDelegate.createDelegate(actionId, getModelItem(), null, icon);
    }

    protected JComponent buildPropertiesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        propertiesTable = new PropertyHolderTable(getModelItem());
        panel.add(propertiesTable, BorderLayout.CENTER);
        return panel;
    }

    protected GroovyEditorComponent buildStartScriptPanel() {
        startGroovyEditor = new GroovyEditorComponent(new StartScriptGroovyEditorModel(), null);
        return startGroovyEditor;
    }

    protected GroovyEditorComponent buildStopScriptPanel() {
        stopGroovyEditor = new GroovyEditorComponent(new StopScriptGroovyEditorModel(), null);
        return stopGroovyEditor;
    }

    protected GroovyEditorComponent buildOnRequestScriptPanel() {
        onRequestGroovyEditor = new GroovyEditorComponent(new OnRequestScriptGroovyEditorModel(), null);
        return onRequestGroovyEditor;
    }

    protected GroovyEditorComponent buildAfterRequestScriptPanel() {
        afterRequestGroovyEditor = new GroovyEditorComponent(new AfterRequestScriptGroovyEditorModel(), null);
        return afterRequestGroovyEditor;
    }

    protected JPanel buildDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        descriptionArea = new JUndoableTextArea(getModelItem().getDescription());
        descriptionArea.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            public void update(Document document) {
                ((AbstractMockService) getModelItem()).setDescription(descriptionArea.getText());
            }
        });

        panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        UISupport.addTitledBorder(panel, "MockService Description");
        panel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        return panel;
    }

    protected JComponent buildLog() {
        JPanel panel = new JPanel(new BorderLayout());
        JXToolBar builder = UISupport.createToolbar();

        enableLogCheckBox = new JCheckBox(" ", true);
        enableLogCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                testLogList.setEnabled(enableLogCheckBox.isSelected());
                if (mockRunner != null) {
                    mockRunner.setLogEnabled(enableLogCheckBox.isSelected());
                }

                // border needs to be repainted..
                logScrollPane.repaint();
            }
        });
        enableLogCheckBox.setOpaque(false);

        builder.addFixed(enableLogCheckBox);
        builder.addRelatedGap();
        builder.addFixed(new JLabel("Enable"));
        builder.addRelatedGap();
        addLogActions(builder);

        builder.addGlue();
        builder.setBorder(BorderFactory.createEmptyBorder(2, 3, 3, 3));

        panel.add(builder, BorderLayout.NORTH);

        logListModel = new LogListModel();
        testLogList = new JList(logListModel);
        testLogList.setCellRenderer(new LogCellRenderer());
        // testLogList.setPrototypeCellValue( "Testing 123" );
        // testLogList.setFixedCellWidth( -1 );
        testLogList.addMouseListener(new LogListMouseListener());
        testLogList.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        logScrollPane = new JScrollPane(testLogList);

        panel.add(logScrollPane, BorderLayout.CENTER);

        return panel;
    }

    protected void addLogActions(JXToolBar builder) {
        builder.addFixed(UISupport.createToolbarButton(new ClearLogAction()));
        builder.addRelatedGap();
        builder.addFixed(UISupport.createToolbarButton(new SetLogOptionsAction()));
    }

    protected JXToolBar buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        runButton = createActionButton(new RunMockServiceAction(), true);
        stopButton = createActionButton(new StopMockServiceAction(), false);
        MockServiceType modelItem = getModelItem();

        AbstractSoapUIAction<MockServiceType> action = (AbstractSoapUIAction<MockServiceType>) new MockServiceOptionsAction();

        if (modelItem instanceof RestMockService) {
            action = (AbstractSoapUIAction<MockServiceType>) new RestMockServiceOptionsAction();
        }

        optionsButton = createActionButton(
                SwingActionDelegate.createDelegate(action, modelItem, null, "/preferences.png"),
                true);
        showWsdlButton = createActionButton(new ShowWsdlAction(), false);

        toolbar.addFixed(runButton);
        toolbar.addFixed(stopButton);

        if (modelItem instanceof WsdlMockService) {
            toolbar.addFixed(showWsdlButton);
        }
        toolbar.addFixed(optionsButton);

        toolbar.addGlue();

        runInfoLabel = new JLabel("", SwingConstants.RIGHT);
        runInfoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
        toolbar.addFixed(UISupport.setFixedSize(runInfoLabel, 205, 20));
        toolbar.addRelatedGap();

        progressBarWrapper.addToToolBar(toolbar);
        toolbar.addRelatedGap();

        toolbar.addFixed(createActionButton(new ShowOnlineHelpAction(getModelItem().getHelpUrl()), true));

        return toolbar;
    }

    public void startMockService() {
        if ((mockRunner != null && mockRunner.isRunning()) || SoapUI.getMockEngine().hasRunningMock(getModelItem())) {
            UISupport.showErrorMessage("MockService is already running");
        } else {
            if (mockRunner != null) {
                mockRunner.release();
            }

            try {
                getModelItem().start();
            } catch (Exception e) {
                UISupport.showErrorMessage(e);
                return;
            }
        }
    }

    private final class InternalMockRunListener extends MockRunListenerAdapter {
        @Override
        public void onMockRunnerStart(MockRunner runner) {
            mockRunner = (WsdlMockRunner) runner;
            mockRunner.setMaxResults(logListModel.getMaxSize());
            mockRunner.setLogEnabled(enableLogCheckBox.isSelected());

            progressBarWrapper.setIndeterminate(true);

            runButton.setEnabled(false);
            stopButton.setEnabled(true);
            optionsButton.setEnabled(false);
            showWsdlButton.setEnabled(true);

            runInfoLabel.setText("running on port " + getModelItem().getPort());
        }

        @Override
        public void onMockRunnerStop(MockRunner mockRunner) {
            progressBarWrapper.setIndeterminate(false);

            runButton.setEnabled(true);
            stopButton.setEnabled(false);
            optionsButton.setEnabled(true);
            showWsdlButton.setEnabled(false);

            runInfoLabel.setText("");
        }

        public void onMockResult(MockResult result) {
            if (logIsEnabled()) {
                logListModel.addElement(result);
            }
        }
    }

    public class OperationListModel extends AbstractListModel implements ListModel, MockServiceListener,
            PropertyChangeListener {
        private List<MockOperation> operations = new ArrayList<MockOperation>();

        public OperationListModel() {
            for (int c = 0; c < getModelItem().getMockOperationCount(); c++) {
                MockOperation mockOperation = getModelItem().getMockOperationAt(c);
                mockOperation.addPropertyChangeListener(this);

                operations.add(mockOperation);
            }

            getModelItem().addMockServiceListener(this);
        }

        public Object getElementAt(int arg0) {
            return operations.get(arg0);
        }

        public int getSize() {
            return operations.size();
        }

        public void mockOperationAdded(MockOperation operation) {
            operations.add(operation);
            operation.addPropertyChangeListener(this);
            fireIntervalAdded(this, operations.size() - 1, operations.size() - 1);
        }

        public void mockOperationRemoved(MockOperation operation) {
            int ix = operations.indexOf(operation);
            operations.remove(ix);
            operation.removePropertyChangeListener(this);
            fireIntervalRemoved(this, ix, ix);
        }

        public void mockResponseAdded(MockResponse request) {
        }

        public void mockResponseRemoved(MockResponse request) {
        }

        public void propertyChange(PropertyChangeEvent arg0) {
            if (arg0.getPropertyName().equals(WsdlMockOperation.NAME_PROPERTY)) {
                int ix = operations.indexOf(arg0.getSource());
                fireContentsChanged(this, ix, ix);
            }
        }

        public void release() {
            for (MockOperation operation : operations) {
                operation.removePropertyChangeListener(this);
            }

            getModelItem().removeMockServiceListener(this);
        }
    }

    private final static class OperationListCellRenderer extends JLabel implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            MockOperation testStep = (MockOperation) value;
            setText(testStep.getName());
            setIcon(testStep.getIcon());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

            return this;
        }
    }

    public class RunMockServiceAction extends AbstractAction {
        public RunMockServiceAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/submit_request.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Starts this MockService on the specified port and endpoint");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("alt ENTER"));
        }

        public void actionPerformed(ActionEvent arg0) {

            if (getModelItem() instanceof WsdlMockService) {
                Analytics.trackAction(SoapUIActions.START_SOAP_MOCK.getActionName());
            } else if (getModelItem() instanceof RestMockService) {
                Analytics.trackAction(SoapUIActions.START_REST_MOCK.getActionName());
            }

            startMockService();
        }
    }

    public class ShowWsdlAction extends AbstractAction {
        public ShowWsdlAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/interface.png"));
            putValue(Action.SHORT_DESCRIPTION, "Opens the root WSDL page in a browser");
        }

        public void actionPerformed(ActionEvent arg0) {
            WsdlMockService mockService = (WsdlMockService) getModelItem();
            Tools.openURL(mockService.getLocalEndpoint() + "?WSDL");
        }
    }

    public class StopMockServiceAction extends AbstractAction {
        public StopMockServiceAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/cancel_request.png"));
            putValue(Action.SHORT_DESCRIPTION, "Stops this MockService on the specified port and endpoint");
        }

        public void actionPerformed(ActionEvent arg0) {
            if (mockRunner == null) {
                UISupport.showErrorMessage("MockService is not running");
            } else {
                mockRunner.stop();
                mockRunner.release();
                mockRunner = null;
            }
        }
    }

    private static final class LogCellRenderer extends JLabel implements ListCellRenderer {
        public LogCellRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            setText(String.valueOf(value));

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setEnabled(list.isEnabled());

            return this;
        }
    }

    private long getDefaultMaxSize() {
        return getModelItem().getSettings().getLong(LogListModel.class.getName() + "@maxSize", 100);
    }

    protected long getMaxLogSize() {
        if (logListModel != null) {
            return logListModel.getMaxSize();
        } else {
            return getDefaultMaxSize();
        }
    }

    protected void setMaxLogSize(long size) {
        logListModel.setMaxSize(size);
        if (mockRunner != null) {
            mockRunner.setMaxResults(logListModel.getMaxSize());
        }
    }

    @SuppressWarnings("unchecked")
    private class LogListModel extends AbstractListModel {
        private List<MockResult> elements = Collections.synchronizedList(new TreeList());
        private long maxSize;

        public LogListModel() {
            maxSize = getDefaultMaxSize();
        }

        public void addElement(MockResult result) {
            elements.add(result);
            fireIntervalAdded(this, elements.size() - 1, elements.size() - 1);

            synchronized (this) {
                while (elements.size() > maxSize) {
                    removeElementAt(0);
                }
            }
        }

        public Object getElementAt(int index) {
            try {
                if (elements.size() <= index) {
                    return null;
                }

                return elements.get(index);
            } catch (Throwable t) {
                return null;
            }
        }

        public void removeElementAt(int index) {
            elements.remove(index);
            fireIntervalRemoved(this, index, index);
        }

        public void clear() {
            synchronized (this) {
                int sz = elements.size();
                if (sz > 0) {
                    elements.clear();
                    fireIntervalRemoved(this, 0, sz - 1);
                }
            }
        }

        public int getSize() {
            return elements.size();
        }

        public long getMaxSize() {
            return maxSize;
        }

        public synchronized void setMaxSize(long l) {
            this.maxSize = l;

            while (elements.size() > 0 && elements.size() > maxSize) {
                removeElementAt(0);
            }

            getModelItem().getSettings().setLong(LogListModel.class.getName() + "@maxSize", maxSize);
        }
    }

    private class SetLogOptionsAction extends AbstractAction {
        public SetLogOptionsAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/preferences.png"));
            putValue(Action.SHORT_DESCRIPTION, "Sets MockService Log Options");
        }

        public void actionPerformed(ActionEvent e) {
            String s = UISupport.prompt("Enter maximum number of rows for MockService Log", "Log Options",
                    String.valueOf(logListModel.getMaxSize()));
            if (s != null) {
                try {
                    long newMaxSize = Long.parseLong(s);
                    if (newMaxSize > 0) {
                        setMaxLogSize(newMaxSize);
                    }
                } catch (NumberFormatException e1) {
                }
            }
        }
    }

    private class ClearLogAction extends AbstractAction {
        public ClearLogAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/clear.png"));
            putValue(Action.SHORT_DESCRIPTION, "Clears the MockService Log");
        }

        public void actionPerformed(ActionEvent e) {
            logListModel.clear();
            if (mockRunner != null) {
                mockRunner.clearResults();
            }
        }
    }

    /**
     * Mouse Listener for triggering default action and showing popup for log
     * list items
     *
     * @author Ole.Matzura
     */

    private final class LogListMouseListener extends AbstractListMouseListener {
        @Override
        protected ActionList getActionsForRow(JList list, int row) {
            MockResult result = (MockResult) logListModel.getElementAt(row);
            return result == null ? null : result.getActions();
        }
    }

    private class StartScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        public StartScriptGroovyEditorModel() {
            super(new String[]{"log", "context", "mockRunner"}, WsdlMockServiceDesktopPanel.this.getModelItem(),
                    "Start");
        }

        public String getScript() {
            return WsdlMockServiceDesktopPanel.this.getModelItem().getStartScript();
        }

        public void setScript(String text) {
            WsdlMockServiceDesktopPanel.this.getModelItem().setStartScript(text);
        }

        @Override
        public Action createRunAction() {
            return new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        WsdlMockRunContext context = mockRunner == null ? new WsdlMockRunContext(
                                WsdlMockServiceDesktopPanel.this.getModelItem(), null) : mockRunner.getMockContext();
                        WsdlMockServiceDesktopPanel.this.getModelItem().runStartScript(context, mockRunner);
                    } catch (Exception e1) {
                        UISupport.showErrorMessage(e1);
                    }
                }
            };
        }
    }

    private class StopScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        public StopScriptGroovyEditorModel() {
            super(new String[]{"log", "context", "mockRunner"}, WsdlMockServiceDesktopPanel.this.getModelItem(),
                    "Stop");
        }

        public String getScript() {
            return WsdlMockServiceDesktopPanel.this.getModelItem().getStopScript();
        }

        public void setScript(String text) {
            WsdlMockServiceDesktopPanel.this.getModelItem().setStopScript(text);
        }

        @Override
        public Action createRunAction() {
            return new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        WsdlMockRunContext context = mockRunner == null ? new WsdlMockRunContext(
                                WsdlMockServiceDesktopPanel.this.getModelItem(), null) : mockRunner.getMockContext();
                        WsdlMockServiceDesktopPanel.this.getModelItem().runStopScript(context, mockRunner);
                    } catch (Exception e1) {
                        UISupport.showErrorMessage(e1);
                    }
                }
            };
        }
    }

    private class OnRequestScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        public OnRequestScriptGroovyEditorModel() {
            super(new String[]{"log", "context", "mockRequest", "mockRunner"}, WsdlMockServiceDesktopPanel.this
                    .getModelItem(), "OnRequest");
        }

        public String getScript() {
            return WsdlMockServiceDesktopPanel.this.getModelItem().getOnRequestScript();
        }

        public void setScript(String text) {
            WsdlMockServiceDesktopPanel.this.getModelItem().setOnRequestScript(text);
        }

        @Override
        public Action createRunAction() {
            return new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        WsdlMockRunContext context = mockRunner == null ? new WsdlMockRunContext(
                                WsdlMockServiceDesktopPanel.this.getModelItem(), null) : mockRunner.getMockContext();
                        WsdlMockServiceDesktopPanel.this.getModelItem().runOnRequestScript(context, null);
                    } catch (Exception e1) {
                        UISupport.showErrorMessage(e1);
                    }
                }
            };
        }
    }

    private class AfterRequestScriptGroovyEditorModel extends AbstractGroovyEditorModel {
        public AfterRequestScriptGroovyEditorModel() {
            super(new String[]{"log", "context", "mockResult", "mockRunner"}, WsdlMockServiceDesktopPanel.this
                    .getModelItem(), "AfterRequest");
        }

        public String getScript() {
            return WsdlMockServiceDesktopPanel.this.getModelItem().getAfterRequestScript();
        }

        public void setScript(String text) {
            WsdlMockServiceDesktopPanel.this.getModelItem().setAfterRequestScript(text);
        }

        @Override
        public Action createRunAction() {
            return new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        WsdlMockRunContext context = mockRunner == null ? new WsdlMockRunContext(
                                WsdlMockServiceDesktopPanel.this.getModelItem(), null) : mockRunner.getMockContext();
                        WsdlMockServiceDesktopPanel.this.getModelItem().runAfterRequestScript(context, null);
                    } catch (Exception e1) {
                        UISupport.showErrorMessage(e1);
                    }
                }
            };
        }
    }

    @Override
    protected void renameModelItem() {
        SoapUI.getActionRegistry().performAction("RenameMockServiceAction", getModelItem(), null);
    }

    @Override
    protected void cloneModelItem() {
        SoapUI.getActionRegistry().performAction("CloneMockServiceAction", getModelItem(), null);
    }

}
