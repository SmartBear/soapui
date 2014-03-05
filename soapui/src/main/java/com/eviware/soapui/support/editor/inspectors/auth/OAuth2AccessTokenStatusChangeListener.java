package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface OAuth2AccessTokenStatusChangeListener
{
	/**
	 * @param status The new Access Token status
	 */
	void onAccessTokenStatusChanged( @Nullable OAuth2Profile.AccessTokenStatus status );

	/**
	 * @return The OAuth 2 profile accociated with the listener
	 */
	@Nonnull
	OAuth2Profile getProfile();
}
