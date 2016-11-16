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

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

public abstract class AbstractResponse<T extends Request> implements Response {
    private StringToStringMap properties = new StringToStringMap();
    private final T request;

    public AbstractResponse(T request) {
        this.request = request;
    }

    public Attachment[] getAttachments() {
        return null;
    }

    public Attachment[] getAttachmentsForPart(String partName) {
        return null;
    }

    public long getContentLength() {
        return getContentAsString().length();
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public String[] getPropertyNames() {
        return properties.getKeys();
    }

    public byte[] getRawRequestData() {
        return null;
    }

    public byte[] getRawResponseData() {
        return null;
    }

    public T getRequest() {
        return request;
    }

    public String getContentAsXml() {
        return getContentAsString();
    }

    public StringToStringsMap getRequestHeaders() {
        return null;
    }

    public StringToStringsMap getResponseHeaders() {
        return null;
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }
}
