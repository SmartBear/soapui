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

package com.eviware.soapui.model.util;

import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.rest.support.MediaTypeHandlerRegistry;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

/**
 * Created by ole on 18/06/14.
 */
public class BaseResponse implements Response {

    private StringToStringMap properties = new StringToStringMap();
    private Request request;
    private String responseContent;
    private String responseContentType;
    private String xmlContent;

    public BaseResponse( Request request, String responseContent, String responseContentType )
    {
        this.request = request;
        this.responseContent = responseContent;
        this.responseContentType = responseContentType;
    }

    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    public String getRequestContent() {
        return request.getRequestContent();
    }

    @Override
    public long getTimeTaken() {
        return 0;
    }

    @Override
    public Attachment[] getAttachments() {
        return new Attachment[0];
    }

    @Override
    public Attachment[] getAttachmentsForPart(String partName) {
        return new Attachment[0];
    }

    @Override
    public StringToStringsMap getRequestHeaders() {
        return new StringToStringsMap();
    }

    @Override
    public StringToStringsMap getResponseHeaders() {
        return new StringToStringsMap();
    }

    @Override
    public long getTimestamp() {
        return 0;
    }

    @Override
    public byte[] getRawRequestData() {
        return request.getRequestContent() == null ? null : request.getRequestContent().getBytes();
    }

    @Override
    public byte[] getRawResponseData() {
        return responseContent == null ? null : responseContent.getBytes();
    }

    @Override
    public String getContentAsXml() {
        if (xmlContent == null) {
            MediaTypeHandler typeHandler = MediaTypeHandlerRegistry.getTypeHandler(getContentType());
            xmlContent = (typeHandler == null) ? "<xml/>" : typeHandler.createXmlRepresentation(this);
        }
        return xmlContent;
    }

    @Override
    public String getProperty(String name) {
        return properties.get( name );
    }

    @Override
    public void setProperty(String name, String value) {
        properties.put( name, value );
    }

    @Override
    public String[] getPropertyNames() {
        return properties.getKeys();
    }

    @Override
    public String getContentAsString() {
        return getRequestContent();
    }

    @Override
    public String getContentType() {
        return responseContentType;
    }

    @Override
    public long getContentLength() {
        return responseContent == null ? 0 : responseContent.length();
    }
}
