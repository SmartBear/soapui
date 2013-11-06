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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.support.StringUtils;

/**
 * Utilities for setting proxy-servers correctly
 * 
 * @author ole.matzura
 */

public class ProxyUtils
{
	private static boolean proxyEnabled = SoapUI.getSettings().getBoolean( ProxySettings.ENABLE_PROXY );

	public static void initProxySettings( Settings settings, HttpUriRequest httpMethod, HttpContext httpContext,
			String urlString, PropertyExpansionContext context )
	{
		boolean enabled = proxyEnabled;
		CredentialsProvider credsProvider = new BasicCredentialsProvider();

		// check system properties first
		String proxyHost = System.getProperty( "http.proxyHost" );
		String proxyPort = System.getProperty( "http.proxyPort" );
		if( proxyHost == null && enabled )
			proxyHost = PropertyExpander.expandProperties( context, settings.getString( ProxySettings.HOST, "" ) );
		if( proxyPort == null && proxyHost != null && enabled )
			proxyPort = PropertyExpander.expandProperties( context, settings.getString( ProxySettings.PORT, "" ) );

		if( !StringUtils.isNullOrEmpty( proxyHost ) && !StringUtils.isNullOrEmpty( proxyPort ) )
		{
			// check excludes
			String[] excludes = PropertyExpander.expandProperties( context,
					settings.getString( ProxySettings.EXCLUDES, "" ) ).split( "," );

			try
			{
				URL url = new URL( urlString );

				if( !excludes( excludes, url.getHost(), url.getPort() ) )
				{
					HttpHost proxy = new HttpHost( proxyHost, Integer.parseInt( proxyPort ) );

					String proxyUsername = PropertyExpander.expandProperties( context,
							settings.getString( ProxySettings.USERNAME, null ) );
					String proxyPassword = PropertyExpander.expandProperties( context,
							settings.getString( ProxySettings.PASSWORD, null ) );

					if( proxyUsername != null && proxyPassword != null
							&& ( proxyUsername.trim().length() > 0 && proxyPassword.trim().length() > 0 ) )
					{
						Credentials proxyCreds = new UsernamePasswordCredentials( proxyUsername, proxyPassword );

						// check for nt-username
						int ix = proxyUsername.indexOf( '\\' );
						if( ix > 0 )
						{
							String domain = proxyUsername.substring( 0, ix );
							if( proxyUsername.length() > ix + 1 )
							{
								String user = proxyUsername.substring( ix + 1 );
								proxyCreds = new NTCredentials( user, proxyPassword, proxyHost, domain );
							}
						}
						credsProvider.setCredentials( new AuthScope( proxy.getHostName(), proxy.getPort() ), proxyCreds );
						httpContext.setAttribute( ClientContext.CREDS_PROVIDER, credsProvider );
					}
					httpMethod.getParams().setParameter( ConnRoutePNames.DEFAULT_PROXY, proxy );
				}
			}
			catch( MalformedURLException e )
			{
				SoapUI.logError( e );
			}
		}
	}

	public static boolean excludes( String[] excludes, String proxyHost, int proxyPort )
	{
		for( int c = 0; c < excludes.length; c++ )
		{
			String exclude = excludes[c].trim();
			if( exclude.length() == 0 )
				continue;

			// check for port
			int ix = exclude.indexOf( ':' );

			if( ix >= 0 && exclude.length() > ix + 1 )
			{
				String excludePort = exclude.substring( ix + 1 );
				if( proxyPort != -1 && excludePort.equals( String.valueOf( proxyPort ) ) )
				{
					exclude = exclude.substring( 0, ix );
				}
				else
				{
					continue;
				}
			}

			/*
			 * This will exclude addresses with wildcard *, too.
			 */
			// if( proxyHost.endsWith( exclude ) )
			// return true;
			String excludeIp = exclude.indexOf( '*' ) >= 0 ? exclude : nslookup( exclude, true );
			String ip = nslookup( proxyHost, true );
			Pattern pattern = Pattern.compile( excludeIp );
			Matcher matcher = pattern.matcher( ip );
			Matcher matcher2 = pattern.matcher( proxyHost );
			if( matcher.find() || matcher2.find() )
				return true;
		}

		return false;
	}

	private static String nslookup( String s, boolean ip )
	{

		InetAddress host;
		String address;

		// get the bytes of the IP address
		try
		{
			host = InetAddress.getByName( s );
			if( ip )
				address = host.getHostAddress();
			else
				address = host.getHostName();
		}
		catch( UnknownHostException ue )
		{
			return s; // no host
		}

		return address;

	} // end lookup

	public static boolean isProxyEnabled()
	{
		return proxyEnabled;
	}

	public static void setProxyEnabled( boolean proxyEnabled )
	{
		ProxyUtils.proxyEnabled = proxyEnabled;
	}
}
