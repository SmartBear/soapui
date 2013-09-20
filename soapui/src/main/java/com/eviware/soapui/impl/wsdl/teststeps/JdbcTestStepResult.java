/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps;

import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.util.Map;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wsdl.panels.teststeps.JdbcResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.AssertedXPath;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.XmlHolder;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

public class JdbcTestStepResult extends WsdlTestStepResult implements AssertedXPathsContainer, MessageExchange
{
	private JdbcResponse response;
	private SoftReference<JdbcResponse> softResponse;
	private String requestContent;
	private boolean addedAction;

	public JdbcTestStepResult( WsdlTestStep testStep )
	{
		super( testStep );
	}

	public void setResponse( JdbcResponse response, boolean useSoftReference )
	{
		if( useSoftReference )
			this.softResponse = new SoftReference<JdbcResponse>( response );
		else
			this.response = response;
	}

	public void setRequestContent( String requestContent )
	{
		this.requestContent = requestContent;
	}

	public void addAssertedXPath( AssertedXPath assertedXPath )
	{
	}

	@Override
	public ActionList getActions()
	{
		if( !addedAction )
		{
			addAction( new ShowMessageExchangeAction( this, "TestStep" ), true );
			addedAction = true;
		}

		return super.getActions();
	}

	public ModelItem getModelItem()
	{
		return getTestStep();
	}

	public Operation getOperation()
	{
		return null;
	}

	public StringToStringMap getProperties()
	{
		return new StringToStringMap();
	}

	public String getProperty( String name )
	{
		return null;
	}

	public byte[] getRawRequestData()
	{
		return hasResponse() ? getResponse().getRawRequestData() : null;
	}

	public byte[] getRawResponseData()
	{
		return getResponseContent().getBytes();
	}

	public Attachment[] getRequestAttachments()
	{
		return new Attachment[0];
	}

	public Attachment[] getRequestAttachmentsForPart( String partName )
	{
		return new Attachment[0];
	}

	public String getRequestContent()
	{
		return requestContent != null ? requestContent : hasResponse() ? getResponse().getRequestContent() : null;
	}

	public JdbcResponse getResponse()
	{
		return softResponse != null ? softResponse.get() : response;
	}

	public String getRequestContentAsXml()
	{
		return null;
	}

	public StringToStringsMap getRequestHeaders()
	{
		return new StringToStringsMap();
	}

	public Attachment[] getResponseAttachments()
	{
		return new Attachment[0];
	}

	public Attachment[] getResponseAttachmentsForPart( String partName )
	{
		return new Attachment[0];
	}

	public String getResponseContent()
	{
		return hasResponse() ? getResponse().getContentAsString() : null;
	}

	public String getResponseContentAsXml()
	{
		return getResponseContent();
	}

	public StringToStringsMap getResponseHeaders()
	{
		return new StringToStringsMap();
	}

	public long getTimestamp()
	{
		return hasResponse() ? getResponse().getTimestamp() : -1;
	}

	public boolean hasRawData()
	{
		return true;
	}

	public boolean hasRequest( boolean ignoreEmpty )
	{
		return hasResponse();
	}

	public boolean hasResponse()
	{
		return getResponse() != null;
	}

	public String getEndpoint()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Write out a log for a JDBC test step.
	 * 
	 * @author SiKing
	 */
	@Override
	public void writeTo(PrintWriter writer) {
		super.writeTo(writer);

		writer.println();
		writer.println("----------------- Properties ------------------------------");
		PropertyExpansionContext context = new DefaultPropertyExpansionContext(getModelItem());
		Map<String, TestProperty> properties = getTestStep().getProperties();
		for (String key : properties.keySet()) {
			if (key.equals("ResponseAsXml"))
				continue;
			writer.println(key + ": " + context.expand(properties.get(key).getValue()));
		}

		writer.println();
		writer.println("---------------- Request ---------------------------");
		StringToStringsMap requestHeaders = getRequestHeaders();
		for (String key : requestHeaders.keySet())
			writer.println(key + ": " + requestHeaders.get(key).toString());
		writer.println();
		writer.println(getRequestContent());

		writer.println();
		writer.println("---------------- Response --------------------------");
		StringToStringsMap responseHeaders = getResponseHeaders();
		for (String key : responseHeaders.keySet())
			writer.println(key + ": " + responseHeaders.get(key).toString());
		writer.println();
		XmlHolder xmlResponse = null;
		try {
			xmlResponse = new XmlHolder(getResponseContentAsXml());
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println(xmlResponse.getPrettyXml());
	}
}
