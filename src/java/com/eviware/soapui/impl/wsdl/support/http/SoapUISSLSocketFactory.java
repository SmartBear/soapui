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
package com.eviware.soapui.impl.wsdl.support.http;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.ssl.KeyMaterial;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.support.StringUtils;

public class SoapUISSLSocketFactory extends SSLSocketFactory
{
	// a cache of factories for custom certificates/Keystores at the project level - never cleared
	private static Map<String, SSLSocketFactory> factoryMap = new HashMap<String, SSLSocketFactory>();
	private SSLContext sslContext = SSLContext.getInstance( "TLS" );
	private final static Logger log = Logger.getLogger( SoapUISSLSocketFactory.class );

	@SuppressWarnings( "deprecation" )
	public SoapUISSLSocketFactory( KeyStore keyStore, String keystorePassword ) throws KeyManagementException,
			UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
	{
		super( keyStore );

		// trust everyone!
		X509TrustManager tm = new X509TrustManager()
		{
			@Override
			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}

			@Override
			public void checkClientTrusted( X509Certificate[] arg0, String arg1 ) throws CertificateException
			{
			}

			@Override
			public void checkServerTrusted( X509Certificate[] arg0, String arg1 ) throws CertificateException
			{
			}
		};

		if( keyStore != null )
		{
			KeyManagerFactory kmfactory = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
			kmfactory.init( keyStore, keystorePassword != null ? keystorePassword.toCharArray() : null );
			KeyManager[] keymanagers = kmfactory.getKeyManagers();
			sslContext.init( keymanagers, new TrustManager[] { tm }, null );
		}
		else
		{
			sslContext.init( null, new TrustManager[] { tm }, null );
		}

		setHostnameVerifier( SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
	}

	private synchronized SSLSocket enableSocket( SSLSocket socket )
	{
		socket.getSession().invalidate();

		String protocols = System.getProperty( "soapui.https.protocols" );
		String ciphers = System.getProperty( "soapui.https.ciphers" );

		if( StringUtils.hasContent( protocols ) )
		{
			socket.setEnabledProtocols( protocols.split( "," ) );
		}
		else if( socket.getSupportedProtocols() != null )
		{
			socket.setEnabledProtocols( socket.getSupportedProtocols() );
		}

		if( StringUtils.hasContent( ciphers ) )
		{
			socket.setEnabledCipherSuites( ciphers.split( "," ) );
		}
		else if( socket.getSupportedCipherSuites() != null )
		{
			socket.setEnabledCipherSuites( socket.getSupportedCipherSuites() );
		}
		return socket;
	}

	@Override
	public Socket createSocket( HttpParams params ) throws IOException
	{
		String sslConfig = ( String )params.getParameter( BaseHttpRequestTransport.SOAPUI_SSL_CONFIG );

		if( StringUtils.isNullOrEmpty( sslConfig ) )
		{
			return enableSocket( ( SSLSocket )sslContext.getSocketFactory().createSocket() );
		}

		SSLSocketFactory factory = factoryMap.get( sslConfig );

		if( factory != null )
		{
			if( factory == this )
				return enableSocket( ( SSLSocket )sslContext.getSocketFactory().createSocket() );
			else
				return enableSocket( ( SSLSocket )factory.createSocket( params ) );
		}

		try
		{
			// try to create new factory for specified config
			int ix = sslConfig.lastIndexOf( ' ' );
			String keyStore = sslConfig.substring( 0, ix );
			String pwd = sslConfig.substring( ix + 1 );

			KeyStore ks = KeyStore.getInstance( KeyStore.getDefaultType() );

			if( keyStore.trim().length() > 0 )
			{
				File f = new File( keyStore );

				if( f.exists() )
				{
					log.info( "Initializing Keystore from [" + keyStore + "]" );

					try
					{
						KeyMaterial km = new KeyMaterial( f, pwd.toCharArray() );
						ks = km.getKeyStore();
					}
					catch( Exception e )
					{
						SoapUI.logError( e );
						pwd = null;
					}
				}
			}

			factory = new SoapUISSLSocketFactory( ks, pwd );
			factoryMap.put( sslConfig, factory );

			return enableSocket( ( SSLSocket )factory.createSocket( params ) );
		}
		catch( Exception gse )
		{
			SoapUI.logError( gse );
			return enableSocket( ( SSLSocket )super.createSocket( params ) );
		}
	}
}
