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

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import com.eviware.soapui.impl.wsdl.actions.mockresponse.OpenRequestForMockResponseAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.CreateEmptyWsdlMockResponseAction;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.CreateFaultWsdlMockResponseAction;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.RecreateMockResponseAction;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.WSIValidateResponseAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.ui.support.AbstractMockResponseDesktopPanel;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class AbstractWsdlMockResponseDesktopPanel<ModelItemType extends ModelItem>
        extends AbstractMockResponseDesktopPanel<ModelItemType, WsdlMockResponse> {
    private JButton createEmptyButton;
    private JButton createFaultButton;
    private AbstractAction wsiValidateAction;

    private InternalPropertyChangeListener propertyChangeListener = new InternalPropertyChangeListener();

    private JButton openRequestButton;
    private JButton recreateButton;

    public AbstractWsdlMockResponseDesktopPanel(ModelItemType modelItem) {
        super(modelItem);
        modelItem.addPropertyChangeListener(propertyChangeListener);
    }

    protected JComponent buildContent() {
        MockResponse mockResponse = getMockResponse();

        createEmptyButton = createActionButton(new CreateEmptyWsdlMockResponseAction(mockResponse), isBidirectional());
        createFaultButton = createActionButton(new CreateFaultWsdlMockResponseAction(mockResponse), isBidirectional());
        wsiValidateAction = SwingActionDelegate.createDelegate(new WSIValidateResponseAction(), mockResponse, "alt W");

        openRequestButton = createActionButton(SwingActionDelegate.createDelegate(
                OpenRequestForMockResponseAction.SOAPUI_ACTION_ID, mockResponse, null, "/open_request.gif"), true);

        recreateButton = createActionButton(new RecreateMockResponseAction(mockResponse), isBidirectional());

        return super.buildContent();
    }

    protected void createToolbar(JXToolBar toolbar) {
        toolbar.add(openRequestButton);
        toolbar.addUnrelatedGap();
        toolbar.add(recreateButton);

        toolbar.add(createEmptyButton);
        toolbar.add(createFaultButton);
    }

    public void setEnabled(boolean enabled) {
        recreateButton.setEnabled(enabled);
        createEmptyButton.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    protected boolean isBidirectional() {
        return getMockResponse().getMockOperation().getOperation().isBidirectional();
    }

    @Override
    public String getHelpUrl() {
        return HelpUrls.REQUESTEDITOR_HELP_URL;
    }

    private final class InternalPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(WsdlMockResponse.MOCKRESULT_PROPERTY)) {
                wsiValidateAction.setEnabled(isBidirectional());
            }
        }
    }

    public boolean onClose(boolean canCancel) {
        getMockResponse().removePropertyChangeListener(propertyChangeListener);
        return super.onClose(canCancel);
    }

    public class WsdlMockResponseMessageEditor extends MockResponseMessageEditor {
        public WsdlMockResponseMessageEditor(XmlDocument document) {
            super(document);

            if (isBidirectional()) {
                XmlSourceEditorView<?> editor = getSourceEditor();
                JPopupMenu inputPopup = editor.getEditorPopup();
                inputPopup.insert(wsiValidateAction, 3);
            }
        }
    }

    protected MockResponseMessageEditor buildResponseEditor() {
        return new WsdlMockResponseMessageEditor(new MockResponseXmlDocument(getMockResponse()));
    }


}
