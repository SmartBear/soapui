package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;

/**
 * Wrapper class that performs property expansion on the values in an OAuth2Profile instance.
 */
class OAuth2Parameters
{

	private final OAuth2Profile profile;

	final String authorizationUri;
	final String redirectUri;
	final String accessTokenUri;
	final String clientId;
	final String clientSecret;
	final String scope;
	public OAuth2Profile.AccessTokenRetrievalLocation accessTokenRetrievalLocation;

	/**
	 * Constructs an OAuth2Parameters object
	 *
	 * @param profile the profile to be wrapped
	 */
	OAuth2Parameters( OAuth2Profile profile )
	{
		this.profile = profile;
		this.authorizationUri = expandProperty( profile, profile.getAuthorizationURI() );
		this.redirectUri = expandProperty( profile, profile.getRedirectURI() );
		this.accessTokenUri = expandProperty( profile, profile.getAccessTokenURI() );
		this.clientId = expandProperty( profile, profile.getClientID() );
		this.clientSecret = expandProperty( profile, profile.getClientSecret() );
		this.scope = expandProperty( profile, profile.getScope() );
		this.accessTokenRetrievalLocation = profile.getAccessTokenRetrievalLocation();
	}


	public void startAccessTokenFlow()
	{
		profile.startAccessTokenFlow();
	}


	/**
	 * Sets the accessToken property on the wrapped OAuth2Profile instance
	 *
	 * @param accessToken the access token String
	 */
	void setAccessTokenInProfile( String accessToken )
	{
		profile.applyRetrievedAccessToken( accessToken );
	}

	public void setRefreshTokenInProfile( String refreshToken )
	{
		profile.setRefreshToken( refreshToken );
	}

	public void setAccessTokenExpirationTimeInProfile( long expirationTime )
	{
		profile.setAccessTokenExpirationTime( expirationTime );
	}

	public void setAccessTokenIssuedTimeInProfile( long issuedTime )
	{
		profile.setAccessTokenIssuedTime( issuedTime );
	}

	public void waitingForAuthorization()
	{
		profile.waitingForAuthorization();
	}

	private String expandProperty( OAuth2Profile profile, String value )
	{
		return PropertyExpander.expandProperties( profile.getContainer().getProject(), value );
	}

	public void receivedAuthorizationCode()
	{
		profile.receivedAuthorizationCode();
	}
}
