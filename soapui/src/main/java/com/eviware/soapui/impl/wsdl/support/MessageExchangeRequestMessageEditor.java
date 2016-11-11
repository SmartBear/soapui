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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.components.RequestMessageXmlEditor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.DocumentContent;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.support.editor.xml.support.DefaultXmlDocument;

import java.beans.PropertyChangeEvent;

public class MessageExchangeRequestMessageEditor extends
        RequestMessageXmlEditor<MessageExchangeModelItem, DefaultXmlDocument> {
    private final MessageExchangeModelItem messageExchangeModelItem;

    public MessageExchangeRequestMessageEditor(MessageExchange messageExchange) {
        this(new MessageExchangeModelItem("message exchange request", messageExchange));
    }

    public MessageExchangeRequestMessageEditor(MessageExchangeModelItem messageExchangeModelItem) {
        super(new DefaultXmlDocument(), messageExchangeModelItem);
        this.messageExchangeModelItem = messageExchangeModelItem;

        if (messageExchangeModelItem.getMessageExchange() != null) {
            updateXml();
        }

        messageExchangeModelItem.addPropertyChangeListener(MessageExchangeModelItem.MESSAGE_EXCHANGE, this);

        setEditable(false);
    }

    public void updateXml() {
        try {
            MessageExchange messageExchange = messageExchangeModelItem.getMessageExchange();
            DefaultXmlDocument defaultXmlDocument = getDocument();

            if (messageExchange != null && messageExchange.getOperation() != null) {
                defaultXmlDocument.setTypeSystem(messageExchange.getOperation().getInterface().getDefinitionContext()
                        .getInterfaceDefinition().getSchemaTypeSystem());
            }

            defaultXmlDocument.setDocumentContent(new DocumentContent(null, messageExchange == null ? null : messageExchange.getRequestContentAsXml()));
        } catch (Throwable e) {
            SoapUI.logError(e);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == messageExchangeModelItem
                && evt.getPropertyName().equals(MessageExchangeModelItem.MESSAGE_EXCHANGE)) {
            updateXml();
        } else {
            super.propertyChange(evt);
        }
    }

    @Override
    public void release() {
        super.release();

        messageExchangeModelItem.removePropertyChangeListener(MessageExchangeModelItem.MESSAGE_EXCHANGE, this);
    }
}
