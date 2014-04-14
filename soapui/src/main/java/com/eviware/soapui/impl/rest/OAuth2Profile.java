/*
 * Copyright 2004-2014 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
*/

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.*;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import static com.eviware.soapui.impl.rest.OAuth2Profile.RefreshAccessTokenMethods.AUTOMATIC;

/**
 * Encapsulates values associated with an Oauth2 flow. Mostly they will be input by users, but the "accessToken" and
 * "status" properties will be modified during the OAuth2 interactions.
 */
public class OAuth2Profile implements PropertyExpansionContainer {
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
    public static final String ACCESS_TOKEN_EXPIRATION_TIME = "accessTokenExpirationTime";
    public static final String ACCESS_TOKEN_ISSUED_TIME = "accessTokenIssuedTime";
    public static final String MANUAL_ACCESS_TOKEN_EXPIRATION_TIME = "manualAccessTokenExpirationTime";
    public static final String USE_MANUAL_ACCESS_TOKEN_EXPIRATION_TIME = "useManualAccessTokenExpirationTime";

    public static final String REFRESH_ACCESS_TOKEN_METHOD_PROPERTY = "refreshAccessTokenMethod";
    public static final String OAUTH2_FLOW_PROPERTY = "oAuth2Flow";
    public static final String JAVA_SCRIPTS_PROPERTY = "javaScripts";
    public static final String MANUAL_ACCESS_TOKEN_EXPIRATION_TIME_UNIT_PROPERTY = "manualAccessTokenExpirationTimeUnit";

    public void waitForAccessTokenStatus(AccessTokenStatus accessTokenStatus, int timeout) {
        int timeLeft = timeout;
        while ((getAccessTokenStatus() != accessTokenStatus) && timeLeft > 0) {
            long startTime = System.currentTimeMillis();
            try {
                synchronized (this) {
                    wait(timeLeft);
                }
            } catch (InterruptedException ignore) {

            }
            timeLeft -= (System.currentTimeMillis() - startTime);
        }
    }

    public enum AccessTokenStatus {
        ENTERED_MANUALLY("Entered Manually"),
        WAITING_FOR_AUTHORIZATION("Waiting for Authorization"),
        RECEIVED_AUTHORIZATION_CODE("Received authorization code"),
        RETRIEVED_FROM_SERVER("Retrieved from server"),
        RETRIEVAL_CANCELED("Retrieval canceled"),
        EXPIRED("Expired");

        private String description;

        AccessTokenStatus(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public enum AccessTokenPosition {
        QUERY,
        HEADER,
        BODY
    }

    public enum OAuth2Flow {
        AUTHORIZATION_CODE_GRANT("Authorization Code Grant"),
        IMPLICIT_GRANT("Implicit Grant");

        private String description;

        OAuth2Flow(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

    }

    public enum RefreshAccessTokenMethods {
        AUTOMATIC,
        MANUAL
    }

    private final OAuth2ProfileContainer oAuth2ProfileContainer;
    private final OAuth2ProfileConfig configuration;
    private final PropertyChangeSupport pcs;

    public OAuth2Profile(OAuth2ProfileContainer oAuth2ProfileContainer, OAuth2ProfileConfig configuration) {
        this.oAuth2ProfileContainer = oAuth2ProfileContainer;
        this.configuration = configuration;
        pcs = new PropertyChangeSupport(this);
    }

    public String getName() {
        //TODO: this is only for backward compatibility where we had only one profile without name, should be removed in 5.1
        if (StringUtils.isEmpty(configuration.getName())) {
            configuration.setName("OAuth 2 - Profile 1");
        }

        return configuration.getName();
    }

    public void setName(String newName) {
        configuration.setName(newName);
    }

    public boolean hasAutomationJavaScripts() {
        List<String> javaScripts = getAutomationJavaScripts();
        return javaScripts != null && !javaScripts.isEmpty();
    }

    public void waitingForAuthorization() {
        setAccessTokenStatus(AccessTokenStatus.WAITING_FOR_AUTHORIZATION);
    }

    public void receivedAuthorizationCode() {
        setAccessTokenStatus(AccessTokenStatus.RECEIVED_AUTHORIZATION_CODE);
    }

    public void expired() {
        setAccessTokenStatus(AccessTokenStatus.EXPIRED);
        setAccessTokenStartingStatus(AccessTokenStatus.EXPIRED);

    }

    public void retrivalCanceled() {
        setAccessTokenStatus(AccessTokenStatus.RETRIEVAL_CANCELED);
    }

    public void applyRetrievedAccessToken(String accessToken) {
        // Ignore return value in this case: even if it is not a change, it is important to know that a token has been
        // retrieved from the server
        doSetAccessToken(accessToken);
        setAccessTokenStatus(AccessTokenStatus.RETRIEVED_FROM_SERVER);
        setAccessTokenStartingStatus(AccessTokenStatus.RETRIEVED_FROM_SERVER);
    }

    public String getAccessToken() {
        return configuration.getAccessToken();
    }

    /**
     * NOTE: This setter should only be used from the GUI, because it also sets the property "accessTokenStatus" to
     * ENTERED_MANUALLY
     *
     * @param accessToken the access token supplied by the user
     */
    public void setAccessToken(String accessToken) {
        if (doSetAccessToken(accessToken)) {
            setAccessTokenStatus(AccessTokenStatus.ENTERED_MANUALLY);
            setAccessTokenStartingStatus(AccessTokenStatus.ENTERED_MANUALLY);
        }
    }

    public AccessTokenPosition getAccessTokenPosition() {
        if (configuration.getAccessTokenPosition() == null) {
            configuration.setAccessTokenPosition(AccessTokenPositionConfig.HEADER);
        }
        return AccessTokenPosition.valueOf(configuration.getAccessTokenPosition().toString());
    }

    public void setAccessTokenPosition(AccessTokenPosition accessTokenPosition) {
        AccessTokenPosition oldValue = getAccessTokenPosition();
        if (!accessTokenPosition.equals(oldValue.toString())) {
            configuration.setAccessTokenPosition(AccessTokenPositionConfig.Enum.forString(accessTokenPosition.toString()));
            pcs.firePropertyChange(ACCESS_TOKEN_POSITION_PROPERTY, AccessTokenPosition.valueOf(oldValue.toString()),
                    accessTokenPosition);
        }
    }

    public void setOAuth2Flow(OAuth2Flow oauth2Flow) {
        OAuth2Flow existingFlow = getOAuth2Flow();
        if (!oauth2Flow.equals(existingFlow)) {
            configuration.setOAuth2Flow(OAuth2FlowConfig.Enum.forString(oauth2Flow.name()));
            pcs.firePropertyChange(OAUTH2_FLOW_PROPERTY, existingFlow, oauth2Flow);
        }
    }

    public OAuth2Flow getOAuth2Flow() {
        if (configuration.getOAuth2Flow() == null) {
            configuration.setOAuth2Flow(OAuth2FlowConfig.AUTHORIZATION_CODE_GRANT);
        }
        return OAuth2Flow.valueOf(configuration.getOAuth2Flow().toString());
    }

    public String getRefreshToken() {
        return configuration.getRefreshToken();
    }

    public void setRefreshToken(String refreshToken) {
        String oldValue = configuration.getRefreshToken();
        if (!StringUtils.equals(oldValue, refreshToken)) {
            configuration.setRefreshToken(refreshToken);
            pcs.firePropertyChange(REFRESH_TOKEN_PROPERTY, oldValue, refreshToken);
        }
    }

    private boolean doSetAccessToken(String accessToken) {
        String oldValue = configuration.getAccessToken();
        if (!StringUtils.equals(oldValue, accessToken)) {
            configuration.setAccessToken(accessToken);
            pcs.firePropertyChange(ACCESS_TOKEN_PROPERTY, oldValue, accessToken);
            return true;
        }
        return false;
    }

    public String getAuthorizationURI() {
        return configuration.getAuthorizationURI();
    }

    public void setAuthorizationURI(String authorizationURI) {
        String oldValue = configuration.getAuthorizationURI();
        if (!StringUtils.equals(oldValue, authorizationURI)) {
            configuration.setAuthorizationURI(authorizationURI);
            pcs.firePropertyChange(AUTHORIZATION_URI_PROPERTY, oldValue, authorizationURI);
        }
    }

    public String getClientID() {
        return configuration.getClientID();
    }

    public void setClientID(String clientID) {
        String oldValue = configuration.getClientID();
        if (!StringUtils.equals(oldValue, clientID)) {
            configuration.setClientID(clientID);
            pcs.firePropertyChange(CLIENT_ID_PROPERTY, oldValue, clientID);
        }
    }

    public String getClientSecret() {
        return configuration.getClientSecret();
    }

    public void setClientSecret(String clientSecret) {
        String oldValue = configuration.getClientSecret();
        if (!StringUtils.equals(oldValue, clientSecret)) {
            configuration.setClientSecret(clientSecret);
            pcs.firePropertyChange(CLIENT_SECRET_PROPERTY, oldValue, clientSecret);
        }
    }

    public String getRedirectURI() {
        return configuration.getRedirectURI();
    }

    public void setRedirectURI(String redirectURI) {
        String oldValue = configuration.getRedirectURI();
        if (!StringUtils.equals(oldValue, redirectURI)) {
            configuration.setRedirectURI(redirectURI);
            pcs.firePropertyChange(REDIRECT_URI_PROPERTY, oldValue, redirectURI);
        }
    }

    public String getScope() {
        return configuration.getScope();
    }

    public void setScope(String scope) {
        String oldValue = configuration.getScope();
        if (!StringUtils.equals(oldValue, scope)) {
            configuration.setScope(scope);
            pcs.firePropertyChange(SCOPE_PROPERTY, oldValue, scope);
        }
    }

    public OAuth2ProfileConfig getConfiguration() {
        return configuration;
    }

    public String getAccessTokenURI() {
        return configuration.getAccessTokenURI();
    }

    public void setAccessTokenURI(String accessTokenURI) {
        String oldValue = configuration.getAccessTokenURI();
        if (!StringUtils.equals(oldValue, accessTokenURI)) {
            configuration.setAccessTokenURI(accessTokenURI);
            pcs.firePropertyChange(ACCESS_TOKEN_URI_PROPERTY, oldValue, accessTokenURI);
        }
    }

    public AccessTokenStatus getAccessTokenStatus() {
        return getSavedEnum(configuration.getAccessTokenStatus());
    }

    public void setAccessTokenStatus(@Nonnull AccessTokenStatus newStatus) {
        Preconditions.checkNotNull(newStatus);

        AccessTokenStatus oldStatus = getSavedEnum(configuration.getAccessTokenStatus());

        if (newStatus == oldStatus) {
            return;
        }

        synchronized (this) {
            notifyAll();
        }

        pcs.firePropertyChange(ACCESS_TOKEN_STATUS_PROPERTY, oldStatus, newStatus);
    }

    public AccessTokenStatus getAccessTokenStartingStatus() {
        if (configuration.getAccessTokenStartingStatus() != null) {
            return AccessTokenStatus.valueOf(configuration.getAccessTokenStartingStatus().toString());
        }
        return null;
    }

    public void resetAccessTokenStatusToStartingStatus() {
        setAccessTokenStatus(getAccessTokenStartingStatus());
    }

    public long getAccessTokenExpirationTime() {
        return configuration.getAccessTokenExpirationTime();
    }

    public void setAccessTokenExpirationTime(long newExpirationTime) {
        long oldExpirationTime = configuration.getAccessTokenExpirationTime();

        if (oldExpirationTime != newExpirationTime) {
            configuration.setAccessTokenExpirationTime(newExpirationTime);
            pcs.firePropertyChange(ACCESS_TOKEN_EXPIRATION_TIME, oldExpirationTime, newExpirationTime);
        }
    }

    public long getAccessTokenIssuedTime() {
        return configuration.getAccessTokenIssuedTime();
    }

    public void setAccessTokenIssuedTime(long newIssuedTime) {
        long oldIssuedTime = configuration.getAccessTokenIssuedTime();

        if (oldIssuedTime != newIssuedTime) {
            configuration.setAccessTokenIssuedTime(newIssuedTime);
            pcs.firePropertyChange(ACCESS_TOKEN_ISSUED_TIME, oldIssuedTime, newIssuedTime);
        }
    }

    public String getManualAccessTokenExpirationTime() {
        return configuration.getManualAccessTokenExpirationTime();
    }

    public void setManualAccessTokenExpirationTime(String newExpirationTime) {
        String oldExpirationTime = configuration.getManualAccessTokenExpirationTime();

        if (oldExpirationTime != newExpirationTime) {
            configuration.setManualAccessTokenExpirationTime(newExpirationTime);
            pcs.firePropertyChange(MANUAL_ACCESS_TOKEN_EXPIRATION_TIME, oldExpirationTime, newExpirationTime);
        }
    }

    public boolean useManualAccessTokenExpirationTime() {
        return configuration.getUseManualAccessTokenExpirationTime();
    }

    public void setUseManualAccessTokenExpirationTime(boolean useManual) {
        boolean oldValue = configuration.getUseManualAccessTokenExpirationTime();

        if (oldValue != useManual) {
            configuration.setUseManualAccessTokenExpirationTime(useManual);
            pcs.firePropertyChange(USE_MANUAL_ACCESS_TOKEN_EXPIRATION_TIME, oldValue, useManual);
        }
    }

    public TimeUnitConfig.Enum getManualAccessTokenExpirationTimeUnit() {
        if (configuration.getManualAccessTokenExpirationTimeUnit() == null) {
            configuration.setManualAccessTokenExpirationTimeUnit(TimeUnitConfig.SECONDS);
        }
        return configuration.getManualAccessTokenExpirationTimeUnit();
    }

    public void setManualAccessTokenExpirationTimeUnit(TimeUnitConfig.Enum newValue) {
        TimeUnitConfig.Enum oldValue = getManualAccessTokenExpirationTimeUnit();

        if (!oldValue.equals(newValue)) {
            configuration.setManualAccessTokenExpirationTimeUnit(newValue);
            pcs.firePropertyChange(MANUAL_ACCESS_TOKEN_EXPIRATION_TIME_UNIT_PROPERTY, oldValue.toString(), newValue.toString());
        }
    }

    public RefreshAccessTokenMethods getRefreshAccessTokenMethod() {
        if (configuration.getRefreshAccessTokenMethod() == null) {
            configuration.setRefreshAccessTokenMethod(RefreshAccessTokenMethodConfig.Enum
                    .forString(RefreshAccessTokenMethods.AUTOMATIC.toString()));
        }
        return RefreshAccessTokenMethods.valueOf(configuration.getRefreshAccessTokenMethod().toString());
    }

    public void setRefreshAccessTokenMethod(RefreshAccessTokenMethods newValue) {
        RefreshAccessTokenMethods oldValue = getRefreshAccessTokenMethod();
        if (!oldValue.equals(newValue)) {
            configuration.setRefreshAccessTokenMethod(RefreshAccessTokenMethodConfig.Enum.forString(newValue.toString()));
            pcs.firePropertyChange(REFRESH_ACCESS_TOKEN_METHOD_PROPERTY, oldValue.toString(), newValue.toString());
        }
    }

    public boolean shouldReloadAccessTokenAutomatically() {
        return getRefreshAccessTokenMethod().equals(AUTOMATIC) && (!StringUtils.isEmpty(getRefreshToken()) ||
                hasAutomationJavaScripts());
    }

    public OAuth2ProfileContainer getContainer() {
        return oAuth2ProfileContainer;
    }

    @Override
    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(oAuth2ProfileContainer.getProject(), this);

        result.extractAndAddAll(CLIENT_ID_PROPERTY);
        result.extractAndAddAll(CLIENT_SECRET_PROPERTY);
        result.extractAndAddAll(AUTHORIZATION_URI_PROPERTY);
        result.extractAndAddAll(ACCESS_TOKEN_URI_PROPERTY);
        result.extractAndAddAll(REDIRECT_URI_PROPERTY);
        result.extractAndAddAll(ACCESS_TOKEN_PROPERTY);
        result.extractAndAddAll(SCOPE_PROPERTY);
        result.extractAndAddAll(MANUAL_ACCESS_TOKEN_EXPIRATION_TIME);

        return result.toArray();
    }

    public List<String> getAutomationJavaScripts() {
        StringListConfig configurationEntry = configuration.getJavaScripts();
        return configurationEntry == null ? Collections.<String>emptyList() : new ArrayList<String>(
                configurationEntry.getEntryList());
    }

    public void setAutomationJavaScripts(List<String> javaScripts) {
        List<String> oldScripts = getAutomationJavaScripts();
        String[] scriptArray = javaScripts.toArray(new String[javaScripts.size()]);
        StringListConfig javaScriptsConfiguration = configuration.getJavaScripts();
        if (javaScriptsConfiguration == null) {
            javaScriptsConfiguration = StringListConfig.Factory.newInstance();
        }
        javaScriptsConfiguration.setEntryArray(scriptArray);
        configuration.setJavaScripts(javaScriptsConfiguration);
        pcs.firePropertyChange(JAVA_SCRIPTS_PROPERTY, oldScripts, javaScripts);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    private void setAccessTokenStartingStatus(@Nonnull AccessTokenStatus startingStatus) {
        Preconditions.checkNotNull(startingStatus);
        configuration.setAccessTokenStartingStatus(AccessTokenStatusConfig.Enum.forString(startingStatus.name()));
    }

    // TODO Make generic
    private AccessTokenStatus getSavedEnum(AccessTokenStatusConfig.Enum persistedEnum) {
        return AccessTokenStatus.valueOf(persistedEnum.toString());
    }
}
