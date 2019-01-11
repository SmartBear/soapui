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

package com.eviware.soapui.impl.wsdl.submit;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

public abstract class AbstractMessageExchange<T extends ModelItem> implements MessageExchange {
    protected StringToStringMap properties;
    private String[] messages;
    private T modelItem;
    protected boolean discardResponse;

    public AbstractMessageExchange(T modelItem) {
        super();
        this.modelItem = modelItem;
        if (modelItem != null) {
            discardResponse = modelItem.getSettings().getBoolean("discardResponse");
        }
    }

    public T getModelItem() {
        return modelItem;
    }

    public String getRequestContentAsXml() {
        if (hasRequest(true) && XmlUtils.seemsToBeXml(getRequestContent())) {
            return getRequestContent();
        } else {
            return "<not-xml/>";
        }
    }

    public String getResponseContentAsXml() {
        if (hasResponse() && XmlUtils.seemsToBeXml(getResponseContent())) {
            return getResponseContent();
        } else {
            return null;
        }
    }

    public void addProperty(String name, String value) {
        if (properties == null) {
            properties = new StringToStringMap();
        }

        properties.put(name, value);
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public StringToStringMap getProperties() {
        return properties;
    }

    public String[] getMessages() {
        return messages;
    }

    public void setMessages(String[] messages) {
        this.messages = messages;
    }

}
