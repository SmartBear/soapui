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

import com.eviware.soapui.config.OAuthConfigConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.token.BasicOAuthToken;
import org.apache.oltu.oauth2.httpclient4.HttpClient4;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-12-04
 * Time: 16:50
 * To change this template use File | Settings | File Templates.
 */
public class OltuAuth2ClientFacadeTest
{

	private OltuAuth2ClientFacadeTest.SpyingOauthClientStub spyingOauthClientStub;

	private String authorizationCode;
	private String accessToken;
	private OAuth2Profile profile;
	private OltuAuth2ClientFacade oltuClientFacade;

	@Before
	public void setUp() throws Exception
	{
		initializeOAuthProfileWithDefaultValues();
		authorizationCode = "some_code";
		accessToken = "expected_access_token";
		spyingOauthClientStub = new SpyingOauthClientStub();
		oltuClientFacade = new OltuAuth2ClientFacade() {
			@Override
			protected OAuthClient getOAuthClient()
			{
				return spyingOauthClientStub;
			}
		};
		oltuClientFacade.browserFacade = new UserBrowserFacadeStub();
	}

	/* Happy path tests */

	@Test
	public void getsTheAccessTokenFromResponseUri() throws Exception
	{
		oltuClientFacade.requestAccessToken( profile );

		assertThat( profile.getAccessToken(), is( accessToken ) );
	}

	@Test
	public void getsTheAccessTokenFromResponseBodyInOobRequest() throws Exception
	{
		profile.setRedirectUri( OltuAuth2ClientFacade.OAUTH_2_OOB_URN );
		oltuClientFacade.requestAccessToken( profile );

		assertThat( profile.getAccessToken(), is( accessToken ) );
	}

	@Test
	public void sendsAuthorizationCodeInMessageBody() throws Exception
	{
		oltuClientFacade.requestAccessToken( profile );

		assertThat( spyingOauthClientStub.oAuthClientRequest.getBody(), containsString( authorizationCode ) );
	}

	/* Validation tests */

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsUrnAsAuthorizationUri() throws Exception
	{
		profile.setAuthorizationUri( OltuAuth2ClientFacade.OAUTH_2_OOB_URN );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsNonHttpAuthorizationUrl() throws Exception
	{
		profile.setAuthorizationUri( "ftp://ftp.sunet.se" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsNonHttpRedirectUri() throws Exception
	{
		profile.setRedirectUri( "ftp://ftp.sunet.se" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsUrnAsAccessTokenUri() throws Exception
	{
		profile.setAccessTokenUri( OltuAuth2ClientFacade.OAUTH_2_OOB_URN );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsNonHttpAccessTokenUri() throws Exception
	{
		profile.setAccessTokenUri( "ftp://ftp.sunet.se" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsEmptyClientId() throws Exception
	{
		profile.setClientId( "" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsEmptyClientSecret() throws Exception
	{
		profile.setClientSecret( "" );
		oltuClientFacade.requestAccessToken( profile );
	}



	/* Helpers */

	private void initializeOAuthProfileWithDefaultValues()
	{
		OAuthConfigConfig configuration = OAuthConfigConfig.Factory.newInstance();
		profile = new OAuth2Profile( configuration );
		profile.setAuthorizationUri( "http://localhost:8080/authorize" );
		profile.setAccessTokenUri( "http://localhost:8080/accesstoken" );
		profile.setRedirectUri( "http://localhost:8080/redirect" );
		profile.setClientId( "ClientId" );
		profile.setClientSecret( "ClientSecret" );
	}

	class SpyingOauthClientStub extends OAuthClient
	{

		OAuthClientRequest oAuthClientRequest;

		public SpyingOauthClientStub()
		{
			super( new HttpClient4() );
		}

		@Override
		public <T extends OAuthAccessTokenResponse> T accessToken( OAuthClientRequest request, Class<T> responseClass ) throws OAuthSystemException, OAuthProblemException
		{
			oAuthClientRequest = request;
			OAuthJSONAccessTokenResponse response = mock( OAuthJSONAccessTokenResponse.class );
			when( response.getOAuthToken() ).thenReturn( new BasicOAuthToken( accessToken ) );
			return ( T )response;
		}

	}

	private class UserBrowserFacadeStub implements UserBrowserFacade
	{

		private BrowserStateChangeListener listener;

		@Override
		public void open( URL url )
		{
			String queryString = url.getQuery();
			if( !queryString.contains( "redirect_uri=urn%3Aietf%3Awg%3Aoauth%3A2.0%3Aoob" ) )
			{
				String[] parameters = queryString.split( "&" );
				for( String parameter : parameters )
				{
					String prefix = "redirect_uri=";
					if( parameter.startsWith( prefix ) )
					{
						String redirectUri = parameter.substring( prefix.length() );
						listener.locationChanged( redirectUri + "?code=" + authorizationCode );
					}
				}
			}
			else {
				listener.contentChanged( "<TITLE>code="+authorizationCode+"</TITLE>" );
			}
		}

		@Override
		public void addBrowserStateListener( BrowserStateChangeListener listener )
		{
			this.listener = listener;
		}

		@Override
		public void removeBrowserStateListener( BrowserStateChangeListener listener )
		{
			this.listener = null;
		}
	}
}
