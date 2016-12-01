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

package com.eviware.soapui.support.editor.inspectors.jms.property;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.HermesJmsRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSEndpoint;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSHeader;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.JMSUtils;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.inspectors.jms.property.JMSHeaderAndPropertyInspectorModel.AbstractJMSHeaderAndPropertyModel;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;
import com.eviware.soapui.support.types.StringToStringMap;

import javax.jms.Message;
import java.beans.PropertyChangeEvent;

public class JMSHeaderAndPropertyInspectorFactory implements RequestInspectorFactory, ResponseInspectorFactory {
    public static final String INSPECTOR_ID = "JMS Headers and Properties";

    public String getInspectorId() {
        return INSPECTOR_ID;
    }

    public EditorInspector<?> createRequestInspector(Editor<?> editor, ModelItem modelItem) {
        if (modelItem instanceof MessageExchangeModelItem) {
            JMSHeaderAndPropertyInspector inspector = new JMSHeaderAndPropertyInspector(
                    (JMSHeaderAndPropertyInspectorModel) new MessageExchangeRequestJMSHeaderAndPropertiesModel(
                            (MessageExchangeModelItem) modelItem));
            inspector.setEnabled(JMSUtils.checkIfJMS(modelItem));
            return inspector;
        }
        return null;
    }

    public EditorInspector<?> createResponseInspector(Editor<?> editor, ModelItem modelItem) {

        if (modelItem instanceof AbstractHttpRequest<?>) {
            JMSHeaderAndPropertyInspector inspector = new JMSHeaderAndPropertyInspector(
                    (JMSHeaderAndPropertyInspectorModel) new ResponseJMSHeaderAndPropertiesModel(
                            (AbstractHttpRequest<?>) modelItem));
            inspector.setEnabled(JMSUtils.checkIfJMS(modelItem));
            return inspector;
        } else if (modelItem instanceof MessageExchangeModelItem) {

            JMSHeaderAndPropertyInspector inspector = new JMSHeaderAndPropertyInspector(
                    (JMSHeaderAndPropertyInspectorModel) new MessageExchangeResponseJMSHeaderAndPropertiesModel(
                            (MessageExchangeModelItem) modelItem));
            inspector.setEnabled(JMSUtils.checkIfJMS(modelItem));
            return inspector;

        }
        return null;
    }

    private class ResponseJMSHeaderAndPropertiesModel extends AbstractJMSHeaderAndPropertyModel<AbstractHttpRequest<?>>
            implements SubmitListener {
        AbstractHttpRequest<?> request;
        JMSHeaderAndPropertyInspector inspector;
        StringToStringMap headersAndProperties;

        public ResponseJMSHeaderAndPropertiesModel(AbstractHttpRequest<?> wsdlRequest) {
            super(true, wsdlRequest, "jmsHeaderAndProperties");
            this.request = wsdlRequest;
            request.addSubmitListener(this);
            request.addPropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (request.getEndpoint() != null && evt.getPropertyName().equals(AbstractHttpRequest.ENDPOINT_PROPERTY)) {
                inspector.setEnabled(request.getEndpoint().startsWith(JMSEndpoint.JMS_ENDPOINT_PREFIX));
            }
            super.propertyChange(evt);
        }

        public void release() {
            super.release();

            request.removeSubmitListener(this);
            request.removePropertyChangeListener(this);
        }

        public StringToStringMap getJMSHeadersAndProperties() {
            return headersAndProperties;
        }

        public void afterSubmit(Submit submit, SubmitContext context) {
            headersAndProperties = new StringToStringMap();
            JMSResponse jmsResponse = (JMSResponse) context.getProperty(HermesJmsRequestTransport.JMS_RESPONSE);
            if (jmsResponse instanceof JMSResponse) {
                Message message = jmsResponse.getMessageReceive();
                if (message != null) {
                    headersAndProperties.putAll(JMSHeader.getMessageHeadersAndProperties(message).toStringToStringMap());
                }
            }
            inspector.getHeadersTableModel().setData(headersAndProperties);
        }

        public boolean beforeSubmit(Submit submit, SubmitContext context) {
            return true;
        }

        public void setInspector(JMSHeaderAndPropertyInspector inspector) {
            this.inspector = inspector;
        }
    }

    private class MessageExchangeResponseJMSHeaderAndPropertiesModel extends
            AbstractJMSHeaderAndPropertyModel<MessageExchangeModelItem>

    {
        @SuppressWarnings("unused")
        MessageExchangeModelItem messageExchangeModelItem;
        @SuppressWarnings("unused")
        JMSHeaderAndPropertyInspector inspector;

        public MessageExchangeResponseJMSHeaderAndPropertiesModel(MessageExchangeModelItem messageExchangeModelItem) {
            super(true, messageExchangeModelItem, MessageExchangeModelItem.MESSAGE_EXCHANGE);
            this.messageExchangeModelItem = messageExchangeModelItem;
        }

        public StringToStringMap getJMSHeadersAndProperties() {
            MessageExchange messageExchange = getModelItem().getMessageExchange();
            if (messageExchange != null) {
                return messageExchange.getResponseHeaders().toStringToStringMap();
            } else {
                return new StringToStringMap();
            }
        }

        public void setInspector(JMSHeaderAndPropertyInspector inspector) {
            this.inspector = inspector;
        }

    }

    private class MessageExchangeRequestJMSHeaderAndPropertiesModel extends
            AbstractJMSHeaderAndPropertyModel<MessageExchangeModelItem>

    {
        @SuppressWarnings("unused")
        MessageExchangeModelItem messageExchangeModelItem;
        @SuppressWarnings("unused")
        JMSHeaderAndPropertyInspector inspector;

        public MessageExchangeRequestJMSHeaderAndPropertiesModel(MessageExchangeModelItem messageExchangeModelItem) {
            super(true, messageExchangeModelItem, MessageExchangeModelItem.MESSAGE_EXCHANGE);
            this.messageExchangeModelItem = messageExchangeModelItem;
        }

        public StringToStringMap getJMSHeadersAndProperties() {
            MessageExchange messageExchange = getModelItem().getMessageExchange();
            if (messageExchange != null && messageExchange.getRequestHeaders() != null) {
                return messageExchange.getRequestHeaders().toStringToStringMap();
            } else {
                return new StringToStringMap();
            }
        }

        public void setInspector(JMSHeaderAndPropertyInspector inspector) {
            this.inspector = inspector;
        }

    }

}
