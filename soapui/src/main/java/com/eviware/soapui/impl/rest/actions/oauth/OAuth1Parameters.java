package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.config.AccessTokenStatusConfig;
import com.eviware.soapui.impl.rest.OAuth1Profile;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;

public class OAuth1Parameters {
    final String temporaryTokenUri;
    final String authorizationUri;
    final String redirectUri;
    final String accessTokenUri;
    final String consumerKey;
    final String consumerSecret;
    private final OAuth1Profile profile;

    public OAuth1Parameters(OAuth1Profile profile) {
        this.profile = profile;
        this.temporaryTokenUri = expandProperty(profile, profile.getTemporaryTokenURI());
        this.authorizationUri = expandProperty(profile, profile.getAuthorizationURI());
        this.redirectUri = expandProperty(profile, profile.getRedirectURI());
        this.accessTokenUri = expandProperty(profile, profile.getAccessTokenURI());
        this.consumerKey = expandProperty(profile, profile.getConsumerKey());
        this.consumerSecret = expandProperty(profile, profile.getConsumerSecret());
    }

    void setAccessTokenInProfile(String accessToken) {
        profile.applyRetrievedAccessToken(accessToken);
    }

    void setTokenSecretInProfile(String tokenSecret) {
        profile.applyRetrievedTokenSecret(tokenSecret);
    }

    public void setAccessTokenIssuedTimeInProfile(long issuedTime) {
        profile.setAccessTokenIssuedTime(issuedTime);
    }

    public void waitingForAuthorization() {
        profile.setAccessTokenStatus(AccessTokenStatusConfig.WAITING_FOR_AUTHORIZATION);
    }

    private String expandProperty(OAuth1Profile profile, String value) {
        return PropertyExpander.expandProperties(profile.getContainer().getProject(), value);
    }

    public void receivedAuthorizationCode() {
        profile.setAccessTokenStatus(AccessTokenStatusConfig.RECEIVED_AUTHORIZATION_CODE);
    }

    public void retrievalCanceled() {
        profile.setAccessTokenStatus(AccessTokenStatusConfig.RETRIEVAL_CANCELED);
    }

    public void applyRetrievedAccessToken(String accessToken) {
        profile.applyRetrievedAccessToken(accessToken);
    }

    public boolean isAccessTokenRetrievedFromServer() {
        return profile.getAccessTokenStatus() == AccessTokenStatusConfig.RETRIEVED_FROM_SERVER;
    }
}
