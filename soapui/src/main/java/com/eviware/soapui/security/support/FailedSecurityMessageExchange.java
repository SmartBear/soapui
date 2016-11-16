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

package com.eviware.soapui.security.support;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

public class FailedSecurityMessageExchange implements MessageExchange {

    @Override
    public String getEndpoint() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response getResponse() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getMessages() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ModelItem getModelItem() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Operation getOperation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StringToStringMap getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProperty(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getRawRequestData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getRawResponseData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Attachment[] getRequestAttachments() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Attachment[] getRequestAttachmentsForPart(String partName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestContent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestContentAsXml() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StringToStringsMap getRequestHeaders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Attachment[] getResponseAttachments() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Attachment[] getResponseAttachmentsForPart(String partName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getResponseContent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getResponseContentAsXml() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StringToStringsMap getResponseHeaders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getTimeTaken() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getTimestamp() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasRawData() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasRequest(boolean ignoreEmpty) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasResponse() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDiscarded() {
        // TODO Auto-generated method stub
        return false;
    }

}
