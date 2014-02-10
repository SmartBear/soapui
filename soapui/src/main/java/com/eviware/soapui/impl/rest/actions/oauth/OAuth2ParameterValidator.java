package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.support.StringUtils;

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
		validateHttpUrl( parameters.authorizationUri, "Authorization URI " );
		validateUri( parameters.redirectUri, "Redirect URI" );
		validateHttpUrl( parameters.accessTokenUri, "Access token URI" );
		validateRequiredStringValue( parameters.clientId, "Client ID" );
		validateRequiredStringValue( parameters.clientSecret, "Client secret" );
	}

	private static void validateUri( String uri, String uriName )
	{
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
