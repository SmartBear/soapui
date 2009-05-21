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

package com.eviware.soapui.impl.wsdl.support.http;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.KeyMaterial;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.SSLSettings;

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

	private static class Helper
	{
		private HttpClient httpClient;
		private final static Logger log = Logger.getLogger( HttpClientSupport.Helper.class );
		private SoapUIEasySSLProtocolSocketFactory easySSL;
		private SoapUIMultiThreadedHttpConnectionManager connectionManager;

		public Helper()
		{
			try
			{
				easySSL = new SoapUIEasySSLProtocolSocketFactory();
				initSSL( easySSL );

				Protocol easyhttps = new Protocol( "https", ( ProtocolSocketFactory )easySSL, 443 );
				Protocol.registerProtocol( "https", easyhttps );
			}
			catch( Throwable e )
			{
				SoapUI.logError( e );
			}

			Settings settings = SoapUI.getSettings();

			connectionManager = new SoapUIMultiThreadedHttpConnectionManager();
			connectionManager.getParams().setDefaultMaxConnectionsPerHost(
					( int )settings.getLong( HttpSettings.MAX_CONNECTIONS_PER_HOST, 500 ) );
			connectionManager.getParams().setMaxTotalConnections(
					( int )settings.getLong( HttpSettings.MAX_TOTAL_CONNECTIONS, 2000 ) );
			httpClient = new HttpClient( connectionManager );

			settings.addSettingsListener( new SettingsListener()
			{

				public void settingChanged( String name, String newValue, String oldValue )
				{
					if( newValue == null )
						return;

					if( name.equals( SSLSettings.KEYSTORE ) || name.equals( SSLSettings.KEYSTORE_PASSWORD ) )
					{
						try
						{
							log.info( "Updating keyStore.." );
							initKeyMaterial( easySSL );
						}
						catch( Throwable e )
						{
							SoapUI.logError( e );
						}
					}
					else if( name.equals( HttpSettings.MAX_CONNECTIONS_PER_HOST ) )
					{
						log.info( "Updating max connections per host to " + newValue );
						connectionManager.getParams().setDefaultMaxConnectionsPerHost( Integer.parseInt( newValue ) );
					}
					else if( name.equals( HttpSettings.MAX_TOTAL_CONNECTIONS ) )
					{
						log.info( "Updating max total connections host to " + newValue );
						connectionManager.getParams().setMaxTotalConnections( Integer.parseInt( newValue ) );
					}
				}
			} );
		}

		private void initSSL( EasySSLProtocolSocketFactory easySSL ) throws IOException, GeneralSecurityException
		{
			initKeyMaterial( easySSL );

			/*
			 * Commented out for now - EasySSLProtocolSocketFactory already trusts
			 * everything! Below is some code that might work for when SoapUI moves
			 * away from "EasySSLProtocolSocketFactory".
			 * 
			 * String trustStore = settings.getString( SSLSettings.TRUSTSTORE, null
			 * ); trustStore = trustStore != null ? trustStore.trim() : ""; pass =
			 * settings.getString( SSLSettings.TRUSTSTORE_PASSWORD, "" ); pwd =
			 * pass.toCharArray(); if ( !"".equals( trustStore ) ) { File f = new
			 * File( trustStore ); if ( f.exists() ) { TrustMaterial tm = null; try
			 * { tm = new TrustMaterial( trustStore, pwd ); } catch (
			 * GeneralSecurityException gse ) { String trimmedPass = pass.trim();
			 * if ( "".equals( trimmedPass ) ) { // If the password is all spaces,
			 * then we'll allow // loading of the TrustMaterial without a password.
			 * tm = new TrustMaterial( trustStore ); } else { log.error(
			 * "Failed to load TrustMaterial: " + gse ); } } if ( tm != null ) {
			 * easySSL.setTrustMaterial( tm ); log.info(
			 * "Added TrustStore from file [" + trustStore + "]" ); } } else {
			 * log.error( "Missing trustStore [" + trustStore + "]" ); } }
			 */
		}

		private void initKeyMaterial( EasySSLProtocolSocketFactory easySSL ) throws IOException,
				NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException
		{
			Settings settings = SoapUI.getSettings();

			String keyStore = settings.getString( SSLSettings.KEYSTORE, null );
			keyStore = keyStore != null ? keyStore.trim() : "";
			String pass = settings.getString( SSLSettings.KEYSTORE_PASSWORD, "" );
			char[] pwd = pass.toCharArray();
			if( !"".equals( keyStore ) )
			{
				log.info( "Initializing KeyStore" );

				File f = new File( keyStore );
				if( f.exists() )
				{
					KeyMaterial km = null;
					try
					{
						km = new KeyMaterial( keyStore, pwd );
						log.info( "Set KeyMaterial from file [" + keyStore + "]" );
					}
					catch( GeneralSecurityException gse )
					{
						SoapUI.logError( gse );
					}
					if( km != null )
					{
						easySSL.setKeyMaterial( km );
					}
				}
			}
			else
			{
				easySSL.setKeyMaterial( null );
			}
		}

		public HttpClient getHttpClient()
		{
			return httpClient;
		}
	}

	public static HttpClient getHttpClient()
	{
		return helper.getHttpClient();
	}

	public static void applyHttpSettings( HttpMethod httpMethod, Settings settings )
	{
		// user agent?
		String userAgent = settings.getString( HttpSettings.USER_AGENT, null );
		if( userAgent != null && userAgent.length() > 0 )
			httpMethod.setRequestHeader( "User-Agent", userAgent );

		// timeout?
		long timeout = settings.getLong( HttpSettings.SOCKET_TIMEOUT, HttpSettings.DEFAULT_SOCKET_TIMEOUT );
		httpMethod.getParams().setSoTimeout( ( int )timeout );
	}

	public static String getResponseCompressionType( HttpMethod method )
	{
		Header contentType = method.getResponseHeader( "Content-Type" );
		Header contentEncoding = method.getResponseHeader( "Content-Encoding" );

		String compressionAlg = contentType == null ? null : CompressionSupport.getAvailableAlgorithm( contentType.getValue() );
		if( compressionAlg != null )
			return compressionAlg;

		if( contentEncoding == null )
			return null;
		else
			return CompressionSupport.getAvailableAlgorithm( contentEncoding.getValue() );
	}
}
