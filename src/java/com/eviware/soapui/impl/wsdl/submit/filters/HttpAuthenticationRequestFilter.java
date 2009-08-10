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

package com.eviware.soapui.impl.wsdl.submit.filters;

import java.io.IOException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.auth.NTLMScheme;
import org.apache.commons.httpclient.auth.RFC2617Scheme;
import org.apache.log4j.Logger;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
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
			wssPasswordType = PropertyExpander.expandProperties( context, ( ( WsdlRequest )wsdlRequest )
					.getWssPasswordType() );
		}

		if( StringUtils.isNullOrEmpty( wssPasswordType ) )
		{
			initRequestCredentials( context, username, settings, password, domain );
		}
	}

	public static void initRequestCredentials( SubmitContext context, String username, Settings settings,
			String password, String domain )
	{
		HttpClient httpClient = ( HttpClient )context.getProperty( BaseHttpRequestTransport.HTTP_CLIENT );
		HttpMethod httpMethod = ( HttpMethod )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );

		if( StringUtils.isNullOrEmpty( username ) && StringUtils.isNullOrEmpty( password ) )
		{
			httpClient.getParams().setAuthenticationPreemptive( false );
			httpMethod.setDoAuthentication( false );
		}
		else
		{
			// set preemptive authentication
			if( settings.getBoolean( HttpSettings.AUTHENTICATE_PREEMPTIVELY ) )
			{
				httpClient.getParams().setAuthenticationPreemptive( true );
				HttpState state = ( HttpState )context.getProperty( SubmitContext.HTTP_STATE_PROPERTY );

				if( state != null )
				{
					Credentials defaultcreds = new UsernamePasswordCredentials( username, password == null ? "" : password );
					state.setCredentials( AuthScope.ANY, defaultcreds );
				}
			}
			else
			{
				httpClient.getParams().setAuthenticationPreemptive( false );
			}

			httpMethod.getParams().setParameter( CredentialsProvider.PROVIDER,
					new UPDCredentialsProvider( username, password, domain ) );

			httpMethod.setDoAuthentication( true );
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

		public Credentials getCredentials( final AuthScheme authscheme, final String host, int port, boolean proxy )
				throws CredentialsNotAvailableException
		{
			if( checkedCredentials )
				throw new CredentialsNotAvailableException( "Missing valid credentials" );

			if( authscheme == null )
			{
				return null;
			}
			try
			{
				if( authscheme instanceof NTLMScheme )
				{
					logger.info( host + ":" + port + " requires Windows authentication" );
					return new NTCredentials( username, password, host, domain );
				}
				else if( authscheme instanceof RFC2617Scheme )
				{
					logger.info( host + ":" + port + " requires authentication with the realm '" + authscheme.getRealm()
							+ "'" );
					return new UsernamePasswordCredentials( username, password );
				}
				else
				{
					throw new CredentialsNotAvailableException( "Unsupported authentication scheme: "
							+ authscheme.getSchemeName() );
				}
			}
			catch( IOException e )
			{
				throw new CredentialsNotAvailableException( e.getMessage(), e );
			}
			finally
			{
				checkedCredentials = true;
			}
		}
	}
}
