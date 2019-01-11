/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
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
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStepInterface;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.actions.AddAssertionAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
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
import com.eviware.soapui.support.log.JLogList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.Date;

public class HttpTestRequestDesktopPanel extends
        AbstractHttpXmlRequestDesktopPanel<HttpTestRequestStepInterface, HttpTestRequestInterface<?>> {
    private JLogList logArea;
    private InternalTestMonitorListener testMonitorListener = new InternalTestMonitorListener();
    private JButton addAssertionButton;
    protected boolean updatingRequest;
    private AssertionsPanel assertionsPanel;
    private JInspectorPanel inspectorPanel;
    private JComponentInspector<?> assertionInspector;
    private JComponentInspector<?> logInspector;
    private InternalAssertionsListener assertionsListener = new InternalAssertionsListener();
    private long startTime;
    private boolean updating;
    private JUndoableTextField pathTextField;
    private JCheckBox downloadResources;
    private JComboBox methodCombo;

    public HttpTestRequestDesktopPanel(HttpTestRequestStepInterface testStep) {
        super(testStep, testStep.getTestRequest());

        SoapUI.getTestMonitor().addTestMonitorListener(testMonitorListener);
        setEnabled(!SoapUI.getTestMonitor().hasRunningTest(testStep.getTestCase()));

        testStep.getTestRequest().addAssertionsListener(assertionsListener);

        getSubmitButton().setEnabled(getSubmit() == null && StringUtils.hasContent(getRequest().getEndpoint()));
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
    public void setContent(JComponent content) {
        inspectorPanel.setContentComponent(content);
    }

    @Override
    public void removeContent(JComponent content) {
        inspectorPanel.setContentComponent(null);
    }

    @Override
    protected String getHelpUrl() {
        return HelpUrls.HTTP_REQUEST_HELP_URL;
    }

    @Override
    protected JComponent buildContent() {
        JComponent component = super.buildContent();

        inspectorPanel = JInspectorPanelFactory.build(component);
        assertionsPanel = buildAssertionsPanel();

        assertionInspector = new JComponentInspector<JComponent>(assertionsPanel, "Assertions ("
                + getModelItem().getAssertionCount() + ")", "Assertions for this Request", true);

        inspectorPanel.addInspector(assertionInspector);

        logInspector = new JComponentInspector<JComponent>(buildLogPanel(), "Request Log (0)", "Log of requests", true);
        inspectorPanel.addInspector(logInspector);
        inspectorPanel.setDefaultDividerLocation(0.6F);
        inspectorPanel.setCurrentInspector("Assertions");

        updateStatusIcon();

        getSubmitButton().setEnabled(getSubmit() == null && StringUtils.hasContent(getRequest().getEndpoint()));

        return inspectorPanel.getComponent();
    }

    @Override
    protected JComponent buildEndpointComponent() {
        return null;
    }

    private void updateStatusIcon() {
        AssertionStatus status = getModelItem().getTestRequest().getAssertionStatus();
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

    protected void addMethodCombo(JXToolBar toolbar) {
        methodCombo = new JComboBox(RestRequestInterface.HttpMethod.getMethods());

        methodCombo.setSelectedItem(getRequest().getMethod());
        methodCombo.setToolTipText("Set desired HTTP method");
        methodCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updatingRequest = true;
                getRequest().setMethod((RestRequestInterface.HttpMethod) methodCombo.getSelectedItem());
                updatingRequest = false;
            }
        });

        toolbar.addLabeledFixed("Method", methodCombo);
        toolbar.addSeparator();
    }

    protected void addToolbarComponents(JXToolBar toolbar) {
        toolbar.addSeparator();
        addMethodCombo(toolbar);

        pathTextField = new JUndoableTextField();
        pathTextField.setPreferredSize(new Dimension(300, 20));
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
        JPanel pathPanel = new JPanel(new BorderLayout(0, 0));
        pathPanel.add(getLockIcon(), BorderLayout.WEST);
        pathPanel.add(pathTextField, BorderLayout.CENTER);
        toolbar.addLabeledFixed("Request URL:", pathPanel);

        toolbar.addSeparator();
        addCheckBox(toolbar);
    }

    private void addCheckBox(JXToolBar toolbar) {
        downloadResources = new JCheckBox();
        try {
            downloadResources.setSelected(((HttpRequest) getModelItem().getHttpRequest())
                    .getDownloadIncludedResources());
        } catch (Exception cce) {
            SoapUI.logError(cce);
        }
        downloadResources.setPreferredSize(new Dimension(17, 17));
        downloadResources.setToolTipText("Download all included resources as attachments!");
        downloadResources.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (1001 == e.getID() && getModelItem() instanceof HttpTestRequestStep) {
                        ((HttpRequest) getModelItem().getHttpRequest())
                                .setDownloadIncludedResources(((JCheckBox) e.getSource()).isSelected());
                    }
                } catch (Exception cce) {
                    SoapUI.logError(cce);
                }

            }
        });
        toolbar.addLabeledFixed("Download Resources", downloadResources);

        toolbar.addSeparator();
    }

    @Override
    protected JComponent buildToolbar() {
        addAssertionButton = createActionButton(new AddAssertionAction(getRequest()), true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(super.buildToolbar(), BorderLayout.NORTH);

        JXToolBar toolbar = UISupport.createToolbar();
        addToolbarComponents(toolbar);

        panel.add(toolbar, BorderLayout.SOUTH);
        return panel;

    }

    @Override
    protected void insertButtons(JXToolBar toolbar) {
        toolbar.add(addAssertionButton);
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
    protected Submit doSubmit() throws SubmitException {
        Analytics.trackAction(SoapUIActions.RUN_TEST_STEP_FROM_PANEL, "StepType", "HTTP",
                "HTTPMethod", getRequest().getMethod().name());
        Analytics.trackAction(SoapUIActions.RUN_TEST_STEP, "HTTPMethod", getRequest().getMethod().name());

        return getRequest().submit(new WsdlTestRunContext(getModelItem()), true);
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

    @Override
    public boolean beforeSubmit(Submit submit, SubmitContext context) {
        boolean result = super.beforeSubmit(submit, context);
        startTime = System.currentTimeMillis();
        return result;
    }

    @Override
    protected void logMessages(String message, String infoMessage) {
        super.logMessages(message, infoMessage);
        logArea.addLine(DateUtil.formatFull(new Date(startTime)) + " - " + message);
    }

    @Override
    public void afterSubmit(Submit submit, SubmitContext context) {
        super.afterSubmit(submit, context);
        if (!isHasClosed()) {
            updateStatusIcon();
        }
    }

    @Override
    public boolean onClose(boolean canCancel) {
        if (super.onClose(canCancel)) {
            assertionsPanel.release();
            inspectorPanel.release();
            SoapUI.getTestMonitor().removeTestMonitorListener(testMonitorListener);
            getModelItem().getTestRequest().removeAssertionsListener(assertionsListener);
            return true;
        }

        return false;
    }

    @Override
    public boolean dependsOn(ModelItem modelItem) {
        if (getRequest().getOperation() == null) {
            return modelItem == getRequest() || modelItem == getModelItem()
                    || ModelSupport.getModelItemProject(getRequest()) == modelItem
                    || modelItem == getModelItem().getTestCase() || modelItem == getModelItem().getTestCase().getTestSuite();
        } else {
            return modelItem == getRequest() || modelItem == getModelItem() || modelItem == getRequest().getOperation()
                    || modelItem == getRequest().getOperation().getInterface()
                    || modelItem == getRequest().getOperation().getInterface().getProject()
                    || modelItem == getModelItem().getTestCase() || modelItem == getModelItem().getTestCase().getTestSuite();
        }
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RestTestRequestInterface.STATUS_PROPERTY)) {
            updateStatusIcon();
        } else if (evt.getPropertyName().equals("path")) {
            getSubmitButton().setEnabled(getSubmit() == null && StringUtils.hasContent(getRequest().getEndpoint()));
        } else if (evt.getPropertyName().equals(AbstractHttpRequest.ENDPOINT_PROPERTY)) {
            // fix SOAP-3369
            getSubmitButton().setEnabled(getSubmit() == null && StringUtils.hasContent(getRequest().getEndpoint()));
            // end of SOAP-3369
            if (updating) {
                return;
            }

            updating = true;
            pathTextField.setText(String.valueOf(evt.getNewValue()));
            updating = false;
        }
        super.propertyChange(evt);
    }

}
