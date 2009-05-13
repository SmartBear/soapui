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
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.auth.NTLMScheme;
import org.apache.commons.httpclient.auth.RFC2617Scheme;
import org.apache.log4j.Logger;

import com.eviware.soapui.impl.wsdl.WsdlRequest;

/**
 * Provided credentials from equivilant WsdlRequest properties
 * 
 * @author Ole.Matzura
 */

public final class WsdlRequestCredentialsProvider implements CredentialsProvider
{
	private boolean checkedCredentials;
	private final WsdlRequest wsdlRequest;
	private final static Logger logger = Logger.getLogger( WsdlRequestCredentialsProvider.class );

	public WsdlRequestCredentialsProvider( WsdlRequest wsdlRequest )
	{
		this.wsdlRequest = wsdlRequest;
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
			String password = wsdlRequest.getPassword();
			if( password == null )
				password = "";

			if( authscheme instanceof NTLMScheme )
			{
				logger.info( host + ":" + port + " requires Windows authentication" );
				return new NTCredentials( wsdlRequest.getUsername(), password, host, wsdlRequest.getDomain() );
			}
			else if( authscheme instanceof RFC2617Scheme )
			{
				logger.info( host + ":" + port + " requires authentication with the realm '" + authscheme.getRealm() + "'" );
				return new UsernamePasswordCredentials( wsdlRequest.getUsername(), password );
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