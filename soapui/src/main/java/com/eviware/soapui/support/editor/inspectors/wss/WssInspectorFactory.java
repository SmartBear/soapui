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

package com.eviware.soapui.support.editor.inspectors.wss;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;
import com.eviware.soapui.support.editor.xml.XmlInspector;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

public class WssInspectorFactory implements RequestInspectorFactory, ResponseInspectorFactory {
    public static final String INSPECTOR_ID = "WSS";

    public String getInspectorId() {
        return INSPECTOR_ID;
    }

    public EditorInspector<?> createRequestInspector(Editor<?> editor, ModelItem modelItem) {
        if (modelItem instanceof WsdlMockResponse) {
            return new WsdlMockRequestWssInspector((WsdlMockResponse) modelItem);
        } else if (modelItem instanceof MessageExchangeModelItem) {
            return new RequestMessageExchangeWssInspector((MessageExchangeModelItem) modelItem);
        }

        return null;
    }

    public EditorInspector<?> createResponseInspector(Editor<?> editor, ModelItem modelItem) {
        if (modelItem instanceof WsdlRequest) {
            return new WsdlResponseWssInspector((WsdlRequest) modelItem);
        } else if (modelItem instanceof MessageExchangeModelItem) {
            return new ResponseMessageExchangeWssInspector((MessageExchangeModelItem) modelItem);
        }

        return null;
    }

    public class WsdlMockRequestWssInspector extends AbstractWssInspector implements XmlInspector,
            PropertyChangeListener {
        private final WsdlMockResponse response;

        public WsdlMockRequestWssInspector(WsdlMockResponse response) {
            this.response = response;

            response.addPropertyChangeListener(WsdlMockResponse.MOCKRESULT_PROPERTY, this);
        }

        @Override
        public Vector<?> getWssResults() {
            return response.getMockResult() == null ? null : ((WsdlMockResult) response.getMockResult()).getRequestWssResult();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            update();
        }

        @Override
        public void release() {
            response.removePropertyChangeListener(WsdlMockResponse.MOCKRESULT_PROPERTY, this);
        }
    }

    public class RequestMessageExchangeWssInspector extends AbstractWssInspector implements XmlInspector,
            PropertyChangeListener {
        private final MessageExchangeModelItem item;

        public RequestMessageExchangeWssInspector(MessageExchangeModelItem item) {
            this.item = item;

            item.addPropertyChangeListener(MessageExchangeModelItem.MESSAGE_EXCHANGE, this);
        }

        @Override
        public Vector<?> getWssResults() {
            return item.getMessageExchange() instanceof WsdlMessageExchange ? ((WsdlMessageExchange) item
                    .getMessageExchange()).getRequestWssResult() : null;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            update();
        }

        @Override
        public void release() {
            item.removePropertyChangeListener(MessageExchangeModelItem.MESSAGE_EXCHANGE, this);
        }
    }

    public class WsdlResponseWssInspector extends AbstractWssInspector implements XmlInspector, PropertyChangeListener {
        private final WsdlRequest response;

        public WsdlResponseWssInspector(WsdlRequest response) {
            this.response = response;

            response.addPropertyChangeListener(WsdlRequest.RESPONSE_PROPERTY, this);
        }

        @Override
        public Vector<?> getWssResults() {
            return response.getResponse() == null ? null : response.getResponse().getWssResult();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            update();
        }

        @Override
        public void release() {
            response.removePropertyChangeListener(WsdlRequest.RESPONSE_PROPERTY, this);
        }
    }

    public class ResponseMessageExchangeWssInspector extends AbstractWssInspector implements XmlInspector,
            PropertyChangeListener {
        private final MessageExchangeModelItem item;

        public ResponseMessageExchangeWssInspector(MessageExchangeModelItem item) {
            this.item = item;

            item.addPropertyChangeListener(MessageExchangeModelItem.MESSAGE_EXCHANGE, this);
        }

        @Override
        public Vector<?> getWssResults() {
            return item.getMessageExchange() instanceof WsdlMessageExchange ? ((WsdlMessageExchange) item
                    .getMessageExchange()).getResponseWssResult() : null;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            update();
        }

        @Override
        public void release() {
            item.removePropertyChangeListener(MessageExchangeModelItem.MESSAGE_EXCHANGE, this);
        }
    }
}
