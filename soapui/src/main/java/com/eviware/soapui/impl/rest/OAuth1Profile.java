package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.AccessTokenPositionConfig;
import com.eviware.soapui.config.AccessTokenStatusConfig;
import com.eviware.soapui.config.OAuth1ProfileConfig;
import com.eviware.soapui.config.RefreshAccessTokenMethodConfig;
import com.eviware.soapui.config.TimeUnitConfig;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import static org.opensaml.xml.util.DatatypeHelper.safeTrim;

public class OAuth1Profile implements PropertyExpansionContainer {
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
    public static final String MANUAL_ACCESS_TOKEN_EXPIRATION_TIME_UNIT_PROPERTY = "manualAccessTokenExpirationTimeUnit";
    public static final String TOKEN_SECRET_PROPERTY = "tokenSecret";
    public static final String TEMPORARY_TOKEN_URI_PROPERTY = "temporaryTokenURI";
    public static final String CONSUMER_SECRET_PROPERTY = "consumerSecret";
    public static final String CONSUMER_KEY_PROPERTY = "consumerKey";

    public static final String RESOURCE_OWNER_LOGIN_PROPERTY = "resourceOwnerName";
    public static final String RESOURCE_OWNER_PASSWORD_PROPERTY = "resourceOwnerPassword";
    public static final String TOKEN_SECRET_STATUS_PROPERTY = "tokenSecretStatus";
    private final OAuth1ProfileContainer OAuth1ProfileContainer;
    private final OAuth1ProfileConfig configuration;
    private final PropertyChangeSupport pcs;

    public OAuth1Profile(OAuth1ProfileContainer OAuth1ProfileContainer, OAuth1ProfileConfig configuration) {
        this.OAuth1ProfileContainer = OAuth1ProfileContainer;
        this.configuration = configuration;
        pcs = new PropertyChangeSupport(this);

        setDefaultAccessTokenPosition();
        setDefaultAccessTokenStatus();
    }

    public String getName() {
        if (StringUtils.isEmpty(configuration.getName())) {
            configuration.setName("OAuth 1 - Profile 1");
        }

        return configuration.getName();
    }

    public void setName(String newName) {
        configuration.setName(newName);
    }

    public void waitForAccessTokenStatus(AccessTokenStatusConfig.Enum accessTokenStatus, int timeout) {
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

    public void applyRetrievedAccessToken(String accessToken) {
        // Ignore return value in this case: even if it is not a change, it is important to know that a token has been
        // retrieved from the server
        doSetAccessToken(accessToken);
        setAccessTokenStatus(AccessTokenStatusConfig.RETRIEVED_FROM_SERVER);
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
            setAccessTokenStatus(AccessTokenStatusConfig.ENTERED_MANUALLY);
        }
    }

    public String getConsumerKey() {
        return configuration.getConsumerKey();
    }

    public void setConsumerKey(String consumerKey) {
        String oldValue = configuration.getConsumerKey();
        String newValue = safeTrim(consumerKey);
        if (!StringUtils.equals(oldValue, newValue)) {
            configuration.setConsumerKey(newValue);
            pcs.firePropertyChange(CONSUMER_KEY_PROPERTY, oldValue, newValue);
        }
    }

    public String getTokenSecret() {
        return configuration.getTokenSecret();
    }

    public void setTokenSecret(String tokenSecret) {
        if (doSetTokenSecret(tokenSecret)) {
            setTokenSecretStatus(AccessTokenStatusConfig.ENTERED_MANUALLY);
        }
    }

    public String getTemporaryTokenURI() {
        return configuration.getTemporaryTokenURI();
    }

    public void setTemporaryTokenURI(String temporaryTokenURI) {
        String oldValue = configuration.getTemporaryTokenURI();
        String newValue = safeTrim(temporaryTokenURI);
        if (!StringUtils.equals(oldValue, newValue)) {
            configuration.setTemporaryTokenURI(newValue);
            pcs.firePropertyChange(TEMPORARY_TOKEN_URI_PROPERTY, oldValue, newValue);
        }
    }

    public String getConsumerSecret() {
        return configuration.getConsumerSecret();
    }

    public void setConsumerSecret(String consumerSecret) {
        String oldValue = configuration.getConsumerSecret();
        String newValue = safeTrim(consumerSecret);
        if (!StringUtils.equals(oldValue, newValue)) {
            configuration.setConsumerSecret(newValue);
            pcs.firePropertyChange(CONSUMER_SECRET_PROPERTY, oldValue, newValue);
        }
    }

    public void resetTokenSecretStatusToStartingStatus() {
        setTokenSecretStatus(getTokenSecretStartingStatus());
    }

    public AccessTokenStatusConfig.Enum getTokenSecretStartingStatus() {
        AccessTokenStatusConfig.Enum result = configuration.getTokenSecretStartingStatus();
        if (result == null) {
            result = AccessTokenStatusConfig.UNKNOWN;
        }
        return result;
    }

    public void setTokenSecretStartingStatus(AccessTokenStatusConfig.Enum startingStatus) {
        if (startingStatus == null) {
            startingStatus = AccessTokenStatusConfig.UNKNOWN;
        }
        configuration.setTokenSecretStartingStatus(startingStatus);
    }

    public AccessTokenStatusConfig.Enum getTokenSecretStatus() {
        AccessTokenStatusConfig.Enum result = configuration.getTokenSecretStatus();
        if (result == null) {
            result = AccessTokenStatusConfig.UNKNOWN;
        }
        return result;
    }

    public void setTokenSecretStatus(AccessTokenStatusConfig.Enum newStatus) {
        if (newStatus == null) {
            newStatus = AccessTokenStatusConfig.UNKNOWN;
        }

        AccessTokenStatusConfig.Enum oldStatus = configuration.getTokenSecretStatus();
        if (newStatus == oldStatus) {
            return;
        }

        if (newStatus == AccessTokenStatusConfig.UNKNOWN) {
            if (configuration.isSetTokenSecretStatus()) {
                configuration.unsetTokenSecretStatus();
            }
        } else {
            configuration.setTokenSecretStatus(newStatus);
        }

        boolean isStartingStatus =
                newStatus == AccessTokenStatusConfig.ENTERED_MANUALLY
                        || newStatus == AccessTokenStatusConfig.RETRIEVED_FROM_SERVER
                        || newStatus == AccessTokenStatusConfig.EXPIRED;

        if (isStartingStatus) {
            setTokenSecretStartingStatus(newStatus);
        }

        pcs.firePropertyChange(TOKEN_SECRET_STATUS_PROPERTY, oldStatus, newStatus);
    }

    private boolean doSetAccessToken(String accessToken) {
        String oldValue = configuration.getAccessToken();
        String newValue = accessToken == null ? null : accessToken.trim();
        if (!StringUtils.equals(oldValue, newValue)) {
            configuration.setAccessToken(newValue);
            pcs.firePropertyChange(ACCESS_TOKEN_PROPERTY, oldValue, newValue);
            return true;
        }
        return false;
    }

    public String getAuthorizationURI() {
        return configuration.getAuthorizationURI();
    }

    public void setAuthorizationURI(String authorizationURI) {
        String oldValue = configuration.getAuthorizationURI();
        String newValue = nullSafeTrim(authorizationURI);
        if (!StringUtils.equals(oldValue, newValue)) {
            configuration.setAuthorizationURI(newValue);
            pcs.firePropertyChange(AUTHORIZATION_URI_PROPERTY, oldValue, newValue);
        }
    }

    private String nullSafeTrim(String inputString) {
        return inputString == null ? null : inputString.trim();
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

    public OAuth1ProfileConfig getConfiguration() {
        return configuration;
    }

    public String getAccessTokenURI() {
        return configuration.getAccessTokenURI();
    }

    public void setAccessTokenURI(String accessTokenURI) {
        String oldValue = configuration.getAccessTokenURI();
        String newValue = nullSafeTrim(accessTokenURI);
        if (!StringUtils.equals(oldValue, newValue)) {
            configuration.setAccessTokenURI(newValue);
            pcs.firePropertyChange(ACCESS_TOKEN_URI_PROPERTY, oldValue, newValue);
        }
    }

    public AccessTokenStatusConfig.Enum getAccessTokenStatus() {
        return getSavedAccessTokenStatusEnum(configuration.getAccessTokenStatus());
    }

    public void setAccessTokenStatus(AccessTokenStatusConfig.Enum newStatus) {
        AccessTokenStatusConfig.Enum oldStatus = getSavedAccessTokenStatusEnum(configuration.getAccessTokenStatus());

        if (newStatus == oldStatus) {
            return;
        }

        saveAccessTokenStatusEnum(newStatus, configuration);

        if (isAStartingStatus(newStatus)) {
            setAccessTokenStartingStatus(newStatus);
        }

        pcs.firePropertyChange(ACCESS_TOKEN_STATUS_PROPERTY, oldStatus, newStatus);
    }

    public AccessTokenStatusConfig.Enum getAccessTokenStartingStatus() {
        return getSavedAccessTokenStartingStatusEnum(configuration.getAccessTokenStartingStatus());
    }

    private void setAccessTokenStartingStatus(@Nonnull AccessTokenStatusConfig.Enum startingStatus) {
        Preconditions.checkNotNull(startingStatus);
        saveAccessTokenStartingStatusEnum(startingStatus, configuration);
    }

    public void resetAccessTokenStatusToStartingStatus() {
        setAccessTokenStatus(getAccessTokenStartingStatus());
    }

    public AccessTokenPositionConfig.Enum getAccessTokenPosition() {
        return getSavedAccessTokenPositionEnum(configuration.getAccessTokenPosition());
    }

    public void setAccessTokenPosition(@Nonnull AccessTokenPositionConfig.Enum newAccessTokenPosition) {
        Preconditions.checkNotNull(newAccessTokenPosition);

        AccessTokenPositionConfig.Enum oldAccessTokenPosition = getSavedAccessTokenPositionEnum(configuration.getAccessTokenPosition());

        saveAccessTokenPositionEnum(newAccessTokenPosition, configuration);

        pcs.firePropertyChange(ACCESS_TOKEN_POSITION_PROPERTY, oldAccessTokenPosition, newAccessTokenPosition);
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

    public void setManualAccessTokenExpirationTime(@Nonnull String newExpirationTime) {
        String oldExpirationTime = configuration.getManualAccessTokenExpirationTime();
        if (!Objects.equal(oldExpirationTime, newExpirationTime)) {
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

    public OAuth1ProfileContainer getContainer() {
        return OAuth1ProfileContainer;
    }

    @Override
    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(OAuth1ProfileContainer.getProject(), this);

        result.extractAndAddAll(CLIENT_ID_PROPERTY);
        result.extractAndAddAll(CLIENT_SECRET_PROPERTY);
        result.extractAndAddAll(AUTHORIZATION_URI_PROPERTY);
        result.extractAndAddAll(ACCESS_TOKEN_URI_PROPERTY);
        result.extractAndAddAll(REDIRECT_URI_PROPERTY);
        result.extractAndAddAll(ACCESS_TOKEN_PROPERTY);
        result.extractAndAddAll(SCOPE_PROPERTY);
        result.extractAndAddAll(MANUAL_ACCESS_TOKEN_EXPIRATION_TIME);
        result.extractAndAddAll(RESOURCE_OWNER_LOGIN_PROPERTY);
        result.extractAndAddAll(RESOURCE_OWNER_PASSWORD_PROPERTY);

        return result.toArray();
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

    private boolean isAStartingStatus(AccessTokenStatusConfig.Enum newStatus) {
        return newStatus == AccessTokenStatusConfig.ENTERED_MANUALLY
                || newStatus == AccessTokenStatusConfig.RETRIEVED_FROM_SERVER
                || newStatus == AccessTokenStatusConfig.EXPIRED;
    }

    private void setDefaultAccessTokenPosition() {
        if (getAccessTokenPosition() == null) {
            setAccessTokenPosition(AccessTokenPositionConfig.HEADER);
        }
    }

    private void setDefaultAccessTokenStatus() {
        setAccessTokenStatus(AccessTokenStatusConfig.UNKNOWN);
    }

    private AccessTokenStatusConfig.Enum getSavedAccessTokenStartingStatusEnum(AccessTokenStatusConfig.Enum persistedEnum) {
        return getSavedAccessTokenStatusEnum(persistedEnum);
    }

    private AccessTokenStatusConfig.Enum getSavedAccessTokenStatusEnum(AccessTokenStatusConfig.Enum persistedEnum) {
        if (persistedEnum == null) {
            return AccessTokenStatusConfig.UNKNOWN;
        } else {
            return persistedEnum;
        }
    }

    private AccessTokenPositionConfig.Enum getSavedAccessTokenPositionEnum(AccessTokenPositionConfig.Enum persistedEnum) {
        if (persistedEnum == null) {
            return null;
        } else {
            return persistedEnum;
        }
    }

    private RefreshAccessTokenMethods getSavedRefreshAccessTokenMethodsEnum(RefreshAccessTokenMethodConfig.Enum persistedEnum) {
        if (persistedEnum == null) {
            return null;
        } else {
            return RefreshAccessTokenMethods.valueOf(persistedEnum.toString());
        }
    }

    private void saveAccessTokenStatusEnum(AccessTokenStatusConfig.Enum enumToBePersisted, OAuth1ProfileConfig configuration) {
        configuration.setAccessTokenStatus(enumToBePersisted);
    }

    private void saveAccessTokenStartingStatusEnum(AccessTokenStatusConfig.Enum enumToBePersisted, OAuth1ProfileConfig configuration) {
        configuration.setAccessTokenStartingStatus(enumToBePersisted);
    }

    private void saveAccessTokenPositionEnum(AccessTokenPositionConfig.Enum enumToBePersisted, OAuth1ProfileConfig configuration) {
        configuration.setAccessTokenPosition(enumToBePersisted);
    }

    public void applyRetrievedTokenSecret(String tokenSecret) {
        doSetTokenSecret(tokenSecret);
        setTokenSecretStatus(AccessTokenStatusConfig.RETRIEVED_FROM_SERVER);
    }

    private boolean doSetTokenSecret(String tokenSecret) {
        String oldValue = configuration.getTokenSecret();
        String newValue = tokenSecret == null ? null : tokenSecret.trim();
        if (!StringUtils.equals(oldValue, newValue)) {
            configuration.setTokenSecret(newValue);
            pcs.firePropertyChange(TOKEN_SECRET_PROPERTY, oldValue, newValue);
            return true;
        }
        return false;
    }

    public enum AccessTokenStatus {
        UNKNOWN("Unknown"),
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
        QUERY("Query"),
        HEADER("Header"),
        BODY("Body");

        private String description;

        AccessTokenPosition(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public enum RefreshAccessTokenMethods {
        AUTOMATIC("Automatic"),
        MANUAL("Manual");

        private final String description;

        RefreshAccessTokenMethods(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

}
