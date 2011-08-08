package com.eviware.soapui.support.httpclient;

/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import junit.framework.JUnit4TestAdapter;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.localserver.BasicServerTestBase;
import org.apache.http.localserver.LocalTestServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SSLSocketFactory}.
 */
public class TestSSLSocketFactory extends BasicServerTestBase
{

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( TestSSLSocketFactory.class );
	}

	private KeyManagerFactory createKeyManagerFactory() throws NoSuchAlgorithmException
	{
		String algo = KeyManagerFactory.getDefaultAlgorithm();
		try
		{
			return KeyManagerFactory.getInstance( algo );
		}
		catch( NoSuchAlgorithmException ex )
		{
			return KeyManagerFactory.getInstance( "SunX509" );
		}
	}

	private TrustManagerFactory createTrustManagerFactory() throws NoSuchAlgorithmException
	{
		String algo = TrustManagerFactory.getDefaultAlgorithm();
		try
		{
			return TrustManagerFactory.getInstance( algo );
		}
		catch( NoSuchAlgorithmException ex )
		{
			return TrustManagerFactory.getInstance( "SunX509" );
		}
	}

	private SSLContext serverSSLContext;
	private SSLContext clientSSLContext;

	@Before
	public void setUp() throws Exception
	{
		ClassLoader cl = getClass().getClassLoader();
		URL url = cl.getResource( "test.keystore" );
		KeyStore keystore = KeyStore.getInstance( "jks" );
		char[] pwd = "nopassword".toCharArray();
		keystore.load( url.openStream(), pwd );

		TrustManagerFactory tmf = createTrustManagerFactory();
		tmf.init( keystore );
		TrustManager[] tm = tmf.getTrustManagers();

		KeyManagerFactory kmfactory = createKeyManagerFactory();
		kmfactory.init( keystore, pwd );
		KeyManager[] km = kmfactory.getKeyManagers();

		this.serverSSLContext = SSLContext.getInstance( "TLS" );
		this.serverSSLContext.init( km, tm, null );

		this.clientSSLContext = SSLContext.getInstance( "TLS" );
		this.clientSSLContext.init( null, tm, null );

		this.localServer = new LocalTestServer( this.serverSSLContext );
		this.localServer.registerDefaultHandlers();

		this.localServer.start();
	}

	@Override
	protected HttpHost getServerHttp()
	{
		InetSocketAddress address = this.localServer.getServiceAddress();
		return new HttpHost( address.getHostName(), address.getPort(), "https" );
	}

	static class TestX509HostnameVerifier implements X509HostnameVerifier
	{

		private boolean fired = false;

		public boolean verify( String host, SSLSession session )
		{
			return true;
		}

		public void verify( String host, SSLSocket ssl ) throws IOException
		{
			this.fired = true;
		}

		public void verify( String host, String[] cns, String[] subjectAlts ) throws SSLException
		{
		}

		public void verify( String host, X509Certificate cert ) throws SSLException
		{
		}

		public boolean isFired()
		{
			return this.fired;
		}

	}

	@Test
	public void testBasicSSL() throws Exception
	{
		TestX509HostnameVerifier hostVerifier = new TestX509HostnameVerifier();

		SSLSocketFactory socketFactory = new SSLSocketFactory( this.clientSSLContext, hostVerifier );
		Scheme https = new Scheme( "https", 443, socketFactory );

		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getConnectionManager().getSchemeRegistry().register( https );

		HttpHost target = getServerHttp();
		HttpGet httpget = new HttpGet( "/random/100" );
		HttpResponse response = httpclient.execute( target, httpget );
		Assert.assertEquals( 200, response.getStatusLine().getStatusCode() );
		Assert.assertTrue( hostVerifier.isFired() );
	}

	@Test( expected = SSLPeerUnverifiedException.class )
	public void testSSLTrustVerification() throws Exception
	{
		// Use default SSL context
		SSLContext defaultsslcontext = SSLContext.getInstance( "TLS" );
		defaultsslcontext.init( null, null, null );

		SSLSocketFactory socketFactory = new SSLSocketFactory( defaultsslcontext,
				SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );

		Scheme https = new Scheme( "https", 443, socketFactory );
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getConnectionManager().getSchemeRegistry().register( https );

		HttpHost target = getServerHttp();
		HttpGet httpget = new HttpGet( "/random/100" );
		httpclient.execute( target, httpget );
	}

	@Test
	public void testSSLTrustVerificationOverride() throws Exception
	{
		// Use default SSL context
		SSLContext defaultsslcontext = SSLContext.getInstance( "TLS" );
		defaultsslcontext.init( null, null, null );

		SSLSocketFactory socketFactory = new SSLSocketFactory( new TrustStrategy()
		{

			public boolean isTrusted( final X509Certificate[] chain, final String authType ) throws CertificateException
			{
				return chain.length == 1;
			}

		}, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );

		Scheme https = new Scheme( "https", 443, socketFactory );
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getConnectionManager().getSchemeRegistry().register( https );

		HttpHost target = getServerHttp();
		HttpGet httpget = new HttpGet( "/random/100" );
		HttpResponse response = httpclient.execute( target, httpget );
		Assert.assertEquals( 200, response.getStatusLine().getStatusCode() );
	}

}
