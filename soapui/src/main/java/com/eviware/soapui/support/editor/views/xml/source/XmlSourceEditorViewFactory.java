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

package com.eviware.soapui.support.editor.views.xml.source;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wadl.support.WadlValidator;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.AddWsaHeadersToMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.ApplyOutgoingWSSToMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.RemoveAllOutgoingWSSFromMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.RemoveWsaHeadersFromMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.request.AddWSSUsernameTokenAction;
import com.eviware.soapui.impl.wsdl.actions.request.AddWSTimestampAction;
import com.eviware.soapui.impl.wsdl.actions.request.AddWsaHeadersToRequestAction;
import com.eviware.soapui.impl.wsdl.actions.request.ApplyOutgoingWSSToRequestAction;
import com.eviware.soapui.impl.wsdl.actions.request.RemoveAllOutgoingWSSFromRequestAction;
import com.eviware.soapui.impl.wsdl.actions.request.RemoveWsaHeadersFromRequestAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockResultMessageExchange;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlValidator;
import com.eviware.soapui.impl.wsdl.support.wss.DefaultWssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.RestResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.RequestEditorViewFactory;
import com.eviware.soapui.support.editor.registry.ResponseEditorViewFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import com.eviware.soapui.support.editor.xml.XmlEditorView;
import com.eviware.soapui.support.editor.xml.support.ValidationError;
import com.eviware.soapui.support.propertyexpansion.PropertyExpansionPopupListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.List;

/**
 * Factory for default "XML" source editor view in SoapUI
 *
 * @author ole.matzura
 */

public class XmlSourceEditorViewFactory implements ResponseEditorViewFactory, RequestEditorViewFactory {
    public static final String VIEW_ID = "Source";

    @SuppressWarnings("unchecked")
    public XmlEditorView createEditorView(XmlEditor editor) {
        return new XmlSourceEditorView<ModelItem>(editor, null, false);
    }

    public String getViewId() {
        return VIEW_ID;
    }

    @SuppressWarnings("unchecked")
    public EditorView<?> createRequestEditorView(Editor<?> editor, ModelItem modelItem) {
        if (modelItem instanceof WsdlRequest) {
            return new WsdlRequestXmlSourceEditor((XmlEditor) editor, (WsdlRequest) modelItem);
        } else if (modelItem instanceof WsdlMockResponse) {
            return new WsdlMockRequestXmlSourceEditor((XmlEditor) editor, (WsdlMockResponse) modelItem);
        } else if (modelItem instanceof MessageExchangeModelItem) {
            return new XmlSourceEditorView<MessageExchangeModelItem>((XmlEditor) editor,
                    (MessageExchangeModelItem) modelItem, false);
        } else if (modelItem instanceof RestMockResponse) {
            boolean readOnly = false;
            return new XmlSourceEditorView((XmlEditor) editor, modelItem, readOnly);
        }


        return null;
    }

    @SuppressWarnings("unchecked")
    public EditorView<?> createResponseEditorView(Editor<?> editor, ModelItem modelItem) {
        if (modelItem instanceof WsdlRequest) {
            return new WsdlResponseXmlSourceEditor((XmlEditor) editor, (WsdlRequest) modelItem);
        } else if (modelItem instanceof WsdlMockResponse) {
            return new WsdlMockResponseXmlSourceEditor((XmlEditor) editor, (WsdlMockResponse) modelItem);
        } else if (modelItem instanceof HttpRequestInterface<?>) {
            return new RestResponseXmlSourceEditor((XmlEditor) editor, (HttpRequestInterface<?>) modelItem);
        } else if (modelItem instanceof MessageExchangeModelItem) {
            return new XmlSourceEditorView<MessageExchangeModelItem>((XmlEditor) editor,
                    (MessageExchangeModelItem) modelItem, true);
        } else if (modelItem instanceof JdbcRequestTestStep) {
            return new XmlSourceEditorView<JdbcRequestTestStep>((XmlEditor) editor, (JdbcRequestTestStep) modelItem,
                    true);
        } else if (modelItem instanceof AMFRequestTestStep) {
            return new XmlSourceEditorView<AMFRequestTestStep>((XmlEditor) editor, (AMFRequestTestStep) modelItem, true);
        } else if (modelItem instanceof RestMockResponse) {
            boolean readOnly = false;
            return new XmlSourceEditorView((XmlEditor) editor, modelItem, readOnly, "Editor");
        }

        return null;
    }

    /**
     * XmlSource editor for a WsdlRequest
     *
     * @author ole.matzura
     */

    public static class WsdlRequestXmlSourceEditor extends XmlSourceEditorView<WsdlRequest> {
        private JMenu applyMenu;
        private JMenu wsaApplyMenu;

        @SuppressWarnings("unchecked")
        public WsdlRequestXmlSourceEditor(XmlEditor xmlEditor, WsdlRequest request) {
            super(xmlEditor, request, false);
        }

        protected ValidationError[] validateXml(String xml) {
            WsdlOperation operation = getModelItem().getOperation();
            WsdlValidator validator = new WsdlValidator((operation.getInterface()).getWsdlContext());

            WsdlResponseMessageExchange wsdlResponseMessageExchange = new WsdlResponseMessageExchange(getModelItem());
            wsdlResponseMessageExchange.setRequestContent(xml);
            return validator.assertRequest(wsdlResponseMessageExchange, false);
        }

        @Override
        protected void buildUI() {
            super.buildUI();
            PropertyExpansionPopupListener.enable(getInputArea(), getModelItem(), getInputArea().getPopupMenu());
        }

        protected void buildPopup(JPopupMenu inputPopup, RSyntaxTextArea editArea) {
            super.buildPopup(inputPopup, editArea);

            inputPopup.insert(new JSeparator(), 2);
            inputPopup.insert(new AddWSSUsernameTokenAction(getModelItem()), 3);
            inputPopup.insert(new AddWSTimestampAction(getModelItem()), 4);
            inputPopup.insert(applyMenu = new JMenu("Outgoing WSS"), 5);
            inputPopup.insert(wsaApplyMenu = new JMenu("WS-A headers"), 6);
            inputPopup.insert(new JSeparator(), 7);

            inputPopup.addPopupMenuListener(new PopupMenuListener() {

                public void popupMenuCanceled(PopupMenuEvent e) {

                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

                }

                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    applyMenu.removeAll();
                    DefaultWssContainer wss = getModelItem().getOperation().getInterface().getProject().getWssContainer();
                    List<OutgoingWss> outgoingWssList = wss.getOutgoingWssList();
                    applyMenu.setEnabled(!outgoingWssList.isEmpty());

                    for (OutgoingWss outgoing : outgoingWssList) {
                        applyMenu.add(new ApplyOutgoingWSSToRequestAction(getModelItem(), outgoing));
                    }
                    applyMenu.add(new RemoveAllOutgoingWSSFromRequestAction(getModelItem()));

                    wsaApplyMenu.removeAll();
                    wsaApplyMenu.add(new AddWsaHeadersToRequestAction(getModelItem()));
                    wsaApplyMenu.add(new RemoveWsaHeadersFromRequestAction(getModelItem()));
                    wsaApplyMenu.setEnabled(getModelItem().getWsaConfig().isWsaEnabled());
                }
            });
        }

        public WsdlRequest getRequest() {
            return getModelItem();
        }
    }

    /**
     * XmlSource editor for a WsdlMockRequest
     *
     * @author ole.matzura
     */

    public static class WsdlMockRequestXmlSourceEditor extends XmlSourceEditorView<WsdlMockResponse> {
        @SuppressWarnings("unchecked")
        public WsdlMockRequestXmlSourceEditor(XmlEditor xmlEditor, WsdlMockResponse mockResponse) {
            super(xmlEditor, mockResponse, false);
        }

        protected ValidationError[] validateXml(String xml) {
            WsdlOperation operation = getModelItem().getMockOperation().getOperation();

            if (operation == null) {
                return new ValidationError[]{new AssertionError("Missing operation for MockResponse")};
            }

            WsdlValidator validator = new WsdlValidator((operation.getInterface()).getWsdlContext());
            WsdlMockResultMessageExchange messageExchange =
                    new WsdlMockResultMessageExchange((WsdlMockResult) getModelItem().getMockResult(), getModelItem());
            return validator.assertRequest(messageExchange, false);
        }

        protected void buildPopup(JPopupMenu inputPopup, RSyntaxTextArea editArea) {
            super.buildPopup(inputPopup, editArea);
            // inputPopup.insert( new JSeparator(), 2 );
        }
    }

    /**
     * XmlSource editor for a WsdlResponse
     *
     * @author ole.matzura
     */

    public static class WsdlResponseXmlSourceEditor extends XmlSourceEditorView<WsdlRequest> {
        @SuppressWarnings("unchecked")
        public WsdlResponseXmlSourceEditor(XmlEditor xmlEditor, WsdlRequest request) {
            super(xmlEditor, request, true);
        }

        protected ValidationError[] validateXml(String xml) {
            if (getModelItem() instanceof WsdlTestRequest) {
                WsdlTestRequest testRequest = (WsdlTestRequest) getModelItem();
                testRequest.assertResponse(new WsdlTestRunContext(testRequest.getTestStep()));
            }

            WsdlOperation operation = getModelItem().getOperation();
            WsdlValidator validator = new WsdlValidator((operation.getInterface()).getWsdlContext());

            return validator.assertResponse(new WsdlResponseMessageExchange(getModelItem()), false);
        }
    }

    /**
     * XmlSource editor for a WsdlMockResponse
     *
     * @author ole.matzura
     */

    public static class WsdlMockResponseXmlSourceEditor extends XmlSourceEditorView<WsdlMockResponse> {
        private JMenu applyMenu;
        private JMenu wsaApplyMenu;

        @SuppressWarnings("unchecked")
        public WsdlMockResponseXmlSourceEditor(XmlEditor xmlEditor, WsdlMockResponse mockResponse) {
            super(xmlEditor, mockResponse, false);
        }

        @Override
        protected void buildUI() {
            super.buildUI();

            getValidateXmlAction().setEnabled(getModelItem().getMockOperation().getOperation().isBidirectional());
        }

        protected ValidationError[] validateXml(String xml) {
            WsdlOperation operation = getModelItem().getMockOperation().getOperation();
            if (operation == null) {
                return new ValidationError[]{new AssertionError("Missing operation for MockResponse")};
            }

            WsdlValidator validator = new WsdlValidator((operation.getInterface()).getWsdlContext());
            return validator.assertResponse(new WsdlMockResponseMessageExchange(getModelItem()), false);
        }

        public WsdlMockResponse getMockResponse() {
            return getModelItem();
        }

        protected void buildPopup(JPopupMenu inputPopup, RSyntaxTextArea editArea) {
            super.buildPopup(inputPopup, editArea);

            inputPopup.insert(applyMenu = new JMenu("Outgoing WSS"), 2);
            inputPopup.insert(wsaApplyMenu = new JMenu("WS-A headers"), 3);

            inputPopup.addPopupMenuListener(new PopupMenuListener() {

                public void popupMenuCanceled(PopupMenuEvent e) {

                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

                }

                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    applyMenu.removeAll();
                    WsdlProject project = getModelItem().getMockOperation().getMockService().getProject();
                    DefaultWssContainer wss = project.getWssContainer();
                    List<OutgoingWss> outgoingWssList = wss.getOutgoingWssList();
                    applyMenu.setEnabled(!outgoingWssList.isEmpty());

                    for (OutgoingWss outgoing : outgoingWssList) {
                        applyMenu.add(new ApplyOutgoingWSSToMockResponseAction(getModelItem(), outgoing));
                    }
                    applyMenu.add(new RemoveAllOutgoingWSSFromMockResponseAction(getModelItem()));

                    wsaApplyMenu.removeAll();
                    wsaApplyMenu.add(new AddWsaHeadersToMockResponseAction(getModelItem()));
                    wsaApplyMenu.add(new RemoveWsaHeadersFromMockResponseAction(getModelItem()));
                    wsaApplyMenu.setEnabled(getModelItem().getWsaConfig().isWsaEnabled());

                }
            });
        }
    }

    private class RestResponseXmlSourceEditor extends XmlSourceEditorView<HttpRequestInterface<?>> {
        public RestResponseXmlSourceEditor(XmlEditor<XmlDocument> xmlEditor, HttpRequestInterface<?> restRequest) {
            super(xmlEditor, restRequest, true);
        }

        @SuppressWarnings("unchecked")
        protected ValidationError[] validateXml(String xml) {
            if (getModelItem() instanceof HttpRequestInterface
                    || ((RestRequestInterface) getModelItem()).getResource() == null) {
                return new ValidationError[0];
            }

            WadlValidator validator = new WadlValidator(((RestRequestInterface) getModelItem()).getResource()
                    .getService().getWadlContext());
            return validator.assertResponse(new RestResponseMessageExchange((RestRequest) getModelItem()));
        }
    }
}
