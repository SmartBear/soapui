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

package com.eviware.soapui.impl.support.panels;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.EndpointsComboBoxModel;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.components.RequestMessageXmlEditor;
import com.eviware.soapui.impl.support.components.ResponseMessageXmlEditor;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.Submit.Status;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.actions.ChangeSplitPaneOrientationAction;
import com.eviware.soapui.support.components.Inspector;
import com.eviware.soapui.support.components.JEditorStatusBarWithProgress;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.inspectors.auth.AuthInspectorFactory;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorView;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorView.JEditorStatusBarTargetProxy;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.swing.SoapUISplitPaneUI;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Abstract DesktopPanel for HttpRequests
 *
 * @author Ole.Matzura
 */

public abstract class AbstractHttpRequestDesktopPanel<T extends ModelItem, T2 extends AbstractHttpRequestInterface<?>>
        extends ModelItemDesktopPanel<T> implements SubmitListener {
    private final static Logger log = LogManager.getLogger(AbstractHttpRequestDesktopPanel.class);
    public static final String END_POINT_COMBO_BOX = "EndPointComboBox";

    protected EndpointsComboBoxModel endpointsModel;

    private JButton submitButton;
    private JButton cancelButton;
    private JLabel lockIcon;
    private JComponent endpointComponent;
    private JEditorStatusBarWithProgress statusBar;
    private JButton splitButton;
    private Submit submit;
    private JSplitPane requestSplitPane;
    private MoveFocusAction moveFocusAction;
    private ClosePanelAction closePanelAction = new ClosePanelAction();
    private T2 request;

    private ModelItemXmlEditor<?, ?> requestEditor;
    private ModelItemXmlEditor<?, ?> responseEditor;

    private JTabbedPane requestTabs;
    private JPanel requestTabPanel;
    private JToggleButton tabsButton;

    private boolean responseHasFocus;
    private SubmitAction submitAction;
    private boolean hasClosed;

    public AbstractHttpRequestDesktopPanel(T modelItem, T2 request) {
        super(modelItem);

        this.request = request;

        init(request);

        try {
            // required to avoid deadlock in UI when opening attachments inspector
            if (request.getAttachmentCount() > 0 && request.getOperation() != null) {
                request.getOperation().getInterface().getDefinitionContext().loadIfNecessary();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEndpointsModel(T2 request) {
        this.endpointsModel = new EndpointsComboBoxModel(request);
    }

    public ComboBoxModel getEndpointsModel() {
        return endpointsModel;
    }

    public void setEndpointComponent(JComponent endpointComponent) {
        this.endpointComponent = endpointComponent;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JButton getSplitButton() {
        return splitButton;
    }

    public JToggleButton getTabsButton() {
        return tabsButton;
    }

    protected void init(T2 request) {
        initializeFields();
        setEndpointsModel(request);

        request.addSubmitListener(this);
        request.addPropertyChangeListener(this);

        add(buildContent(), BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildStatusLabel(), BorderLayout.SOUTH);

        //TODO: remove this when other hardcoded sizes are gone
        setPreferredSize(new Dimension(1200, 800));

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

    protected void initializeFields() {

    }

    public final T2 getRequest() {
        return request;
    }

    public final ModelItemXmlEditor<?, ?> getRequestEditor() {
        return requestEditor;
    }

    public final ModelItemXmlEditor<?, ?> getResponseEditor() {
        return responseEditor;
    }

    public Submit getSubmit() {
        return submit;
    }

    protected JComponent buildStatusLabel() {
        statusBar = new JEditorStatusBarWithProgress();
        statusBar.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));

        return statusBar;
    }

    public JEditorStatusBarWithProgress getStatusBar() {
        return statusBar;
    }

    protected JComponent buildContent() {
        requestSplitPane = UISupport.createHorizontalSplit();
        requestSplitPane.setResizeWeight(0.5);
        requestSplitPane.setBorder(null);

        submitAction = new SubmitAction();
        submitButton = createActionButton(submitAction, true);
        submitButton.setEnabled(request.getEndpoint() != null && request.getEndpoint().trim().length() > 0);

        cancelButton = createActionButton(new CancelAction(), false);
        splitButton = createActionButton(new ChangeSplitPaneOrientationAction(requestSplitPane), true);

        tabsButton = new JToggleButton(new ChangeToTabsAction());
        tabsButton.setPreferredSize(UISupport.TOOLBAR_BUTTON_DIMENSION);

        moveFocusAction = new MoveFocusAction();

        requestEditor = buildRequestEditor();
        responseEditor = buildResponseEditor();

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

        requestTabPanel = UISupport.createTabPanel(requestTabs, true);

        if (request.getSettings().getBoolean(UISettings.START_WITH_REQUEST_TABS)) {
            requestTabs.addTab("Request", requestEditor);
            if (responseEditor != null) {
                requestTabs.addTab("Response", responseEditor);
            }
            splitButton.setEnabled(false);
            tabsButton.setSelected(true);

            return requestTabPanel;
        } else {
            requestSplitPane.setTopComponent(requestEditor);
            requestSplitPane.setBottomComponent(responseEditor);
            requestSplitPane.setDividerLocation(0.5);
            return requestSplitPane;
        }
    }

    public SubmitAction getSubmitAction() {
        return submitAction;
    }

    protected abstract ModelItemXmlEditor<?, ?> buildResponseEditor();

    protected abstract ModelItemXmlEditor<?, ?> buildRequestEditor();

    protected JComponent buildToolbar() {
        JPanel endpointPanel = buildEndpointPanel();

        JXToolBar toolbar = UISupport.createToolbar();
        toolbar.add(submitButton);

        insertButtons(toolbar);

        toolbar.add(cancelButton);

        if (endpointComponent != null) {
            toolbar.addSeparator();

            toolbar.add(endpointPanel);
        }

        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(tabsButton);
        toolbar.add(splitButton);
        toolbar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(getHelpUrl())));

        return toolbar;
    }

    protected JComponent buildEndpointComponent() {
        final JComboBox endpointCombo = new JComboBox(endpointsModel);
        endpointCombo.setEditable(true);
        endpointCombo.setName(END_POINT_COMBO_BOX);
        Document textFieldDocument = ((JTextComponent) endpointCombo.getEditor().getEditorComponent()).getDocument();
        endpointsModel.listenToChangesIn(textFieldDocument);
        endpointCombo.addPropertyChangeListener(this);
        endpointCombo.setToolTipText(endpointsModel.getSelectedItem().toString());

        return UISupport.addTooltipListener(endpointCombo, "- no endpoint set for request -");
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AbstractHttpRequest.ENDPOINT_PROPERTY)) {
            submitButton.setEnabled(submit == null && StringUtils.hasContent(request.getEndpoint()));
        }

        super.propertyChange(evt);
    }

    public JButton getSubmitButton() {
        return submitButton;
    }

    protected abstract String getHelpUrl();

    protected abstract void insertButtons(JXToolBar toolbar);

    public void setEnabled(boolean enabled) {
        if (endpointComponent != null) {
            WsdlProject project = (WsdlProject) ModelSupport.getModelItemProject(getModelItem());
            if (project.isEnvironmentMode()) {
                endpointComponent.setEnabled(false);
            } else {
                endpointComponent.setEnabled(true);
            }

        }

        requestEditor.setEditable(enabled);
        if (responseEditor != null) {
            responseEditor.setEditable(enabled);
        }

        submitButton.setEnabled(enabled && request.hasEndpoint());

        statusBar.setIndeterminate(!enabled);
    }

    protected void buildLockIcon() {
        lockIcon = new JLabel(UISupport.createImageIcon("/lock.png"));
        lockIcon.setVisible(isAuthActivated(getRequest().getAuthType()));
        lockIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lockIcon.addMouseListener(new MouseAdapter() {
                                      @Override
                                      public void mouseClicked(MouseEvent e) {
                                          Inspector inspector = getRequestEditor().getInspector(AuthInspectorFactory.INSPECTOR_ID);
                                          if (inspector != null) {
                                              ((AbstractXmlInspector) inspector).showInPanel();
                                          }
                                      }
                                  }
        );
        getRequest().addPropertyChangeListener(AbstractHttpRequest.SELECTED_AUTH_PROFILE_PROPERTY_NAME, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                lockIcon.setVisible(isAuthActivated((String) evt.getNewValue()));
            }
        });

    }

    protected boolean isAuthActivated(String authType) {
        return authType != null && !(authType.equals(CredentialsConfig.AuthType.NO_AUTHORIZATION.toString()));
    }

    protected JPanel buildEndpointPanel() {
        JComponent endpointCombo = buildEndpointComponent();
        setEndpointComponent(endpointCombo);

        JPanel comboBoxPanel = new JPanel(new BorderLayout(0, 0));
        buildLockIcon();
        comboBoxPanel.add(lockIcon, BorderLayout.WEST);
        if (endpointComponent != null) {
            comboBoxPanel.add(endpointCombo, BorderLayout.CENTER);
        }
        return comboBoxPanel;
    }

    protected JLabel getLockIcon() {
        return lockIcon;
    }

    public abstract class AbstractHttpRequestMessageEditor<T3 extends XmlDocument> extends
            RequestMessageXmlEditor<T2, T3> {
        private InputAreaFocusListener inputAreaFocusListener;
        private RSyntaxTextArea inputArea;

        public AbstractHttpRequestMessageEditor(T3 document) {
            super(document, request);

            XmlSourceEditorView<?> editor = getSourceEditor();
            if (editor != null) {
                inputArea = editor.getInputArea();

                if (UISupport.isMac()) {
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("meta ENTER"), submitButton.getAction());
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("meta X"), cancelButton.getAction());
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl meta TAB"), moveFocusAction);
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl F4"), closePanelAction);
                } else {
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("alt ENTER"), submitButton.getAction());
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("alt X"), cancelButton.getAction());
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl alt TAB"), moveFocusAction);
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl F4"), closePanelAction);
                }

                inputAreaFocusListener = new InputAreaFocusListener(editor);
                inputArea.addFocusListener(inputAreaFocusListener);
            }

            submitButton.setMnemonic(KeyEvent.VK_ENTER);

        }

        @Override
        public void release() {
            super.release();
            if (inputArea != null) {
                inputArea.removeFocusListener(inputAreaFocusListener);
            }
        }
    }

    public abstract class AbstractHttpResponseMessageEditor<T3 extends XmlDocument> extends
            ResponseMessageXmlEditor<T2, T3> {
        private RSyntaxTextArea inputArea;
        private ResultAreaFocusListener resultAreaFocusListener;

        public AbstractHttpResponseMessageEditor(T3 document) {
            super(document, request);

            XmlSourceEditorView<?> editor = getSourceEditor();

            inputArea = editor.getInputArea();
            if (inputArea != null) {
                resultAreaFocusListener = new ResultAreaFocusListener(editor);
                inputArea.addFocusListener(resultAreaFocusListener);

                if (UISupport.isMac()) {
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("meta ENTER"), submitButton.getAction());
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("meta X"), cancelButton.getAction());
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl meta TAB"), moveFocusAction);
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl F4"), closePanelAction);
                } else {
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("alt ENTER"), submitButton.getAction());
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("alt X"), cancelButton.getAction());
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl alt TAB"), moveFocusAction);
                    inputArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl F4"), closePanelAction);
                }
            }

            submitButton.setMnemonic(KeyEvent.VK_ENTER);

        }

        @Override
        public void release() {
            super.release();

            if (inputArea != null) {
                inputArea.removeFocusListener(resultAreaFocusListener);
            }
        }
    }

    protected final class InputAreaFocusListener implements FocusListener {
        private final XmlSourceEditorView<?> sourceEditor;

        public InputAreaFocusListener(XmlSourceEditorView<?> editor) {
            this.sourceEditor = editor;
        }

        public void focusGained(FocusEvent e) {
            responseHasFocus = false;

            statusBar.setTarget(new JEditorStatusBarTargetProxy(sourceEditor.getInputArea()));
            if (!splitButton.isEnabled()) {
                requestTabs.setSelectedIndex(0);
                return;
            }

            if (getModelItem().getSettings().getBoolean(UISettings.NO_RESIZE_REQUEST_EDITOR)) {
                return;
            }

            // dont resize if split has been dragged
            if (requestSplitPane.getUI() instanceof SoapUISplitPaneUI
                    && ((SoapUISplitPaneUI) requestSplitPane.getUI()).hasBeenDragged()) {
                return;
            }

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
        private final XmlSourceEditorView<?> sourceEditor;

        public ResultAreaFocusListener(XmlSourceEditorView<?> editor) {
            this.sourceEditor = editor;
        }

        public void focusGained(FocusEvent e) {
            responseHasFocus = true;

            statusBar.setTarget(new JEditorStatusBarTargetProxy(sourceEditor.getInputArea()));
            if (!splitButton.isEnabled()) {
                requestTabs.setSelectedIndex(1);
                return;
            }

            if (request.getSettings().getBoolean(UISettings.NO_RESIZE_REQUEST_EDITOR)) {
                return;
            }

            // dont resize if split has been dragged or result is empty
            if (requestSplitPane.getUI() instanceof SoapUISplitPaneUI
                    && ((SoapUISplitPaneUI) requestSplitPane.getUI()).hasBeenDragged() || request.getResponse() == null) {
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

    public class SubmitAction extends AbstractAction {
        public SubmitAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/submit_request.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Submit request to specified endpoint URL (Alt-Enter)");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("alt ENTER"));
        }

        public void actionPerformed(ActionEvent e) {
            onSubmit();
        }
    }

    protected abstract Submit doSubmit() throws SubmitException;

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

    private class ClosePanelAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            SoapUI.getDesktop().closeDesktopPanel(getModelItem());
        }
    }

    private class MoveFocusAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (requestEditor.hasFocus()) {
                responseEditor.requestFocus();
            } else {
                requestEditor.requestFocus();
            }
        }
    }

    public boolean beforeSubmit(Submit submit, SubmitContext context) {
        if (submit.getRequest() != request) {
            return true;
        }

        if (getModelItem().getSettings().getBoolean(UISettings.AUTO_VALIDATE_REQUEST)) {
            boolean result = requestEditor.saveDocument(true);
            if (!result && getModelItem().getSettings().getBoolean(UISettings.ABORT_ON_INVALID_REQUEST)) {
                statusBar.setInfo("Cancelled request due to invalid content");
                return false;
            }
        } else {
            if (requestEditor != null) {
                requestEditor.saveDocument(false);
            }
        }

        setEnabled(false);
        cancelButton.setEnabled(AbstractHttpRequestDesktopPanel.this.submit != null);
        return true;
    }

    public void afterSubmit(Submit submit, SubmitContext context) {
        if (submit.getRequest() != request) {
            return;
        }

        Status status = submit.getStatus();
        HttpResponse response = (HttpResponse) submit.getResponse();
        if (status == Status.FINISHED || status == Status.ERROR) {
            request.setResponse(response, context);
        }

        if (hasClosed) {
            request.removeSubmitListener(this);
            return;
        }

        cancelButton.setEnabled(false);
        setEnabled(true);

        String message;
        String infoMessage;
        String requestName = request.getOperation() == null ? request.getName() : request.getOperation().getInterface()
                .getName()
                + "." + request.getOperation().getName() + ":" + request.getName();

        if (status == Status.CANCELED) {
            message = "CANCELED";
            infoMessage = "[" + requestName + "] - CANCELED";
        } else {
            if (status == Status.ERROR || response == null) {
                message = "Error getting response; " + submit.getError();
                infoMessage = "Error getting response for [" + requestName + "]; " + submit.getError();
            } else {
                long attchmentsSize = 0;
                if (response.getAttachments().length > 0) {
                    for (Attachment att : response.getAttachments()) {
                        attchmentsSize += att.getSize();
                    }
                }
                message = "response time: " + response.getTimeTaken() + "ms ("
                        + (response.getContentLength() + attchmentsSize) + " bytes)";
                infoMessage = "Got response for [" + requestName + "] in " + response.getTimeTaken() + "ms ("
                        + (response.getContentLength() + attchmentsSize) + " bytes)";

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

        AbstractHttpRequestDesktopPanel.this.submit = null;
    }

    protected void logMessages(String message, String infoMessage) {
        log.info(infoMessage);
        statusBar.setInfo(message);
    }

    public boolean onClose(boolean canCancel) {
        if (canCancel) {
            if (submit != null && submit.getStatus() == Submit.Status.RUNNING) {
                Boolean retVal = UISupport.confirmOrCancel("Cancel request before closing?", "Closing window");
                if (retVal == null) {
                    return false;
                }

                if (retVal && submit.getStatus() == Submit.Status.RUNNING) {
                    submit.cancel();
                }

                hasClosed = true;
            } else {
                request.removeSubmitListener(this);
            }
        } else if (submit != null && submit.getStatus() == Submit.Status.RUNNING) {
            submit.cancel();
            hasClosed = true;
        } else {
            request.removeSubmitListener(this);
        }

        request.removePropertyChangeListener(this);
        requestEditor.saveDocument(false);

        //		if( responseEditor != null )
        //			responseEditor.getParent().remove( responseEditor );

        requestEditor.getParent().remove(requestEditor);
        requestSplitPane.removeAll();

        return release();
    }

    @Override
    protected boolean release() {
        endpointsModel.release();
        requestEditor.release();

        if (responseEditor != null) {
            responseEditor.release();
        }

        return super.release();
    }

    public boolean dependsOn(ModelItem modelItem) {
        return request.dependsOn(modelItem);
    }

    private final class ChangeToTabsAction extends AbstractAction {
        public ChangeToTabsAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/toggle_tabs.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Toggles to tab-based layout");
        }

        public void actionPerformed(ActionEvent e) {
            if (splitButton.isEnabled()) {
                splitButton.setEnabled(false);
                showTabbedView(responseHasFocus);
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
            }

            revalidate();
        }
    }

    private void showTabbedView(boolean respFocus) {
        removeContent(requestSplitPane);
        setContent(requestTabPanel);
        requestTabs.addTab("Request", requestEditor);

        if (responseEditor != null) {
            requestTabs.addTab("Response", responseEditor);
        }

        if (respFocus) {
            requestTabs.setSelectedIndex(1);
            requestEditor.requestFocus();
        }
    }

    public void focusResponseInTabbedView(boolean respFocus) {
        showTabbedView(respFocus);
        getResponseEditor().selectView(2);
    }

    public void setContent(JComponent content) {
        add(content, BorderLayout.CENTER);
    }

    public void removeContent(JComponent content) {
        remove(content);
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

    protected void onCancel() {
        if (submit == null) {
            return;
        }

        cancelButton.setEnabled(false);
        submit.cancel();
        setEnabled(true);
        submit = null;
    }

    public boolean isHasClosed() {
        return hasClosed;
    }

    protected void enableEndpointCombo(final JComboBox endpointCombo, WsdlProject project) {
        if (project.isEnvironmentMode()) {
            endpointCombo.setEnabled(false);
        } else {
            endpointCombo.setEnabled(true);
        }
    }

}
