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

package com.eviware.soapui.impl.wsdl.submit.filters;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.params.AuthPolicy;
import org.apache.log4j.Logger;

import com.eviware.soapui.impl.wsdl.WsdlRequest;

/**
 * Provided credentials from equivilant WsdlRequest properties
 * 
 * @author Ole.Matzura
 */

@Deprecated
public final class WsdlRequestCredentialsProvider implements CredentialsProvider
{
	private boolean checkedCredentials;
	private final WsdlRequest wsdlRequest;
	private final static Logger logger = Logger.getLogger( WsdlRequestCredentialsProvider.class );

	public WsdlRequestCredentialsProvider( WsdlRequest wsdlRequest )
	{
		this.wsdlRequest = wsdlRequest;
	}

	public Credentials getCredentials( final AuthScope authScope )

	{
		if( checkedCredentials )
		{
			throw new IllegalArgumentException( "Missing valid credentials" );
		}

		if( authScope == null )
		{
			throw new IllegalArgumentException( "Authentication scope may not be null" );
		}

		try
		{
			String password = wsdlRequest.getPassword();
			if( password == null )
				password = "";

			if( AuthPolicy.NTLM.equalsIgnoreCase( authScope.getScheme() ) )
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
				return new NTCredentials( wsdlRequest.getUsername(), password, workstation, wsdlRequest.getDomain() );

			}
			else if( AuthPolicy.BASIC.equalsIgnoreCase( authScope.getScheme() )
					|| AuthPolicy.DIGEST.equalsIgnoreCase( authScope.getScheme() )
					|| AuthPolicy.SPNEGO.equalsIgnoreCase( authScope.getScheme() ) )
			{
				logger.info( authScope.getHost() + ":" + authScope.getPort() + " requires authentication with the realm '"
						+ authScope.getRealm() + "'" );
				return new UsernamePasswordCredentials( wsdlRequest.getUsername(), password );
			}
		}
		finally
		{
			checkedCredentials = true;
		}

		logger.error( "Unsupported authentication scheme: " + authScope.getScheme() );
		return null;
	}

	public void clear()
	{
	}

	public void setCredentials( AuthScope arg0, Credentials arg1 )
	{
	}
}
