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

package com.eviware.soapui.impl.wsdl.panels.request;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.components.RequestXmlDocument;
import com.eviware.soapui.impl.support.components.ResponseXmlDocument;
import com.eviware.soapui.impl.support.panels.AbstractHttpRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.actions.request.AddRequestToMockServiceAction;
import com.eviware.soapui.impl.wsdl.actions.request.CloneRequestAction;
import com.eviware.soapui.impl.wsdl.actions.request.CreateEmptyRequestAction;
import com.eviware.soapui.impl.wsdl.actions.request.RecreateRequestAction;
import com.eviware.soapui.impl.wsdl.panels.request.actions.WSIValidateRequestAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.support.DefaultXmlDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

/**
 * Abstract DesktopPanel for WsdlRequests
 *
 * @author Ole.Matzura
 */

public abstract class AbstractWsdlRequestDesktopPanel<T extends ModelItem, T2 extends WsdlRequest> extends
        AbstractHttpRequestDesktopPanel<T, T2> implements SubmitListener {
    private JButton recreateButton;
    private JButton cloneButton;
    private JButton createEmptyButton;
    private JButton addToMockServiceButton;
    private AbstractAction wsiValidateAction;

    public AbstractWsdlRequestDesktopPanel(T modelItem, T2 request) {
        super(modelItem, request);
    }

    @Override
    protected void init(T2 request) {
        recreateButton = createActionButton(new RecreateRequestAction(request), true);
        addToMockServiceButton = createActionButton(SwingActionDelegate.createDelegate(
                AddRequestToMockServiceAction.SOAPUI_ACTION_ID, request, null, "/addToMockService.gif"), true);

        cloneButton = createActionButton(SwingActionDelegate.createDelegate(CloneRequestAction.SOAPUI_ACTION_ID,
                request, null, "/clone.png"), true);

        createEmptyButton = createActionButton(new CreateEmptyRequestAction(request), true);

        wsiValidateAction = SwingActionDelegate.createDelegate(new WSIValidateRequestAction(), request, "alt W");
        wsiValidateAction.setEnabled(request.getResponse() != null);

        super.init(request);
    }

    protected ModelItemXmlEditor<?, ?> buildResponseEditor() {
        return new WsdlResponseMessageEditor(new ResponseXmlDocument(getRequest()));
    }

    protected ModelItemXmlEditor<?, ?> buildRequestEditor() {
        return new WsdlRequestMessageEditor(new RequestXmlDocument(getRequest()));
    }

    public static class OneWayResponseMessageEditor extends ModelItemXmlEditor<ModelItem, DefaultXmlDocument> {
        public OneWayResponseMessageEditor(ModelItem modelItem) {
            super(new DefaultXmlDocument(), modelItem);
        }
    }

    protected void insertButtons(JXToolBar toolbar) {
        toolbar.add(addToMockServiceButton);
        toolbar.add(recreateButton);
        toolbar.add(createEmptyButton);
        toolbar.add(cloneButton);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        recreateButton.setEnabled(enabled);
        createEmptyButton.setEnabled(enabled);
        cloneButton.setEnabled(enabled);
    }

    public class WsdlRequestMessageEditor extends
            AbstractHttpRequestDesktopPanel<T, T2>.AbstractHttpRequestMessageEditor<XmlDocument> {
        public WsdlRequestMessageEditor(XmlDocument document) {
            super(document);

            XmlSourceEditorView<?> editor = getSourceEditor();
            RSyntaxTextArea inputArea = editor.getInputArea();
            inputArea.getInputMap().put(KeyStroke.getKeyStroke("F5"), recreateButton.getAction());
        }
    }

    public class WsdlResponseMessageEditor extends
            AbstractHttpRequestDesktopPanel<T, T2>.AbstractHttpResponseMessageEditor<XmlDocument> {
        public WsdlResponseMessageEditor(XmlDocument document) {
            super(document);

            XmlSourceEditorView<?> editor = getSourceEditor();

            JPopupMenu inputPopup = editor.getEditorPopup();
            inputPopup.insert(new JSeparator(), 2);
            inputPopup.insert(wsiValidateAction, 3);
            inputPopup.insert(new JSeparator(), 4);
        }
    }

    protected Submit doSubmit() throws SubmitException {
        Analytics.trackAction(SoapUIActions.RUN_REQUEST_FROM_REQUEST_EDITOR.getActionName(), "Type", "SOAP");
        return getRequest().submit(new WsdlSubmitContext(getModelItem()), true);
    }

    public boolean beforeSubmit(Submit submit, SubmitContext context) {
        boolean result = super.beforeSubmit(submit, context);
        wsiValidateAction.setEnabled(!result);
        return result;
    }

    public void afterSubmit(Submit submit, SubmitContext context) {
        super.afterSubmit(submit, context);
        if (!isHasClosed()) {
            wsiValidateAction.setEnabled(submit.getResponse() != null);
        }
    }
}
