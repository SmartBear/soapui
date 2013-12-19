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

import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.token.BasicOAuthToken;
import org.apache.oltu.oauth2.httpclient4.HttpClient4;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for OltuAuth2ClientFacade
 */
public class OltuOAuth2ClientFacadeTest
{

	private SpyingOauthClientStub spyingOauthClientStub;

	private String authorizationCode;
	private String accessToken;
	private OAuth2Profile profile;
	private OAuth2Profile profileWithOnlyAccessToken;
	private OltuOAuth2ClientFacade oltuClientFacade;
	private String refreshToken;
	private ExtendedPostMethod httpRequest;


	@Before
	public void setUp() throws Exception
	{
		authorizationCode = "some_code";
		accessToken = "expected_access_token";
		refreshToken = "expected_refresh_token";
		initializeOAuthProfileWithDefaultValues();
		initializeOAuthProfileWithOnlyAccessToken();
		spyingOauthClientStub = new SpyingOauthClientStub();
//		httpRequest =  new RestRequest( ModelItemFactory.makeRestMethod(), RestRequestConfig.Factory.newInstance(), false);
		httpRequest = new ExtendedPostMethod(  );
		httpRequest.setURI(  new URI( "endpoint/path" ) );
		oltuClientFacade = new OltuOAuth2ClientFacade()
		{
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
	public void getsTheAccessTokenFromResponseURI() throws Exception
	{
		oltuClientFacade.requestAccessToken( profile );

		assertThat( profile.getAccessToken(), is( accessToken ) );
	}

	@Test
	public void getsRefreshTokenFromResponseURI() throws Exception
	{
		oltuClientFacade.requestAccessToken( profile );

		assertThat( profile.getRefreshToken(), is( refreshToken ) );
	}

	@Test
	public void getsTheAccessTokenFromResponseBodyInOobRequest() throws Exception
	{
		profile.setRedirectURI( OltuOAuth2ClientFacade.OAUTH_2_OOB_URN );
		oltuClientFacade.requestAccessToken( profile );

		assertThat( profile.getAccessToken(), is( accessToken ) );
	}

	@Test
	public void storesTheAccessTokenAfterUsingRefreshToken() throws Exception
	{
		profile.setAccessToken( "expired_token!" );
		profile.setRefreshToken( refreshToken );
		oltuClientFacade.refreshAccessToken( profile );

		assertThat( profile.getAccessToken(), is( accessToken ) );
	}

	@Test
	public void performsPropertyExpansionBeforeRequestingToken() throws Exception
	{
		String authorizationPropertyName = "myAuthorizationURI";
		String redirectURIPropertyName = "myRedirectURI";
		WsdlProject project = profile.getContainer().getProject();
		project.addProperty( authorizationPropertyName).setValue( profile.getAuthorizationURI() );
		project.addProperty( redirectURIPropertyName).setValue( profile.getRedirectURI() );
		profile.setAuthorizationURI( "${#Project#" + authorizationPropertyName + "}" );
		profile.setRedirectURI( "${#Project#" + redirectURIPropertyName + "}" );
		oltuClientFacade.requestAccessToken( profile );

		assertThat( profile.getAccessToken(), is( accessToken ) );
	}

	@Test
	public void updatesProfileAccessTokenStatus() throws Exception
	{
		final List<OAuth2Profile.AccessTokenStatus> statusValues = new ArrayList<OAuth2Profile.AccessTokenStatus>(  );
		profile.addPropertyChangeListener(OAuth2Profile.ACCESS_TOKEN_STATUS_PROPERTY, new PropertyChangeListener()
		{
			@Override
			public void propertyChange( PropertyChangeEvent evt )
			{
				statusValues.add(( OAuth2Profile.AccessTokenStatus )evt.getNewValue());
			}
		} );

		oltuClientFacade.requestAccessToken( profile );
		assertThat(statusValues, hasItem( OAuth2Profile.AccessTokenStatus.PENDING));
		assertThat(statusValues, hasItem( OAuth2Profile.AccessTokenStatus.RETRIEVED_FROM_SERVER ));
	}

	@Test
	public void sendsAuthorizationCodeInMessageBody() throws Exception
	{
		oltuClientFacade.requestAccessToken( profile );

		assertThat( spyingOauthClientStub.oAuthClientRequest.getBody(), containsString( authorizationCode ) );
	}

	@Test
	public void closesBrowserWindowAfterSavingTheAccessTokenToProfile() throws Exception
	{
		oltuClientFacade.requestAccessToken( profile );

		assertThat( ( ( UserBrowserFacadeStub )oltuClientFacade.browserFacade ).browserClosed, is( true ) );
	}

	@Test
	public void appendsAccessTokenToHeader() throws Exception
	{
		profileWithOnlyAccessToken.setAccessTokenPosition( OAuth2Profile.AccessTokenPosition.HEADER );
		String expectedAccessTokenValue = "Bearer "+ profileWithOnlyAccessToken.getAccessToken();
		oltuClientFacade.applyAccessToken( profileWithOnlyAccessToken, httpRequest, "" );

		assertThat( httpRequest.getHeaders(OAuth.HeaderType.AUTHORIZATION )[0].getValue(), is( expectedAccessTokenValue ) ) ;
	}

	@Test
	public void appendsAccessTokenToHeaderByDefault() throws Exception
	{
		String expectedAccessTokenValue = "Bearer "+ profileWithOnlyAccessToken.getAccessToken();
		oltuClientFacade.applyAccessToken( profileWithOnlyAccessToken, httpRequest, "" );

		assertThat( httpRequest.getHeaders(OAuth.HeaderType.AUTHORIZATION )[0].getValue(), is( expectedAccessTokenValue ) ) ;
	}

	@Test
	public void appendsAccessTokenToQuery() throws Exception
	{
		profileWithOnlyAccessToken.setAccessTokenPosition( OAuth2Profile.AccessTokenPosition.QUERY );
		oltuClientFacade.applyAccessToken( profileWithOnlyAccessToken, httpRequest, "" );

		assertThat( httpRequest.getURI().getQuery(), is( "access_token=" + profileWithOnlyAccessToken.getAccessToken() ) ) ;
	}

	@Test
	public void appendsAccessTokenToBody() throws OAuth2Exception, IOException
	{
		String expectedBodyContent = "access_token=" + profileWithOnlyAccessToken.getAccessToken();
		profileWithOnlyAccessToken.setAccessTokenPosition( OAuth2Profile.AccessTokenPosition.BODY );
		oltuClientFacade.applyAccessToken( profileWithOnlyAccessToken, httpRequest, "" );

		StringWriter writer = new StringWriter();
		IOUtils.copy( httpRequest.getEntity().getContent(), writer, "UTF-8" );
		String actualContent = writer.toString();

		assertThat( actualContent, is( expectedBodyContent ) );
	}

	@Test
	public void performsPropertyExpansionBeforeRefreshingToken() throws Exception
	{
		String clientIdPropertyName = "myClientId";
		String clientSecretPropertyName = "myRedirectURI";
		WsdlProject project = profile.getContainer().getProject();
		String clientIdValue = "some_client_id";
		String clientSecretValue = "some_client_secret";
		project.addProperty( clientIdPropertyName).setValue( clientIdValue );
		project.addProperty( clientSecretPropertyName).setValue( clientSecretValue );
		profile.setClientID( "${#Project#" + clientIdPropertyName + "}" );
		profile.setClientSecret( "${#Project#" + clientSecretPropertyName + "}" );
		profile.setRefreshToken( "some_refresh_token" );
		oltuClientFacade.refreshAccessToken( profile );

		assertThat( spyingOauthClientStub.oAuthClientRequest.getBody(), containsString( clientIdValue ) );
		assertThat( spyingOauthClientStub.oAuthClientRequest.getBody(), containsString( clientSecretValue ) );
	}

	/* Validation tests */

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsUrnAsAuthorizationURI() throws Exception
	{
		profile.setAuthorizationURI( OltuOAuth2ClientFacade.OAUTH_2_OOB_URN );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsNonHttpAuthorizationUrl() throws Exception
	{
		profile.setAuthorizationURI( "ftp://ftp.sunet.se" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsNonHttpRedirectURI() throws Exception
	{
		profile.setRedirectURI( "ftp://ftp.sunet.se" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsUrnAsAccessTokenURI() throws Exception
	{
		profile.setAccessTokenURI( OltuOAuth2ClientFacade.OAUTH_2_OOB_URN );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsNonHttpAccessTokenURI() throws Exception
	{
		profile.setAccessTokenURI( "ftp://ftp.sunet.se" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsEmptyClientId() throws Exception
	{
		profile.setClientID( "" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsEmptyClientSecret() throws Exception
	{
		profile.setClientSecret( "" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsEmptyRefreshTokenOnRefresh() throws Exception
	{
		profile.setRefreshToken( "" );
		oltuClientFacade.refreshAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsEmptyClientIdOnRefresh() throws Exception
	{
		profile.setRefreshToken( "someRefreshToken" );
		profile.setClientID( "" );
		oltuClientFacade.refreshAccessToken( profile );
	}

	@Test(expected = InvalidOAuth2ParametersException.class)
	public void rejectsEmptyClientSecretOnRefresh() throws Exception
	{
		profile.setRefreshToken( "someRefreshToken" );
		profile.setClientSecret( "" );
		oltuClientFacade.refreshAccessToken( profile );
	}

	/* Helpers */

	private void initializeOAuthProfileWithDefaultValues() throws SoapUIException
	{
		OAuth2ProfileConfig configuration = OAuth2ProfileConfig.Factory.newInstance();
		profile = new OAuth2Profile( ModelItemFactory.makeOAuth2ProfileContainer(), configuration );
		profile.setAuthorizationURI( "http://localhost:8080/authorize" );
		profile.setAccessTokenURI( "http://localhost:8080/accesstoken" );
		profile.setRedirectURI( "http://localhost:8080/redirect" );
		profile.setClientID( "ClientId" );
		profile.setClientSecret( "ClientSecret" );
	}

	private void initializeOAuthProfileWithOnlyAccessToken( ) throws SoapUIException
	{
		OAuth2ProfileConfig configuration = OAuth2ProfileConfig.Factory.newInstance();
		profileWithOnlyAccessToken = new OAuth2Profile( ModelItemFactory.makeOAuth2ProfileContainer(), configuration );
		profileWithOnlyAccessToken.setAccessToken( accessToken );
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
			when( response.getOAuthToken() ).thenReturn( new BasicOAuthToken( accessToken, 60000L, refreshToken, "user" ));
			return ( T )response;
		}

	}

	private class UserBrowserFacadeStub implements UserBrowserFacade
	{

		private BrowserStateChangeListener listener;
		private boolean browserClosed;

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
						String redirectURI = parameter.substring( prefix.length() );
						listener.locationChanged( redirectURI + "?code=" + authorizationCode );
					}
				}
			}
			else
			{
				listener.contentChanged( "<TITLE>code=" + authorizationCode + "</TITLE>" );
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

		@Override
		public void close()
		{
			browserClosed = true;
		}
	}
}
