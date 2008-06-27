/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SSLInfo;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.support.types.StringToStringMap;

public class RestResponse implements HttpResponse
{
	public Attachment[] getAttachments()
	{
		return null;
	}

	public Attachment[] getAttachmentsForPart(String partName)
	{
		return null;
	}

	public String getContentAsString()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public long getContentLength()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public byte[] getRawRequestData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getRawResponseData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getRequestContent()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public StringToStringMap getRequestHeaders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public StringToStringMap getResponseHeaders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public long getTimeTaken()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public long getTimestamp()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public SSLInfo getSSLInfo()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setResponseContent(String responseContent)
	{
		// TODO Auto-generated method stub
		
	}

	public AbstractHttpRequest<?> getRequest()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
