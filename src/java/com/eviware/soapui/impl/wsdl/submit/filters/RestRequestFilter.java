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

package com.eviware.soapui.impl.wsdl.submit.filters;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.PreencodedMimeBodyPart;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.xmlbeans.XmlBoolean;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.RestParamProperty;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.AttachmentDataSource;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.AttachmentUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.RestRequestDataSource;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.RestRequestMimeMessageRequestEntity;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * RequestFilter that adds SOAP specific headers
 * 
 * @author Ole.Matzura
 */

public class RestRequestFilter extends AbstractRequestFilter
{
	@SuppressWarnings( "deprecation" )
	@Override
	public void filterRestRequest( SubmitContext context, RestRequest request )
	{
		HttpMethod httpMethod = ( HttpMethod )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );

		String path = PropertyExpansionUtils.expandProperties( context, request.getPath() );
		StringBuffer query = new StringBuffer();

		StringToStringMap responseProperties = ( StringToStringMap )context
				.getProperty( BaseHttpRequestTransport.RESPONSE_PROPERTIES );

		MimeMultipart formMp = "multipart/form-data".equals( request.getMediaType() )
				&& httpMethod instanceof EntityEnclosingMethod ? new MimeMultipart() : null;

		XmlBeansRestParamsTestPropertyHolder params = request.getParams();
		for( int c = 0; c < params.getPropertyCount(); c++ )
		{
			RestParamProperty param = params.getPropertyAt( c );

			String value = PropertyExpansionUtils.expandProperties( context, param.getValue() );
			responseProperties.put( param.getName(), value );

			if( value != null && formMp == null && !param.isDisableUrlEncoding() )
			{
				try
				{
					String encoding = System.getProperty( "soapui.request.encoding", request.getEncoding() );

					if( StringUtils.hasContent( encoding ) )
						value = URLEncoder.encode( value, encoding );
					else
						value = URLEncoder.encode( value );
				}
				catch( UnsupportedEncodingException e1 )
				{
					SoapUI.logError( e1 );
					value = URLEncoder.encode( value );
				}
			}

			if( !StringUtils.hasContent( value ) && !param.getRequired() )
				continue;

			switch( param.getStyle() )
			{
			case HEADER :
				httpMethod.setRequestHeader( param.getName(), value );
				break;
			case QUERY :
				if( formMp == null || !request.isPostQueryString() )
				{
					if( query.length() > 0 )
						query.append( '&' );

					query.append( URLEncoder.encode( param.getName() ) );
					query.append( '=' );
					if( StringUtils.hasContent( value ) )
						query.append( value );
				}
				else
				{
					try
					{
						addFormMultipart( request, formMp, param.getName(), value );
					}
					catch( MessagingException e )
					{
						e.printStackTrace();
					}
				}

				break;
			case TEMPLATE :
				path = path.replaceAll( "\\{" + param.getName() + "\\}", value );
				break;
			case MATRIX :
				if( param.getType().equals( XmlBoolean.type.getName() ) )
				{
					if( value.toUpperCase().equals( "TRUE" ) || value.equals( "1" ) )
					{
						path += ";" + param.getName();
					}
				}
				else
				{
					path += ";" + param.getName();
					if( StringUtils.hasContent( value ) )
					{
						path += "=" + value;
					}
				}
			case PLAIN :
				break;
			}
		}

		if( PathUtils.isHttpPath( path ) )
		{
			try
			{
				httpMethod.setURI( new URI( path ) );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if( StringUtils.hasContent( path ) )
		{
			httpMethod.setPath( path );
		}

		if( query.length() > 0 && !request.isPostQueryString() )
		{
			httpMethod.setQueryString( query.toString() );
		}

		String acceptEncoding = request.getAccept();
		if( StringUtils.hasContent( acceptEncoding ) )
		{
			httpMethod.setRequestHeader( "Accept", acceptEncoding );
		}

		String encoding = System.getProperty( "soapui.request.encoding", StringUtils.unquote( request.getEncoding() ) );

		if( formMp != null )
		{
			// create request message
			try
			{
				if( request.hasRequestBody() && httpMethod instanceof EntityEnclosingMethod )
				{
					String requestContent = PropertyExpansionUtils.expandProperties( context, request.getRequestContent(),
							request.isEntitizeProperties() );
					if( StringUtils.hasContent( requestContent ) )
					{
						initRootPart( request, requestContent, formMp );
					}
				}

				for( Attachment attachment : request.getAttachments() )
				{
					MimeBodyPart part = new PreencodedMimeBodyPart( "binary" );
					part.setDisposition( "form-data; name=\"" + attachment.getName() + "\"" );
					part.setDataHandler( new DataHandler( new AttachmentDataSource( attachment ) ) );

					formMp.addBodyPart( part );
				}

				MimeMessage message = new MimeMessage( AttachmentUtils.JAVAMAIL_SESSION );
				message.setContent( formMp );
				message.saveChanges();
				RestRequestMimeMessageRequestEntity mimeMessageRequestEntity = new RestRequestMimeMessageRequestEntity(
						message, request );
				( ( EntityEnclosingMethod )httpMethod ).setRequestEntity( mimeMessageRequestEntity );
				httpMethod.setRequestHeader( "Content-Type", mimeMessageRequestEntity.getContentType() );
				httpMethod.setRequestHeader( "MIME-Version", "1.0" );
			}
			catch( Throwable e )
			{
				SoapUI.logError( e );
			}
		}
		else if( request.hasRequestBody() && httpMethod instanceof EntityEnclosingMethod )
		{
			httpMethod.setRequestHeader( "Content-Type", request.getMediaType() );

			if( request.isPostQueryString() )
			{
				( ( EntityEnclosingMethod )httpMethod ).setRequestEntity( new StringRequestEntity( query.toString() ) );
			}
			else
			{
				String requestContent = PropertyExpansionUtils.expandProperties( context, request.getRequestContent(),
						request.isEntitizeProperties() );
				List<Attachment> attachments = new ArrayList<Attachment>();

				for( Attachment attachment : request.getAttachments() )
				{
					if( attachment.getContentType().equals( request.getMediaType() ) )
					{
						attachments.add( attachment );
					}
				}

				if( StringUtils.hasContent( requestContent ) && attachments.isEmpty() )
				{
					try
					{
						byte[] content = encoding == null ? requestContent.getBytes() : requestContent.getBytes( encoding );
						( ( EntityEnclosingMethod )httpMethod ).setRequestEntity( new ByteArrayRequestEntity( content ) );
					}
					catch( UnsupportedEncodingException e )
					{
						( ( EntityEnclosingMethod )httpMethod ).setRequestEntity( new ByteArrayRequestEntity( requestContent
								.getBytes() ) );
					}
				}
				else if( attachments.size() > 0 )
				{
					try
					{
						MimeMultipart mp = null;

						if( StringUtils.hasContent( requestContent ) )
						{
							mp = new MimeMultipart();
							initRootPart( request, requestContent, mp );
						}
						else if( attachments.size() == 1 )
						{
							( ( EntityEnclosingMethod )httpMethod ).setRequestEntity( new InputStreamRequestEntity(
									attachments.get( 0 ).getInputStream() ) );

							httpMethod.setRequestHeader( "Content-Type", request.getMediaType() );
						}

						if( ( ( EntityEnclosingMethod )httpMethod ).getRequestEntity() == null )
						{
							if( mp == null )
								mp = new MimeMultipart();

							// init mimeparts
							AttachmentUtils.addMimeParts( request, attachments, mp, new StringToStringMap() );

							// create request message
							MimeMessage message = new MimeMessage( AttachmentUtils.JAVAMAIL_SESSION );
							message.setContent( mp );
							message.saveChanges();
							RestRequestMimeMessageRequestEntity mimeMessageRequestEntity = new RestRequestMimeMessageRequestEntity(
									message, request );
							( ( EntityEnclosingMethod )httpMethod ).setRequestEntity( mimeMessageRequestEntity );
							httpMethod.setRequestHeader( "Content-Type", mimeMessageRequestEntity.getContentType() );
							httpMethod.setRequestHeader( "MIME-Version", "1.0" );
						}
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void addFormMultipart( RestRequest request, MimeMultipart formMp, String name, String value )
			throws MessagingException
	{
		MimeBodyPart part = new MimeBodyPart();

		if( value.startsWith( "file:" ) )
		{
			String fileName = value.substring( 5 );
			File file = new File( fileName );
			part.setDisposition( "form-data; name=\"" + name + "\"; filename=\"" + file.getName() + "\"" );
			if( file.exists() )
			{
				part.setDataHandler( new DataHandler( new FileDataSource( file ) ) );
			}
			else
			{
				for( Attachment attachment : request.getAttachments() )
				{
					if( attachment.getName().equals( fileName ) )
					{
						part.setDataHandler( new DataHandler( new AttachmentDataSource( attachment ) ) );
						break;
					}
				}
			}

			part.setHeader( "Content-Type", ContentTypeHandler.getContentTypeFromFilename( file.getName() ) );
			part.setHeader( "Content-Transfer-Encoding", "binary" );
		}
		else
		{
			part.setDisposition( "form-data; name=\"" + name + "\"" );
			part.setText( URLEncoder.encode( value ), System
					.getProperty( "soapui.request.encoding", request.getEncoding() ) );
		}

		if( part != null )
		{
			formMp.addBodyPart( part );
		}
	}

	protected void initRootPart( RestRequest wsdlRequest, String requestContent, MimeMultipart mp )
			throws MessagingException
	{
		MimeBodyPart rootPart = new PreencodedMimeBodyPart( "8bit" );
		// rootPart.setContentID( AttachmentUtils.ROOTPART_SOAPUI_ORG );
		mp.addBodyPart( rootPart, 0 );

		DataHandler dataHandler = new DataHandler( new RestRequestDataSource( wsdlRequest, requestContent ) );
		rootPart.setDataHandler( dataHandler );
	}
}
