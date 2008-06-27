/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.PreencodedMimeBodyPart;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.support.MessageXmlObject;
import com.eviware.soapui.impl.wsdl.support.MessageXmlPart;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.SoapUIHostConfiguration;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * HTTP transport that uses HttpClient to send/receive SOAP messages
 * 
 * @author Ole.Matzura
 */

public class HttpClientRequestTransport implements BaseHttpRequestTransport
{
	private List<RequestFilter> filters = new ArrayList<RequestFilter>();
	private final static Logger log = Logger.getLogger(HttpClientRequestTransport.class);
	
	public HttpClientRequestTransport()
	{
	}
	
	public void addRequestFilter(RequestFilter filter)
	{
		filters.add( filter );
	}

	public void removeRequestFilter(RequestFilter filter)
	{
		filters.remove( filter );
	}

	public void abortRequest( SubmitContext submitContext )
	{
		HttpMethodBase postMethod = (HttpMethodBase) submitContext.getProperty( HTTP_METHOD );
		if( postMethod != null )
			postMethod.abort();
	}

	public Response sendRequest( SubmitContext submitContext, AbstractHttpRequest<?> httpRequest ) throws Exception
	{
		HttpClient httpClient = HttpClientSupport.getHttpClient();
		ExtendedHttpMethod httpMethod = createHttpMethod( httpRequest );
		boolean createdState = false;
		
		HttpState httpState = (HttpState) submitContext.getProperty(SubmitContext.HTTP_STATE_PROPERTY);
		if( httpState == null )
		{
		   httpState = new HttpState();
		   submitContext.setProperty( SubmitContext.HTTP_STATE_PROPERTY, httpState );
		   createdState = true;
		}
		
		HostConfiguration hostConfiguration = new HostConfiguration();

		String localAddress = System.getProperty( "soapui.bind.address", httpRequest.getBindAddress() );
		if( localAddress == null || localAddress.trim().length() == 0 )
			localAddress = SoapUI.getSettings().getString( HttpSettings.BIND_ADDRESS, null );
		
		if( localAddress != null && localAddress.trim().length() > 0 )
		{
			try
			{
				hostConfiguration.setLocalAddress( InetAddress.getByName( localAddress ));
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
		
		submitContext.setProperty( HTTP_METHOD, httpMethod );
		submitContext.setProperty( POST_METHOD, httpMethod );
		submitContext.setProperty( HTTP_CLIENT, httpClient );
		submitContext.setProperty( REQUEST_CONTENT, httpRequest.getRequestContent() );
		submitContext.setProperty( HOST_CONFIGURATION, hostConfiguration );
		submitContext.setProperty( WSDL_REQUEST, httpRequest );
		
		for( RequestFilter filter : filters )
		{
			filter.filterRequest( submitContext, httpRequest );
		}
		
		Response response = null;
		
		try
		{			
			Settings settings = httpRequest.getSettings();

			// get request content
			String requestContent = (String) submitContext.getProperty(REQUEST_CONTENT);

			// init
			boolean isWsdl = httpRequest instanceof WsdlRequest;
			if( isWsdl)
				requestContent = initWsdlRequest((WsdlRequest) httpRequest, (ExtendedPostMethod) httpMethod, requestContent);
			
			//	custom http headers last so they can be overridden
			StringToStringMap headers = httpRequest.getRequestHeaders();
			for( String header : headers.keySet() )
			{
				String headerValue = headers.get( header );
				headerValue = PropertyExpansionUtils.expandProperties( submitContext, headerValue );
				httpMethod.setRequestHeader( header, headerValue );
			}
			
			//	do request
			WsdlProject project = (WsdlProject) httpRequest.getOperation().getInterface().getProject();
			WssCrypto crypto = project.getWssContainer().getCryptoByName( httpRequest.getSslKeystore() );
			if( crypto != null && WssCrypto.STATUS_OK.equals( crypto.getStatus() ) )
			{
				hostConfiguration.getParams().setParameter( SoapUIHostConfiguration.SOAPUI_SSL_CONFIG, 
							 crypto.getSource() + " " + crypto.getPassword() );
			}	
				
			// dump file?
			httpMethod.setDumpFile( 
					PropertyExpansionUtils.expandProperties( submitContext, httpRequest.getDumpFile() ));

			//	include request time?
			if (settings.getBoolean(HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN))
				httpMethod.initStartTime();
			
			// submit!
			httpClient.executeMethod(hostConfiguration, httpMethod, httpState);
			httpMethod.getTimeTaken();
			
			// check content-type for multiplart
			Header responseContentTypeHeader = httpMethod.getResponseHeader( "Content-Type" );
			
			if( !settings.getBoolean( WsdlRequest.INLINE_RESPONSE_ATTACHMENTS ) && 
				 responseContentTypeHeader != null && 
				 responseContentTypeHeader.getValue().toUpperCase().startsWith( "MULTIPART" ))
			{
				if( isWsdl )
					response = new WsdlMimeMessageResponse( (WsdlRequest) httpRequest, httpMethod, requestContent, submitContext );
				else
					response = new MimeMessageResponse( httpRequest, httpMethod, requestContent, submitContext );
			}
			else
			{
				if( isWsdl )
					response = new WsdlSinglePartHttpResponse(  (WsdlRequest) httpRequest, httpMethod, requestContent, submitContext );
				else
					response = new SinglePartHttpResponse(  httpRequest, httpMethod, requestContent, submitContext );
			}
			
			return response;
		}
		catch( Throwable t )
		{
			throw new Exception( t );
		}
		finally
		{
			for( RequestFilter filter : filters )
			{
				filter.afterRequest( submitContext, response );
			}
			
			if (httpMethod != null)
			{
				httpMethod.releaseConnection();
			}
			else log.error( "PostMethod is null");
			
			if( createdState )
				submitContext.setProperty( SubmitContext.HTTP_STATE_PROPERTY, null );
		}		
	}

	private ExtendedHttpMethod createHttpMethod(AbstractHttpRequest<?> httpRequest)
	{
		if( httpRequest instanceof RestRequest )
		{
			RestRequest restRequest = (RestRequest) httpRequest;
			switch( restRequest.getMethod())
			{
				case GET : return new ExtendedGetMethod();
				case DELETE : return new ExtendedDeleteMethod();
				case PUT : return new ExtendedPutMethod(); 
			}
		}
		
		return new ExtendedPostMethod();
	}

	private String initWsdlRequest(WsdlRequest wsdlRequest, ExtendedPostMethod postMethod, String requestContent) throws Exception
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
				
				MessageXmlObject requestXmlObject = new MessageXmlObject(( WsdlOperation ) wsdlRequest.getOperation(), 
							requestContent, true);
				MessageXmlPart[] requestParts = requestXmlObject.getMessageParts();
				for (MessageXmlPart requestPart : requestParts)
				{
					if (AttachmentUtils.prepareMessagePart(wsdlRequest, mp, requestPart, contentIds))
						isXOP = true;
				}
				requestContent = requestXmlObject.getMessageContent();
			}
			catch (Throwable e)
			{
				log.warn( "Failed to process inline/MTOM attachments; " + e );
			}			
		}
		
		// non-multipart request?
		if( !isXOP && (mp == null || mp.getCount() == 0 ) && wsdlRequest.getAttachmentCount() == 0 )
		{
			String encoding = StringUtils.unquote( wsdlRequest.getEncoding());
			byte[] content = encoding == null ? requestContent.getBytes() : requestContent.getBytes(encoding);
			postMethod.setRequestEntity(new ByteArrayRequestEntity(content));
		}
		else
		{
			// make sure..
			if( mp == null )
				mp = new MimeMultipart();
			
			// init root part
			initRootPart(wsdlRequest, requestContent, mp, isXOP);
			
			// init mimeparts
			AttachmentUtils.addMimeParts(wsdlRequest, mp, contentIds);
			
			// create request message
			MimeMessage message = new MimeMessage( AttachmentUtils.JAVAMAIL_SESSION );
			message.setContent( mp );
			message.saveChanges();
			MimeMessageRequestEntity mimeMessageRequestEntity = new MimeMessageRequestEntity( message, isXOP, wsdlRequest );
			postMethod.setRequestEntity( mimeMessageRequestEntity );
			postMethod.setRequestHeader( "Content-Type", mimeMessageRequestEntity.getContentType() );
			postMethod.setRequestHeader( "MIME-Version", "1.0" );
		}
		
		return requestContent;
	}

	/**
	 * Creates root BodyPart containing message
	 */
	
	private void initRootPart(WsdlRequest wsdlRequest, String requestContent, MimeMultipart mp, boolean isXOP) throws MessagingException
	{
		MimeBodyPart rootPart = new PreencodedMimeBodyPart( "8bit" );
		rootPart.setContentID( AttachmentUtils.ROOTPART_SOAPUI_ORG );
		mp.addBodyPart( rootPart, 0 );
		
		DataHandler dataHandler = new DataHandler( new WsdlRequestDataSource( wsdlRequest, requestContent, isXOP ) );
		rootPart.setDataHandler( dataHandler);
	}
}
