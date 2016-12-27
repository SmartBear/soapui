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

package com.eviware.soapui.impl.wsdl.submit;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageExchange for WSDL-based exchanges
 *
 * @author ole.matzura
 */

public abstract class AbstractRestMessageExchange<T extends ModelItem> extends AbstractMessageExchange<T> implements
        RestMessageExchange {
    public AbstractRestMessageExchange(T modelItem) {
        super(modelItem);
    }

    public boolean hasResponse() {
        String responseContent = getResponseContent();
        return responseContent != null && responseContent.trim().length() > 0;
    }

    public Attachment[] getResponseAttachmentsForPart(String name) {
        List<Attachment> result = new ArrayList<Attachment>();

        if (getResponseAttachments() != null) {
            for (Attachment attachment : getResponseAttachments()) {
                if (attachment.getPart().equals(name)) {
                    result.add(attachment);
                }
            }
        }

        return result.toArray(new Attachment[result.size()]);
    }

    public Attachment[] getRequestAttachmentsForPart(String name) {
        List<Attachment> result = new ArrayList<Attachment>();

        for (Attachment attachment : getRequestAttachments()) {
            if (attachment.getPart().equals(name)) {
                result.add(attachment);
            }
        }

        return result.toArray(new Attachment[result.size()]);
    }

    public boolean hasRequest(boolean ignoreEmpty) {
        String requestContent = getRequestContent();
        return !(requestContent == null || (ignoreEmpty && requestContent.trim().length() == 0));
    }

    public boolean hasRawData() {
        return false;
    }

    public byte[] getRawRequestData() {
        return null;
    }

    public byte[] getRawResponseData() {
        return null;
    }
}
