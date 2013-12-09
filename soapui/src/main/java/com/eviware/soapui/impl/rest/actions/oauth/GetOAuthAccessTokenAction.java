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

/**
 * Action for retrieving an OAuth2 access token using the values in the OAuth2Profile object.
 */
public class GetOAuthAccessTokenAction
{



	public void perform( OAuth2Profile target, Object param )
	{
		try
		{
			getOAuthClientFacade().requestAccessToken( target );
		}
		catch (InvalidOAuth2ParametersException e)
		{
			UISupport.showErrorMessage( "Invalid OAuth2 parameters: " + e.getMessage() );
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
