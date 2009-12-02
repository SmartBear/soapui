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

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.Header;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.support.AbstractResponse;
import com.eviware.soapui.support.types.StringToStringMap;

import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.MessageHeader;

public class AMFResponse extends AbstractResponse<AMFRequest>
{

	public static final String AMF_POST_METHOD = "AMF_POST_METHOD";
	public static final String AMF_RESPONSE_HEADERS = "responseHeaders";
	public static final String AMF_RESPONSE_ACTION_MESSAGE = "AMF_RESPONSE_ACTION_MESSAGE";

	private Object responseContent = "";
	private String responseContentXML = "";
	private long timeTaken;
	private long timestamp;
	private AMFRequest request;
	private StringToStringMap requestHeaders;
	private StringToStringMap responseHeaders;
	private StringToStringMap responseAMFHeaders = new StringToStringMap();
	private byte[] rawRequestData;
	private byte[] rawResponseData;
	private ActionMessage actionMessage;

	public AMFResponse( AMFRequest request, SubmitContext submitContext, Object responseContent ) throws SQLException,
			ParserConfigurationException, TransformerConfigurationException, TransformerException
	{
		super( request );

		this.request = request;
		this.responseContent = responseContent;
		if( responseContent != null )
			setResponseContentXML( new com.thoughtworks.xstream.XStream().toXML( responseContent ) );
		this.actionMessage = ( ActionMessage )submitContext.getProperty( AMF_RESPONSE_ACTION_MESSAGE );
		initHeaders( ( ExtendedPostMethod )submitContext.getProperty( AMF_POST_METHOD ) );

	}

	public String getContentAsString()
	{
		return getResponseContentXML();
	}

	public String getContentType()
	{
		return "text/xml";
	}

	public long getContentLength()
	{
		return rawResponseData != null ? rawResponseData.length : 0;
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

	public void setContentAsString( String content )
	{
		responseContent = content;
	}

	public void setTimeTaken( long timeTaken )
	{
		this.timeTaken = timeTaken;
	}

	public void setTimestamp( long timestamp )
	{
		this.timestamp = timestamp;
	}

	public void setResponseContentXML( String responseContentXML )
	{
		this.responseContentXML = responseContentXML;
	}

	public String getResponseContentXML()
	{
		return responseContentXML;
	}

	protected void initHeaders( ExtendedPostMethod postMethod )
	{
		try
		{
			ByteArrayOutputStream rawResponse = new ByteArrayOutputStream();
			ByteArrayOutputStream rawRequest = new ByteArrayOutputStream();

			if( !postMethod.isFailed() )
			{
				rawResponse.write( String.valueOf( postMethod.getStatusLine() ).getBytes() );
				rawResponse.write( "\r\n".getBytes() );
			}

			rawRequest.write( ( postMethod.getMethod() + " " + postMethod.getURI().toString() + " "
					+ postMethod.getParams().getVersion().toString() + "\r\n" ).getBytes() );

			requestHeaders = new StringToStringMap();
			Header[] headers = postMethod.getRequestHeaders();
			for( Header header : headers )
			{
				requestHeaders.put( header.getName(), header.getValue() );
				rawRequest.write( header.toExternalForm().getBytes() );
			}

			if( !postMethod.isFailed() )
			{
				responseHeaders = new StringToStringMap();
				headers = postMethod.getResponseHeaders();
				for( Header header : headers )
				{
					responseHeaders.put( header.getName(), header.getValue() );
					rawResponse.write( header.toExternalForm().getBytes() );
				}

				responseHeaders.put( "#status#", String.valueOf( postMethod.getStatusLine() ) );
			}

			if( postMethod.getRequestEntity() != null )
			{
				rawRequest.write( "\r\n".getBytes() );
				if( postMethod.getRequestEntity().isRepeatable() )
				{
					postMethod.getRequestEntity().writeRequest( rawRequest );
				}
				else
					rawRequest.write( "<request data not available>".getBytes() );
			}

			if( !postMethod.isFailed() )
			{
				rawResponse.write( "\r\n".getBytes() );

				for( Object body : actionMessage.getBodies() )
				{
					MessageBody mb = (MessageBody)body;
					rawResponse.write( mb.getData().toString().getBytes() );
				}
			}

			rawResponseData = rawResponse.toByteArray();
			rawRequestData = rawRequest.toByteArray();
			

			initAMFHeaders( postMethod );
			
		}
		catch( Throwable e )
		{
			SoapUI.logError( e );
		}
	}

	@SuppressWarnings("unchecked")
	private void initAMFHeaders( ExtendedPostMethod postMethod )
	{
		if( !postMethod.isFailed() )
		{
		ArrayList<MessageHeader> amfHeaders = 	actionMessage.getHeaders();
 
			for( MessageHeader header :amfHeaders)
			{
				responseAMFHeaders.put( header.getName(), header.getData().toString() );
			}
		}
	}

	public byte[] getRawRequestData()
	{
		return rawRequestData;
	}

	public byte[] getRawResponseData()
	{
		return rawResponseData;
	}

	public StringToStringMap getRequestHeaders()
	{
		return requestHeaders;
	}

	public StringToStringMap getResponseHeaders()
	{
		return responseHeaders;
	}

	public StringToStringMap getResponseAMFHeaders()
	{
		return responseAMFHeaders;
	}

}
