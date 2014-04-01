/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eviware.soapui.impl.support.AbstractMockRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.settings.Settings;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MockRequestDataSource;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MultipartMessageSupport;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Request-class created when receiving an external request to a WsdlMockService
 * 
 * @author ole.matzura
 */

public class WsdlMockRequest extends AbstractMockRequest
{
	private SoapVersion soapVersion;
	private String soapAction;
	private Vector<Object> wssResult;

	public WsdlMockRequest( HttpServletRequest request, HttpServletResponse response, WsdlMockRunContext context )
			throws Exception
	{

		super(request, response, context);

		if( "POST".equals( request.getMethod() ) )
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
			MultipartMessageSupport multipartMessageSupport = getMultipartMessageSupport();
			if( multipartMessageSupport != null && multipartMessageSupport.getRootPart() != null )
				contentType = multipartMessageSupport.getRootPart().getContentType();
		}
		else
		{
			String requestContent = readRequestContent( request );
			super.setRequestContent( requestContent );

			WsdlMockService mockService = (WsdlMockService)context.getMockService();
			if( StringUtils.hasContent( mockService.getIncomingWss() ) )
			{
				IncomingWss incoming = ((WsdlProject)mockService.getProject()).getWssContainer()
						.getIncomingWssByName( mockService.getIncomingWss() );
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
							setActualRequestContent( requestContent );
							super.setRequestContent( writer.toString() );
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

		try
		{
		soapVersion = SoapUtils.deduceSoapVersion( contentType, getRequestXmlObject() );
		}
		catch( Exception e )
		{
			// ignore non xml requests
		}

		if( soapVersion == null )
			soapVersion = SoapVersion.Soap11;

		soapAction = SoapUtils.getSoapAction( soapVersion, getRequestHeaders() );
	}

	public SoapVersion getSoapVersion()
	{
		return soapVersion;
	}

	public String getProtocol()
	{
		return super.getProtocol();
	}

	public Vector<?> getWssResult()
	{
		return wssResult;
	}

	private void readMultipartRequest( HttpServletRequest request ) throws MessagingException
	{
		StringToStringMap values = StringToStringMap.fromHttpHeader( request.getContentType() );
		MockRequestDataSource mockRequestDataSource = new MockRequestDataSource( request );
		setMockRequestDataSource( mockRequestDataSource );
		Settings settings = getRequestContext().getMockService().getSettings();
		boolean isPrettyPrint = settings.getBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES );
		MultipartMessageSupport mmSupport = new MultipartMessageSupport( mockRequestDataSource, values.get( "start" ), null, true, isPrettyPrint );
		setMultipartMessageSupport( mmSupport );
	}

	private String readRequestContent( HttpServletRequest request ) throws Exception
	{
		String messageContent = null;
		String encoding = request.getCharacterEncoding();
		if( encoding != null )
			encoding = StringUtils.unquote( encoding );

		ServletInputStream is = request.getInputStream();
		if( is.markSupported() && request.getContentLength() > 0 )
			is.mark( request.getContentLength() );

		ByteArrayOutputStream out = Tools.readAll( is, Tools.READ_ALL );
		byte[] data = out.toByteArray();

		if( is.markSupported() && request.getContentLength() > 0 )
		{
			try
			{
				is.reset();
			}
			catch( IOException e )
			{
				SoapUI.logError( e );
			}
		}

		// decompress
		String compressionAlg = HttpClientSupport.getCompressionType( request.getContentType(),
				getRequestHeaders().get( "Content-Encoding", ( String )null ) );

		if( compressionAlg != null )
		{
			try
			{
				data = CompressionSupport.decompress( compressionAlg, data );
			}
			catch( Exception e )
			{
				IOException ioe = new IOException( "Decompression of response failed" );
				ioe.initCause( e );
				throw ioe;
			}
		}

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

			messageContent = encoding == null ? new String( data ) : new String( data, contentOffset,
					( int )( data.length - contentOffset ), encoding );
		}

		if( encoding == null )
			encoding = "UTF-8";

		if( messageContent == null )
		{
			messageContent = new String( data, encoding );
		}

		return messageContent;
	}

	public void setRequestContent( String requestContent )
	{
		super.setRequestContent( requestContent );
		setRequestXmlObject( null );

		try
		{
			soapVersion = SoapUtils.deduceSoapVersion( getRequest().getContentType(), getRequestXmlObject() );
		}
		catch( XmlException e )
		{
			SoapUI.logError( e );
		}

		if( soapVersion == null )
			soapVersion = SoapVersion.Soap11;
	}


	public XmlObject getContentElement() throws XmlException
	{
		return SoapUtils.getContentElement( getRequestXmlObject(), soapVersion );
	}

	public String getSoapAction()
	{
		return soapAction;
	}

	public void setSoapAction( String soapAction )
	{
		this.soapAction = soapAction;
	}

}
