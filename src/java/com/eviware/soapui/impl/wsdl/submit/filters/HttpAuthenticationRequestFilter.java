/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.support.BrowserCredentials;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;

/**
 * RequestFilter for setting preemptive authentication and related credentials
 */

public class HttpAuthenticationRequestFilter extends AbstractRequestFilter
{
	@Override
	public void filterAbstractHttpRequest( SubmitContext context, AbstractHttpRequest<?> wsdlRequest )
	{
		String username = PropertyExpander.expandProperties( context, wsdlRequest.getUsername() );

		// check for authorization prerequisites
		if( username == null || username.length() == 0 )
			return;

		Settings settings = wsdlRequest.getSettings();
		String password = PropertyExpander.expandProperties( context, wsdlRequest.getPassword() );
		String domain = PropertyExpander.expandProperties( context, wsdlRequest.getDomain() );

		String wssPasswordType = null;

		if( wsdlRequest instanceof WsdlRequest )
		{
			wssPasswordType = PropertyExpander.expandProperties( context,
					( ( WsdlRequest )wsdlRequest ).getWssPasswordType() );
		}

		if( StringUtils.isNullOrEmpty( wssPasswordType ) )
		{
			initRequestCredentials( context, username, settings, password, domain );

			if( !SoapUI.isJXBrowserDisabled() )
			{
				BrowserCredentials.initBrowserCredentials( username, password );
			}
		}
	}

	public static void initRequestCredentials( SubmitContext context, String username, Settings settings,
			String password, String domain )
	{
		HttpRequestBase httpMethod = ( HttpRequestBase )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );

		if( !StringUtils.isNullOrEmpty( username ) && !StringUtils.isNullOrEmpty( password ) )
		{
			// set preemptive authentication
			if( settings.getBoolean( HttpSettings.AUTHENTICATE_PREEMPTIVELY ) )
			{
				UsernamePasswordCredentials creds = new UsernamePasswordCredentials( username, password );
				httpMethod.addHeader( BasicScheme.authenticate( creds, "utf-8", false ) );
			}

			HttpClientSupport.getHttpClient().setCredentialsProvider(
					new UPDCredentialsProvider( username, password, domain ) );
		}
	}

	public static class UPDCredentialsProvider implements CredentialsProvider
	{
		private boolean checkedCredentials;
		private final static Logger logger = Logger.getLogger( WsdlRequestCredentialsProvider.class );
		private final String username;
		private final String password;
		private final String domain;

		public UPDCredentialsProvider( String username, String password, String domain )
		{
			this.username = username;
			this.password = password == null ? "" : password;
			this.domain = domain;
		}

		public Credentials getCredentials( final AuthScope authScope )
		{
			if( checkedCredentials )
				return null;

			if( authScope == null )
			{
				throw new IllegalArgumentException( "Authentication scope may not be null" );
			}

			try
			{
				if( AuthPolicy.NTLM.equals( authScope.getScheme() ) )
				{
					logger.info( authScope.getHost() + ":" + authScope.getPort() + " requires Windows authentication" );

					String workstation = "";
					try
					{
						workstation = InetAddress.getLocalHost().getHostName();
					}
					catch( UnknownHostException e )
					{
					}
					return new NTCredentials( username, password, workstation, domain );
				}
				else if( AuthPolicy.BASIC.equals( authScope.getScheme() )
						|| AuthPolicy.DIGEST.equals( authScope.getScheme() )
						|| AuthPolicy.SPNEGO.equals( authScope.getScheme() ) )
				{
					logger.info( authScope.getHost() + ":" + authScope.getPort()
							+ " requires authentication with the realm '" + authScope.getRealm() + "'" );
					return new UsernamePasswordCredentials( username, password );
				}
			}
			finally
			{
				checkedCredentials = true;
			}
			return null;
		}

		public void clear()
		{
		}

		public void setCredentials( final AuthScope authscope, final Credentials credentials )
		{
		}
	}

}
