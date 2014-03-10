package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.google.common.base.Preconditions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Helper class used to subscribe to Access Token Status changes
 * <br/>
 * <b>Note!</b> You need to call <i>unregister()</i> when you are done with the manager to remove its internal property listener
 */
final class OAuth2AccessTokenStatusChangeManager implements PropertyChangeListener
{
	OAuth2AccessTokenStatusChangeListener listener = null;

	public OAuth2AccessTokenStatusChangeManager( OAuth2AccessTokenStatusChangeListener listener )
	{
		this.listener = listener;
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( OAuth2Profile.ACCESS_TOKEN_STATUS_PROPERTY ) )
		{
			OAuth2Profile.AccessTokenStatus status = OAuth2Profile.AccessTokenStatus.byDescription( ( String )evt.getNewValue() );
			listener.onAccessTokenStatusChanged( status );
		}
	}

	/**
	 * Start reciving Access Token Status change events
	 */
	public void register()
	{
		Preconditions.checkNotNull( listener.getProfile(), "Could not get OAuth 2 profile from the listener" );
		listener.getProfile().addPropertyChangeListener( this );
	}

	/**
	 * Stop reciving Acess Token Status change events.
	 */
	public void unregister()
	{
		Preconditions.checkNotNull( listener.getProfile(), "Could not get OAuth 2 profile from the listener" );
		listener.getProfile().removePropertyChangeListener( this );
	}
}