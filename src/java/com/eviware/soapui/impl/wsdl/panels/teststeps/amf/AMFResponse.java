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
import java.sql.Statement;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import com.eviware.soapui.model.support.AbstractResponse;
import com.eviware.soapui.support.xml.XmlUtils;

public class AMFResponse extends AbstractResponse<AMFRequest>
{
	private String responseContent;
	private long timeTaken;
	private long timestamp;

	public AMFResponse( AMFRequest request, Statement statement ) throws SQLException, ParserConfigurationException, TransformerConfigurationException, TransformerException
	{
		super( request );
		
//		org.w3c.dom.Document xmlDocumentResult = XmlUtils.createAMFXmlResult( statement );
//		responseContent = XmlUtils.serializePretty( xmlDocumentResult );
	}

	public String getContentAsString()
	{
		return responseContent;
	}

	public String getContentType()
	{
		return "text/xml";
	}

	public long getContentLength()
	{
		return 0;
	}
	
	public String getRequestContent()
	{
		return null;//getRequest().getTestStep().getQuery();
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
}
