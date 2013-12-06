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

import com.eviware.soapui.config.OAuthConfigConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractModelItem;

import javax.swing.ImageIcon;

/**
 * Encapsulates information necessary to perform an OAuth2 three-legged authorization. All property values may contain
 * property expansion expressions.
 */
public class OAuth2Profile extends AbstractModelItem
{

	private OAuthConfigConfig configuration;

	public OAuth2Profile(OAuthConfigConfig configuration)
	{
		this.configuration = configuration;
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public String getId()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getDescription()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Settings getSettings()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public ModelItem getParent()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getAccessToken()
	{
		return configuration.getAccessToken();
	}

	public void setAccessToken( String accessToken )
	{
		configuration.setAccessToken( accessToken );
	}

	public String getAuthorizationURL()
	{
		return configuration.getAuthorizeURI();
	}

	public void setAuthorizationUri( String authorizationURI )
	{
		configuration.setAuthorizeURI( authorizationURI );
	}

	public String getClientId()
	{
		return configuration.getClientID();
	}

	public void setClientId( String clientId )
	{
		configuration.setClientID( clientId );
	}

	public String getRedirectUri()
	{
		return configuration.getRedirectURI();
	}

	public void setRedirectUri( String redirectUri )
	{
		configuration.setRedirectURI( redirectUri );
	}

	public String getScope()
	{
		return null;
	}

	public String getAccessTokenUri()
	{
		return configuration.getAccessTokenURI();
	}

	public void setAccessTokenUri( String accessTokenUri )
	{
		configuration.setAccessTokenURI( accessTokenUri );
	}

	public String getClientSecret()
	{
		return configuration.getClientSecret();
	}

	public void setClientSecret( String clientSecret )
	{
		configuration.setClientSecret( clientSecret );
	}
}
