/*
 * Copyright 2004-2014 SmartBear Software
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

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;

public class MessageExchangeModelItem extends EmptyModelItem {
    public final static String MESSAGE_EXCHANGE = "messageExchange";
    private MessageExchange messageExchange;

    public MessageExchangeModelItem(String title, MessageExchange messageExchange) {
        super(title, null);
        this.messageExchange = messageExchange;
    }

    public MessageExchange getMessageExchange() {
        return messageExchange;
    }

    public void setMessageExchange(MessageExchange messageExchange) {
        MessageExchange oldExchange = this.messageExchange;
        this.messageExchange = messageExchange;

        notifyPropertyChanged(MESSAGE_EXCHANGE, oldExchange, messageExchange);
    }

    public boolean hasRawData() {
        return messageExchange == null ? false : messageExchange.hasRawData();
    }

    @Override
    public ModelItem getParent() {
        return messageExchange == null ? null : messageExchange.getModelItem();
    }

}
