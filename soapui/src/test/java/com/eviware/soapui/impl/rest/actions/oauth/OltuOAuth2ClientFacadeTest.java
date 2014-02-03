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
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.utils.OAuth2TestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for OltuAuth2ClientFacade
 */
public class OltuOAuth2ClientFacadeTest
{

	private OAuth2Profile profile;
	private OAuth2Profile profileWithOnlyAccessToken;
	private OltuOAuth2ClientFacade oltuClientFacade;
	private ExtendedPostMethod httpRequest;

	@Before
	public void setUp() throws Exception
	{
		profile = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
		profileWithOnlyAccessToken = OAuth2TestUtils.getOAuth2ProfileWithOnlyAccessToken();
		httpRequest = new ExtendedPostMethod();
		httpRequest.setURI( new URI( "endpoint/path" ) );
		oltuClientFacade = new OltuOAuth2ClientFacade();
		mockOAuth2TokenExtractor();
	}

	@Test
	public void getsTheAccessTokenForAuthorizationCodeGrantFlow() throws OAuth2Exception
	{
		oltuClientFacade.requestAccessToken( profile );
		assertThat( profile.getAccessToken(), is( OAuth2TestUtils.ACCESS_TOKEN ) );
	}

	@Test
	public void getsTheAccessTokenForImplicitGrantFlow() throws OAuth2Exception
	{
		profile.setOAuth2Flow( OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT );
		oltuClientFacade.requestAccessToken( profile );
		assertThat( profile.getAccessToken(), is( OAuth2TestUtils.ACCESS_TOKEN ) );
	}

	@Test
	public void refreshesAccessToken() throws Exception
	{
		profile.setAccessToken( "expiredAccessToken" );
		profile.setRefreshToken( OAuth2TestUtils.REFRESH_TOKEN );
		oltuClientFacade.refreshAccessToken( profile );
		assertThat( profile.getAccessToken(), is( OAuth2TestUtils.ACCESS_TOKEN ) );
	}

	@Test
	public void appendsAccessTokenToHeader() throws Exception
	{
		profileWithOnlyAccessToken.setAccessTokenPosition( OAuth2Profile.AccessTokenPosition.HEADER );
		String expectedAccessTokenValue = "Bearer " + profileWithOnlyAccessToken.getAccessToken();
		oltuClientFacade.applyAccessToken( profileWithOnlyAccessToken, httpRequest, "" );

		assertThat( httpRequest.getHeaders( OAuth.HeaderType.AUTHORIZATION )[0].getValue(), is( expectedAccessTokenValue ) );
	}

	@Test
	public void appendsAccessTokenToHeaderByDefault() throws Exception
	{
		String expectedAccessTokenValue = "Bearer " + profileWithOnlyAccessToken.getAccessToken();
		oltuClientFacade.applyAccessToken( profileWithOnlyAccessToken, httpRequest, "" );

		assertThat( httpRequest.getHeaders( OAuth.HeaderType.AUTHORIZATION )[0].getValue(), is( expectedAccessTokenValue ) );
	}

	@Test
	public void appendsAccessTokenToQuery() throws Exception
	{
		profileWithOnlyAccessToken.setAccessTokenPosition( OAuth2Profile.AccessTokenPosition.QUERY );
		oltuClientFacade.applyAccessToken( profileWithOnlyAccessToken, httpRequest, "" );

		assertThat( httpRequest.getURI().getQuery(), is( "access_token=" + profileWithOnlyAccessToken.getAccessToken() ) );
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

/* Validation tests */

	@Test( expected = InvalidOAuth2ParametersException.class )
	public void rejectsUrnAsAuthorizationURI() throws Exception
	{
		profile.setAuthorizationURI( OAuth2TestUtils.OAUTH_2_OOB_URN );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test( expected = InvalidOAuth2ParametersException.class )
	public void rejectsNonHttpAuthorizationUrl() throws Exception
	{
		profile.setAuthorizationURI( "ftp://ftp.sunet.se" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test( expected = InvalidOAuth2ParametersException.class )
	public void rejectsNonUriRedirectUri() throws Exception
	{
		profile.setRedirectURI( "(/&#)!#%/(¤#!" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test( expected = InvalidOAuth2ParametersException.class )
	public void rejectsUrnAsAccessTokenURI() throws Exception
	{
		profile.setAccessTokenURI( OAuth2TestUtils.OAUTH_2_OOB_URN );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test( expected = InvalidOAuth2ParametersException.class )
	public void rejectsNonHttpAccessTokenURI() throws Exception
	{
		profile.setAccessTokenURI( "ftp://ftp.sunet.se" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test( expected = InvalidOAuth2ParametersException.class )
	public void rejectsEmptyClientId() throws Exception
	{
		profile.setClientID( "" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test( expected = InvalidOAuth2ParametersException.class )
	public void rejectsEmptyClientSecret() throws Exception
	{
		profile.setClientSecret( "" );
		oltuClientFacade.requestAccessToken( profile );
	}

	@Test( expected = InvalidOAuth2ParametersException.class )
	public void rejectsEmptyRefreshTokenOnRefresh() throws Exception
	{
		profile.setRefreshToken( "" );
		oltuClientFacade.refreshAccessToken( profile );
	}

	@Test( expected = InvalidOAuth2ParametersException.class )
	public void rejectsEmptyClientIdOnRefresh() throws Exception
	{
		profile.setRefreshToken( "someRefreshToken" );
		profile.setClientID( "" );
		oltuClientFacade.refreshAccessToken( profile );
	}

	@Test( expected = InvalidOAuth2ParametersException.class )
	public void rejectsEmptyClientSecretOnRefresh() throws Exception
	{
		profile.setRefreshToken( "someRefreshToken" );
		profile.setClientSecret( "" );
		oltuClientFacade.refreshAccessToken( profile );
	}

	private void mockOAuth2TokenExtractor(  ) throws URISyntaxException,
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
