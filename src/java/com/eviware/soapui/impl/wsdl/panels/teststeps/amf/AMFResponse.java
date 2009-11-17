/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.support.AbstractResponse;

public class AMFResponse extends AbstractResponse<AMFRequest>
{
	private Object responseContent="";
	private String responseContentXML="";
	private long timeTaken;
	private long timestamp;
	private SubmitContext submitContext;
	private AMFRequest request;

	public AMFResponse( AMFRequest request, SubmitContext submitContext ) throws SQLException,
			ParserConfigurationException, TransformerConfigurationException, TransformerException
	{
		super( request );

		this.request = request;
		this.responseContent = submitContext.getProperty( AMFRequest.AMF_RESPONSE_CONTENT );
		this.submitContext = submitContext;
		if(responseContent != null)
			 setResponseContentXML(new com.thoughtworks.xstream.XStream().toXML( responseContent ) ) ;
	}

	public String getContentAsString()
	{
		return responseContent != null ? responseContent.toString() : "";
	}
	
	public String getContentType()
	{
		return "text/xml";
	}

	public long getContentLength()
	{
		return responseContent != null ? responseContent.toString().length() : 0;
	}

	public String getRequestContent()
	{
		return request.toString();
	}

	public long getTimeTaken()
	{
		return timeTaken;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setContentAsString( String xml )
	{
		responseContent = xml;
	}

	public void setTimeTaken( long timeTaken )
	{
		this.timeTaken = timeTaken;
	}

	public void setTimestamp( long timestamp )
	{
		this.timestamp = timestamp;
	}

	@Override
	public byte[] getRawResponseData()
	{
		return responseContent != null ? responseContent.toString().getBytes() : null;
	}

	public void setResponseContentXML( String responseContentXML )
	{
		this.responseContentXML = responseContentXML;
	}

	public String getResponseContentXML()
	{
		return responseContentXML;
	}
	
	
}
