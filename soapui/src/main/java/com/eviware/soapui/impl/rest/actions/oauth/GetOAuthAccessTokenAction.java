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

package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-12-04
 * Time: 15:25
 * To change this template use File | Settings | File Templates.
 */
public class GetOAuthAccessTokenAction extends AbstractSoapUIAction<OAuth2Profile>
{

	public GetOAuthAccessTokenAction()
	{
		super( "Get Authorization Code", "Gets an OAuth authorization code for a given client id and client secret " );
	}

	@Override
	public void perform( OAuth2Profile target, Object param )
	{
		try
		{
			getOAuthClientFacade().requestAccessToken( target );
		}
		catch( Exception e )
		{
			SoapUI.logError( e, "Error retrieving OAuth2 access token" );
			UISupport.showErrorMessage( "Could not retrieve access token. Check the SoapUI log for details" );
		}
	}

	protected OAuth2ClientFacade getOAuthClientFacade()
	{
		return new OltuAuth2ClientFacade();
	}
}
