package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.panels.request.views.content.GraphQLRequestContentView;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLTestRequestStepInterface;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.actions.AddAssertionAction;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.support.DateUtil;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.ListDataChangeListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.auth.AuthInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.httpheaders.HttpHeadersInspectorFactory;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlInspector;
import com.eviware.soapui.support.editor.xml.support.DefaultXmlDocument;
import com.eviware.soapui.support.log.JLogList;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.Date;

public class GraphQLRequestTestStepDesktopPanel extends
        AbstractHttpXmlRequestDesktopPanel<GraphQLTestRequestStepInterface, GraphQLTestRequestInterface> {
    private static final RestRequestInterface.HttpMethod[] graphQLMethods = {RestRequestInterface.HttpMethod.GET, RestRequestInterface.HttpMethod.POST};
    private JLogList logArea;
    private InternalTestMonitorListener testMonitorListener = new InternalTestMonitorListener();
    private InternalAssertionsListener assertionsListener = new InternalAssertionsListener();
    private JButton addAssertionButton;
    private JUndoableTextField pathTextField;

    private AssertionsPanel assertionsPanel;
    private JComponentInspector<?> assertionInspector;
    private JInspectorPanel inspectorPanel;
    private JComponentInspector<?> logInspector;

    private boolean updating;
    private long startTime;

    public GraphQLRequestTestStepDesktopPanel(GraphQLRequestTestStep testStep) {
        super(testStep, testStep.getTestRequest());

        SoapUI.getTestMonitor().addTestMonitorListener(testMonitorListener);
        setEnabled(!SoapUI.getTestMonitor().hasRunningTest(testStep.getTestCase()));

        testStep.getTestRequest().addAssertionsListener(assertionsListener);
        getSubmitButton().setEnabled(getSubmit() == null && StringUtils.hasContent(getRequest().getEndpoint()));
    }

    protected EditorView getRequestEditorView(GraphQLRequestMessageEditor editor,
                                              GraphQLTestRequestInterface graphQLRequest) {
        return new GraphQLRequestContentView(editor, graphQLRequest);
    }

    @Override
    protected ModelItemXmlEditor<?, ?> buildRequestEditor() {
        GraphQLRequestMessageEditor graphQLRequestMessageEditor = new GraphQLRequestMessageEditor(getRequest());

        graphQLRequestMessageEditor.addEditorView(getRequestEditorView(graphQLRequestMessageEditor, getRequest()));
        RawXmlEditorFactory rawXmlEditorFactory = new RawXmlEditorFactory();
        graphQLRequestMessageEditor.addEditorView(rawXmlEditorFactory.createRequestEditorView(graphQLRequestMessageEditor, getRequest()));

        AuthInspectorFactory authInspectorFactory = new AuthInspectorFactory();
        HttpHeadersInspectorFactory httpHeadersInspectorFactory = new HttpHeadersInspectorFactory();
        graphQLRequestMessageEditor.addInspector((XmlInspector) authInspectorFactory.createRequestInspector(graphQLRequestMessageEditor, getRequest()));
        graphQLRequestMessageEditor.addInspector((XmlInspector) httpHeadersInspectorFactory.createRequestInspector(graphQLRequestMessageEditor, getRequest()));

        return graphQLRequestMessageEditor;
    }

    @Override
    protected ModelItemXmlEditor<?, ?> buildResponseEditor() {
        return new GraphQLResponseMessageEditor(getRequest());
    }

    public class GraphQLRequestMessageEditor extends ModelItemXmlEditor<GraphQLTestRequestInterface, XmlDocument> {
        public GraphQLRequestMessageEditor(GraphQLTestRequestInterface modelItem) {
            super(new DefaultXmlDocument(), modelItem);
        }
    }

    public class GraphQLResponseMessageEditor extends HttpResponseMessageEditor {
        public GraphQLResponseMessageEditor(GraphQLTestRequestInterface modelItem) {
            super(modelItem);
        }
    }

    @Override
    public void setContent(JComponent content) {
        inspectorPanel.setContentComponent(content);
    }

    @Override
    public void removeContent(JComponent content) {
        inspectorPanel.setContentComponent(null);
    }

    @Override
    protected String getHelpUrl() {
        return HelpUrls.GRAPHQL_REQUEST_HELP_URL;
    }

    protected JComponent buildLogPanel() {
        logArea = new JLogList("Request Log");

        logArea.getLogList().getModel().addListDataListener(new ListDataChangeListener() {
            @Override
            public void dataChanged(ListModel model) {
                logInspector.setTitle("Request Log (" + model.getSize() + ")");
            }
        });

        return logArea;
    }

    @Override
    protected void insertButtons(JXToolBar toolbar) {
        toolbar.add(addAssertionButton);
    }

    @Override
    protected JComponent buildEndpointComponent() {
        return null;
    }

    @Override
    protected JComponent buildContent() {
        JComponent component = super.buildContent();

        inspectorPanel = JInspectorPanelFactory.build(component);
        assertionsPanel = buildAssertionsPanel();

        assertionInspector = new JComponentInspector<JComponent>(assertionsPanel, "Assertions ("
                + getModelItem().getAssertionCount() + ")", "Assertions for this request", true);

        inspectorPanel.addInspector(assertionInspector);

        logInspector = new JComponentInspector<JComponent>(buildLogPanel(), "Request Log (0)", "Log of requests", true);
        inspectorPanel.addInspector(logInspector);
        inspectorPanel.setDefaultDividerLocation(0.6F);
        inspectorPanel.setCurrentInspector("Assertions");

        updateStatusIcon();

        getSubmitButton().setEnabled(getSubmit() == null && StringUtils.hasContent(getRequest().getEndpoint()));

        return inspectorPanel.getComponent();
    }

    private void updateStatusIcon() {
        Assertable.AssertionStatus status = getModelItem().getTestRequest().getAssertionStatus();
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
                assertionInspector.setIcon(UISupport.createImageIcon("/vFalid_assertion.gif"));
                inspectorPanel.deactivate();
                break;
            }
        }
    }

    private JComponent createPathPanel() {
        MigLayout layout = new MigLayout("", "[grow]", "[][]");
        JPanel panel = new JPanel(layout);

        pathTextField = new JUndoableTextField();
        UISupport.setPreferredHeight(pathTextField, 23);
        pathTextField.setText(getRequest().getEndpoint());
        pathTextField.setToolTipText(pathTextField.getText());
        pathTextField.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            @Override
            public void update(Document document) {
                if (updating) {
                    return;
                }

                updating = true;
                String text = pathTextField.getText();
                getRequest().setEndpoint(HttpUtils.completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(text));
                if (!text.equals(getRequest().getEndpoint())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            pathTextField.setText(getRequest().getEndpoint());
                        }
                    });
                }
                updating = false;
            }
        });

        pathTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onSubmit();
                }
            }
        });

        panel.add(new JLabel("Request URL"), "wrap");
        panel.add(pathTextField, "growx, hmin 20");

        return panel;
    }

    protected void addToolbarComponents(JPanel toolbar) {
        addMethodCombo(toolbar);
        toolbar.add(createPathPanel(), "growx");
    }

    protected void addMethodCombo(JPanel toolbar) {
        MigLayout layout = new MigLayout("", "[]", "[][]");
        JPanel panel = new JPanel(layout);
        JComboBox methodCombo = new JComboBox(graphQLMethods);

        methodCombo.setSelectedItem(getRequest().getMethod());
        methodCombo.setToolTipText("Select HTTP method");
        methodCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                getRequest().setMethod((RestRequestInterface.HttpMethod) methodCombo.getSelectedItem());
            }
        });

        panel.add(new JLabel("Method"), "wrap");
        panel.add(methodCombo);

        toolbar.add(panel);
    }

    @Override
    protected JComponent buildToolbar() {
        addAssertionButton = createActionButton(new AddAssertionAction(getRequest()), true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(super.buildToolbar(), BorderLayout.NORTH);

        JPanel lowerToolbar = new JPanel(new MigLayout("", "0[][grow][]0", "0[]0"));
        addToolbarComponents(lowerToolbar);

        panel.add(lowerToolbar);
        return panel;
    }


    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == true) {
            enabled = !SoapUI.getTestMonitor().hasRunningLoadTest(getModelItem().getTestCase())
                    && !SoapUI.getTestMonitor().hasRunningSecurityTest(getModelItem().getTestCase());
        }

        super.setEnabled(enabled);
        addAssertionButton.setEnabled(enabled);
        assertionsPanel.setEnabled(enabled);

        if (SoapUI.getTestMonitor().hasRunningLoadTest(getRequest().getTestCase())
                || SoapUI.getTestMonitor().hasRunningSecurityTest(getModelItem().getTestCase())) {
            getRequest().removeSubmitListener(this);
        } else {
            getRequest().addSubmitListener(this);
        }
    }

    @Override
    protected Submit doSubmit() throws Request.SubmitException {
        return getRequest().submit(new WsdlTestRunContext(getModelItem()), true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RestTestRequestInterface.STATUS_PROPERTY)) {
            updateStatusIcon();
        } else if (evt.getPropertyName().equals("path")) {
            getSubmitButton().setEnabled(getSubmit() == null && StringUtils.hasContent(getRequest().getEndpoint()));
        } else if (evt.getPropertyName().equals(AbstractHttpRequest.ENDPOINT_PROPERTY)) {
            getSubmitButton().setEnabled(getSubmit() == null && StringUtils.hasContent(getRequest().getEndpoint()));
            if (updating) {
                return;
            }

            updating = true;
            pathTextField.setText(String.valueOf(evt.getNewValue()));
            updating = false;
        }
        super.propertyChange(evt);
    }

    @Override
    public boolean beforeSubmit(Submit submit, SubmitContext context) {
        boolean result = super.beforeSubmit(submit, context);
        startTime = System.currentTimeMillis();
        return result;
    }

    @Override
    protected void logMessages(String message, String infoMessage) {
        super.logMessages(message, infoMessage);
        logArea.addLine(DateUtil.formatExtraFull(new Date(startTime)) + " - " + message);
    }

    protected AssertionsPanel buildAssertionsPanel() {
        return new AssertionsPanel(getRequest()) {
            @Override
            protected void selectError(AssertionError error) {
                ModelItemXmlEditor<?, ?> editor = getResponseEditor();
                editor.requestFocus();
            }
        };
    }

    @Override
    public boolean onClose(boolean canCancel) {
        if (super.onClose(canCancel)) {
            if (assertionsPanel != null) {
                assertionsPanel.release();
            }
            if (inspectorPanel != null) {
                inspectorPanel.release();
            }
            if (testMonitorListener != null) {
                SoapUI.getTestMonitor().removeTestMonitorListener(testMonitorListener);
            }
            GraphQLTestRequestInterface testRequestInterface = getRequest();
            if (testRequestInterface != null) {
                testRequestInterface.removeAssertionsListener(assertionsListener);
            }
            return true;
        }

        return false;
    }

    private class InternalTestMonitorListener extends TestMonitorListenerAdapter {
        @Override
        public void loadTestFinished(LoadTestRunner runner) {
            setEnabled(!SoapUI.getTestMonitor().hasRunningTest(getModelItem().getTestCase()));
        }

        @Override
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

        @Override
        public void testCaseFinished(TestCaseRunner runner) {
            setEnabled(!SoapUI.getTestMonitor().hasRunningTest(getModelItem().getTestCase()));
        }

        @Override
        public void testCaseStarted(TestCaseRunner runner) {
            if (runner.getTestCase() == getModelItem().getTestCase()) {
                setEnabled(false);
            }
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
}
