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

package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.OAuth2TestUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class OAuth2ParametersTest
{

	private static String AUTHORIZATION_URI_PROPERTY_NAME = "myAuthorizationURI";
	private static String REDIRECT_URI_PROPERTY_NAME = "myRedirectURI";
	private static String ACCESS_TOKEN_URI_PROPERTY_NAME = "myAccessTokenURI";
	private static String CLIENT_ID_PROPERTY_NAME = "myClientId";
	private static String CLIENT_SECRET_PROPERTY_NAME = "myClientSecret";
	private static String SCOPE_PROPERTY_NAME = "myScope";
	private static String REFRESH_TOKEN_PROPERTY_NAME = "myRefreshToken";

	private OAuth2Profile profile;

	@Before
	public void setUp() throws SoapUIException
	{
		profile = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
		profile.setRefreshToken( "RefreshToken" );
	}

	@Test
	public void performsPropertyExpansion() throws Exception
	{
		WsdlProject project = profile.getContainer().getProject();
		project.addProperty( AUTHORIZATION_URI_PROPERTY_NAME ).setValue( profile.getAuthorizationURI() );
		project.addProperty( REDIRECT_URI_PROPERTY_NAME ).setValue( profile.getRedirectURI() );
		project.addProperty( ACCESS_TOKEN_URI_PROPERTY_NAME ).setValue( profile.getAccessTokenURI() );
		project.addProperty( CLIENT_ID_PROPERTY_NAME ).setValue( profile.getClientID() );
		project.addProperty( CLIENT_SECRET_PROPERTY_NAME ).setValue( profile.getClientSecret() );
		project.addProperty( SCOPE_PROPERTY_NAME ).setValue( profile.getScope() );
		project.addProperty( REFRESH_TOKEN_PROPERTY_NAME ).setValue( profile.getRefreshToken() );

		profile.setAuthorizationURI( "${#Project#" + AUTHORIZATION_URI_PROPERTY_NAME + "}" );
		profile.setRedirectURI( "${#Project#" + REDIRECT_URI_PROPERTY_NAME + "}" );
		profile.setAccessTokenURI( "${#Project#" + ACCESS_TOKEN_URI_PROPERTY_NAME + "}" );
		profile.setClientID( "${#Project#" + CLIENT_ID_PROPERTY_NAME + "}" );
		profile.setClientSecret( "${#Project#" + CLIENT_SECRET_PROPERTY_NAME + "}" );
		profile.setScope( "${#Project#" + SCOPE_PROPERTY_NAME + "}" );
		profile.setRefreshToken( "${#Project#" + REFRESH_TOKEN_PROPERTY_NAME + "}" );

		OAuth2Parameters parameters = new OAuth2Parameters( profile );

		assertThat( parameters.authorizationUri, is( project.getPropertyValue( AUTHORIZATION_URI_PROPERTY_NAME ) ) );
		assertThat( parameters.redirectUri, is( project.getPropertyValue( REDIRECT_URI_PROPERTY_NAME ) ) );
		assertThat( parameters.accessTokenUri, is( project.getPropertyValue( ACCESS_TOKEN_URI_PROPERTY_NAME ) ) );
		assertThat( parameters.clientId, is( project.getPropertyValue( CLIENT_ID_PROPERTY_NAME ) ) );
		assertThat( parameters.clientSecret, is( project.getPropertyValue( CLIENT_SECRET_PROPERTY_NAME ) ) );
		assertThat( parameters.scope, is( project.getPropertyValue( SCOPE_PROPERTY_NAME ) ) );
		assertThat( parameters.refreshToken, is( project.getPropertyValue( REFRESH_TOKEN_PROPERTY_NAME ) ) );
	}


}
