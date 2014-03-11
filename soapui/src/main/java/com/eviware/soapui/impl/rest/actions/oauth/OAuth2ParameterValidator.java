package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.editor.inspectors.auth.OAuth2GetAccessTokenForm;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 */
public class OAuth2ParameterValidator
{

	static void validate( OAuth2Parameters parameters )
	{
		validateRequiredStringValue( parameters.clientId, OAuth2GetAccessTokenForm.CLIENT_ID_TITLE );
		if( parameters.getOAuth2Flow() != OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT )
		{
			validateRequiredStringValue( parameters.clientSecret, OAuth2GetAccessTokenForm.CLIENT_SECRET_TITLE );
		}
		validateHttpUrl( parameters.authorizationUri, OAuth2GetAccessTokenForm.AUTHORIZATION_URI_TITLE );
		validateHttpUrl( parameters.accessTokenUri, OAuth2GetAccessTokenForm.ACCESS_TOKEN_URI_TITLE );
		validateUri( parameters.redirectUri, OAuth2GetAccessTokenForm.REDIRECT_URI_TITLE );

	}

	private static void validateUri( String uri, String uriName )
	{
		if( !StringUtils.hasContent( uri ) )
		{
			throw new InvalidOAuth2ParametersException( uri + " is not a valid " + uriName );
		}

		try
		{
			new URI( uri );
		}
		catch( URISyntaxException e )
		{
			throw new InvalidOAuth2ParametersException( uri + " is not a valid " + uriName );
		}
	}

	private static void validateHttpUrl( String authorizationUri, String uriName )
	{
		if( !isValidHttpUrl( authorizationUri ) )
		{
			throw new InvalidOAuth2ParametersException( uriName + " " + authorizationUri + " is not a valid HTTP URL" );
		}
	}

	private static boolean isValidHttpUrl( String authorizationUri )
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

	static void validateRequiredStringValue( String value, String propertyName )
	{
		if( !StringUtils.hasContent( value ) )
		{
			throw new InvalidOAuth2ParametersException( propertyName + " is empty" );
		}
	}

}
