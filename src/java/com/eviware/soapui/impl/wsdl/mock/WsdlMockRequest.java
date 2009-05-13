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

package com.eviware.soapui.impl.wsdl.mock;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MockRequestDataSource;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MultipartMessageSupport;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Request-class created when receiving an external request to a WsdlMockService
 * 
 * @author ole.matzura
 */

public class WsdlMockRequest implements MockRequest
{
	private StringToStringMap requestHeaders;
	private String requestContent;
	private MultipartMessageSupport mmSupport;
	private XmlObject requestXmlObject;
	private SoapVersion soapVersion;
	private final HttpServletResponse response;
	private String protocol;
	private String path;
	private String soapAction;
	private final WsdlMockRunContext context;
	private final WsdlMockRunContext requestContext;
	private final HttpServletRequest request;
	private Vector<Object> wssResult;
	private MockRequestDataSource mockRequestDataSource;
	private String actualRequestContent;
	private boolean responseMessage;

	public WsdlMockRequest( HttpServletRequest request, HttpServletResponse response, WsdlMockRunContext context )
			throws Exception
	{
		this.request = request;
		this.response = response;
		this.context = context;

		requestContext = new WsdlMockRunContext( context.getMockService(), null );

		requestHeaders = new StringToStringMap();
		for( Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements(); )
		{
			String header = ( String )e.nextElement();
			requestHeaders.put( header, request.getHeader( header ) );
		}

		protocol = request.getProtocol();
		path = request.getPathInfo();

		if( request.getMethod().equals( "POST" ) )
		{
			initPostRequest( request, context );
		}
	}

	protected void initPostRequest( HttpServletRequest request, WsdlMockRunContext context ) throws Exception
	{
		String contentType = request.getContentType();

		if( contentType != null && contentType.toUpperCase().startsWith( "MULTIPART" ) )
		{
			readMultipartRequest( request );
			contentType = mmSupport.getRootPart().getContentType();
		}
		else
		{
			this.requestContent = readRequestContent( request );

			if( StringUtils.hasContent( context.getMockService().getIncomingWss() ) )
			{
				IncomingWss incoming = context.getMockService().getProject().getWssContainer().getIncomingWssByName(
						context.getMockService().getIncomingWss() );
				if( incoming != null )
				{
					Document dom = XmlUtils.parseXml( requestContent );
					try
					{
						wssResult = incoming.processIncoming( dom, context );
						if( wssResult != null && wssResult.size() > 0 )
						{
							StringWriter writer = new StringWriter();
							XmlUtils.serialize( dom, writer );
							actualRequestContent = requestContent;
							requestContent = writer.toString();
						}
					}
					catch( Exception e )
					{
						if( wssResult == null )
							wssResult = new Vector<Object>();
						wssResult.add( e );
					}
				}
			}
		}

		soapVersion = SoapUtils.deduceSoapVersion( contentType, getRequestXmlObject() );
		if( soapVersion == null )
			soapVersion = SoapVersion.Soap11;

		soapAction = SoapUtils.getSoapAction( soapVersion, requestHeaders );
	}

	public SoapVersion getSoapVersion()
	{
		return soapVersion;
	}

	public String getProtocol()
	{
		return protocol;
	}

	public Vector<?> getWssResult()
	{
		return wssResult;
	}

	private void readMultipartRequest( HttpServletRequest request ) throws MessagingException
	{
		StringToStringMap values = StringToStringMap.fromHttpHeader( request.getContentType() );
		mockRequestDataSource = new MockRequestDataSource( request );
		mmSupport = new MultipartMessageSupport( mockRequestDataSource, values.get( "start" ), null, true, requestContext
				.getMockService().getSettings().getBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES ) );
	}

	private String readRequestContent( HttpServletRequest request ) throws Exception
	{
		String responseContent = null;
		String encoding = request.getCharacterEncoding();
		if( encoding != null )
			encoding = StringUtils.unquote( encoding );

		ByteArrayOutputStream out = Tools.readAll( request.getInputStream(), Tools.READ_ALL );
		byte[] data = out.toByteArray();
		int contentOffset = 0;

		String contentType = request.getContentType();
		if( contentType != null && data.length > 0 )
		{
			if( contentType.toLowerCase().endsWith( "xml" ) )
			{
				if( data.length > 3 && data[0] == ( byte )239 && data[1] == ( byte )187 && data[2] == ( byte )191 )
				{
					encoding = "UTF-8";
					contentOffset = 3;
				}
			}

			encoding = StringUtils.unquote( encoding );

			responseContent = encoding == null ? new String( data ) : new String( data, contentOffset,
					( int )( data.length - contentOffset ), encoding );
		}

		if( encoding == null )
			encoding = "UTF-8";

		if( responseContent == null )
		{
			responseContent = new String( data, encoding );
		}

		return responseContent;
	}

	public Attachment[] getRequestAttachments()
	{
		return mmSupport == null ? new Attachment[0] : mmSupport.getAttachments();
	}

	public String getRequestContent()
	{
		return mmSupport == null ? requestContent : mmSupport.getContentAsString();
	}

	public StringToStringMap getRequestHeaders()
	{
		return requestHeaders;
	}

	public void setRequestContent( String requestContent )
	{
		this.requestContent = requestContent;
	}

	public XmlObject getRequestXmlObject() throws XmlException
	{
		if( requestXmlObject == null )
			requestXmlObject = XmlObject.Factory.parse( getRequestContent() );

		return requestXmlObject;
	}

	public HttpServletResponse getHttpResponse()
	{
		return response;
	}

	public HttpServletRequest getHttpRequest()
	{
		return request;
	}

	public XmlObject getContentElement() throws XmlException
	{
		return SoapUtils.getContentElement( getRequestXmlObject(), soapVersion );
	}

	public String getPath()
	{
		return path;
	}

	public WsdlMockRunContext getContext()
	{
		return context;
	}

	public void setOperation( WsdlOperation operation )
	{
		if( mmSupport != null )
			mmSupport.setOperation( operation );
	}

	public WsdlMockRunContext getRequestContext()
	{
		return requestContext;
	}

	public String getSoapAction()
	{
		return soapAction;
	}

	public byte[] getRawRequestData()
	{
		return mockRequestDataSource == null ? actualRequestContent == null ? requestContent.getBytes()
				: actualRequestContent.getBytes() : mockRequestDataSource.getData();
	}

	public void setResponseMessage( boolean responseMessage )
	{
		this.responseMessage = responseMessage;
	}

	public boolean isResponseMessage()
	{
		return responseMessage;
	}
}
