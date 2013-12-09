/*
 * SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-12-04
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
 */
public class OAuth2Profile implements PropertyExpansionContainer
{

	private final OAuth2ProfileContainer oAuth2ProfileContainer;
	private final OAuth2ProfileConfig configuration;

	public OAuth2Profile( OAuth2ProfileContainer oAuth2ProfileContainer, OAuth2ProfileConfig configuration )
	{
		this.oAuth2ProfileContainer = oAuth2ProfileContainer;
		this.configuration = configuration;
	}

	public String getAccessToken()
	{
		return configuration.getAccessToken();
	}

	public void setAccessToken( String accessToken )
	{
		configuration.setAccessToken( accessToken );
	}

	public String getAuthorizationURI()
	{
		return configuration.getAuthorizationURI();
	}

	public void setAuthorizationURI( String authorizationURI )
	{
		configuration.setAuthorizationURI( authorizationURI );
	}

	public String getClientID()
	{
		return configuration.getClientID();
	}

	public void setClientID( String clientID )
	{
		configuration.setClientID( clientID );
	}

	public String getClientSecret()
	{
		return configuration.getClientSecret();
	}

	public void setClientSecret( String clientSecret )
	{
		configuration.setClientSecret( clientSecret );
	}

	public String getRedirectURI()
	{
		return configuration.getRedirectURI();
	}

	public void setRedirectURI( String redirectURI )
	{
		configuration.setRedirectURI( redirectURI );
	}

	public String getScope()
	{
		return configuration.getScope();
	}

	public void setScope( String scope )
	{
		configuration.setScope( scope );
	}

	public OAuth2ProfileConfig getConfiguration()
	{
		return configuration;
	}

	public String getAccessTokenURI()
	{
		return configuration.getAccessTokenURI();
	}

	public void setAccessTokenURI( String accessTokenURI )
	{
		configuration.setAccessTokenURI( accessTokenURI );
	}

	@Override
	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( oAuth2ProfileContainer.getProject(), this );

		result.extractAndAddAll( "clientID" );
		result.extractAndAddAll( "clientSecret" );
		result.extractAndAddAll( "authorizationURI" );
		result.extractAndAddAll( "accessTokenURI" );
		result.extractAndAddAll( "redirectURI" );
		result.extractAndAddAll( "accessToken" );
		result.extractAndAddAll( "scope" );

		return result.toArray();
	}

}
