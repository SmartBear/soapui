package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;

/**
* Wrapper class that performs property expansion on the values in an OAuth2Profile instance.
*/
class OAuth2Parameters
{

	private final OAuth2Profile profile;

	/**
	 * Constructs an OAuth2Parameters object
	 * @param profile the profile to be wrapped
	 */
	OAuth2Parameters( OAuth2Profile profile )
	{
		this.profile = profile;
	}

	String getAuthorizationUri()
	{
		return expandProperty(profile.getAuthorizationURL());
	}

	String getRedirectUri()
	{
		return expandProperty( profile.getRedirectUri() );
	}

	String getAccessTokenUri()
	{
		return expandProperty( profile.getAccessTokenUri() );
	}

	String getClientId()
	{
		return expandProperty( profile.getClientId() );
	}

	String getClientSecret()
	{
		return expandProperty( profile.getClientSecret() );
	}

	/**
	 * Sets the accessToken property on the wrapped OAuth2Profile instance
	 * @param accessToken the access token String
	 */
	void setAccessTokenInProfile( String accessToken )
	{
		profile.setAccessToken( accessToken );
	}

	String getScope()
	{
		return expandProperty( profile.getScope() );
	}

	private String expandProperty( String value )
	{
		return PropertyExpander.expandProperties( profile, value );
	}
}
