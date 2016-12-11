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
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.AbstractWsdlMockResponseDesktopPanel;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.ModelItemPropertyEditorModel;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.xml.XmlUtils;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Date;

public class WsdlMockResponseStepDesktopPanel extends AbstractWsdlMockResponseDesktopPanel<WsdlMockResponseTestStep> {
    private JTextArea logArea;
    private AssertionsPanel assertionsPanel;
    private JTextField portField;
    private JTextField pathField;
    private InternalTestRunListener testRunListener;
    private InternalTestMonitorListener testMonitorListener = new InternalTestMonitorListener();
    private InternalAssertionsListener assertionsListener = new InternalAssertionsListener();
    private JInspectorPanel inspectorPanel;
    private JComponentInspector<JComponent> assertionInspector;
    private JComponentInspector<JComponent> logInspector;
    private ModelItemPropertyEditorModel<WsdlMockResponseTestStep> queryEditorModel;
    private ModelItemPropertyEditorModel<WsdlMockResponseTestStep> matchEditorModel;

    public WsdlMockResponseStepDesktopPanel(WsdlMockResponseTestStep mockResponseStep) {
        super(mockResponseStep);
        init(mockResponseStep.getMockResponse());

        testRunListener = new InternalTestRunListener();
        mockResponseStep.getTestCase().addTestRunListener(testRunListener);

        SoapUI.getTestMonitor().addTestMonitorListener(testMonitorListener);
        setEnabled(!SoapUI.getTestMonitor().hasRunningTest(mockResponseStep.getTestCase()));

        mockResponseStep.addAssertionsListener(assertionsListener);
    }

    @Override
    protected JComponent buildContent() {
        inspectorPanel = JInspectorPanelFactory.build(super.buildContent());

        assertionsPanel = buildAssertionsPanel();

        assertionInspector = new JComponentInspector<JComponent>(assertionsPanel, "Assertions ("
                + getModelItem().getAssertionCount() + ")", "Assertions for this Request", true);

        inspectorPanel.addInspector(assertionInspector);

        logInspector = new JComponentInspector<JComponent>(buildLogPanel(), "Request Log (0)", "Log of requests", true);
        inspectorPanel.addInspector(logInspector);

        inspectorPanel.addInspector(new JComponentInspector<JComponent>(buildQueryMatchPanel(), "Query/Match",
                "Query/Match configuration", true));

        inspectorPanel.setDefaultDividerLocation(0.6F);
        inspectorPanel.setCurrentInspector("Assertions");

        updateStatusIcon();

        return inspectorPanel.getComponent();
    }

    private void updateStatusIcon() {
        Assertable.AssertionStatus status = getModelItem().getAssertionStatus();
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

    private JComponent buildLogPanel() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setToolTipText("Response Log");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        return panel;
    }

    public void setContent(JComponent content) {
        inspectorPanel.setContentComponent(content);
    }

    public void removeContent(JComponent content) {
        inspectorPanel.setContentComponent(null);
    }

    @Override
    protected void createToolbar(JXToolBar toolbar) {
        toolbar.addUnrelatedGap();
        toolbar.addFixed(new JLabel("Path"));
        toolbar.addRelatedGap();
        pathField = new JTextField(getModelItem().getPath(), 15);
        pathField.getDocument().addDocumentListener(new DocumentListenerAdapter() {

            @Override
            public void update(Document document) {
                getModelItem().setPath(pathField.getText());
            }
        });

        toolbar.addFixed(pathField);

        toolbar.addUnrelatedGap();
        toolbar.addFixed(new JLabel("Port"));
        toolbar.addRelatedGap();
        portField = new JTextField(String.valueOf(getModelItem().getPort()), 5);
        portField.getDocument().addDocumentListener(new DocumentListenerAdapter() {

            @Override
            public void update(Document document) {
                try {
                    getModelItem().setPort(Integer.parseInt(portField.getText()));
                } catch (NumberFormatException e) {
                }
            }
        });

        toolbar.addFixed(portField);
    }

    private JComponent buildQueryMatchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildQueryMatchToolbar(), BorderLayout.NORTH);
        JSplitPane splitPane = UISupport.createHorizontalSplit(buildQueryEditor(), buildMatchEditor());
        panel.add(splitPane, BorderLayout.CENTER);
        splitPane.setDividerLocation(200);
        return panel;
    }

    private Component buildMatchEditor() {
        JPanel panel = new JPanel(new BorderLayout());

        matchEditorModel = new ModelItemPropertyEditorModel<WsdlMockResponseTestStep>(getModelItem(), "match");
        panel.add(UISupport.getEditorFactory().buildXmlEditor(matchEditorModel), BorderLayout.CENTER);

        UISupport.addTitledBorder(panel, "Matching Value");

        return panel;
    }

    private Component buildQueryEditor() {
        JPanel panel = new JPanel(new BorderLayout());

        queryEditorModel = new ModelItemPropertyEditorModel<WsdlMockResponseTestStep>(getModelItem(), "query");
        panel.add(UISupport.getEditorFactory().buildXPathEditor(queryEditorModel), BorderLayout.CENTER);

        UISupport.addTitledBorder(panel, "XPath Query");

        return panel;
    }

    protected JXToolBar buildQueryMatchToolbar() {
        JXToolBar toolBar = UISupport.createSmallToolbar();
        toolBar.addFixed(new JButton(new SelectFromCurrentAction()));
        return toolBar;
    }

    public class SelectFromCurrentAction extends AbstractAction {
        public SelectFromCurrentAction() {
            super("Select from current");
            putValue(Action.SHORT_DESCRIPTION, "Selects the Query XPath expression from the last request Match field");
        }

        public void actionPerformed(ActionEvent arg0) {
            if (getModelItem().getLastResult() != null && getModelItem().getLastResult().getMockRequest() != null
                    && StringUtils.hasContent(getModelItem().getQuery())) {
                getModelItem().setMatch(
                        XmlUtils.getXPathValue(getModelItem().getLastResult().getMockRequest().getRequestContent(),
                                PropertyExpander.expandProperties(getModelItem(), getModelItem().getQuery())));
            }
        }
    }

    private AssertionsPanel buildAssertionsPanel() {
        assertionsPanel = new AssertionsPanel(getModelItem()) {
            protected void selectError(AssertionError error) {
                ModelItemXmlEditor<?, ?> editor = getResponseEditor();
                editor.requestFocus();
            }
        };

        return assertionsPanel;
    }

    @Override
    public boolean onClose(boolean canCancel) {
        getModelItem().getTestCase().removeTestRunListener(testRunListener);
        SoapUI.getTestMonitor().removeTestMonitorListener(testMonitorListener);
        assertionsPanel.release();

        queryEditorModel.release();
        matchEditorModel.release();

        inspectorPanel.release();

        getModelItem().removeAssertionsListener(assertionsListener);
        return super.onClose(canCancel);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        pathField.setEnabled(enabled);
        portField.setEnabled(enabled);
    }

    public boolean dependsOn(ModelItem modelItem) {
        return modelItem == getModelItem() || modelItem == getModelItem().getTestCase()
                || modelItem == getModelItem().getOperation() || modelItem == getModelItem().getOperation().getInterface()
                || modelItem == getModelItem().getTestCase().getTestSuite()
                || modelItem == getModelItem().getTestCase().getTestSuite().getProject();
    }

    public class InternalTestRunListener extends TestRunListenerAdapter {
        @Override
        public void afterRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
            setEnabled(true);
        }

        @Override
        public void beforeRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
            setEnabled(false);
        }

        @Override
        public void beforeStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep) {
            if (testStep == getModelItem()) {
                logArea.setText(logArea.getText() + new Date(System.currentTimeMillis()).toString()
                        + ": Waiting for request on http://127.0.0.1:" + getModelItem().getPort() + getModelItem().getPath()
                        + "\r\n");
            }
        }

        @Override
        public void afterStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result) {
            if (result.getTestStep() == getModelItem()) {
                String msg = new Date(result.getTimeStamp()).toString() + ": Handled request in " + result.getTimeTaken()
                        + "ms";
                logArea.setText(logArea.getText() + msg + "\r\n");
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

    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);

        if (evt.getPropertyName().equals(WsdlMockResponseTestStep.STATUS_PROPERTY)) {
            updateStatusIcon();
        }
    }

    @SuppressWarnings("unused")
    private final class DeclareNamespacesAction extends AbstractAction {
        public DeclareNamespacesAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/declareNs.gif"));
            putValue(Action.SHORT_DESCRIPTION,
                    "Declare available response/request namespaces in source/target expressions");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                MockResult lastResult = getMockResponse().getMockResult();
                String content = null;
                if (lastResult == null) {
                    if (!UISupport.confirm("Missing last result, declare from default request instead?",
                            "Declare Namespaces")) {
                        return;
                    }

                    content = ((WsdlOperation) getMockResponse().getMockOperation().getOperation()).createRequest(true);
                } else {
                    content = lastResult.getMockRequest().getRequestContent();
                }

                String path = getModelItem().getQuery();
                if (path == null) {
                    path = "";
                }

                getModelItem().setQuery(XmlUtils.declareXPathNamespaces(content) + path);
            } catch (Exception e1) {
                UISupport.showErrorMessage(e1);
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
