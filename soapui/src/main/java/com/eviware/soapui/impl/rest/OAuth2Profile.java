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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.AccessTokenPositionConfig;
import com.eviware.soapui.config.AccessTokenRetrievalLocationConfig;
import com.eviware.soapui.config.AccessTokenStatusConfig;
import com.eviware.soapui.config.OAuth2FlowConfig;
import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.config.StringListConfig;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import org.apache.commons.lang.StringUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates values associated with an Oauth2 flow. Mostly they will be input by users, but the "accessToken" and
 * "status" properties will be modified during the OAuth2 interactions.
 */
public class OAuth2Profile implements PropertyExpansionContainer
{

	public static final String CLIENT_ID_PROPERTY = "clientID";
	public static final String CLIENT_SECRET_PROPERTY = "clientSecret";
	public static final String AUTHORIZATION_URI_PROPERTY = "authorizationURI";
	public static final String ACCESS_TOKEN_URI_PROPERTY = "accessTokenURI";
	public static final String REDIRECT_URI_PROPERTY = "redirectURI";
	public static final String ACCESS_TOKEN_PROPERTY = "accessToken";
	public static final String REFRESH_TOKEN_PROPERTY = "refreshToken";
	public static final String SCOPE_PROPERTY = "scope";
	public static final String ACCESS_TOKEN_STATUS_PROPERTY = "accessTokenStatus";
	public static final String ACCESS_TOKEN_POSITION_PROPERTY = "accessTokenPosition";
	public static final String ACCESS_TOKEN_RETRIEVAL_PROPERTY = "accessTokenRetrievalLocation";
	public static final String ACCESS_TOKEN_EXPIRATION_TIME = "accessTokenExpirationTime";
	public static final String ACCESS_TOKEN_ISSUED_TIME = "accessTokenIssuedTime";
	public static final String OAUTH2_FLOW_PROPERTY = "oAuth2Flow";
	public static final String JAVA_SCRIPTS_PROPERTY = "javaScripts";



	public enum AccessTokenStatus
	{
		UPDATED_MANUALLY,
		PENDING,
		WAITING_FOR_AUTHORIZATION,
		RECEIVED_AUTHORIZATION_CODE,
		FAILED,
		RETRIEVED_FROM_SERVER
	}

	public enum AccessTokenPosition
	{
		QUERY,
		HEADER,
		BODY
	}

	public enum AccessTokenRetrievalLocation
	{
		BODY_JSON,
		BODY_URL_ENCODED_FORM
	}


	public enum OAuth2Flow
	{
		AUTHORIZATION_CODE_GRANT("Authorization Code Grant"),
		RESOURCE_OWNER_CREDENTIALS_GRANT("Resource Owner Credentials Grant"),
		CLIENT_CREDENTIALS_GRANT("Client Credentials Grant"),
		IMPLICIT_GRANT("Implicit Grant");

		private String description;

		OAuth2Flow( String description )
		{
			this.description = description;
		}

		@Override
		public String toString()
		{
			return description;
		}

	}
	private final OAuth2ProfileContainer oAuth2ProfileContainer;
	private final OAuth2ProfileConfig configuration;
	private final PropertyChangeSupport pcs;

	public OAuth2Profile( OAuth2ProfileContainer oAuth2ProfileContainer, OAuth2ProfileConfig configuration )
	{
		this.oAuth2ProfileContainer = oAuth2ProfileContainer;
		this.configuration = configuration;
		pcs = new PropertyChangeSupport( this );
	}


	public boolean hasAutomationJavaScripts()
	{
		List<String> javaScripts = getAutomationJavaScripts();
		return javaScripts != null && !javaScripts.isEmpty();
	}

	public void waitingForAuthorization()
	{
		setAccessTokenStatus( AccessTokenStatus.WAITING_FOR_AUTHORIZATION );
	}

	public void receivedAuthorizationCode()
	{
		setAccessTokenStatus( AccessTokenStatus.RECEIVED_AUTHORIZATION_CODE );
	}

	public void startAccessTokenFlow()
	{
		setAccessTokenStatus( AccessTokenStatus.PENDING );
	}

	public void applyRetrievedAccessToken( String accessToken )
	{
		// Ignore return value in this case: even if it is not a change, it is important to know that a token has been
		// retrieved from the server
		doSetAccessToken( accessToken );
		setAccessTokenStatus( AccessTokenStatus.RETRIEVED_FROM_SERVER );
	}

	public String getAccessToken()
	{
		return configuration.getAccessToken();
	}

	/**
	 * NOTE: This setter should only be used from the GUI, because it also sets the property "accessTokenStatus" to
	 * UPDATED_MANUALLY
	 *
	 * @param accessToken the access token supplied by the user
	 */
	public void setAccessToken( String accessToken )
	{
		if( doSetAccessToken( accessToken ) )
		{
			setAccessTokenStatus( AccessTokenStatus.UPDATED_MANUALLY );
		}
	}

	public AccessTokenPosition getAccessTokenPosition()
	{
		if( configuration.getAccessTokenPosition() == null )
		{
			configuration.setAccessTokenPosition( AccessTokenPositionConfig.HEADER );
		}
		return AccessTokenPosition.valueOf( configuration.getAccessTokenPosition().toString() );
	}

	public AccessTokenRetrievalLocation getAccessTokenRetrievalLocation()
	{
		if( configuration.getAccessTokenRetrievalLocation() == null )
		{
			configuration.setAccessTokenRetrievalLocation( AccessTokenRetrievalLocationConfig.BODY_JSON );
		}
		return AccessTokenRetrievalLocation.valueOf( configuration.getAccessTokenRetrievalLocation().toString() );
	}

	public void setAccessTokenPosition( AccessTokenPosition accessTokenPosition )
	{
		AccessTokenPosition oldValue = getAccessTokenPosition();
		if( !accessTokenPosition.equals( oldValue.toString() ) )
		{
			configuration.setAccessTokenPosition( AccessTokenPositionConfig.Enum.forString( accessTokenPosition.toString() ) );
			pcs.firePropertyChange( ACCESS_TOKEN_POSITION_PROPERTY, AccessTokenPosition.valueOf( oldValue.toString() ),
					accessTokenPosition );
		}
	}

	public void setOAuth2Flow( OAuth2Flow oauth2Flow )
	{
		OAuth2Flow existingFlow = getOAuth2Flow();
		if( !oauth2Flow.equals( existingFlow ) )
		{
			configuration.setOAuth2Flow( OAuth2FlowConfig.Enum.forString( oauth2Flow.name() ) );
			pcs.firePropertyChange( OAUTH2_FLOW_PROPERTY, existingFlow, oauth2Flow );
		}
	}

	public OAuth2Flow getOAuth2Flow()
	{
		if( configuration.getOAuth2Flow() == null )
		{
			configuration.setOAuth2Flow( OAuth2FlowConfig.AUTHORIZATION_CODE_GRANT );
		}
		return OAuth2Flow.valueOf( configuration.getOAuth2Flow().toString() );
	}

	public void setAccessTokenRetrievalLocation( AccessTokenRetrievalLocation retrievalLocation )
	{
		AccessTokenRetrievalLocation oldValue = getAccessTokenRetrievalLocation();
		if( !retrievalLocation.equals( oldValue.toString() ) )
		{
			configuration.setAccessTokenRetrievalLocation( AccessTokenRetrievalLocationConfig.Enum.forString(
					retrievalLocation.toString() ) );
			pcs.firePropertyChange( ACCESS_TOKEN_RETRIEVAL_PROPERTY, oldValue, retrievalLocation );
		}
	}

	public String getRefreshToken()
	{
		return configuration.getRefreshToken();
	}

	public void setRefreshToken( String refreshToken )
	{
		String oldValue = configuration.getRefreshToken();
		if( !StringUtils.equals( oldValue, refreshToken ) )
		{
			configuration.setRefreshToken( refreshToken );
			pcs.firePropertyChange( REFRESH_TOKEN_PROPERTY, oldValue, refreshToken );
		}
	}

	private boolean doSetAccessToken( String accessToken )
	{
		String oldValue = configuration.getAccessToken();
		if( !StringUtils.equals( oldValue, accessToken ) )
		{
			configuration.setAccessToken( accessToken );
			pcs.firePropertyChange( ACCESS_TOKEN_PROPERTY, oldValue, accessToken );
			return true;
		}
		return false;
	}

	public String getAuthorizationURI()
	{
		return configuration.getAuthorizationURI();
	}

	public void setAuthorizationURI( String authorizationURI )
	{
		String oldValue = configuration.getAuthorizationURI();
		if( !StringUtils.equals( oldValue, authorizationURI ) )
		{
			configuration.setAuthorizationURI( authorizationURI );
			pcs.firePropertyChange( AUTHORIZATION_URI_PROPERTY, oldValue, authorizationURI );
		}
	}

	public String getClientID()
	{
		return configuration.getClientID();
	}

	public void setClientID( String clientID )
	{
		String oldValue = configuration.getClientID();
		if( !StringUtils.equals( oldValue, clientID ) )
		{
			configuration.setClientID( clientID );
			pcs.firePropertyChange( CLIENT_ID_PROPERTY, oldValue, clientID );
		}
	}

	public String getClientSecret()
	{
		return configuration.getClientSecret();
	}

	public void setClientSecret( String clientSecret )
	{
		String oldValue = configuration.getClientSecret();
		if( !StringUtils.equals( oldValue, clientSecret ) )
		{
			configuration.setClientSecret( clientSecret );
			pcs.firePropertyChange( CLIENT_SECRET_PROPERTY, oldValue, clientSecret );
		}
	}

	public String getRedirectURI()
	{
		return configuration.getRedirectURI();
	}

	public void setRedirectURI( String redirectURI )
	{
		String oldValue = configuration.getRedirectURI();
		if( !StringUtils.equals( oldValue, redirectURI ) )
		{
			configuration.setRedirectURI( redirectURI );
			pcs.firePropertyChange( REDIRECT_URI_PROPERTY, oldValue, redirectURI );
		}
	}

	public String getScope()
	{
		return configuration.getScope();
	}

	public void setScope( String scope )
	{
		String oldValue = configuration.getScope();
		if( !StringUtils.equals( oldValue, scope ) )
		{
			configuration.setScope( scope );
			pcs.firePropertyChange( SCOPE_PROPERTY, oldValue, scope );
		}
	}

	public OAuth2ProfileConfig getConfiguration()
	{
		return configuration;
	}

	public String getAccessTokenURI()
	{
		return configuration.getAccessTokenURI();
	}

	public void setAccessTokenURI( String accessTokenURI )
	{
		String oldValue = configuration.getAccessTokenURI();
		if( !StringUtils.equals( oldValue, accessTokenURI ) )
		{
			configuration.setAccessTokenURI( accessTokenURI );
			pcs.firePropertyChange( ACCESS_TOKEN_URI_PROPERTY, oldValue, accessTokenURI );
		}
	}

	public String getAccessTokenStatus()
	{
		if( configuration.getAccessTokenStatus() != null )
		{
			return configuration.getAccessTokenStatus().toString();
		}
		return null;
	}

	public long getAccessTokenExpirationTime()
	{
		return configuration.getAccessTokenExpirationTime();
	}

	public void setAccessTokenExpirationTime( long newExpirationTime )
	{
		long oldExpirationTime = configuration.getAccessTokenExpirationTime();

		if( oldExpirationTime != newExpirationTime )
		{
			configuration.setAccessTokenExpirationTime( newExpirationTime );
			pcs.firePropertyChange( ACCESS_TOKEN_EXPIRATION_TIME, oldExpirationTime, newExpirationTime );
		}
	}

	public long getAccessTokenIssuedTime()
	{
		return configuration.getAccessTokenIssuedTime();
	}

	public void setAccessTokenIssuedTime( long newIssuedTime )
	{
		long oldIssuedTime = configuration.getAccessTokenIssuedTime();

		if( oldIssuedTime != newIssuedTime )
		{
			configuration.setAccessTokenIssuedTime( newIssuedTime );
			pcs.firePropertyChange( ACCESS_TOKEN_ISSUED_TIME, oldIssuedTime, newIssuedTime );
		}
		configuration.setAccessTokenIssuedTime( newIssuedTime );
	}

	public OAuth2ProfileContainer getContainer()
	{
		return oAuth2ProfileContainer;
	}

	@Override
	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( oAuth2ProfileContainer.getProject(), this );

		result.extractAndAddAll( CLIENT_ID_PROPERTY );
		result.extractAndAddAll( CLIENT_SECRET_PROPERTY );
		result.extractAndAddAll( AUTHORIZATION_URI_PROPERTY );
		result.extractAndAddAll( ACCESS_TOKEN_URI_PROPERTY );
		result.extractAndAddAll( REDIRECT_URI_PROPERTY );
		result.extractAndAddAll( ACCESS_TOKEN_PROPERTY );
		result.extractAndAddAll( SCOPE_PROPERTY );

		return result.toArray();
	}

	public List<String> getAutomationJavaScripts()
	{
		StringListConfig configurationEntry = configuration.getJavaScripts();
		return configurationEntry == null ? Collections.<String>emptyList() :  new ArrayList<String>(
				configurationEntry.getEntryList());
	}

	public void setAutomationJavaScripts( List<String> javaScripts )
	{
		List<String> oldScripts = getAutomationJavaScripts();
		String[] scriptArray = javaScripts.toArray( new String[javaScripts.size()] );
		StringListConfig javaScriptsConfiguration = configuration.getJavaScripts();
		if( javaScriptsConfiguration == null )
		{
			javaScriptsConfiguration = StringListConfig.Factory.newInstance();
		}
		javaScriptsConfiguration.setEntryArray( scriptArray );
		configuration.setJavaScripts( javaScriptsConfiguration );
		pcs.firePropertyChange( JAVA_SCRIPTS_PROPERTY, oldScripts, javaScripts );
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		pcs.addPropertyChangeListener( listener );
	}

	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		pcs.addPropertyChangeListener( propertyName, listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		pcs.removePropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		pcs.removePropertyChangeListener( propertyName, listener );
	}

	/* Helper method */

	private void setAccessTokenStatus( AccessTokenStatus status )
	{
		String oldValue = getAccessTokenStatus();

		if( status == null && oldValue == null )
		{
			return;
		}
		else if( status != null )
		{
			if( status.equals( oldValue ) )
			{
				return;
			}
			configuration.setAccessTokenStatus( AccessTokenStatusConfig.Enum.forString( status.toString() ) );
		}
		else
		{
			configuration.setAccessTokenStatus( null );
		}
		pcs.firePropertyChange( ACCESS_TOKEN_STATUS_PROPERTY, oldValue, status.toString() );
	}

}
