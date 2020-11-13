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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.config.JdbcRequestTestStepConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.components.ResponseMessageXmlEditor;
import com.eviware.soapui.impl.support.panels.AbstractHttpRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.DefaultPropertyHolderTableModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.submit.transports.http.DocumentContent;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepWithProperties;
import com.eviware.soapui.impl.wsdl.teststeps.actions.AddAssertionAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.Submit.Status;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DateUtil;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.ListDataChangeListener;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.actions.ChangeSplitPaneOrientationAction;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JEditorStatusBarWithProgress;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;
import com.eviware.soapui.support.jdbc.JdbcUtils;
import com.eviware.soapui.support.log.JLogList;
import com.eviware.soapui.support.propertyexpansion.PropertyExpansionPopupListener;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JdbcRequestTestStepDesktopPanel extends ModelItemDesktopPanel<JdbcRequestTestStep> implements
        SubmitListener {
    private final static Logger log = LogManager.getLogger(AbstractHttpRequestDesktopPanel.class);
    private final static MessageSupport messages = MessageSupport.getMessages(JdbcRequestTestStepDesktopPanel.class);
    protected JPanel configPanel;
    private JButton addAssertionButton;
    protected JInspectorPanel inspectorPanel;
    protected JdbcRequestTestStep jdbcRequestTestStep;
    protected JComponentInspector<?> assertionInspector;
    protected AssertionsPanel assertionsPanel;
    private InternalAssertionsListener assertionsListener = new InternalAssertionsListener();
    private InternalTestMonitorListener testMonitorListener = new InternalTestMonitorListener();
    protected JComponent requestEditor;
    protected ModelItemXmlEditor<?, ?> responseEditor;
    protected JPanel panel;
    protected SimpleForm configForm;
    protected static final String DRIVER_FIELD = "Driver";
    protected static final String CONNSTR_FIELD = "Connection String";
    protected static final String PASS_FIELD = "Password";
    public static final String QUERY_FIELD = "SQL Query";
    protected static final String STOREDPROCEDURE_FIELD = "Stored Procedure";
    protected static final String RESULT_COLUMNS_NAMES_TO_UPPER_CASE = messages.get("JdbcRequestTestStepDesktopPanel.ResultColumnsToUpperCase.Name");
    protected static final String DATA_CONNECTION_FIELD = "Connection";

    protected static final String QUERY_ELEMENT = "query";
    protected static final String STOREDPROCEDURE_ELEMENT = "stored-procedure";
    protected Connection connection;
    protected RSyntaxTextArea queryArea;
    protected JCheckBox isStoredProcedureCheckBox;
    protected JCheckBox resultColumnsNamesToUpperCaseCheckBox;
    protected JTextField driverTextField;
    protected JTextField connStrTextField;
    protected JButton testConnectionButton;
    protected JPasswordField passField;
    private Submit submit;
    private SubmitAction submitAction;
    protected JButton submitButton;
    private JToggleButton tabsButton;
    private JTabbedPane requestTabs;
    private JPanel requestTabPanel;
    private boolean responseHasFocus;
    private JSplitPane requestSplitPane;
    private JEditorStatusBarWithProgress statusBar;
    private JButton cancelButton;
    private JButton splitButton;
    protected JComponent propertiesTableComponent;
    private JComponentInspector<?> logInspector;
    protected JLogList logArea;
    private long startTime;
    protected JButton reconfigureConnPropertiesButton;
    protected PropertyHolderTable propertyHolderTable;
    protected JdbcRequestTestStepConfig jdbcRequestTestStepConfig;

    public JdbcRequestTestStepDesktopPanel(JdbcRequestTestStep modelItem) {
        super(modelItem);
        jdbcRequestTestStep = modelItem;
        initConfig();
        initContent();

        SoapUI.getTestMonitor().addTestMonitorListener(testMonitorListener);
        setEnabled(!SoapUI.getTestMonitor().hasRunningTest(jdbcRequestTestStep.getTestCase()));

        jdbcRequestTestStep.addAssertionsListener(assertionsListener);
    }

    // added this again cause without it connection is set to none whenever jdbc
    // test step is reopened
    protected void initConfig() {
        jdbcRequestTestStepConfig = jdbcRequestTestStep.getJdbcRequestTestStepConfig();
    }

    protected JComponent buildContent() {
        requestSplitPane = UISupport.createHorizontalSplit();
        requestSplitPane.setResizeWeight(0.5);
        requestSplitPane.setBorder(null);

        JComponent content;
        submitAction = new SubmitAction();
        submitButton = createActionButton(submitAction, true);
        submitButton.setEnabled(enableSubmit());

        cancelButton = createActionButton(new CancelAction(), false);
        tabsButton = new JToggleButton(new ChangeToTabsAction());
        tabsButton.setPreferredSize(UISupport.TOOLBAR_BUTTON_DIMENSION);
        splitButton = createActionButton(new ChangeSplitPaneOrientationAction(requestSplitPane), true);

        addAssertionButton = UISupport.createToolbarButton(new AddAssertionAction(jdbcRequestTestStep));
        addAssertionButton.setEnabled(true);

        requestTabs = new JTabbedPane();
        requestTabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        int ix = requestTabs.getSelectedIndex();
                        if (ix == 0) {
                            requestEditor.requestFocus();
                        } else if (ix == 1 && responseEditor != null) {
                            responseEditor.requestFocus();
                        }
                    }
                });
            }
        });

        addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (requestTabs.getSelectedIndex() == 1 || responseHasFocus) {
                    responseEditor.requestFocusInWindow();
                } else {
                    requestEditor.requestFocusInWindow();
                }
            }
        });

        requestTabPanel = UISupport.createTabPanel(requestTabs, true);

        requestEditor = buildRequestConfigPanel();
        responseEditor = buildResponseEditor();
        if (jdbcRequestTestStep.getSettings().getBoolean(UISettings.START_WITH_REQUEST_TABS)) {
            requestTabs.addTab("Request", requestEditor);
            if (responseEditor != null) {
                requestTabs.addTab("Response", responseEditor);
            }
            tabsButton.setSelected(true);
            splitButton.setEnabled(false);

            content = requestTabPanel;
        } else {
            requestSplitPane.setTopComponent(requestEditor);
            requestSplitPane.setBottomComponent(responseEditor);
            requestSplitPane.setDividerLocation(0.5);
            content = requestSplitPane;
        }

        inspectorPanel = JInspectorPanelFactory.build(content);
        inspectorPanel.setDefaultDividerLocation(0.7F);
        add(buildToolbar(), BorderLayout.NORTH);
        add(inspectorPanel.getComponent(), BorderLayout.CENTER);
        assertionsPanel = buildAssertionsPanel();

        assertionInspector = new JComponentInspector<JComponent>(assertionsPanel, "Assertions ("
                + getModelItem().getAssertionCount() + ")", "Assertions for this Request", true);

        inspectorPanel.addInspector(assertionInspector);
        logInspector = new JComponentInspector<JComponent>(buildLogPanel(), "Request Log (0)", "Log of requests", true);
        inspectorPanel.addInspector(logInspector);
        inspectorPanel.setCurrentInspector("Assertions");

        updateStatusIcon();

        return inspectorPanel.getComponent();
    }

    protected JComponent buildRequestConfigPanel() {
        configPanel = UISupport.addTitledBorder(new JPanel(new BorderLayout()), "Configuration");
        if (panel == null) {
            panel = new JPanel(new BorderLayout());
            configForm = new SimpleForm();
            createSimpleJdbcConfigForm();
            addStoreProcedureChangeListener();
            addResultColumnsNamesToUpperCaseChangeListener();

            panel.add(new JScrollPane(configForm.getPanel()));
        }
        configPanel.add(panel, BorderLayout.CENTER);

        propertiesTableComponent = buildProperties();
        JSplitPane split = UISupport.createVerticalSplit(propertiesTableComponent, configPanel);
        split.setDividerLocation(120);
        split.setPreferredSize(new Dimension(330, 500));

        return new JScrollPane(split);

    }

    protected void initContent() {
        jdbcRequestTestStep.getJdbcRequest().addSubmitListener(this);

        add(buildContent(), BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildStatusLabel(), BorderLayout.SOUTH);

        setPreferredSize(new Dimension(600, 500));

        addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (requestTabs.getSelectedIndex() == 1 || responseHasFocus) {
                    responseEditor.requestFocusInWindow();
                } else {
                    requestEditor.requestFocusInWindow();
                }
            }
        });
    }

    protected JComponent buildStatusLabel() {
        statusBar = new JEditorStatusBarWithProgress();
        statusBar.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));

        return statusBar;
    }

    protected JComponent buildProperties() {
        propertyHolderTable = new PropertyHolderTable(getModelItem()) {
            @Override
            protected DefaultPropertyHolderTableModel getPropertyHolderTableModel() {
                return new DefaultPropertyHolderTableModel(holder) {
                    @Override
                    public String[] getPropertyNames() {
                        List<String> propertyNamesList = new ArrayList<String>();
                        for (String name : holder.getPropertyNames()) {
                            if (name.equals(WsdlTestStepWithProperties.RESPONSE_AS_XML)) {
                                continue;
                            }
                            propertyNamesList.add(name);
                        }
                        return propertyNamesList.toArray(new String[propertyNamesList.size()]);
                    }
                };
            }
        };

        JUndoableTextField textField = new JUndoableTextField(true);

        PropertyExpansionPopupListener.enable(textField, getModelItem());
        propertyHolderTable.getPropertiesTable().setDefaultEditor(String.class, new DefaultCellEditor(textField));

        return propertyHolderTable;
    }

    public PropertyHolderTable getPropertyHolderTable() {
        return propertyHolderTable;
    }

    //Used from SoapUI Pro
    public void setPropertyHolderTable(StringToStringMap preparedProperties) {
        // first remove the old content
        String[] names = propertyHolderTable.getHolder().getPropertyNames();
        if (names.length > 0) {
            for (String propertyName : names) {
                ((MutableTestPropertyHolder) propertyHolderTable.getHolder()).removeProperty(propertyName);
            }
        }
        propertyHolderTable.getPropertiesTable().removeAll();
        if (preparedProperties != null) {
            int i = 0;
            for (String key : preparedProperties.keySet()) {
                String value = preparedProperties.get(key);
                ((MutableTestPropertyHolder) propertyHolderTable.getHolder()).addProperty(key);
                ((MutableTestPropertyHolder) propertyHolderTable.getHolder()).setPropertyValue(key, value);
                i++;
            }
        }
    }

    protected JComponent buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        toolbar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        toolbar.addFixed(submitButton);
        toolbar.add(cancelButton);
        toolbar.addFixed(addAssertionButton);

        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(tabsButton);
        toolbar.add(splitButton);
        toolbar.addFixed(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.JDBCSTEPEDITOR_HELP_URL)));
        return toolbar;

    }

    public JdbcRequestTestStep getJdbcRequestTestStep() {
        return jdbcRequestTestStep;
    }

    public void setQuery(String query) {
        if (configForm != null) {
            configForm.setComponentValue(QUERY_FIELD, query);
            jdbcRequestTestStep.setQuery(query);
        } else {
            // this.query = query;
            jdbcRequestTestStep.setQuery(query);
        }
    }

    protected JComponent buildLogPanel() {
        logArea = new JLogList("Request Log");

        logArea.getLogList().getModel().addListDataListener(new ListDataChangeListener() {
            public void dataChanged(ListModel model) {
                logInspector.setTitle("Request Log (" + model.getSize() + ")");
            }
        });

        return logArea;
    }

    protected AssertionsPanel buildAssertionsPanel() {
        return new JdbcAssertionsPanel(jdbcRequestTestStep) {
        };
    }

    protected class JdbcAssertionsPanel extends AssertionsPanel {
        public JdbcAssertionsPanel(Assertable assertable) {
            super(assertable);
        }
    }

    protected void createSimpleJdbcConfigForm() {
        configForm.addSpace(5);
        configForm.setDefaultTextFieldColumns(SimpleForm.LONG_TEXT_FIELD_COLUMNS);

        driverTextField = configForm.appendTextField(DRIVER_FIELD, "JDBC Driver to use");
        driverTextField.setText(jdbcRequestTestStep.getDriver());
        PropertyExpansionPopupListener.enable(driverTextField, jdbcRequestTestStep);
        addDriverDocumentListener();

        connStrTextField = configForm.appendTextField(CONNSTR_FIELD, "JDBC Driver Connection String");
        connStrTextField.setText(jdbcRequestTestStep.getConnectionString());
        PropertyExpansionPopupListener.enable(connStrTextField, jdbcRequestTestStep);
        addConnStrDocumentListener();

        passField = configForm.appendPasswordField(PASS_FIELD, "Connection string Password");
        passField.setVisible(false);
        passField.setText(jdbcRequestTestStep.getPassword());
        addPasswordDocumentListener();

        reconfigureConnPropertiesButton = new JButton();
        configForm.addLeftComponent(reconfigureConnPropertiesButton);
        reconfigureConnPropertiesButton.setVisible(false);
        configForm.appendSeparator();

        testConnectionButton = configForm.appendButton("TestConnection", "Test selected database connection");
        testConnectionButton.setAction(new TestConnectionAction());
        testConnectionButton.setEnabled(enableTestConnection());
        submitButton.setEnabled(enableSubmit());

        queryArea = SyntaxEditorUtil.createDefaultSQLSyntaxTextArea();
        PropertyExpansionPopupListener.enable(queryArea, jdbcRequestTestStep);
        queryArea.setText(jdbcRequestTestStep.getQuery());
        JScrollPane scrollPane = new JScrollPane(queryArea);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        configForm.append(QUERY_FIELD, scrollPane);
        queryArea.getDocument().addDocumentListener(new DocumentListenerAdapter() {

            @Override
            public void update(Document document) {
                jdbcRequestTestStep.setQuery(queryArea.getText());
                submitButton.setEnabled(enableSubmit());
            }
        });

        isStoredProcedureCheckBox = configForm.appendCheckBox(STOREDPROCEDURE_FIELD,
                "Select if this is a stored procedure", jdbcRequestTestStep.isStoredProcedure());
        resultColumnsNamesToUpperCaseCheckBox = configForm.appendCheckBox(RESULT_COLUMNS_NAMES_TO_UPPER_CASE,
                messages.get("JdbcRequestTestStepDesktopPanel.ResultColumnsToUpperCase.Description"), jdbcRequestTestStep.isConvertColumnNamesToUpperCase());
    }

    protected void addPasswordDocumentListener() {
        passField.getDocument().addDocumentListener(new DocumentListenerAdapter() {

            @Override
            public void update(Document document) {
                jdbcRequestTestStep.setPassword(configForm.getComponentValue(PASS_FIELD));
                testConnectionButton.setEnabled(enableTestConnection());
                submitButton.setEnabled(enableSubmit());
            }
        });
    }

    protected void addConnStrDocumentListener() {
        connStrTextField.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            @Override
            public void update(Document document) {
                jdbcRequestTestStep.setConnectionString(configForm.getComponentValue(CONNSTR_FIELD));
                testConnectionButton.setEnabled(enableTestConnection());
                submitButton.setEnabled(enableSubmit());
            }
        });
    }

    protected void addDriverDocumentListener() {
        driverTextField.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            @Override
            public void update(Document document) {
                jdbcRequestTestStep.setDriver(configForm.getComponentValue(DRIVER_FIELD));
                testConnectionButton.setEnabled(enableTestConnection());
                submitButton.setEnabled(enableSubmit());
            }
        });
    }

    protected void addStoreProcedureChangeListener() {
        isStoredProcedureCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                jdbcRequestTestStep.setStoredProcedure(((JCheckBox) arg0.getSource()).isSelected());
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        if (UISupport.confirm("Would you like to empty current sql and properties?",
                                "Reset query and properties")) {
                            queryArea.setText("");
                            ((JdbcRequestTestStep) getPropertyHolderTable().getHolder()).removeAllProperties();
                        }
                    }
                });
            }
        });
    }

    protected void addResultColumnsNamesToUpperCaseChangeListener() {
        resultColumnsNamesToUpperCaseCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                jdbcRequestTestStep.setConvertColumnNamesToUpperCase(((JCheckBox) arg0.getSource()).isSelected());
            }
        });
    }

    protected boolean enableTestConnection() {
        if (StringUtils.isNullOrEmpty(jdbcRequestTestStep.getDriver())
                || StringUtils.isNullOrEmpty(jdbcRequestTestStep.getConnectionString())
                || (JdbcRequestTestStep.isNeededPassword(jdbcRequestTestStep.getConnectionString()) && StringUtils
                .isNullOrEmpty(jdbcRequestTestStep.getPassword()))) {
            return false;
        } else {
            if (jdbcRequestTestStep.getConnectionString().contains(JdbcRequestTestStep.PASS_TEMPLATE)) {
                return !StringUtils.isNullOrEmpty(jdbcRequestTestStep.getPassword());
            } else {
                return true;
            }
        }
    }

    protected boolean enableSubmit() {
        return enableTestConnection() && !StringUtils.isNullOrEmpty(jdbcRequestTestStep.getQuery());
    }

    protected ModelItemXmlEditor<?, ?> buildResponseEditor() {
        return new JdbcResponseMessageEditor();
    }

    public class JdbcResponseMessageEditor extends ResponseMessageXmlEditor<JdbcRequestTestStep, JdbcResponseDocument> {
        public JdbcResponseMessageEditor() {
            super(new JdbcResponseDocument(), jdbcRequestTestStep);
        }
    }

    public boolean dependsOn(ModelItem modelItem) {
        return modelItem == getModelItem() || modelItem == getModelItem().getTestCase()
                || modelItem == getModelItem().getTestCase().getTestSuite()
                || modelItem == getModelItem().getTestCase().getTestSuite().getProject();
    }

    public boolean onClose(boolean canCancel) {
        configPanel.removeAll();

        SoapUI.getTestMonitor().removeTestMonitorListener(testMonitorListener);
        jdbcRequestTestStep.removeAssertionsListener(assertionsListener);
        jdbcRequestTestStep.getJdbcRequest().removeSubmitListener(this);

        responseEditor.release();
        assertionsPanel.release();
        inspectorPanel.release();
        propertyHolderTable.release();

        return release();
    }

    public class JdbcResponseDocument extends AbstractXmlDocument implements PropertyChangeListener {
        public JdbcResponseDocument() {
            jdbcRequestTestStep.addPropertyChangeListener(JdbcRequestTestStep.RESPONSE_PROPERTY, this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            fireContentChanged();
        }

        @Override
        public void setDocumentContent(DocumentContent documentContent) {
            if (jdbcRequestTestStep.getJdbcRequest().getResponse() != null) {
                jdbcRequestTestStep.getJdbcRequest().getResponse().setContentAsString(documentContent.getContentAsString());
            }
        }

        public void release() {
            super.release();
            jdbcRequestTestStep.removePropertyChangeListener(JdbcRequestTestStep.RESPONSE_PROPERTY, this);
        }

        @Nonnull
        @Override
        public DocumentContent getDocumentContent(Format format) {
            JdbcResponse response = jdbcRequestTestStep.getJdbcRequest().getResponse();
            return new DocumentContent(response == null ? null : response.getContentType(), response == null ? null : response.getContentAsString());
        }
    }

    protected class TestConnectionAction extends AbstractAction {
        public TestConnectionAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
            putValue(Action.SHORT_DESCRIPTION, "Test the current Connection");

            setEnabled(false);
        }

        public void actionPerformed(ActionEvent arg0) {
            try {

                PropertyExpansionContext context = new DefaultPropertyExpansionContext(getModelItem());
                JdbcUtils.initConnection(context, jdbcRequestTestStep.getDriver(),
                        jdbcRequestTestStep.getConnectionString(), jdbcRequestTestStep.getPassword());
                UISupport.showInfoMessage("The Connection Successfully Tested");
            } catch (SoapUIException e) {
                SoapUI.logError(e);
                UISupport.showErrorMessage(e.toString());
            } catch (SQLException e) {
                SoapUI.logError(e);
                UISupport.showErrorMessage("Can't get the Connection for specified properties; " + e.toString());
            }
        }
    }

    private class InternalTestMonitorListener extends TestMonitorListenerAdapter {
        public void loadTestFinished(LoadTestRunner runner) {
            setEnabled(!SoapUI.getTestMonitor().hasRunningTest(getModelItem().getTestCase()));
        }

        public void loadTestStarted(LoadTestRunner runner) {
            if (runner.getLoadTest().getTestCase() == getModelItem().getTestCase()) {
                setEnabled(false);
            }
        }

        public void securityTestFinished(SecurityTestRunner runner) {
            setEnabled(!SoapUI.getTestMonitor().hasRunningTest(getModelItem().getTestCase()));
        }

        public void securityTestStarted(SecurityTestRunner runner) {
            if (runner.getSecurityTest().getTestCase() == getModelItem().getTestCase()) {
                setEnabled(false);
            }
        }

        public void testCaseFinished(TestCaseRunner runner) {
            setEnabled(!SoapUI.getTestMonitor().hasRunningTest(getModelItem().getTestCase()));
        }

        public void testCaseStarted(TestCaseRunner runner) {
            if (runner.getTestCase() == getModelItem().getTestCase()) {
                setEnabled(false);
            }
        }
    }

    public class SubmitAction extends AbstractAction {
        public SubmitAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/submit_request.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Submit request to specified database");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("alt ENTER"));
        }

        public void actionPerformed(ActionEvent e) {
            onSubmit();
        }
    }

    protected void onSubmit() {
        if (submit != null && submit.getStatus() == Submit.Status.RUNNING) {
            if (UISupport.confirm("Cancel current request?", "Submit Request")) {
                submit.cancel();
            } else {
                return;
            }
        }

        try {
            submit = doSubmit();
        } catch (SubmitException e1) {
            SoapUI.logError(e1);
        }
    }

    protected Submit doSubmit() throws SubmitException {
        Analytics.trackAction(SoapUIActions.RUN_TEST_STEP_FROM_PANEL, "StepType", "JDBC");
        return jdbcRequestTestStep.getJdbcRequest().submit(new WsdlTestRunContext(getModelItem()), true);
    }

    private final class ChangeToTabsAction extends AbstractAction {
        public ChangeToTabsAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/toggle_tabs.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Toggles to tab-based layout");
        }

        public void actionPerformed(ActionEvent e) {
            if (splitButton.isEnabled()) {
                splitButton.setEnabled(false);
                removeContent(requestSplitPane);
                setContent(requestTabPanel);
                requestTabs.addTab("Request", requestEditor);

                if (responseEditor != null) {
                    requestTabs.addTab("Response", responseEditor);
                }

                if (responseHasFocus) {
                    requestTabs.setSelectedIndex(1);
                    requestEditor.requestFocus();
                }
                requestTabs.repaint();
            } else {
                int selectedIndex = requestTabs.getSelectedIndex();

                splitButton.setEnabled(true);
                removeContent(requestTabPanel);
                setContent(requestSplitPane);
                requestSplitPane.setTopComponent(requestEditor);
                if (responseEditor != null) {
                    requestSplitPane.setBottomComponent(responseEditor);
                }
                requestSplitPane.setDividerLocation(0.5);

                if (selectedIndex == 0 || responseEditor == null) {
                    requestEditor.requestFocus();
                } else {
                    responseEditor.requestFocus();
                }
                requestSplitPane.repaint();
            }

            revalidate();
        }
    }

    public void setContent(JComponent content) {
        inspectorPanel.setContentComponent(content);
    }

    public void removeContent(JComponent content) {
        inspectorPanel.setContentComponent(null);
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            super();
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/cancel_request.png"));
            putValue(Action.SHORT_DESCRIPTION, "Aborts ongoing request");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("alt X"));
        }

        public void actionPerformed(ActionEvent e) {
            onCancel();
        }
    }

    protected void onCancel() {
        if (submit == null) {
            return;
        }

        cancelButton.setEnabled(false);
        submit.cancel();
        setEnabled(true);
        submit = null;
    }

    public void setEnabled(boolean enabled) {
        if (responseEditor != null) {
            responseEditor.setEditable(enabled);
        }

        submitButton.setEnabled(enabled);
        addAssertionButton.setEnabled(enabled);
        driverTextField.setEnabled(enabled);
        connStrTextField.setEnabled(enabled);
        passField.setEnabled(enabled);
        queryArea.setEnabled(enabled);
        queryArea.setEditable(enabled);
        isStoredProcedureCheckBox.setEnabled(enabled);
        propertiesTableComponent.setEnabled(enabled);
        testConnectionButton.setEnabled(enabled);

        statusBar.setIndeterminate(!enabled);
    }

    public void afterSubmit(Submit submit, SubmitContext context) {
        if (submit.getRequest() != jdbcRequestTestStep.getJdbcRequest()) {
            return;
        }

        Status status = submit.getStatus();
        JdbcResponse response = (JdbcResponse) submit.getResponse();
        if (status == Status.FINISHED) {
            jdbcRequestTestStep.setResponse(response, context);
        }

        cancelButton.setEnabled(false);
        setEnabled(true);

        String message = null;
        String infoMessage = null;
        String requestName = jdbcRequestTestStep.getName();
        String rawSql = "";

        if (status == Status.CANCELED) {
            message = "CANCELED";
            infoMessage = "[" + requestName + "] - CANCELED";
        } else {
            if (status == Status.ERROR || response == null) {
                message = "Error getting response; " + submit.getError();
                infoMessage = "Error getting response for [" + requestName + "]; " + submit.getError();
            } else {
                message = "response time: " + response.getTimeTaken() + "ms (" + response.getContentLength() + " bytes)";
                infoMessage = "Got response for [" + requestName + "] in " + response.getTimeTaken() + "ms ("
                        + response.getContentLength() + " bytes)";
                rawSql = ((JdbcSubmit) submit).getRawSql();

                if (!splitButton.isEnabled()) {
                    requestTabs.setSelectedIndex(1);
                }

                responseEditor.requestFocus();
            }
        }
        if (!StringUtils.isNullOrEmpty(rawSql)) {
            logMessages("Sql executed: " + rawSql, rawSql);
        }
        logMessages(message, infoMessage);

        if (getModelItem().getSettings().getBoolean(UISettings.AUTO_VALIDATE_RESPONSE)) {
            responseEditor.getSourceEditor().validate();
        }

        JdbcRequestTestStepDesktopPanel.this.submit = null;

        updateStatusIcon();
    }

    protected void logMessages(String message, String infoMessage) {
        log.info(infoMessage);
        statusBar.setInfo(message);
        logArea.addLine(DateUtil.formatFull(new Date(startTime)) + " - " + message);
    }

    public boolean beforeSubmit(Submit submit, SubmitContext context) {
        if (submit.getRequest() != jdbcRequestTestStep.getJdbcRequest()) {
            return true;
        }

        setEnabled(false);
        cancelButton.setEnabled(JdbcRequestTestStepDesktopPanel.this.submit != null);
        startTime = System.currentTimeMillis();
        return true;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);

        if (evt.getPropertyName().equals(JdbcRequestTestStep.STATUS_PROPERTY)) {
            updateStatusIcon();
        }
    }

    private final class InternalAssertionsListener implements AssertionsListener {
        public void assertionAdded(TestAssertion assertion) {
            assertionInspector.setTitle("Assertions (" + getModelItem().getAssertionCount() + ")");
        }

        public void assertionRemoved(TestAssertion assertion) {
            assertionInspector.setTitle("Assertions (" + getModelItem().getAssertionCount() + ")");
        }

        public void assertionMoved(TestAssertion assertion, int ix, int offset) {
            assertionInspector.setTitle("Assertions (" + getModelItem().getAssertionCount() + ")");
        }
    }

    private void updateStatusIcon() {
        AssertionStatus status = jdbcRequestTestStep.getAssertionStatus();
        switch (status) {
            case FAILED: {
                assertionInspector.setIcon(UISupport.createImageIcon("/failed_assertion.gif"));
                inspectorPanel.activate(assertionInspector);
                break;
            }
            case UNKNOWN: {
                assertionInspector.setIcon(UISupport.createImageIcon("/unknown_assertion.png"));
                break;
            }
            case VALID: {
                assertionInspector.setIcon(UISupport.createImageIcon("/valid_assertion.gif"));
                inspectorPanel.deactivate();
                break;
            }
        }
    }
}
