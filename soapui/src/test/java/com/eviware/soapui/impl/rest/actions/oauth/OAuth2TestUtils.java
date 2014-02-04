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

import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class OAuth2TestUtils
{
	public static String AUTHORIZATION_CODE = "some_code";
	public static String ACCESS_TOKEN = "expected_access_token";
	public static String REFRESH_TOKEN = "expected_refresh_token";

	public static final String OAUTH_2_OOB_URN = "urn:ietf:wg:oauth:2.0:oob";

	public static OAuth2Profile getOAuthProfileWithDefaultValues() throws SoapUIException
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

	public static OAuth2Profile getOAuthProfileWithRefreshToken() throws SoapUIException
	{
		OAuth2Profile profile = getOAuthProfileWithDefaultValues();
		profile.setRefreshToken( "REFRESH#TOKEN" );

		return profile;
	}

	public static void mockOAuth2TokenExtractor( OltuOAuth2ClientFacade oltuClientFacade, final OAuth2Profile profile ) throws URISyntaxException,
			MalformedURLException, OAuthSystemException, OAuthProblemException
	{
		OAuth2TokenExtractor oAuth2TokenExtractor = mock( OAuth2TokenExtractor.class );
		oltuClientFacade.oAuth2TokenExtractor = oAuth2TokenExtractor;
		doAnswer( new Answer<Object>()
		{
			@Override
			public Object answer( InvocationOnMock invocationOnMock ) throws Throwable
			{
				profile.setAccessToken( OAuth2TestUtils.ACCESS_TOKEN );
				return profile;
			}
		} ).when( oAuth2TokenExtractor ).extractAccessTokenForAuthorizationCodeGrantFlow( any( OAuth2Parameters.class ) );

		doAnswer( new Answer()
		{
			@Override
			public Object answer( InvocationOnMock invocationOnMock ) throws Throwable
			{
				profile.setAccessToken( OAuth2TestUtils.ACCESS_TOKEN );
				return profile;
			}
		} ).when( oAuth2TokenExtractor ).extractAccessTokenForImplicitGrantFlow( any( OAuth2Parameters.class ) );

		doAnswer( new Answer()
		{
			@Override
			public Object answer( InvocationOnMock invocationOnMock ) throws Throwable
			{
				profile.setAccessToken( OAuth2TestUtils.ACCESS_TOKEN );
				return profile;
			}
		} ).when( oAuth2TokenExtractor ).refreshAccessToken( any( OAuth2Parameters.class ) );
	}
}
