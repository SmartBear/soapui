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

import java.util.Arrays;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.PreencodedMimeBodyPart;

import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.AttachmentUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlRequestDataSource;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlRequestMimeMessageRequestEntity;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.support.MessageXmlObject;
import com.eviware.soapui.impl.wsdl.support.MessageXmlPart;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringMap;

public class WsdlPackagingRequestFilter extends AbstractRequestFilter
{

	@Override
	public void filterWsdlRequest( SubmitContext context, WsdlRequest request )
	{
		ExtendedPostMethod postMethod = ( ExtendedPostMethod )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );
		String requestContent = ( String )context.getProperty( BaseHttpRequestTransport.REQUEST_CONTENT );

		try
		{
			String content = initWsdlRequest( request, postMethod, requestContent );
			if( content != null )
				context.setProperty( BaseHttpRequestTransport.REQUEST_CONTENT, content );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	protected String initWsdlRequest( WsdlRequest wsdlRequest, ExtendedPostMethod postMethod, String requestContent )
			throws Exception
	{
		MimeMultipart mp = null;

		StringToStringMap contentIds = new StringToStringMap();
		boolean isXOP = wsdlRequest.isMtomEnabled() && wsdlRequest.isForceMtom();

		// preprocess only if neccessary
		if( wsdlRequest.isMtomEnabled() || wsdlRequest.isInlineFilesEnabled() || wsdlRequest.getAttachmentCount() > 0 )
		{
			try
			{
				mp = new MimeMultipart();

				MessageXmlObject requestXmlObject = new MessageXmlObject( wsdlRequest.getOperation(), requestContent, true );
				MessageXmlPart[] requestParts = requestXmlObject.getMessageParts();
				for( MessageXmlPart requestPart : requestParts )
				{
					if( AttachmentUtils.prepareMessagePart( wsdlRequest, mp, requestPart, contentIds ) )
						isXOP = true;
				}
				requestContent = requestXmlObject.getMessageContent();
			}
			catch( Throwable e )
			{
				SoapUI.log.warn( "Failed to process inline/MTOM attachments; " + e );
			}
		}

		// non-multipart request?
		if( !isXOP && ( mp == null || mp.getCount() == 0 ) && hasContentAttachmentsOnly( wsdlRequest ) )
		{
			String encoding = System.getProperty( "soapui.request.encoding", StringUtils.unquote( wsdlRequest
					.getEncoding() ) );
			byte[] content = StringUtils.isNullOrEmpty( encoding ) ? requestContent.getBytes() : requestContent
					.getBytes( encoding );
			postMethod.setRequestEntity( new ByteArrayRequestEntity( content ) );
		}
		else
		{
			// make sure..
			if( mp == null )
				mp = new MimeMultipart();

			// init root part
			initRootPart( wsdlRequest, requestContent, mp, isXOP );

			// init mimeparts
			AttachmentUtils.addMimeParts( wsdlRequest, Arrays.asList( wsdlRequest.getAttachments() ), mp, contentIds );

			// create request message
			MimeMessage message = new MimeMessage( AttachmentUtils.JAVAMAIL_SESSION );
			message.setContent( mp );
			message.saveChanges();
			WsdlRequestMimeMessageRequestEntity mimeMessageRequestEntity = new WsdlRequestMimeMessageRequestEntity(
					message, isXOP, wsdlRequest );
			postMethod.setRequestEntity( mimeMessageRequestEntity );
			postMethod.setRequestHeader( "Content-Type", mimeMessageRequestEntity.getContentType() );
			postMethod.setRequestHeader( "MIME-Version", "1.0" );
		}

		return requestContent;
	}

	private boolean hasContentAttachmentsOnly( WsdlRequest wsdlRequest )
	{
		for( Attachment attachment : wsdlRequest.getAttachments() )
		{
			if( attachment.getAttachmentType() != Attachment.AttachmentType.CONTENT )
				return false;
		}

		return true;
	}

	/**
	 * Creates root BodyPart containing message
	 */

	protected void initRootPart( WsdlRequest wsdlRequest, String requestContent, MimeMultipart mp, boolean isXOP )
			throws MessagingException
	{
		MimeBodyPart rootPart = new PreencodedMimeBodyPart( "8bit" );
		rootPart.setContentID( AttachmentUtils.ROOTPART_SOAPUI_ORG );
		mp.addBodyPart( rootPart, 0 );

		DataHandler dataHandler = new DataHandler( new WsdlRequestDataSource( wsdlRequest, requestContent, isXOP ) );
		rootPart.setDataHandler( dataHandler );
	}
}
