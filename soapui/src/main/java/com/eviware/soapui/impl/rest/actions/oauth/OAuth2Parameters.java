package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.impl.rest.OAuth2Profile;

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

	/**
	 * Constructs an OAuth2Parameters object
	 * @param profile the profile to be wrapped
	 * @param authorizationUri
	 * @param redirectUri
	 * @param accessTokenUri
	 * @param clientId
	 * @param clientSecret
	 */
	OAuth2Parameters( OAuth2Profile profile, String authorizationUri, String redirectUri, String accessTokenUri,
							String clientId, String clientSecret, String scope )
	{
		this.profile = profile;
		this.authorizationUri = authorizationUri;
		this.redirectUri = redirectUri;
		this.accessTokenUri = accessTokenUri;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.scope = scope;
	}


	public void startAccessTokenFlow()
	{
		profile.startAccessTokenFlow();
	}


	/**
	 * Sets the accessToken property on the wrapped OAuth2Profile instance
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
}
