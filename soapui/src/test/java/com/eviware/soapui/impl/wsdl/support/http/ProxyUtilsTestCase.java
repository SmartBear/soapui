/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support.http;

import com.eviware.soapui.impl.settings.SettingsImpl;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedGetMethod;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.*;

public class ProxyUtilsTestCase
{
	private static final String URL = "http://example.com";

	public static final String SYSTEM_PROPERTY_PROXY_HOST = "systempropertyhost.com";
	public static final String SYSTEM_PROPERTY_PROXY_PORT = "1";

	public static final String MANUAL_SETTING_PROXY_HOST = "manualsettingshost.com";
	public static final String MANUAL_SETTING_PROXY_PORT = "2";

	public static final String AUTOMATIC_PROXY_HOST = "autosettingshost.com";
	public static final String AUTOMATIC_PROXY_PORT = "3";

	private HttpUriRequest httpMethod;

	/* FIXME This will do nslookups which will not always mach of natural reasons since test.com is a real domain
		What is the purpose of this? */
	@Test
	@Ignore
	public void testExcludes()
	{
		assertFalse( ProxyUtils.excludes( new String[] { "" }, "www.test.com", 8080 ) );
		assertTrue( ProxyUtils.excludes( new String[] { "test.com" }, "www.test.com", 8080 ) );
		assertFalse( ProxyUtils.excludes( new String[] { "test2.com" }, "www.test.com", 8080 ) );
		assertTrue( ProxyUtils.excludes( new String[] { "test.com:8080" }, "www.test.com", 8080 ) );
		assertFalse( ProxyUtils.excludes( new String[] { "test2.com:8080" }, "www.test.com", 8080 ) );
		assertFalse( ProxyUtils.excludes( new String[] { "test.com:8081" }, "www.test.com", 8080 ) );
		assertTrue( ProxyUtils.excludes( new String[] { "test.com:8080", "test.com:8081" }, "www.test.com", 8080 ) );
		assertTrue( ProxyUtils.excludes( new String[] { "test.com:8080", "test.com" }, "www.test.com", 8080 ) );
	}

	@Before
	public void setup()
	{
		clearProxySystemProperties();
		httpMethod = new ExtendedGetMethod();
	}

	@After
	public void teardown(){
		ProxyUtils.setAutoProxy( false );
		ProxyUtils.setProxyEnabled( false );
	}

	@Test
	public void givenProxyEnabledAndProxyPropertiesSetThenSetAutoProxy()
	{
		ProxyUtils.setProxyEnabled( true );
		ProxyUtils.setAutoProxy( true );
		setProxySystemProperties();

		ProxyUtils.initProxySettings( manualSettings(), httpMethod, null, URL, null );

		assertProxyHost( SYSTEM_PROPERTY_PROXY_HOST );
	}

	@Test
	public void givenAutomaticProxyDetectionAndProxyPropertiesSetThenSetAutoProxy()
	{
		ProxyUtils.setProxyEnabled( true );
		ProxyUtils.setAutoProxy( true );
		setProxySystemProperties();

		ProxyUtils.initProxySettings( emptySettings(), httpMethod, null, URL, null );

		assertProxyHost( SYSTEM_PROPERTY_PROXY_HOST );
	}

	@Test
	public void givenProxyDisabledThenUseDefaultRoutePlanner()
	{
		ProxyUtils.setProxyEnabled( false );
		ProxyUtils.setAutoProxy( false );
		setProxySystemProperties();

		ProxyUtils.initProxySettings( emptySettings(), httpMethod, null, URL, null );
		assertProxyHost( null );

	}

	@Test
	public void givenProxyEnabledAndManuallyConfiguredThenSetProxy()
	{
		ProxyUtils.setProxyEnabled( true );

		ProxyUtils.setAutoProxy( false );

		ProxyUtils.initProxySettings( manualSettings(), httpMethod, null, URL, null );

		assertProxyHost( MANUAL_SETTING_PROXY_HOST );
	}

	@Test
	public void givenAutomaticProxyDetectionAndNoProxyAvailableThenSetDirectProxyType()
	{
		ProxyUtils.setProxyEnabled( true );

		ProxyUtils.setAutoProxy( true );

		ProxyUtils.initProxySettings( manualSettings(), httpMethod, null, URL, null );

		assertProxyHost( null );
	}

	@Test
	@Ignore
	public void givenAutomaticProxyDetectionAndEnvironmentProxySetThenUseTheEnvironmentProxy()
	{
		ProxyUtils.setProxyEnabled( true );

		ProxyUtils.setAutoProxy( true );

		ProxyUtils.initProxySettings( manualSettings(), httpMethod, null, URL, null );

		assertProxyHost( "environmentshost.com" );
	}

	private Settings emptySettings()
	{
		return new SettingsImpl();
	}

	private Settings manualSettings()
	{
		Settings settings = emptySettings();
		settings.setString( ProxySettings.HOST, MANUAL_SETTING_PROXY_HOST );
		settings.setString( ProxySettings.PORT, MANUAL_SETTING_PROXY_PORT );
		return settings;
	}

	private void clearProxySystemProperties()
	{
		System.clearProperty( "http.proxyHost" );
		System.clearProperty( "http.proxyPort" );
	}

	private void setProxySystemProperties()
	{
		System.setProperty( "http.proxyHost", SYSTEM_PROPERTY_PROXY_HOST );
		System.setProperty( "http.proxyPort", SYSTEM_PROPERTY_PROXY_PORT );
	}

	private void assertAutoProxy( Proxy.Type type )
	{
		HttpRoutePlanner routePlanner = HttpClientSupport.getHttpClient().getRoutePlanner();

		ProxySelectorRoutePlanner proxyRoutePlanner = ( ProxySelectorRoutePlanner )routePlanner;

		ProxySelector proxySelector = proxyRoutePlanner.getProxySelector();

		List<Proxy> select = proxySelector.select( URI.create( URL ) );

		assertEquals( type, select.get( 0 ).type() );
	}

	private void assertProxyHost( String expectedProxyHost )
	{
		HttpRoutePlanner routePlanner = HttpClientSupport.getHttpClient().getRoutePlanner();
		HttpRoute httpRoute = null;
		try
		{
			httpRoute = routePlanner.determineRoute( new HttpHost( "soapui.org" ), httpMethod, null );
		}
		catch( HttpException e )
		{
			e.printStackTrace();
			fail( e.getMessage() );
		}
		if( expectedProxyHost == null )
		{
			assertNull( httpRoute.getProxyHost() );
		}
		else
		{
			assertEquals( expectedProxyHost, httpRoute.getProxyHost().getHostName() );
		}
	}
}
