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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.testsuite.AssertedXPath;
import com.eviware.soapui.model.testsuite.MessageExchangeTestStepResult;
import com.eviware.soapui.model.testsuite.ResponseAssertedMessageExchange;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * TestStepResult for a WsdlTestRequestStep
 * 
 * @author ole.matzura
 */

public class RestRequestStepResult extends WsdlTestStepResult implements ResponseAssertedMessageExchange,
		AssertedXPathsContainer, MessageExchangeTestStepResult
{
	private String requestContent;
	private HttpResponse response;
	private String domain;
	private String username;
	private String endpoint;
	private String encoding;
	private String password;
	private StringToStringMap properties;
	private boolean addedAction;
	private List<AssertedXPath> assertedXPaths;

	public RestRequestStepResult( HttpTestRequestStepInterface step )
	{
		super( ( WsdlTestStep )step );
	}

	public Operation getOperation()
	{
		if( response == null )
		{
			response = null;
		}
		return response == null ? null : response.getRequest().getOperation();
	}

	public ModelItem getModelItem()
	{
		if( response != null )
			return response.getRequest();
		else
			return null;
	}

	public String getRequestContent()
	{
		if( isDiscarded() )
			return "<discarded>";

		return requestContent;
	}

	public void setRequestContent( String requestContent )
	{
		this.requestContent = requestContent;
	}

	public HttpResponse getResponse()
	{
		return response;
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

	public void setResponse( HttpResponse response )
	{
		this.response = response;
	}

	public String getDomain()
	{
		return domain;
	}

	public void setDomain( String domain )
	{
		this.domain = domain;
		addProperty( "Domain", domain );
	}

	public void addProperty( String key, String value )
	{
		if( properties == null )
			properties = new StringToStringMap();

		properties.put( key, value );
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding( String encoding )
	{
		this.encoding = encoding;
		addProperty( "Encoding", encoding );
	}

	public String getEndpoint()
	{
		return endpoint;
	}

	public void setEndpoint( String endpoint )
	{
		this.endpoint = endpoint;
		addProperty( "Endpoint", endpoint );
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword( String password )
	{
		this.password = password;
		addProperty( "Password", password );
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername( String username )
	{
		this.username = username;
		addProperty( "Username", username );
	}

	public void discard()
	{
		super.discard();

		requestContent = null;
		response = null;
		properties = null;
		assertedXPaths = null;
	}

	public void writeTo( PrintWriter writer )
	{
		super.writeTo( writer );

		writer.println( "\r\n----------------- Properties ------------------------------" );
		if( properties != null )
		{
			for( String key : properties.keySet() )
			{
				if( properties.get( key ) != null )
					writer.println( key + ": " + properties.get( key ) );
			}
		}

		writer.println( "\r\n---------------- Request ---------------------------" );
		if( response != null )
		{
			StringToStringMap headers = response.getRequestHeaders();
			for( String key : headers.keySet() )
			{
				if( headers.get( key ) != null )
					writer.println( key + ": " + headers.get( key ) );
			}
		}

		if( StringUtils.hasContent( requestContent ) )
			writer.println( "\r\n" + requestContent );
		else
			writer.println( "\r\n- missing request / garbage collected -" );

		writer.println( "\r\n---------------- Response --------------------------" );
		if( response != null )
		{
			StringToStringMap headers = response.getResponseHeaders();
			for( String key : headers.keySet() )
			{
				if( headers.get( key ) != null )
					writer.println( key + ": " + headers.get( key ) );
		}

			String respContent = response.getContentAsString();
			if( respContent != null )
				writer.println( "\r\n" + respContent );
		}
		else
			writer.println( "\r\n- missing response / garbage collected -" );
	}

	public StringToStringMap getProperties()
	{
		return properties;
	}

	public String getProperty( String name )
	{
		return properties == null ? null : properties.get( name );
	}

	public Attachment[] getRequestAttachments()
	{
		if( response == null || response.getRequest() == null )
			return new Attachment[0];

		return response.getRequest().getAttachments();
	}

	public StringToStringMap getRequestHeaders()
	{
		if( response == null )
			return null;

		return response.getRequestHeaders();
	}

	public Attachment[] getResponseAttachments()
	{
		if( response == null )
			return new Attachment[0];

		return response.getAttachments();
	}

	public String getResponseContent()
	{
		if( isDiscarded() )
			return "<discarded>";

		if( response == null )
			return "<missing response>";

		return response.getContentAsString();
	}

	public String getRequestContentAsXml()
	{
		return XmlUtils.seemsToBeXml( requestContent ) ? requestContent : "<not-xml/>";
	}

	public String getResponseContentAsXml()
	{
		return response.getContentAsXml();
	}

	public StringToStringMap getResponseHeaders()
	{
		if( response == null )
			return null;

		return response.getResponseHeaders();
	}

	public long getTimestamp()
	{
		if( isDiscarded() || response == null )
			return -1;

		return response.getTimestamp();
	}

	public AssertedXPath[] getAssertedXPathsForResponse()
	{
		return assertedXPaths == null ? new AssertedXPath[0] : assertedXPaths.toArray( new AssertedXPath[assertedXPaths
				.size()] );
	}

	public void addAssertedXPath( AssertedXPath assertedXPath )
	{
		if( assertedXPaths == null )
			assertedXPaths = new ArrayList<AssertedXPath>();

		assertedXPaths.add( assertedXPath );
	}

	public MessageExchange[] getMessageExchanges()
	{
		return new MessageExchange[] { this };
	}

	public byte[] getRawRequestData()
	{
		return response.getRawRequestData();
	}

	public byte[] getRawResponseData()
	{
		return response.getRawResponseData();
	}

	public Attachment[] getRequestAttachmentsForPart( String partName )
	{
		return null;
	}

	public Attachment[] getResponseAttachmentsForPart( String partName )
	{
		return null;
	}

	public boolean hasRawData()
	{
		return getRawResponseData() != null || getRawRequestData() != null;
	}

	public boolean hasRequest( boolean b )
	{
		return true;
	}

	public boolean hasResponse()
	{
		return response != null;
	}
}