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

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Action for retrieving an OAuth2 access token using the values in the OAuth2Profile object.
 */
public class GetOAuthAccessTokenAction extends AbstractAction
{

	private OAuth2Profile target;

	public GetOAuthAccessTokenAction( OAuth2Profile target )
	{
		this.target = target;
		putValue(Action.NAME, "Get Access Token");
		putValue( Action.SHORT_DESCRIPTION, "Gets an OAuth2 access token and stores it in this profile" );
	}

	public void actionPerformed(ActionEvent event)
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
		return new OltuOAuth2ClientFacade();
	}


}
