package com.eviware.soapui.impl.rest.actions.oauth;

/**
 * Thrown when an OAuth2Profile instance holds invalid values.
 */
public class InvalidOAuth2ParametersException extends IllegalArgumentException
{

	public InvalidOAuth2ParametersException( String s )
	{
		super( s );
	}
}
