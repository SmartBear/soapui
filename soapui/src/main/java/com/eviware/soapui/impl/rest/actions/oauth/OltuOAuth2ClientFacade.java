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
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.support.StringUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.apache.oltu.oauth2.httpclient4.HttpClient4;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * This class implements an OAuth2 three-legged authorization using the third party library Oltu.
 */
public class OltuOAuth2ClientFacade implements OAuth2ClientFacade
{
	public static final String CODE = "code";
	public static final String TITLE = "<TITLE>";
	public static final String OAUTH_2_OOB_URN = "urn:ietf:wg:oauth:2.0:oob";

	UserBrowserFacade browserFacade = new WebViewUserBrowserFacade();

	protected OAuthClient getOAuthClient()
	{
		return new OAuthClient( new HttpClient4( HttpClientSupport.getHttpClient() ) );
	}

	@Override
	public void requestAccessToken( OAuth2Profile profile ) throws OAuth2Exception
	{
		try
		{
			OAuth2Parameters parameters = buildParametersFrom( profile );
			validateProfileContents( parameters );
			String authorizationURL = createAuthorizationURL( parameters );
			launchConsentScreenAndGetAuthorizationCode( authorizationURL, parameters );
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

	private OAuth2Parameters buildParametersFrom( OAuth2Profile profile )
	{
		String authorizationUri = expandProperty( profile, profile.getAuthorizationURI() );
		String redirectUri = expandProperty( profile, profile.getRedirectURI() );
		String accessTokenUri = expandProperty( profile, profile.getAccessTokenURI() );
		String clientId = expandProperty( profile, profile.getClientID() );
		String clientSecret = expandProperty( profile, profile.getClientSecret() );
		String scope = expandProperty( profile, profile.getScope() );
		return new OAuth2Parameters( profile, authorizationUri, redirectUri, accessTokenUri, clientId, clientSecret, scope );
	}

	private void validateProfileContents( OAuth2Parameters parameters )
	{

		validateHttpUrl( parameters.authorizationUri, "Authorization URI " );
		if( !parameters.redirectUri.equals( OAUTH_2_OOB_URN ) )
		{
			validateHttpUrl( parameters.redirectUri, "Redirect URI" );
		}
		validateHttpUrl( parameters.accessTokenUri, "Access token URI" );
		validateRequiredStringValue( parameters.clientId, "Client ID" );
		validateRequiredStringValue( parameters.clientSecret, "Client secret" );
	}

	private String expandProperty( OAuth2Profile profile, String value )
	{
		return PropertyExpander.expandProperties( profile.getContainer().getProject(), value );
	}

	private void validateRequiredStringValue( String value, String propertyName )
	{
		if( !StringUtils.hasContent( value ) )
		{
			throw new InvalidOAuth2ParametersException( propertyName + " is empty" );
		}
	}

	private void validateHttpUrl( String authorizationUri, String uriName )
	{
		if( !isValidHttpUrl( authorizationUri ) )
		{
			throw new InvalidOAuth2ParametersException( uriName + " " + authorizationUri + " is not a valid HTTP URL" );
		}
	}

	private boolean isValidHttpUrl( String authorizationUri )
	{
		if( !StringUtils.hasContent( authorizationUri ) )
		{
			return false;
		}
		try
		{
			URL url = new URL( authorizationUri );
			return url.getProtocol().startsWith( "http" );
		}
		catch( MalformedURLException e )
		{
			return false;
		}
	}

	private void logAndThrowOAuth2Exception( Exception e ) throws OAuth2Exception
	{
		SoapUI.logError( e, "Failed to create the authorization URL" );
		throw new OAuth2Exception( e );
	}

	private String createAuthorizationURL( OAuth2Parameters profile ) throws OAuthSystemException
	{
		return OAuthClientRequest
				.authorizationLocation( profile.authorizationUri )
				.setClientId( profile.clientId )
				.setResponseType( CODE )
				.setScope( profile.scope )
				.setRedirectURI( profile.redirectUri )
				.buildQueryMessage().getLocationUri();

	}

	private void launchConsentScreenAndGetAuthorizationCode( String authorizationURL, final OAuth2Parameters parameters )
			throws URISyntaxException, MalformedURLException
	{
		browserFacade.addBrowserStateListener( new BrowserStateChangeListener()
		{
			@Override
			public void locationChanged( String newLocation )
			{
				if( !parameters.redirectUri.contains( OAUTH_2_OOB_URN ) )
				{
					getAccessTokenAndSaveToProfile( parameters, extractAuthorizationCode( newLocation ) );
				}
			}

			@Override
			public void contentChanged( String newContent )
			{
				if( parameters.redirectUri.contains( OAUTH_2_OOB_URN ) )
				{
					int titlePosition = newContent.indexOf( TITLE );
					if( titlePosition != -1 )
					{
						String title = newContent.substring( titlePosition + TITLE.length(), newContent.indexOf( "</TITLE>" ) );
						getAccessTokenAndSaveToProfile( parameters, extractAuthorizationCode( title ) );
					}
				}
			}

		} );
		parameters.startAccessTokenFlow();
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

	private void getAccessTokenAndSaveToProfile( OAuth2Parameters parameters, String authorizationCode )
	{
		if( authorizationCode != null )
		{
			try
			{
				OAuthClientRequest accessTokenRequest = OAuthClientRequest
						.tokenLocation( parameters.accessTokenUri )
						.setGrantType( GrantType.AUTHORIZATION_CODE )
						.setClientId( parameters.clientId )
						.setClientSecret( parameters.clientSecret )
						.setRedirectURI( parameters.redirectUri )
						.setCode( authorizationCode )
						.buildBodyMessage();
				OAuthToken token = getOAuthClient().accessToken( accessTokenRequest, OAuthJSONAccessTokenResponse.class ).getOAuthToken();
				if( token != null && token.getAccessToken() != null )
				{
					parameters.setAccessTokenInProfile( token.getAccessToken() );
					parameters.setRefreshTokenInProfile( token.getRefreshToken() );
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

	@Override
	public void applyAccessToken( OAuth2Profile profile, HttpRequestBase request, String requestContent )
	{

		String uri = request.getURI().getPath();
		OAuthBearerClientRequest oAuthBearerClientRequest = new OAuthBearerClientRequest( uri ).setAccessToken( profile.getAccessToken() );

		try
		{
			switch( profile.getAccessTokenPosition() )
			{
				case QUERY:
					appendAccessTokenToQuery( request, oAuthBearerClientRequest );
					break;
				case BODY:
					appendAccessTokenToBody( request, oAuthBearerClientRequest, requestContent );
					break;
				case HEADER:
				default:
					appendAccessTokenToHeader( request, oAuthBearerClientRequest );
					break;
			}
		}
		catch( OAuthSystemException e )
		{
			SoapUI.logError( e );
		}
	}

	private void appendAccessTokenToBody( HttpRequestBase request, OAuthBearerClientRequest oAuthBearerClientRequest, String requestContent ) throws OAuthSystemException
	{
	}

	private void appendAccessTokenToQuery( HttpRequestBase request, OAuthBearerClientRequest oAuthBearerClientRequest ) throws OAuthSystemException
	{
		String uriWithAccessToken = oAuthBearerClientRequest.buildQueryMessage().getLocationUri();
		String queryString = uriWithAccessToken.split( "\\?" )[1];
		URI oldUri = request.getURI();
		String requestQueryString = oldUri.getQuery() != null ? oldUri.getQuery() + "&" + queryString : queryString;

		try
		{
			request.setURI( URIUtils.createURI( oldUri.getScheme(), oldUri.getHost(), oldUri.getPort(),
					oldUri.getRawPath(), requestQueryString, oldUri.getFragment() ) );
		}
		catch( URISyntaxException e )
		{
			throw new OAuthSystemException( e );
		}
	}

	private void appendAccessTokenToHeader( HttpRequestBase request, OAuthBearerClientRequest oAuthBearerClientRequest ) throws OAuthSystemException
	{
		OAuthClientRequest oAuthClientRequest = oAuthBearerClientRequest.buildHeaderMessage();

		Map<String, String> oAuthHeaders = oAuthClientRequest.getHeaders();
		request.removeHeaders( OAuth.HeaderType.AUTHORIZATION );
		request.addHeader( OAuth.HeaderType.AUTHORIZATION, oAuthHeaders.get( OAuth.HeaderType.AUTHORIZATION ) );
	}

	@Override
	public void refreshAccessToken( OAuth2Profile profile ) throws Exception
	{
		String refreshToken = profile.getRefreshToken();
		validateRequiredStringValue( refreshToken, "refresh token" );
		OAuth2Parameters parameters = buildParametersFrom( profile );
		validateRequiredStringValue( parameters.clientId, "client ID" );
		validateRequiredStringValue( parameters.clientSecret, "client secret" );
		OAuthClientRequest accessTokenRequest = OAuthClientRequest
				.tokenLocation( parameters.accessTokenUri )
				.setGrantType( GrantType.REFRESH_TOKEN )
				.setClientId( parameters.clientId )
				.setClientSecret( parameters.clientSecret )
				.setRefreshToken( refreshToken )
				.buildBodyMessage();

		OAuthClient oAuthClient = getOAuthClient();

		OAuthToken oAuthToken = oAuthClient.accessToken( accessTokenRequest, OAuthJSONAccessTokenResponse.class ).getOAuthToken();
		profile.applyRetrievedAccessToken( oAuthToken.getAccessToken() );
	}
}
