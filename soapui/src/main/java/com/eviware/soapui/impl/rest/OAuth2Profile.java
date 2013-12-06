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
import com.eviware.soapui.model.ModelItem;
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

	private final ModelItem modelItem;
	private final OAuth2ProfileConfig configuration;

	public OAuth2Profile( ModelItem modelItem, OAuth2ProfileConfig configuration )
	{
		this.modelItem = modelItem;
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
		return configuration.getScope();
	}

	@Override
	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( modelItem, this );

		//TODO: Add the fields we want to support property expansion
		// result.extractAndAddAll( "clientID" );

		return result.toArray();
	}
}
