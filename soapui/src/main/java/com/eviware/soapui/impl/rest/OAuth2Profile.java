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
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-12-04
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
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

	public String getClientID()
	{
		return configuration.getClientID();
	}

	public String getRedirectURL()
	{
		return configuration.getRedirectURI();
	}

	public String getScope()
	{
		return null;
	}
}
