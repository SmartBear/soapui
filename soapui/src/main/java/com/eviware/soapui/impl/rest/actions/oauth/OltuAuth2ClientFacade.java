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
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.apache.oltu.oauth2.httpclient4.HttpClient4;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.oltu.oauth2.client.request.OAuthClientRequest.AuthenticationRequestBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-12-04
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public class OltuAuth2ClientFacade implements OAuth2ClientFacade
{
	public static final String CODE = "code";
	public static final String TITLE = "<TITLE>";
	public static final String OAUTH_2_OOB_URN = "urn:ietf:wg:oauth:2.0:oob";

	protected OAuthClient getOAuthClient()
	{
		return new OAuthClient( new HttpClient4( HttpClientSupport.getHttpClient() ) );
	}

	UserBrowserFacade browserFacade = new WebViewUserBrowserFacade();

	@Override
	public void requestAccessToken( OAuth2Profile profile ) throws OAuth2Exception
	{
		try
		{
			String authorizationURL = createAuthorizationURL( profile );
			launchConsentScreenAndGetAuthorizationCode( authorizationURL, profile );
		}
		catch( OAuthSystemException e )
		{
			logAndThrowOAuth2Exception( e );
		}
		catch( MalformedURLException e )
		{
			logAndThrowOAuth2Exception( e );
		}
		catch( URISyntaxException e )
		{
			logAndThrowOAuth2Exception( e );
		}

	}

	private void logAndThrowOAuth2Exception( Exception e ) throws OAuth2Exception
	{
		SoapUI.logError( e, "Failed to create the authorization URL" );
		throw new OAuth2Exception( e );
	}

	private String createAuthorizationURL( OAuth2Profile profile ) throws OAuthSystemException
	{
		return OAuthClientRequest
				.authorizationLocation( profile.getAuthorizationURL() )
				.setClientId( profile.getClientId() )
				.setResponseType( CODE )
				.setScope( profile.getScope() )
				.setRedirectURI( profile.getRedirectURL() )
				.buildQueryMessage().getLocationUri();

	}

	private void launchConsentScreenAndGetAuthorizationCode( String authorizationURL, final OAuth2Profile profile )
			throws URISyntaxException, MalformedURLException
	{
		browserFacade.addBrowserStateListener( new BrowserStateChangeListener()
		{
			@Override
			public void locationChanged( String newLocation )
			{
				if( !profile.getRedirectURL().contains( OAUTH_2_OOB_URN ) )
				{
					getAccessTokenAndSaveToProfile( profile, extractAuthorizationCode( newLocation ) );
				}
			}

			@Override
			public void contentChanged( String newContent )
			{
				if( profile.getRedirectURL().contains( OAUTH_2_OOB_URN ) )
				{
					String title = newContent.substring( newContent.indexOf( TITLE ) + TITLE.length(), newContent.indexOf( "</TITLE>" ) );
					getAccessTokenAndSaveToProfile( profile, extractAuthorizationCode( title ) );
				}
			}

		} );
		browserFacade.open( new URI( authorizationURL ).toURL() );
	}

	private String extractAuthorizationCode( String title )
	{
		if( title.contains( "code=" ) )
		{
			return title.substring( title.indexOf( "code=" ) + 5 );
		}
		return null;
	}

	private void getAccessTokenAndSaveToProfile( OAuth2Profile profile, String authorizationCode )
	{
		if( authorizationCode != null )
		{
			try
			{
				OAuthClientRequest accessTokenRequest = OAuthClientRequest
						.tokenLocation( profile.getAccessTokenUri() )
						.setGrantType( GrantType.AUTHORIZATION_CODE )
						.setClientId( profile.getClientId() )
						.setClientSecret( profile.getClientSecret() )
						.setRedirectURI( profile.getRedirectURL() )
						.setCode( authorizationCode )
						.buildBodyMessage();
				OAuthToken token = getOAuthClient().accessToken( accessTokenRequest, OAuthJSONAccessTokenResponse.class ).getOAuthToken();
				if( token != null && token.getAccessToken() != null )
				{
					profile.setAccessToken( token.getAccessToken() );
					browserFacade.close();
				}
			}
			catch( OAuthSystemException e )
			{
				SoapUI.logError( e );
			}
			catch( OAuthProblemException e )
			{
				SoapUI.logError( e );
			}
		}
	}
}
