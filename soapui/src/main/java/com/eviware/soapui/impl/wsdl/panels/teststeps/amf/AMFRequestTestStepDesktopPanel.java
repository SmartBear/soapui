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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.config.AMFRequestTestStepConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.components.RequestMessageXmlEditor;
import com.eviware.soapui.impl.support.components.ResponseMessageXmlEditor;
import com.eviware.soapui.impl.support.panels.AbstractHttpRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.panels.support.TestRunComponentEnabler;
import com.eviware.soapui.impl.wsdl.panels.teststeps.AssertionsPanel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.DefaultPropertyHolderTableModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditorModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.submit.transports.http.DocumentContent;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepWithProperties;
import com.eviware.soapui.impl.wsdl.teststeps.actions.AddAssertionAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.Submit.Status;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
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
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.support.AbstractEditorView;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;
import com.eviware.soapui.support.propertyexpansion.PropertyExpansionPopupListener;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.swing.SoapUISplitPaneUI;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.eviware.soapui.impl.wsdl.teststeps.Script.SCRIPT_PROPERTY;

@SuppressWarnings("serial")
public class AMFRequestTestStepDesktopPanel extends ModelItemDesktopPanel<AMFRequestTestStep> implements SubmitListener {
    private static final String ENDPOINT = "Endpoint";
    private static final String AMF_CALL = "AMF Call";
    private final static Logger log = LogManager.getLogger(AbstractHttpRequestDesktopPanel.class);
    private JPanel configPanel;
    private JButton addAssertionButton;
    private JInspectorPanel inspectorPanel;
    private AMFRequestTestStep amfRequestTestStep;
    protected AMFRequestTestStepConfig amfRequestTestStepConfig;
    private JComponentInspector<?> assertionInspector;
    private AssertionsPanel assertionsPanel;
    private InternalAssertionsListener assertionsListener = new InternalAssertionsListener();
    private InternalTestMonitorListener testMonitorListener = new InternalTestMonitorListener();
    private JComponent requestEditor;
    private ModelItemXmlEditor<?, ?> responseEditor;
    private Submit submit;
    private JButton submitButton;
    private JToggleButton tabsButton;
    private JTabbedPane requestTabs;
    private JPanel requestTabPanel;
    private boolean responseHasFocus;
    private JSplitPane requestSplitPane;
    private JEditorStatusBarWithProgress statusBar;
    private JButton cancelButton;
    private JButton splitButton;
    private JComponent propertiesTableComponent;
    private SoapUIScriptEngine scriptEngine;
    private RunAction runAction = new RunAction();
    private GroovyEditor groovyEditor;
    private JTextField amfCallField;
    public boolean updating;
    SimpleForm configForm;
    private JTextField endpointField;
    private TestRunComponentEnabler componentEnabler;
    protected PropertyHolderTable propertyHolderTable;

    public AMFRequestTestStepDesktopPanel(AMFRequestTestStep modelItem) {
        super(modelItem);
        amfRequestTestStep = modelItem;
        componentEnabler = new TestRunComponentEnabler(amfRequestTestStep.getTestCase());
        initConfig();
        initContent();

        SoapUI.getTestMonitor().addTestMonitorListener(testMonitorListener);
        setEnabled(!SoapUI.getTestMonitor().hasRunningTest(amfRequestTestStep.getTestCase()));

        amfRequestTestStep.addAssertionsListener(assertionsListener);

        scriptEngine = SoapUIScriptEngineRegistry.create(modelItem);
        scriptEngine.setScript(amfRequestTestStep.getScript());

    }

    protected void initConfig() {
        amfRequestTestStepConfig = amfRequestTestStep.getAMFRequestTestStepConfig();
    }

    private JComponent buildContent() {
        requestSplitPane = UISupport.createHorizontalSplit();
        requestSplitPane.setResizeWeight(0.5);
        requestSplitPane.setBorder(null);

        JComponent content;
        submitButton = createActionButton(new SubmitAction(), true);
        submitButton.setEnabled(enableSubmit());
        cancelButton = createActionButton(new CancelAction(), false);
        tabsButton = new JToggleButton(new ChangeToTabsAction());
        tabsButton.setPreferredSize(UISupport.TOOLBAR_BUTTON_DIMENSION);
        splitButton = createActionButton(new ChangeSplitPaneOrientationAction(requestSplitPane), true);

        addAssertionButton = UISupport.createToolbarButton(new AddAssertionAction(amfRequestTestStep));
        addAssertionButton.setEnabled(true);

        requestTabs = new JTabbedPane();
        requestTabs.setTabPlacement(JTabbedPane.LEFT);
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

        requestEditor = (JComponent) buildRequestConfigPanel();
        responseEditor = buildResponseEditor();
        if (amfRequestTestStep.getSettings().getBoolean(UISettings.START_WITH_REQUEST_TABS)) {
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
        // setPreferredSize(new Dimension(600, 450));

        updateStatusIcon();

        return inspectorPanel.getComponent();
    }

    @SuppressWarnings("unchecked")
    protected JComponent buildRequestConfigPanel() {
        ModelItemXmlEditor<?, ?> reqEditor = buildRequestEditor();

        configPanel = UISupport.addTitledBorder(new JPanel(new BorderLayout()), "Script");
        configPanel.add(buildToolbarButtonAndText(), BorderLayout.NORTH);
        groovyEditor = (GroovyEditor) UISupport.getEditorFactory().buildGroovyEditor(new ScriptStepGroovyEditorModel());
        configPanel.add(groovyEditor, BorderLayout.CENTER);
        propertiesTableComponent = buildProperties();
        final JSplitPane split = UISupport.createVerticalSplit(propertiesTableComponent, configPanel);
        split.setDividerLocation(120);
        reqEditor.addEditorView((EditorView) new AbstractEditorView<AMFRequestDocument>("AMF",
                (Editor<AMFRequestDocument>) reqEditor, "amf") {
            @Override
            public JComponent buildUI() {
                return split;
            }
        });
        reqEditor.selectView(1);
        return reqEditor;
    }

    private JComponent buildToolbarButtonAndText() {
        JXToolBar toolBar = UISupport.createToolbar();
        JButton runButton = UISupport.createToolbarButton(runAction);
        toolBar.add(runButton);
        toolBar.add(Box.createHorizontalGlue());
        JLabel label = new JLabel("<html>Script is invoked with <code>log</code>, <code>context</code> "
                + ", <code>parameters</code> and <code>amfHeaders</code> variables</html>");
        label.setToolTipText(label.getText());
        label.setMinimumSize(label.getPreferredSize());
        label.setMaximumSize(label.getPreferredSize());

        toolBar.add(label);
        toolBar.addRelatedGap();
        toolBar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.GROOVYSTEPEDITOR_HELP_URL)));

        componentEnabler.add(runButton);

        return toolBar;
    }

    protected JComponent buildToolbar() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildToolbar1(), BorderLayout.NORTH);
        panel.add(buildToolbar2(), BorderLayout.SOUTH);
        return panel;

    }

    protected void initContent() {
        amfRequestTestStep.getAMFRequest().addSubmitListener(this);

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

    protected JComponent buildToolbar1() {
        JXToolBar toolbar = UISupport.createToolbar();

        toolbar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        toolbar.addFixed(submitButton);
        toolbar.add(cancelButton);
        toolbar.addFixed(addAssertionButton);

        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(tabsButton);
        toolbar.add(splitButton);

        toolbar.addFixed(UISupport
                .createToolbarButton(new ShowOnlineHelpAction(HelpUrls.TEST_AMF_REQUEST_EDITOR_HELP_URL)));

        return toolbar;

    }

    protected JComponent buildToolbar2() {
        JXToolBar toolbar = UISupport.createToolbar();

        toolbar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        toolbar.addLabeledFixed(ENDPOINT, addEndpointField());
        toolbar.addSeparator();
        toolbar.addLabeledFixed(AMF_CALL, addAmfCallField());

        return toolbar;

    }

    public AMFRequestTestStep getAMFRequestTestStep() {
        return amfRequestTestStep;
    }

    protected AssertionsPanel buildAssertionsPanel() {
        return new AMFAssertionsPanel(amfRequestTestStep) {
        };
    }

    protected class AMFAssertionsPanel extends AssertionsPanel {
        public AMFAssertionsPanel(Assertable assertable) {
            super(assertable);
        }
    }

    private JTextField addAmfCallField() {
        amfCallField = new JTextField();
        amfCallField.setText(amfRequestTestStep.getAmfCall());
        amfCallField.setColumns(20);
        amfCallField.setToolTipText("object.methodName for amf method call");
        PropertyExpansionPopupListener.enable(amfCallField, amfRequestTestStep);
        addAmfCallDocumentListener();
        return amfCallField;
    }

    private JTextField addEndpointField() {
        endpointField = new JTextField();
        endpointField.setText(amfRequestTestStep.getEndpoint());
        endpointField.setColumns(35);
        endpointField.setToolTipText("http to connect");
        PropertyExpansionPopupListener.enable(endpointField, amfRequestTestStep);
        addEndpointCallDocumentListener();
        return endpointField;
    }

    protected void addAmfCallDocumentListener() {
        amfCallField.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            @Override
            public void update(Document document) {
                if (!updating) {
                    amfRequestTestStep.setAmfCall(amfCallField.getText());
                }
                submitButton.setEnabled(enableSubmit());
            }
        });
    }

    protected void addEndpointCallDocumentListener() {
        endpointField.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            @Override
            public void update(Document document) {
                if (!updating) {
                    amfRequestTestStep.setEndpoint(endpointField.getText());
                }
                submitButton.setEnabled(enableSubmit());
            }
        });
    }

    protected boolean enableSubmit() {
        return !StringUtils.isNullOrEmpty(amfRequestTestStep.getEndpoint())
                && !StringUtils.isNullOrEmpty(amfRequestTestStep.getAmfCall());
    }

    private class ScriptStepGroovyEditorModel implements GroovyEditorModel {
        public String[] getKeywords() {
            return new String[]{"log", "context", "property"};
        }

        public Action getRunAction() {
            return runAction;
        }

        public String getScript() {
            return amfRequestTestStep.getScript();
        }

        public void setScript(String text) {
            if (updating) {
                return;
            }

            updating = true;
            amfRequestTestStep.setScript(text);
            updating = false;
        }

        public Settings getSettings() {
            return SoapUI.getSettings();
        }

        public String getScriptName() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public ModelItem getModelItem() {
            return amfRequestTestStep;
        }
    }

    private class RunAction extends AbstractAction {
        public RunAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
            putValue(Action.SHORT_DESCRIPTION,
                    "Runs this script in a seperate thread using a mock testRunner and testContext");
        }

        public void actionPerformed(ActionEvent e) {
            SoapUI.getThreadPool().execute(new Runnable() {
                public void run() {
                    SubmitContext context = new WsdlTestRunContext(getModelItem());
                    statusBar.setIndeterminate(true);
                    amfRequestTestStep.initAmfRequest(context);

                    if (context.getProperty(AMFRequest.AMF_SCRIPT_ERROR) != null) {
                        UISupport.showInfoMessage(((Throwable) context.getProperty(AMFRequest.AMF_SCRIPT_ERROR))
                                .getMessage());
                    } else {
                        UISupport.showInfoMessage(scriptInfo(context));

                    }
                    statusBar.setIndeterminate(false);
                    amfRequestTestStep.getAMFRequest().clearArguments();
                }

                @SuppressWarnings("unchecked")
                private String scriptInfo(SubmitContext context) {
                    HashMap<String, Object> parameters = (HashMap<String, Object>) context
                            .getProperty(AMFRequest.AMF_SCRIPT_PARAMETERS);
                    HashMap<String, Object> amfHeaders = (HashMap<String, Object>) context
                            .getProperty(AMFRequest.AMF_SCRIPT_HEADERS);
                    StringBuilder sb = new StringBuilder();
                    sb.append("parameters " + (parameters != null ? parameters.toString() : ""));
                    sb.append("\n");
                    sb.append("amfHeaders " + (amfHeaders != null ? amfHeaders.toString() : ""));
                    return sb.toString();
                }
            });
        }

    }

    protected ModelItemXmlEditor<?, ?> buildResponseEditor() {
        return new AMFResponseMessageEditor();
    }

    protected ModelItemXmlEditor<?, ?> buildRequestEditor() {
        return new AMFRequestMessageEditor();
    }

    public class AMFResponseMessageEditor extends ResponseMessageXmlEditor<AMFRequestTestStep, AMFResponseDocument> {
        public AMFResponseMessageEditor() {
            super(new AMFResponseDocument(), amfRequestTestStep);
        }

        @Override
        public void release() {
            getDocument().release();
            super.release();
        }
    }

    public class AMFRequestMessageEditor extends RequestMessageXmlEditor<AMFRequestTestStep, AMFRequestDocument> {
        public AMFRequestMessageEditor() {
            super(new AMFRequestDocument(), amfRequestTestStep);
        }

        @Override
        public void release() {
            getDocument().release();
            super.release();
        }

    }

    public boolean dependsOn(ModelItem modelItem) {
        return modelItem == getModelItem() || modelItem == getModelItem().getTestCase()
                || modelItem == getModelItem().getTestCase().getTestSuite()
                || modelItem == getModelItem().getTestCase().getTestSuite().getProject();
    }

    public boolean onClose(boolean canCancel) {
        configPanel.removeAll();
        inspectorPanel.release();

        requestEditor.removeAll();
        ((ModelItemXmlEditor<?, ?>) requestEditor).release();
        responseEditor.release();
        responseEditor.removeAll();
        responseEditor = null;
        assertionsPanel.release();
        SoapUI.getTestMonitor().removeTestMonitorListener(testMonitorListener);
        amfRequestTestStep.removeAssertionsListener(assertionsListener);
        amfRequestTestStep.getAMFRequest().removeSubmitListener(this);
        componentEnabler.release();
        groovyEditor.release();
        amfRequestTestStep.release();
        propertyHolderTable.release();
        this.removeAll();
        return release();
    }

    public class AMFResponseDocument extends AbstractXmlDocument implements PropertyChangeListener {
        public AMFResponseDocument() {
            amfRequestTestStep.addPropertyChangeListener(AMFRequestTestStep.RESPONSE_PROPERTY, this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            fireContentChanged();
        }

        @Override
        @Nonnull
        public DocumentContent getDocumentContent(Format format) {
            AMFResponse response = amfRequestTestStep.getAMFRequest().getResponse();
            return new DocumentContent(response == null ? null : response.getContentType(), response == null ? null : response.getResponseContentXML());
        }

        @Override
        public void setDocumentContent(DocumentContent documentContent) {
            if (amfRequestTestStep.getAMFRequest().getResponse() != null) {
                amfRequestTestStep.getAMFRequest().getResponse().setResponseContentXML(documentContent.getContentAsString());
            }
        }

        public void release() {
            super.release();
            amfRequestTestStep.removePropertyChangeListener(AMFRequestTestStep.RESPONSE_PROPERTY, this);
        }
    }

    public class AMFRequestDocument extends AbstractXmlDocument implements PropertyChangeListener {
        public AMFRequestDocument() {
            amfRequestTestStep.addPropertyChangeListener(AMFRequestTestStep.REQUEST_PROPERTY, this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            fireContentChanged();
        }

        @Override
        @Nonnull
        public DocumentContent getDocumentContent(Format format) {
            AMFRequest request = amfRequestTestStep.getAMFRequest();
            return new DocumentContent(null, request == null ? null : request.requestAsXML());
        }

        @Override
        public void setDocumentContent(DocumentContent documentContent) {
        }

        public void release() {
            super.release();
            amfRequestTestStep.removePropertyChangeListener(AMFRequestTestStep.REQUEST_PROPERTY, this);
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
            putValue(Action.SHORT_DESCRIPTION, "Submit request to specified endpoint URL");
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

        SubmitContext submitContext = new WsdlTestRunContext(getModelItem());
        if (!amfRequestTestStep.initAmfRequest(submitContext)) {
            throw new SubmitException("AMF request is not initialised properly !");
        }

        Analytics.trackAction(SoapUIActions.RUN_TEST_STEP_FROM_PANEL, "StepType", "AMF");

        return amfRequestTestStep.getAMFRequest().submit(submitContext, true);
    }

    protected final class InputAreaFocusListener implements FocusListener {
        public InputAreaFocusListener(JComponent editor) {
        }

        public void focusGained(FocusEvent e) {
            responseHasFocus = false;

            // statusBar.setTarget(sourceEditor.getInputArea());
            if (!splitButton.isEnabled()) {
                requestTabs.setSelectedIndex(0);
                return;
            }

            // if
            // (getModelItem().getSettings().getBoolean(UISettings.NO_RESIZE_REQUEST_EDITOR))
            // return;

            // // dont resize if split has been dragged
            // if (requestSplitPane.getUI() instanceof SoapUISplitPaneUI
            // && ((SoapUISplitPaneUI) requestSplitPane.getUI()).hasBeenDragged())
            // return;
            //
            int pos = requestSplitPane.getDividerLocation();
            if (pos >= 600) {
                return;
            }
            if (requestSplitPane.getMaximumDividerLocation() > 700) {
                requestSplitPane.setDividerLocation(600);
            } else {
                requestSplitPane.setDividerLocation(0.8);
            }
        }

        public void focusLost(FocusEvent e) {
        }
    }

    protected final class ResultAreaFocusListener implements FocusListener {
        @SuppressWarnings("unused")
        private final ModelItemXmlEditor<?, ?> responseEditor;

        public ResultAreaFocusListener(ModelItemXmlEditor<?, ?> editor) {
            this.responseEditor = editor;
        }

        public void focusGained(FocusEvent e) {
            responseHasFocus = true;

            // statusBar.setTarget(sourceEditor.getInputArea());
            if (!splitButton.isEnabled()) {
                requestTabs.setSelectedIndex(1);
                return;
            }

            if (getModelItem().getSettings().getBoolean(UISettings.NO_RESIZE_REQUEST_EDITOR)) {
                return;
            }

            // dont resize if split has been dragged or result is empty
            if (requestSplitPane.getUI() instanceof SoapUISplitPaneUI
                    && ((SoapUISplitPaneUI) requestSplitPane.getUI()).hasBeenDragged()
                    || amfRequestTestStep.getAMFRequest().getResponse() == null) {
                return;
            }

            int pos = requestSplitPane.getDividerLocation();
            int maximumDividerLocation = requestSplitPane.getMaximumDividerLocation();
            if (pos + 600 < maximumDividerLocation) {
                return;
            }

            if (maximumDividerLocation > 700) {
                requestSplitPane.setDividerLocation(maximumDividerLocation - 600);
            } else {
                requestSplitPane.setDividerLocation(0.2);
            }
        }

        public void focusLost(FocusEvent e) {
        }
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
            responseEditor.setEnabled(enabled);
        }

        submitButton.setEnabled(enabled && enableSubmit());
        addAssertionButton.setEnabled(enabled);
        propertiesTableComponent.setEnabled(enabled);
        groovyEditor.setEnabled(enabled);
        endpointField.setEnabled(enabled);
        amfCallField.setEnabled(enabled);

        statusBar.setIndeterminate(!enabled);
    }

    public void afterSubmit(Submit submit, SubmitContext context) {
        if (submit.getRequest() != amfRequestTestStep.getAMFRequest()) {
            return;
        }

        Status status = submit.getStatus();
        AMFResponse response = (AMFResponse) submit.getResponse();
        if (status == Status.FINISHED) {
            amfRequestTestStep.setResponse(response, context);
        }

        cancelButton.setEnabled(false);
        setEnabled(true);

        String message = null;
        String infoMessage = null;
        String requestName = amfRequestTestStep.getName();

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

                if (!splitButton.isEnabled()) {
                    requestTabs.setSelectedIndex(1);
                }

                responseEditor.requestFocus();
            }
        }

        logMessages(message, infoMessage);

        if (getModelItem().getSettings().getBoolean(UISettings.AUTO_VALIDATE_RESPONSE)) {
            responseEditor.getSourceEditor().validate();
        }

        AMFRequestTestStepDesktopPanel.this.submit = null;

        updateStatusIcon();
    }

    protected void logMessages(String message, String infoMessage) {
        log.info(infoMessage);
        statusBar.setInfo(message);
    }

    public boolean beforeSubmit(Submit submit, SubmitContext context) {
        if (submit.getRequest() != amfRequestTestStep.getAMFRequest()) {
            return true;
        }

        setEnabled(false);
        cancelButton.setEnabled(AMFRequestTestStepDesktopPanel.this.submit != null);
        return true;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (evt.getPropertyName().equals(SCRIPT_PROPERTY) && !updating) {
            updating = true;
            groovyEditor.getEditArea().setText((String) evt.getNewValue());
            updating = false;
        }
        if (evt.getPropertyName().equals(AMFRequestTestStep.STATUS_PROPERTY)) {
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
        AssertionStatus status = amfRequestTestStep.getAssertionStatus();
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
