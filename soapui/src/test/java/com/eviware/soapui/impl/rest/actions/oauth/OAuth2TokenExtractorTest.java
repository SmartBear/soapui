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
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.OAuth2TestUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.token.BasicOAuthToken;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.httpclient4.HttpClient4;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.eviware.soapui.utils.OAuth2TestUtils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class OAuth2TokenExtractorTest
{
	private OAuth2TokenExtractor oAuth2TokenExtractor;
	private OAuth2Parameters parameters;
	private OAuth2Profile profile;
	private SpyingOauthClientStub spyingOauthClientStub;

	@Before
	public void setUp() throws SoapUIException
	{
		spyingOauthClientStub = new SpyingOauthClientStub();
		oAuth2TokenExtractor = new OAuth2TokenExtractor()
		{
			@Override
			protected OAuthClient getOAuthClient()
			{
				return spyingOauthClientStub;
			}
		};
		oAuth2TokenExtractor.browserFacade = new UserBrowserFacadeStub();
		profile = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
		parameters = new OAuth2Parameters( profile );
	}

	@Test
	public void getsAccessTokenFromURLFragmentForImplicitGrantFlow() throws OAuthSystemException, MalformedURLException, URISyntaxException
	{
		oAuth2TokenExtractor.extractAccessTokenForImplicitGrantFlow( parameters );

		assertThat( profile.getAccessToken(), is( ACCESS_TOKEN ) );
	}

	@Test
	public void getsTheAccessTokenFromJsonResponse() throws Exception
	{
		oAuth2TokenExtractor.extractAccessTokenForAuthorizationCodeGrantFlow( parameters );

		assertThat( profile.getAccessToken(), is( ACCESS_TOKEN ) );
	}

	@Test
	public void closesBrowserWindowAfterSavingTheAccessTokenToProfile() throws Exception
	{
		oAuth2TokenExtractor.extractAccessTokenForAuthorizationCodeGrantFlow( parameters );

		assertThat( ( ( UserBrowserFacadeStub )oAuth2TokenExtractor.browserFacade ).browserClosed, is( true ) );
	}

	@Test
	public void getsTheAccessTokenFromUrlEncodedFormResponse() throws Exception
	{
		profile.setAccessTokenRetrievalLocation( OAuth2Profile.AccessTokenRetrievalLocation.BODY_URL_ENCODED_FORM );
		oAuth2TokenExtractor.extractAccessTokenForAuthorizationCodeGrantFlow( parameters );

		assertThat( profile.getAccessToken(), is( ACCESS_TOKEN ) );
	}

	@Test
	public void getsRefreshTokenFromResponseURI() throws Exception
	{
		oAuth2TokenExtractor.extractAccessTokenForAuthorizationCodeGrantFlow( parameters );

		assertThat( profile.getRefreshToken(), is( REFRESH_TOKEN ) );
	}

	@Test
	public void getsTheAccessTokenFromResponseBodyInOobRequest() throws Exception
	{
		profile.setRedirectURI( OAUTH_2_OOB_URN );
		oAuth2TokenExtractor.extractAccessTokenForAuthorizationCodeGrantFlow( parameters );

		assertThat( profile.getAccessToken(), is( ACCESS_TOKEN ) );
	}

	@Test
	public void storesTheAccessTokenAfterUsingRefreshToken() throws Exception
	{
		profile.setAccessToken( "expired_token!" );
		profile.setRefreshToken( REFRESH_TOKEN );
		oAuth2TokenExtractor.refreshAccessToken( parameters );

		assertThat( profile.getAccessToken(), is( ACCESS_TOKEN ) );
	}


	@Test
	public void sendsAuthorizationCodeInMessageBody() throws Exception
	{
		oAuth2TokenExtractor.extractAccessTokenForAuthorizationCodeGrantFlow( parameters );

		String code = ( String )OAuthUtils.decodeForm( spyingOauthClientStub.oAuthClientRequest.getBody() ).get( "code" );
		assertThat( code, is( AUTHORIZATION_CODE ) );
	}

	@Test
	public void updatesProfileAccessTokenStatus() throws Exception
	{
		final List<String> statusValues = new ArrayList<String>();
		profile.addPropertyChangeListener( OAuth2Profile.ACCESS_TOKEN_STATUS_PROPERTY, new PropertyChangeListener()
		{
			@Override
			public void propertyChange( PropertyChangeEvent evt )
			{
				statusValues.add( ( String )evt.getNewValue() );
			}
		} );

		oAuth2TokenExtractor.extractAccessTokenForAuthorizationCodeGrantFlow( parameters );

		assertThat( statusValues.size(), is( 4 ) );
		assertThat( statusValues, hasItem( OAuth2Profile.AccessTokenStatus.PENDING.toString() ) );
		assertThat( statusValues, hasItem( OAuth2Profile.AccessTokenStatus.WAITING_FOR_AUTHORIZATION.toString() ) );
		assertThat( statusValues, hasItem( OAuth2Profile.AccessTokenStatus.RECEIVED_AUTHORIZATION_CODE.toString() ) );
		assertThat( statusValues, hasItem( OAuth2Profile.AccessTokenStatus.RETRIEVED_FROM_SERVER.toString() ) );
	}

	private class UserBrowserFacadeStub implements UserBrowserFacade
	{

		private BrowserStateChangeListener listener;
		private boolean browserClosed;

		@Override
		public void open( URL url )
		{
			String queryString = url.getQuery();
			if( queryString.contains( "response_type=code" ) )
			{
				if( !queryString.contains( "redirect_uri=urn%3Aietf%3Awg%3Aoauth%3A2.0%3Aoob" ) )
				{
					String redirectURI = getRedirectURI( queryString );
					listener.locationChanged( redirectURI + "?code=" + AUTHORIZATION_CODE + "&state=foo" );
				}
				else
				{
					listener.contentChanged( "<TITLE>code=" + AUTHORIZATION_CODE + "</TITLE>" );
				}
			}
			else if( queryString.contains( "response_type=token" ) )
			{
				String redirectURI = getRedirectURI( queryString );
				listener.locationChanged( redirectURI + "#access_token=" + ACCESS_TOKEN + "&refresh_token=" + REFRESH_TOKEN
						+ "&expires_in=3600" );
			}
		}

		private String getRedirectURI( String queryString )
		{
			String[] parameters = queryString.split( "&" );
			for( String parameter : parameters )
			{
				String prefix = "redirect_uri=";
				if( parameter.startsWith( prefix ) )
				{
					return parameter.substring( prefix.length() );
				}
			}
			return null;
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

		@Override
		public void close()
		{
			browserClosed = true;
		}
	}

	private class SpyingOauthClientStub extends OAuthClient
	{

		OAuthClientRequest oAuthClientRequest;

		public SpyingOauthClientStub()
		{
			super( new HttpClient4() );
		}

		@Override
		public <T extends OAuthAccessTokenResponse> T accessToken( OAuthClientRequest request, Class<T> responseClass )
				throws OAuthSystemException, OAuthProblemException
		{
			oAuthClientRequest = request;
			OAuthAccessTokenResponse response = mock( responseClass );
			when( response.getOAuthToken() ).thenReturn( new BasicOAuthToken( ACCESS_TOKEN, 3600L, REFRESH_TOKEN, "user" ) );
			return ( T )response;
		}

	}
}
