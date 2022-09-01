/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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
    Attachment[] getRequestAttachments();

    HttpServletRequest getHttpRequest();

    StringToStringsMap getRequestHeaders();

    String getRequestContent();

    MockRunContext getContext();

    MockRunContext getRequestContext();

    RestRequestInterface.HttpMethod getMethod();

    XmlObject getContentElement() throws XmlException;

    String getPath();

    byte[] getRawRequestData();

    String getProtocol();

    HttpServletResponse getHttpResponse();

    XmlObject getRequestXmlObject() throws XmlException;

    void setRequestContent(String xml);

    void refreshRequestXmlObject() throws XmlException;
}
