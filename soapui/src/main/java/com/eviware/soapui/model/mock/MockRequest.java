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

package com.eviware.soapui.model.mock;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A Request to a MockService
 *
 * @author ole.matzura
 */

public interface MockRequest {
    public Attachment[] getRequestAttachments();

    public HttpServletRequest getHttpRequest();

    public StringToStringsMap getRequestHeaders();

    public String getRequestContent();

    public MockRunContext getContext();

    public MockRunContext getRequestContext();

    public RestRequestInterface.HttpMethod getMethod();

    public XmlObject getContentElement() throws XmlException;

    public String getPath();

    public byte[] getRawRequestData();

    public String getProtocol();

    public HttpServletResponse getHttpResponse();

    public XmlObject getRequestXmlObject() throws XmlException;

    public void setRequestContent(String xml);
}
