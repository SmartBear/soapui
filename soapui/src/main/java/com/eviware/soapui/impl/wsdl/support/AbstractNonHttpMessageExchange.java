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

import com.eviware.soapui.impl.wsdl.submit.AbstractMessageExchange;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.types.StringToStringsMap;

public abstract class AbstractNonHttpMessageExchange<T extends ModelItem> extends AbstractMessageExchange<T> {

    public AbstractNonHttpMessageExchange(T modelItem) {
        super(modelItem);
    }

    public Operation getOperation() {
        return null;
    }

    public byte[] getRawRequestData() {
        return null;
    }

    public byte[] getRawResponseData() {
        return null;
    }

    public Attachment[] getRequestAttachments() {
        return null;
    }

    public Attachment[] getRequestAttachmentsForPart(String partName) {
        return null;
    }

    public StringToStringsMap getResponseHeaders() {
        return new StringToStringsMap();
    }

    public StringToStringsMap getRequestHeaders() {
        return null;
    }

    public Attachment[] getResponseAttachments() {
        return null;
    }

    public Attachment[] getResponseAttachmentsForPart(String partName) {
        return null;
    }

    public boolean hasRawData() {
        return false;
    }
}
