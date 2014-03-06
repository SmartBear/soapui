/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.model.mock;

import com.eviware.soapui.impl.rest.HttpMethod;
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

public interface MockRequest
{
	public Attachment[] getRequestAttachments();

	public HttpServletRequest getHttpRequest();

	public StringToStringsMap getRequestHeaders();

	public String getRequestContent();

	public MockRunContext getContext();

	public MockRunContext getRequestContext();

	public HttpMethod getMethod();

	public XmlObject getContentElement() throws XmlException;

	public String getPath();

	public byte[] getRawRequestData();

	public String getProtocol();

	public HttpServletResponse getHttpResponse();

	public XmlObject getRequestXmlObject() throws XmlException;

	public void setRequestContent( String xml );
}
