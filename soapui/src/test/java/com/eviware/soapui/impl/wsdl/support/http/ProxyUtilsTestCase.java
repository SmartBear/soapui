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
import junit.framework.JUnit4TestAdapter;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
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

	public static final String URL_STRING_SYSTEM_PROPERTY = String.format( "http://%s:%s", SYSTEM_PROPERTY_PROXY_HOST, SYSTEM_PROPERTY_PROXY_PORT );
	public static final String URL_STRING_MANUAL_SETTINGS = String.format( "http://%s:%s", MANUAL_SETTING_PROXY_HOST, MANUAL_SETTING_PROXY_PORT );
	public static final String URL_STRING_AUTOMATIC_PROXY = String.format( "http://%s:%s", AUTOMATIC_PROXY_HOST, AUTOMATIC_PROXY_PORT );

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( ProxyUtilsTestCase.class );
	}

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
	}

	@Test
	public void givenProxyEnabledAndProxyPropertiesSetThenSetAutoProxy()
	{

		ProxyUtils.setManualProxy( false );
		ProxyUtils.setProxyEnabled( true );
		setProxySystemProperties();

		HttpUriRequest httpMethod = new ExtendedGetMethod();

		ProxyUtils.initProxySettings( manualSettings(), httpMethod, null, URL, null );

		assertAutoProxy( Proxy.Type.HTTP );
	}

	@Test
	public void givenAutomaticProxyDetectionAndProxyPropertiesSetThenSetAutoProxy()
	{
		ProxyUtils.setManualProxy( false );
		ProxyUtils.setProxyEnabled( true );
		setProxySystemProperties();

		HttpUriRequest httpMethod = new ExtendedGetMethod();

		ProxyUtils.initProxySettings( emptySettings(), httpMethod, null, URL, null );

		assertAutoProxy( Proxy.Type.HTTP );
	}

	@Test
	public void givenProxyDisabledButProxyPropertiesSetThenSetProxyAnyway()
	{
		ProxyUtils.setManualProxy( false );
		ProxyUtils.setProxyEnabled( true );
		setProxySystemProperties();

		HttpUriRequest httpMethod = new ExtendedGetMethod();

		ProxyUtils.initProxySettings( emptySettings(), httpMethod, null, URL, null );

		assertAutoProxy( Proxy.Type.HTTP );
	}

	@Test
	public void givenProxyEnabledAndManuallyConfiguredThenSetProxy()
	{
		ProxyUtils.setManualProxy( true );
		ProxyUtils.setProxyEnabled( true );

		HttpUriRequest httpMethod = new ExtendedGetMethod();

		ProxyUtils.initProxySettings( manualSettings(), httpMethod, null, URL, null );

		assertManualProxy( httpMethod, URL_STRING_MANUAL_SETTINGS );
	}

	@Test
	public void givenAutomaticProxyDetectionAndNoProxyAvailableThenDoNotSetProxy()
	{
		ProxyUtils.setManualProxy( false );
		ProxyUtils.setProxyEnabled( true );

		ProxyUtils.initProxySettings( manualSettings(), new ExtendedGetMethod(), null, URL, null );

		assertAutoProxy( Proxy.Type.DIRECT );
	}

	@Test
	@Ignore
	public void testSwitchBetweenDifferentProxySettings()
	{

	}


	private void assertManualProxy( HttpUriRequest httpMethod, String proxyUrl )
	{
		HttpHost proxy = ( HttpHost )httpMethod.getParams().getParameter( ConnRoutePNames.DEFAULT_PROXY );

		assertEquals( proxyUrl, proxy.toURI() );
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
}
