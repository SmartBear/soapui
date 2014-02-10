/*
 * SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.utils;

import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.SoapUIException;

/**
 *
 */
public class OAuth2TestUtils
{
	public static String AUTHORIZATION_CODE = "some_code";
	public static String ACCESS_TOKEN = "expected_access_token";
	public static String REFRESH_TOKEN = "expected_refresh_token";

	public static final String OAUTH_2_OOB_URN = "urn:ietf:wg:oauth:2.0:oob";

	public static OAuth2Profile getOAuthProfileWithDefaultValues( ) throws SoapUIException
	{
		OAuth2ProfileConfig configuration = OAuth2ProfileConfig.Factory.newInstance();
		OAuth2Profile profile = new OAuth2Profile( ModelItemFactory.makeOAuth2ProfileContainer(), configuration );
		profile.setAuthorizationURI( "http://localhost:8080/authorize" );
		profile.setAccessTokenURI( "http://localhost:8080/accesstoken" );
		profile.setRedirectURI( "http://localhost:8080/redirect" );
		profile.setClientID( "ClientId" );
		profile.setClientSecret( "ClientSecret" );
		profile.setScope( "ReadOnly" );
		return profile;
	}

	public static OAuth2Profile getOAuth2ProfileWithOnlyAccessToken() throws SoapUIException
	{
		OAuth2ProfileConfig configuration = OAuth2ProfileConfig.Factory.newInstance();
		OAuth2Profile profileWithOnlyAccessToken = new OAuth2Profile( ModelItemFactory.makeOAuth2ProfileContainer(),
				configuration );
		profileWithOnlyAccessToken.setAccessToken( ACCESS_TOKEN );

		return profileWithOnlyAccessToken;
	}
}
