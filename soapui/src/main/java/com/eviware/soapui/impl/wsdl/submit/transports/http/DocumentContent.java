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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import com.eviware.soapui.model.iface.TypedContent;

public class DocumentContent implements TypedContent {

    private final String contentType;
    private final String contentAsString;

    public DocumentContent(String contentType, String contentAsString) {
        this.contentAsString = contentAsString;
        this.contentType = contentType;
    }

    @Override
    public String getContentAsString() {
        return contentAsString;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public long getContentLength() {
        return getContentAsString() == null ? 0 : getContentAsString().length();
    }

    public DocumentContent withContent(String newContent){
        return new DocumentContent(contentType, newContent);
    }

    public DocumentContent withContentType(String newContentType){
        return new DocumentContent(newContentType, contentAsString);
    }
}
