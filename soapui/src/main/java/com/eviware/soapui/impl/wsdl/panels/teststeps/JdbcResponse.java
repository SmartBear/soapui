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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import com.eviware.soapui.model.support.AbstractResponse;
import com.eviware.soapui.support.xml.XmlUtils;

public class JdbcResponse extends AbstractResponse<JdbcRequest>
{
	private String responseContent;
	private long timeTaken;
	private long timestamp;
	private final String rawSql;

	public JdbcResponse( JdbcRequest request, Statement statement, String rawSql ) throws SQLException,
			ParserConfigurationException, TransformerConfigurationException, TransformerException
	{
		super( request );
		this.rawSql = rawSql;

		responseContent = XmlUtils.createJdbcXmlResult( statement );
	}

	public String getContentAsString()
	{
		return responseContent;
	}

	public String getContentType()
	{
		return "text/xml";
	}

	@Override
	public byte[] getRawRequestData()
	{
		return rawSql.getBytes();
	}

	public String getRequestContent()
	{
		return getRequest().getTestStep().getQuery();
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
