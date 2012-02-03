/*
 *  soapUI, copyright (C) 2004-2011 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support.http;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.commons.ssl.KeyMaterial;
import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.SSLSettings;
import com.eviware.soapui.support.StringUtils;

/**
 * HttpClient related tools
 * 
 * @author Ole.Matzura
 */

public class HttpClientSupport
{
	private final static Helper helper = new Helper();

	/**
	 * Internal helper to ensure synchronized access..
	 */

	public static class SoapUIHttpClient extends DefaultHttpClient
	{

		public SoapUIHttpClient( final ClientConnectionManager conman )
		{
			super( conman, null );
		}

		@Override
		protected HttpRequestExecutor createRequestExecutor()
		{
			return new SoapUIHttpRequestExecutor();
		}
	}

	public static class SoapUIHttpRequestExecutor extends HttpRequestExecutor
	{
		protected HttpResponse doSendRequest( HttpRequest request, HttpClientConnection conn, HttpContext context )
				throws IOException, HttpException
		{
			HttpResponse response = super.doSendRequest( request, conn, context );

			RequestWrapper w = ( RequestWrapper )request;
			if( w.getOriginal() instanceof ExtendedHttpMethod )
				( ( ExtendedHttpMethod )w.getOriginal() ).getMetrics().getTimeToFirstByteTimer().start();
			return response;
		}

		@Override
		protected HttpResponse doReceiveResponse( final HttpRequest request, final HttpClientConnection conn,
				final HttpContext context ) throws HttpException, IOException
		{
			if( request == null )
			{
				throw new IllegalArgumentException( "HTTP request may not be null" );
			}
			if( conn == null )
			{
				throw new IllegalArgumentException( "HTTP connection may not be null" );
			}
			if( context == null )
			{
				throw new IllegalArgumentException( "HTTP context may not be null" );
			}

			HttpResponse response = null;
			int statuscode = 0;

			while( response == null || statuscode < HttpStatus.SC_OK )
			{
				response = conn.receiveResponseHeader();

				RequestWrapper w = ( RequestWrapper )request;
				SoapUIMetrics metrics = null;
				if( w.getOriginal() instanceof ExtendedHttpMethod )
				{
					metrics = ( ( ExtendedHttpMethod )w.getOriginal() ).getMetrics();
					metrics.getTimeToFirstByteTimer().stop();
					metrics.getReadTimer().start();
				}

				if( canResponseHaveBody( request, response ) )
				{
					conn.receiveResponseEntity( response );
					if( metrics != null )
						metrics.getReadTimer().stop();
				}

				statuscode = response.getStatusLine().getStatusCode();

				SoapUIMetrics connectionMetrics = ( SoapUIMetrics )conn.getMetrics();
				metrics.getConnectTimer().set( connectionMetrics.getConnectTimer().getStart(),
						connectionMetrics.getConnectTimer().getStop() );
				metrics.getDNSTimer().set( connectionMetrics.getDNSTimer().getStart(),
						connectionMetrics.getDNSTimer().getStop() );
				// reset connection-level metrics
				connectionMetrics.reset();

			} // while intermediate response

			return response;
		}
	}

	private static class Helper
	{
		private SoapUIHttpClient httpClient;
		private final static Logger log = Logger.getLogger( HttpClientSupport.Helper.class );
		private SoapUIMultiThreadedHttpConnectionManager connectionManager;
		private SoapUISSLSocketFactory socketFactory;

		public Helper()
		{
			Settings settings = SoapUI.getSettings();
			SchemeRegistry registry = new SchemeRegistry();
			registry.register( new Scheme( "http", 80, PlainSocketFactory.getSocketFactory() ) );

			try
			{
				socketFactory = initSocketFactory();
				registry.register( new Scheme( "https", 443, socketFactory ) );
			}
			catch( Throwable e )
			{
				SoapUI.logError( e );
			}

			connectionManager = new SoapUIMultiThreadedHttpConnectionManager( registry );
			//			connectionManager.setMaxConnectionsPerHost( ( int )settings.getLong( HttpSettings.MAX_CONNECTIONS_PER_HOST,
			//					500 ) );
			//			connectionManager.setMaxTotalConnections( ( int )settings.getLong( HttpSettings.MAX_TOTAL_CONNECTIONS, 2000 ) );

			httpClient = new SoapUIHttpClient( connectionManager );
			httpClient.getAuthSchemes().register( AuthPolicy.NTLM, new NTLMSchemeFactory() );
			httpClient.getAuthSchemes().register( AuthPolicy.SPNEGO, new NTLMSchemeFactory() );

			settings.addSettingsListener( new SSLSettingsListener() );
		}

		public SoapUIHttpClient getHttpClient()
		{
			return httpClient;
		}

		public HttpResponse execute( ExtendedHttpMethod method, HttpContext httpContext ) throws ClientProtocolException,
				IOException
		{
			method.afterWriteRequest();
			HttpResponse httpResponse = httpClient.execute( ( HttpUriRequest )method, httpContext );
			method.setHttpResponse( httpResponse );
			return httpResponse;
		}

		public HttpResponse execute( ExtendedHttpMethod method ) throws ClientProtocolException, IOException
		{
			method.afterWriteRequest();
			HttpResponse httpResponse = httpClient.execute( ( HttpUriRequest )method );
			method.setHttpResponse( httpResponse );
			return httpResponse;
		}

		public final class SSLSettingsListener implements SettingsListener
		{
			public void settingChanged( String name, String newValue, String oldValue )
			{
				if( !StringUtils.hasContent( newValue ) )
					return;

				if( name.equals( SSLSettings.KEYSTORE ) || name.equals( SSLSettings.KEYSTORE_PASSWORD ) )
				{
					try
					{
						log.info( "Updating keyStore.." );
						initSocketFactory();
					}
					catch( Throwable e )
					{
						SoapUI.logError( e );
					}
				}
				else if( name.equals( HttpSettings.MAX_CONNECTIONS_PER_HOST ) )
				{
					log.info( "Updating max connections per host to " + newValue );
					//					connectionManager.setMaxConnectionsPerHost( Integer.parseInt( newValue ) );
				}
				else if( name.equals( HttpSettings.MAX_TOTAL_CONNECTIONS ) )
				{
					log.info( "Updating max total connections host to " + newValue );
					//					connectionManager.setMaxTotalConnections( Integer.parseInt( newValue ) );
				}
			}

			@Override
			public void settingsReloaded()
			{
				try
				{
					log.info( "Updating keyStore.." );
					initSocketFactory();
				}
				catch( Throwable e )
				{
					SoapUI.logError( e );
				}
			}
		}

		public SoapUISSLSocketFactory initSocketFactory() throws KeyStoreException, NoSuchAlgorithmException,
				CertificateException, IOException, UnrecoverableKeyException, KeyManagementException
		{
			KeyStore keyStore = null;
			Settings settings = SoapUI.getSettings();

			String keyStoreUrl = settings.getString( SSLSettings.KEYSTORE, null );
			keyStoreUrl = keyStoreUrl != null ? keyStoreUrl.trim() : "";
			String pass = settings.getString( SSLSettings.KEYSTORE_PASSWORD, "" );
			char[] pwd = pass.toCharArray();

			if( keyStoreUrl.trim().length() > 0 )
			{
				File f = new File( keyStoreUrl );
				if( f.exists() )
				{
					log.info( "Initializing KeyStore" );

					try
					{
						KeyMaterial km = new KeyMaterial( f, pwd );
						keyStore = km.getKeyStore();
					}
					catch( Exception e )
					{
						SoapUI.logError( e );
					}
				}
			}

			return new SoapUISSLSocketFactory( keyStore, pass );
		}
	}

	public static SoapUIHttpClient getHttpClient()
	{
		return helper.getHttpClient();
	}

	public static HttpResponse execute( ExtendedHttpMethod method, HttpContext httpContext )
			throws ClientProtocolException, IOException
	{
		return helper.execute( method, httpContext );
	}

	public static HttpResponse execute( ExtendedHttpMethod method ) throws ClientProtocolException, IOException
	{
		return helper.execute( method );
	}

	public static void applyHttpSettings( HttpRequest httpMethod, Settings settings )
	{
		// user agent?
		String userAgent = settings.getString( HttpSettings.USER_AGENT, null );
		if( userAgent != null && userAgent.length() > 0 )
			httpMethod.setHeader( "User-Agent", userAgent );

		// timeout?
		long timeout = settings.getLong( HttpSettings.SOCKET_TIMEOUT, HttpSettings.DEFAULT_SOCKET_TIMEOUT );
		httpMethod.getParams().setParameter( CoreConnectionPNames.SO_TIMEOUT, ( int )timeout );
	}

	public static String getResponseCompressionType( HttpResponse httpResponse )
	{
		Header contentType = httpResponse.getEntity().getContentType();
		Header contentEncoding = httpResponse.getEntity().getContentEncoding();

		return getCompressionType( contentType == null ? null : contentType.getValue(), contentEncoding == null ? null
				: contentEncoding.getValue() );
	}

	public static String getCompressionType( String contentType, String contentEncoding )
	{
		String compressionAlg = contentType == null ? null : CompressionSupport.getAvailableAlgorithm( contentType );
		if( compressionAlg != null )
			return compressionAlg;

		if( contentEncoding == null )
			return null;
		else
			return CompressionSupport.getAvailableAlgorithm( contentEncoding );
	}

	public static void addSSLListener( Settings settings )
	{
		settings.addSettingsListener( helper.new SSLSettingsListener() );
	}
}
