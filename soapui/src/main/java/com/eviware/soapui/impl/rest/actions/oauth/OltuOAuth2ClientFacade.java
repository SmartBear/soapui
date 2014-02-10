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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.StringEntity;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.eviware.soapui.impl.rest.actions.oauth.OAuth2ParameterValidator.validateRequiredStringValue;

/**
 * This class implements an OAuth2 three-legged authorization using the third party library Oltu.
 */
public class OltuOAuth2ClientFacade implements OAuth2ClientFacade
{

	OAuth2TokenExtractor oAuth2TokenExtractor = new OAuth2TokenExtractor();

	@Override
	public void requestAccessToken( OAuth2Profile profile ) throws OAuth2Exception
	{
		try
		{
			OAuth2Parameters parameters = buildParametersFrom( profile );
			OAuth2ParameterValidator.validate( parameters );

			switch( profile.getOAuth2Flow() )
			{
				case IMPLICIT_GRANT:
					oAuth2TokenExtractor.extractAccessTokenForImplicitGrantFlow( parameters );
					break;
				case AUTHORIZATION_CODE_GRANT:
			   default:
					oAuth2TokenExtractor.extractAccessTokenForAuthorizationCodeGrantFlow( parameters );
					break;
			}
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

	@Override
	public void refreshAccessToken( OAuth2Profile profile ) throws Exception
	{
		OAuth2Parameters parameters = buildParametersFrom( profile );
		validateRequiredStringValue( parameters.refreshToken, "refresh token" );
		validateRequiredStringValue( parameters.clientId, "client ID" );
		validateRequiredStringValue( parameters.clientSecret, "client secret" );

		oAuth2TokenExtractor.refreshAccessToken( parameters );
	}

	@Override
	public void applyAccessToken( OAuth2Profile profile, HttpRequestBase request, String requestContent )
	{

		String uri = request.getURI().getPath();
		OAuthBearerClientRequest oAuthClientRequest = new OAuthBearerClientRequest( uri ).setAccessToken( profile.getAccessToken() );

		try
		{
			switch( profile.getAccessTokenPosition() )
			{
				case QUERY:
					appendAccessTokenToQuery( request, oAuthClientRequest );
					break;
				case BODY:
					appendAccessTokenToBody( request, oAuthClientRequest );
					break;
				case HEADER:
				default:
					appendAccessTokenToHeader( request, oAuthClientRequest );
					break;
			}
		}
		catch( OAuthSystemException e )
		{
			SoapUI.logError( e );
		}
	}

	private OAuth2Parameters buildParametersFrom( OAuth2Profile profile )
	{
		return new OAuth2Parameters( profile );
	}

	private void logAndThrowOAuth2Exception( Exception e ) throws OAuth2Exception
	{
		SoapUI.logError( e, "Failed to create the authorization URL" );
		throw new OAuth2Exception( e );
	}

	private void appendAccessTokenToBody( HttpRequestBase request, OAuthBearerClientRequest oAuthClientRequest )
			throws OAuthSystemException
	{
		try
		{
			if( request instanceof HttpEntityEnclosingRequest )
			{
				HttpEntity httpEntity = ( ( HttpEntityEnclosingRequest )request ).getEntity();
				if( httpEntity == null )
				{
					String accessTokenParameter = getQueryStringFromOAuthClientRequest( oAuthClientRequest );
					( ( HttpEntityEnclosingRequest )request ).setEntity( new StringEntity( accessTokenParameter ) );
				}
				else
				{
					//TODO: re-create the entity from existing one and append the new content for access token
				}
			}
		}
		catch( UnsupportedEncodingException e )
		{
			throw new OAuthSystemException( e );
		}
	}

	private void appendAccessTokenToQuery( HttpRequestBase request, OAuthBearerClientRequest oAuthClientRequest ) throws OAuthSystemException
	{
		String queryString = getQueryStringFromOAuthClientRequest( oAuthClientRequest );
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

	private String getQueryStringFromOAuthClientRequest( OAuthBearerClientRequest oAuthClientRequest ) throws OAuthSystemException
	{
		String uriWithAccessToken = oAuthClientRequest.buildQueryMessage().getLocationUri();
		return uriWithAccessToken.split( "\\?" )[1];
	}

	private void appendAccessTokenToHeader( HttpRequestBase request, OAuthBearerClientRequest oAuthClientRequest ) throws OAuthSystemException
	{
		Map<String, String> oAuthHeaders = oAuthClientRequest.buildHeaderMessage().getHeaders();
		request.removeHeaders( OAuth.HeaderType.AUTHORIZATION );
		request.addHeader( OAuth.HeaderType.AUTHORIZATION, oAuthHeaders.get( OAuth.HeaderType.AUTHORIZATION ) );
	}
}
