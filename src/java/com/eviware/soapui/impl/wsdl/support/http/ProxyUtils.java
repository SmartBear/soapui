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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.support.StringUtils;

/**
 * Utilities for setting proxy-servers corectly
 * 
 * @author ole.matzura
 */

public class ProxyUtils
{
	public static HostConfiguration initProxySettings( Settings settings, HttpState httpState,
			HostConfiguration hostConfiguration, String urlString, PropertyExpansionContext context )
	{
		// check system properties first
		String proxyHost = System.getProperty( "http.proxyHost" );
		String proxyPort = System.getProperty( "http.proxyPort" );

		if( proxyHost == null )
			proxyHost = PropertyExpander.expandProperties( context, settings.getString( ProxySettings.HOST, "" ) );

		if( proxyPort == null )
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
					hostConfiguration.setProxy( proxyHost, Integer.parseInt( proxyPort ) );

					String proxyUsername = PropertyExpander.expandProperties( context, settings.getString(
							ProxySettings.USERNAME, null ) );
					String proxyPassword = PropertyExpander.expandProperties( context, settings.getString(
							ProxySettings.PASSWORD, null ) );

					if( proxyUsername != null && proxyPassword != null )
					{
						Credentials proxyCreds = new UsernamePasswordCredentials( proxyUsername, proxyPassword == null ? ""
								: proxyPassword );

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

						httpState.setProxyCredentials( AuthScope.ANY, proxyCreds );
					}
				}
			}
			catch( MalformedURLException e )
			{
				SoapUI.logError( e );
			}
		}

		return hostConfiguration;
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
			if( matcher.find() || matcher2.find())
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

}
