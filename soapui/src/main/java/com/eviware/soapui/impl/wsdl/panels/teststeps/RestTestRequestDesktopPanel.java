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
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.actions.AddAssertionAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.support.DateUtil;
import com.eviware.soapui.support.ListDataChangeListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.log.JLogList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.util.Date;

public class RestTestRequestDesktopPanel extends AbstractRestRequestDesktopPanel<RestTestRequestStep, RestTestRequest> {
    private JLogList logArea;
    private InternalTestMonitorListener testMonitorListener = new InternalTestMonitorListener();
    private JButton addAssertionButton;
    protected JComboBox methodResourceCombo;
    private AssertionsPanel assertionsPanel;
    private JInspectorPanel inspectorPanel;
    private JComponentInspector<?> assertionInspector;
    private JComponentInspector<?> logInspector;
    private InternalAssertionsListener assertionsListener = new InternalAssertionsListener();
    private long startTime;
    protected JLabel pathLabel;


    public RestTestRequestDesktopPanel(RestTestRequestStep requestStep) {
        super(requestStep, requestStep.getTestRequest());

        SoapUI.getTestMonitor().addTestMonitorListener(testMonitorListener);
        setEnabled(!SoapUI.getTestMonitor().hasRunningTest(requestStep.getTestCase()));

        requestStep.getTestRequest().addAssertionsListener(assertionsListener);
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
        return new AssertionsPanel(getRequest()) {
            protected void selectError(AssertionError error) {
                ModelItemXmlEditor<?, ?> editor = getResponseEditor();
                editor.requestFocus();
            }
        };
    }

    public void setContent(JComponent content) {
        inspectorPanel.setContentComponent(content);
    }

    public void removeContent(JComponent content) {
        inspectorPanel.setContentComponent(null);
    }

    protected String getHelpUrl() {
        return HelpUrls.TESTREQUESTEDITOR_HELP_URL;
    }

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

        return inspectorPanel.getComponent();
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

    @Override
    protected void addTopToolbarComponents(JXToolBar toolBar) {
        //RestTestRequestDesktopPanel does not need any extra top toolbar component
    }

    @Override
    protected void addBottomToolbar(JPanel panel) {
        if (getRequest().getResource() != null) {
            JXToolBar toolbar = UISupport.createToolbar();
            methodResourceCombo = new JComboBox(new PathComboBoxModel());
            methodResourceCombo.setRenderer(new RestMethodListCellRenderer());
            methodResourceCombo.setPreferredSize(new Dimension(200, 20));
            methodResourceCombo.setSelectedItem(getRequest().getRestMethod());

            toolbar.addLabeledFixed("Resource/Method:", methodResourceCombo);
            toolbar.addSeparator();

            pathLabel = new JLabel();
            updateFullPathLabel();

            toolbar.add(pathLabel);

            panel.add(toolbar, BorderLayout.SOUTH);
        }
    }

    @Override
    protected void updateUiValues() {
        updateFullPathLabel();
    }

    private void updateFullPathLabel() {
        if (pathLabel != null && getRequest().getResource() != null) {
            String text = RestUtils.expandPath(getRequest().getResource().getFullPath(), getRequest().getParams(),
                    getRequest());
            pathLabel.setText("[" + text + "]");
            pathLabel.setToolTipText(text);
        }
    }

    @Override
    protected void insertButtons(JXToolBar toolbar) {
        addAssertionButton = createActionButton(new AddAssertionAction(getRequest()), true);
        toolbar.add(addAssertionButton);
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            enabled = !SoapUI.getTestMonitor().hasRunningLoadTest(getModelItem().getTestCase())
                    && !SoapUI.getTestMonitor().hasRunningSecurityTest(getModelItem().getTestCase());
        }

        super.setEnabled(enabled);
        addAssertionButton.setEnabled(enabled);
        assertionsPanel.setEnabled(enabled);

        if (SoapUI.getTestMonitor().hasRunningLoadTest(getRequest().getTestCase())
                || SoapUI.getTestMonitor().hasRunningSecurityTest(getRequest().getTestCase())) {
            getRequest().removeSubmitListener(this);
        } else {
            getRequest().addSubmitListener(this);
        }
    }

    protected Submit doSubmit() throws SubmitException {
        Analytics.trackAction(SoapUIActions.RUN_TEST_STEP_FROM_PANEL, "StepType", "REST",
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

    public boolean beforeSubmit(Submit submit, SubmitContext context) {
        boolean result = super.beforeSubmit(submit, context);
        startTime = System.currentTimeMillis();
        return result;
    }

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

    public boolean dependsOn(ModelItem modelItem) {
        if (getRequest().getResource() == null) {
            return modelItem == getRequest() || modelItem == getModelItem() || modelItem == getRequest().getOperation()
                    || modelItem == getModelItem().getTestCase() || modelItem == getModelItem().getTestCase().getTestSuite()
                    || modelItem == getModelItem().getTestCase().getTestSuite().getProject();
        } else {
            return modelItem == getRequest() || modelItem == getModelItem() || modelItem == getRequest().getOperation()
                    || modelItem == getRequest().getOperation().getInterface()
                    || modelItem == getRequest().getOperation().getInterface().getProject()
                    || modelItem == getModelItem().getTestCase() || modelItem == getModelItem().getTestCase().getTestSuite();
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

        if (evt.getPropertyName().equals(RestTestRequestInterface.STATUS_PROPERTY)) {
            updateStatusIcon();
        }
    }

    private class PathComboBoxModel extends AbstractListModel implements ComboBoxModel {
        public int getSize() {
            int sz = 0;
            for (RestResource resource : getRequest().getResource().getService().getAllResources()) {
                sz += resource.getRestMethodCount();
            }

            return sz;
        }

        public Object getElementAt(int index) {
            int sz = 0;
            for (RestResource resource : getRequest().getResource().getService().getAllResources()) {
                if (index < sz + resource.getRestMethodCount()) {
                    return resource.getRestMethodAt(index - sz);
                }

                sz += resource.getRestMethodCount();
            }

            return null;
        }

        public void setSelectedItem(Object anItem) {
            getRequest().getTestStep().setRestMethod((RestMethod) anItem);
        }

        public Object getSelectedItem() {
            return getRequest().getRestMethod();
        }
    }

    private class RestMethodListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof RestMethod) {
                RestMethod item = (RestMethod) value;
                setIcon(item.getIcon());
                setText(item.getResource().getName() + " -> " + item.getName());
            }

            return result;
        }

    }


}
