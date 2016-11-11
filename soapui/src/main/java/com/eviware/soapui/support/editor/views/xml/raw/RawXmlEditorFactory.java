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

package com.eviware.soapui.support.editor.views.xml.raw;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.panels.teststeps.amf.AMFRequest;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.RequestEditorViewFactory;
import com.eviware.soapui.support.editor.registry.ResponseEditorViewFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RawXmlEditorFactory implements ResponseEditorViewFactory, RequestEditorViewFactory {
    public static final String VIEW_ID = "Raw";

    public String getViewId() {
        return VIEW_ID;
    }

    @SuppressWarnings("unchecked")
    public EditorView<?> createResponseEditorView(Editor<?> editor, ModelItem modelItem) {

        if (modelItem instanceof MessageExchangeModelItem) {
            return new WsdlMessageExchangeResponseRawXmlEditor((MessageExchangeModelItem) modelItem, (XmlEditor) editor);
        } else if (modelItem instanceof AbstractHttpRequestInterface<?>) {
            return new HttpResponseRawXmlEditor((AbstractHttpRequest<?>) modelItem, (XmlEditor) editor);
        } else if (modelItem instanceof WsdlMockResponse) {
            return new WsdlMockResponseRawXmlEditor((WsdlMockResponse) modelItem, (XmlEditor) editor);
        } else if (modelItem instanceof AMFRequestTestStep) {
            return new AmfResponseRawXmlEditor((AMFRequestTestStep) modelItem, (XmlEditor) editor);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public EditorView<XmlDocument> createRequestEditorView(Editor<?> editor, ModelItem modelItem) {
        if (modelItem instanceof MessageExchangeModelItem) {
            return new WsdlMessageExchangeRequestRawXmlEditor((MessageExchangeModelItem) modelItem, (XmlEditor) editor);
        } else if (modelItem instanceof AbstractHttpRequestInterface<?>) {
            return new HttpRequestRawXmlEditor((AbstractHttpRequest<?>) modelItem, (XmlEditor) editor);
        } else if (modelItem instanceof WsdlMockResponse) {
            return new WsdlMockRequestRawXmlEditor((WsdlMockResponse) modelItem, (XmlEditor) editor);
        } else if (modelItem instanceof AMFRequestTestStep) {
            return new AmfRequestRawXmlEditor((AMFRequestTestStep) modelItem, (XmlEditor) editor);
        }

        return null;
    }

    private static class HttpRequestRawXmlEditor extends RawXmlEditor<XmlDocument> {
        private final AbstractHttpRequest<?> request;

        public HttpRequestRawXmlEditor(AbstractHttpRequest<?> request, XmlEditor<XmlDocument> editor) {
            super("Raw", editor, "The actual content of the last submitted request");
            this.request = request;

            request.addPropertyChangeListener(WsdlRequest.RESPONSE_PROPERTY, this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(WsdlRequest.RESPONSE_PROPERTY)) {
                documentUpdated();
            }
        }

        @Override
        public String getContent() {
            if (request.getResponse() == null || request.getResponse().getRawRequestData() == null
                    || request.getResponse().getRawRequestData().length == 0) {
                return "<missing raw request data>";
            }

            byte[] rawRequestData = request.getResponse().getRawRequestData();
            int maxSize = (int) SoapUI.getSettings().getLong(UISettings.RAW_REQUEST_MESSAGE_SIZE, 10000);

            if (maxSize < rawRequestData.length) {
                return new String(Arrays.copyOf(rawRequestData, maxSize));
            } else {
                return new String(rawRequestData);
            }
        }

        @Override
        public void release() {
            request.removePropertyChangeListener(WsdlRequest.RESPONSE_PROPERTY, this);
            super.release();
        }
    }

    private static class HttpResponseRawXmlEditor extends RawXmlEditor<XmlDocument> {
        private final AbstractHttpRequest<?> request;

        public HttpResponseRawXmlEditor(AbstractHttpRequest<?> request, XmlEditor<XmlDocument> editor) {
            super("Raw", editor, "The actual content of the last received response");
            this.request = request;

            request.addPropertyChangeListener(WsdlRequest.RESPONSE_PROPERTY, this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(WsdlRequest.RESPONSE_PROPERTY)) {
                documentUpdated();
            }
        }

        @Override
        public String getContent() {
            if (request.getResponse() == null || request.getResponse().getRawResponseData() == null
                    || request.getResponse().getRawResponseData().length == 0) {
                return "<missing raw response data>";
            }

            byte[] rawResponseData = request.getResponse().getRawResponseData();
            int maxSize = (int) SoapUI.getSettings().getLong(UISettings.RAW_RESPONSE_MESSAGE_SIZE, 10000);

            if (maxSize < rawResponseData.length) {
                return new String(Arrays.copyOf(rawResponseData, maxSize));
            } else {
                return new String(rawResponseData);
            }
        }

        @Override
        public void release() {
            request.removePropertyChangeListener(WsdlRequest.RESPONSE_PROPERTY, this);
            super.release();
        }
    }

    private static class WsdlMockRequestRawXmlEditor extends RawXmlEditor<XmlDocument> {
        private final WsdlMockResponse request;

        public WsdlMockRequestRawXmlEditor(WsdlMockResponse response, XmlEditor<XmlDocument> editor) {
            super("Raw", editor, "The actual content of the last received mock request");
            this.request = response;

            response.addPropertyChangeListener(WsdlMockResponse.MOCKRESULT_PROPERTY, this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(WsdlMockResponse.MOCKRESULT_PROPERTY)) {
                documentUpdated();
            }
        }

        @Override
        public String getContent() {
            if (request.getMockResult() == null) {
                return "<missing request>";
            }

            return buildRawContent(request.getMockResult().getMockRequest().getRequestHeaders(), request.getMockResult()
                    .getMockRequest().getRawRequestData());
        }

        @Override
        public void release() {
            request.removePropertyChangeListener(WsdlMockResponse.MOCKRESULT_PROPERTY, this);
            super.release();
        }
    }

    private static class WsdlMockResponseRawXmlEditor extends RawXmlEditor<XmlDocument> {
        private final WsdlMockResponse request;

        public WsdlMockResponseRawXmlEditor(WsdlMockResponse response, XmlEditor<XmlDocument> editor) {
            super("Raw", editor, "The actual content of the last returned Mock Response");
            this.request = response;

            response.addPropertyChangeListener(WsdlMockResponse.MOCKRESULT_PROPERTY, this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(WsdlMockResponse.MOCKRESULT_PROPERTY)) {
                documentUpdated();
            }
        }

        @Override
        public String getContent() {
            if (request.getMockResult() == null) {
                return "<missing response>";
            }

            StringToStringsMap headers = request.getMockResult().getResponseHeaders();
            byte[] data = request.getMockResult().getRawResponseData();

            return buildRawContent(headers, data);
        }

        @Override
        public void release() {
            request.removePropertyChangeListener(WsdlMockResponse.MOCKRESULT_PROPERTY, this);
            super.release();
        }
    }

    private static String buildRawContent(StringToStringsMap headers, byte[] data) {
        StringBuffer result = new StringBuffer();
        String status = headers.get("#status#", "");
        if (status != null) {
            result.append(status).append('\n');
        }

        for (Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
            if (headerEntry.getKey().equals("#status#")) {
                continue;
            }

            for (String value : headerEntry.getValue()) {
                result.append(headerEntry.getKey()).append(": ").append(value).append('\n');
            }
        }
        result.append('\n');

        if (data != null) {
            result.append(new String(data).trim());
        }

        return result.toString().trim();
    }

    private static class WsdlMessageExchangeResponseRawXmlEditor extends RawXmlEditor<XmlDocument> {
        private final MessageExchangeModelItem response;

        public WsdlMessageExchangeResponseRawXmlEditor(MessageExchangeModelItem response, XmlEditor<XmlDocument> editor) {
            super("Raw", editor, "The raw response data");
            this.response = response;
        }

        @Override
        public String getContent() {
            MessageExchange me = response.getMessageExchange();
            return me == null || me.getRawResponseData() == null ? "<missing raw response data>" : new String(
                    me.getRawResponseData());
        }
    }

    private static class WsdlMessageExchangeRequestRawXmlEditor extends RawXmlEditor<XmlDocument> {
        private final MessageExchangeModelItem request;

        public WsdlMessageExchangeRequestRawXmlEditor(MessageExchangeModelItem request, XmlEditor<XmlDocument> editor) {
            super("Raw", editor, "The raw request data");
            this.request = request;
        }

        @Override
        public String getContent() {
            MessageExchange me = request.getMessageExchange();
            return me == null || me.getRawRequestData() == null ? "<missing raw request data>" : new String(
                    me.getRawRequestData());
        }
    }

    private static class AmfResponseRawXmlEditor extends RawXmlEditor<XmlDocument> {
        private final AMFRequest request;

        public AmfResponseRawXmlEditor(AMFRequestTestStep requestTestStep, XmlEditor<XmlDocument> editor) {
            super("Raw", editor, "The actual content of the last received response");
            this.request = requestTestStep.getAMFRequest();

            request.addPropertyChangeListener(AMFRequest.AMF_RESPONSE_PROPERTY, this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            documentUpdated();
        }

        @Override
        public String getContent() {
            if (request.getResponse() == null) {
                return "<missing response>";
            }

            byte[] rawResponseData = request.getResponse().getRawResponseData();
            int maxSize = (int) SoapUI.getSettings().getLong(UISettings.RAW_RESPONSE_MESSAGE_SIZE, 10000);

            if (maxSize < rawResponseData.length) {
                return new String(Arrays.copyOf(rawResponseData, maxSize));
            } else {
                return new String(rawResponseData);
            }
        }

        @Override
        public void release() {
            request.removePropertyChangeListener(AMFRequest.AMF_RESPONSE_PROPERTY, this);
            super.release();
        }
    }

    private static class AmfRequestRawXmlEditor extends RawXmlEditor<XmlDocument> {
        private final AMFRequest request;

        public AmfRequestRawXmlEditor(AMFRequestTestStep requestTestStep, XmlEditor<XmlDocument> editor) {
            super("Raw", editor, "The actual content of the last request");
            this.request = requestTestStep.getAMFRequest();

            request.addPropertyChangeListener(AMFRequest.AMF_RESPONSE_PROPERTY, this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            documentUpdated();
        }

        @Override
        public String getContent() {
            if (request.getResponse() == null) {
                return "";
            }

            byte[] rawRequestData = request.getResponse().getRawRequestData();
            int maxSize = (int) SoapUI.getSettings().getLong(UISettings.RAW_RESPONSE_MESSAGE_SIZE, 10000);

            if (maxSize < rawRequestData.length) {
                return new String(Arrays.copyOf(rawRequestData, maxSize));
            } else {
                return new String(rawRequestData);
            }
        }

        @Override
        public void release() {
            request.removePropertyChangeListener(AMFRequest.AMF_RESPONSE_PROPERTY, this);
            super.release();
        }
    }
}
