/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
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

package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Wrapper class that performs property expansion on the values in an OAuth2Profile instance.
 */
public class OAuth2Parameters {

    private final OAuth2Profile profile;

    final String authorizationUri;
    final String redirectUri;
    final String accessTokenUri;
    final String clientId;
    final String clientSecret;
    final String scope;
    final String refreshToken;
    final String resourceOwnerName;
    final String resourceOwnerPassword;

    /**
     * Constructs an OAuth2Parameters object
     *
     * @param profile the profile to be wrapped
     */
    public OAuth2Parameters(OAuth2Profile profile) {
        this.profile = profile;
        this.authorizationUri = expandProperty(profile, profile.getAuthorizationURI());
        this.redirectUri = expandProperty(profile, profile.getRedirectURI());
        this.accessTokenUri = expandProperty(profile, profile.getAccessTokenURI());
        this.clientId = expandProperty(profile, profile.getClientID());
        this.clientSecret = expandProperty(profile, profile.getClientSecret());
        this.scope = expandProperty(profile, profile.getScope());
        this.refreshToken = expandProperty(profile, profile.getRefreshToken());
        this.resourceOwnerName = expandProperty(profile, profile.getResourceOwnerName());
        this.resourceOwnerPassword = expandProperty(profile, profile.getResourceOwnerPassword());
    }

    /**
     * Sets the accessToken property on the wrapped OAuth2Profile instance
     *
     * @param accessToken the access token String
     */
    void setAccessTokenInProfile(String accessToken) {
        profile.applyRetrievedAccessToken(accessToken);
    }

    public void setRefreshTokenInProfile(String refreshToken) {
        profile.setRefreshToken(refreshToken);
    }

    public void setAccessTokenExpirationTimeInProfile(long expirationTime) {
        profile.setAccessTokenExpirationTime(expirationTime);
    }

    public void setAccessTokenIssuedTimeInProfile(long issuedTime) {
        profile.setAccessTokenIssuedTime(issuedTime);
    }

    public void waitingForAuthorization() {
        profile.setAccessTokenStatus(OAuth2Profile.AccessTokenStatus.WAITING_FOR_AUTHORIZATION);
    }

    private String expandProperty(OAuth2Profile profile, String value) {
        return PropertyExpander.expandProperties(profile.getContainer().getProject(), value);
    }

    public void receivedAuthorizationCode() {
        profile.setAccessTokenStatus(OAuth2Profile.AccessTokenStatus.RECEIVED_AUTHORIZATION_CODE);
    }

    public void retrivalCanceled() {
        profile.setAccessTokenStatus(OAuth2Profile.AccessTokenStatus.RETRIEVAL_CANCELED);
    }

    public void applyRetrievedAccessToken(String accessToken) {
        profile.applyRetrievedAccessToken(accessToken);
    }

    public OAuth2Profile.OAuth2Flow getOAuth2Flow() {
        return profile.getOAuth2Flow();
    }

    public List<String> getJavaScripts() {
        WsdlProject project = profile.getContainer().getProject();
        return Lists.transform(profile.getAutomationJavaScripts(), new PropertyExpansionFunction(project));
    }

    public boolean isAccessTokenRetrivedFromServer() {
        return profile.getAccessTokenStatus() == OAuth2Profile.AccessTokenStatus.RETRIEVED_FROM_SERVER;
    }
}
