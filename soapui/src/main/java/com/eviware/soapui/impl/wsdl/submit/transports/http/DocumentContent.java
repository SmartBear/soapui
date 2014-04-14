/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.submit.transports.http;

public class DocumentContent {

    private final String contentType;
    private final String contentAsString;

    public DocumentContent(String contentType, String contentAsString) {
        this.contentAsString = contentAsString;
        this.contentType = contentType;
    }

    public String getContentAsString() {
        return contentAsString;
    }

    public String getContentType() {
        return contentType;
    }
}
