/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.config.TimeUnitConfig;
import com.eviware.soapui.impl.rest.OAuth1Profile;
import com.eviware.soapui.impl.rest.OAuth1ProfileContainer;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.oauth.GoogleOAuth1ClientFacade;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth1ClientFacade;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2ClientFacade;
import com.eviware.soapui.impl.rest.actions.oauth.OltuOAuth2ClientFacade;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.TimeUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.eviware.soapui.config.CredentialsConfig.AuthType.O_AUTH_1_0;
import static com.eviware.soapui.config.CredentialsConfig.AuthType.O_AUTH_2_0;

public class OAuth2RequestFilter extends AbstractRequestFilter {
    private static final int ACCESS_TOKEN_RETRIEVAL_TIMEOUT = 5000;
    // intentionally left non-final to facilitate testing, but should not be modified in production!
    private static Logger log = LogManager.getLogger(OAuth2RequestFilter.class);


	/* setLog() and getLog() should only be used for testing */

    static Logger getLog() {
        return log;
    }

    static void setLog(Logger newLog) {
        log = newLog;
    }

    @Override
    public void filterRestRequest(SubmitContext context, RestRequestInterface request) {

        HttpRequestBase httpMethod = (HttpRequestBase) context.getProperty(BaseHttpRequestTransport.HTTP_METHOD);

        if (O_AUTH_2_0.toString().equals(request.getAuthType())) {
            OAuth2ProfileContainer profileContainer = request.getResource().getService().getProject()
                    .getOAuth2ProfileContainer();
            OAuth2Profile profile = profileContainer.getProfileByName(((AbstractHttpRequest) request).getSelectedAuthProfile());
            if (profile == null || StringUtils.isNullOrEmpty(profile.getAccessToken())) {
                return;
            }
            OAuth2ClientFacade oAuth2Client = getOAuth2ClientFacade();

            if (accessTokenIsExpired(profile)) {
                if (profile.shouldReloadAccessTokenAutomatically()) {
                    reloadAccessToken(profile, oAuth2Client);
                } else {
                    profile.setAccessTokenStatus(OAuth2Profile.AccessTokenStatus.EXPIRED);
                }
            }
            oAuth2Client.applyAccessToken(profile, httpMethod, request.getRequestContent());
        } else if (O_AUTH_1_0.toString().equals(request.getAuthType())) {
            OAuth1ProfileContainer profileContainer = request.getResource().getService().getProject()
                    .getOAuth1ProfileContainer();
            OAuth1Profile profile = profileContainer.getProfileByName(
                    ((AbstractHttpRequest) request).getSelectedAuthProfile());

            if (profile == null || StringUtils.isNullOrEmpty(profile.getAccessToken())) {
                return;
            }
            OAuth1ClientFacade oAuth1Client = getOAuth1ClientFacade();

            oAuth1Client.applyAccessToken(profile, httpMethod, request.getRequestContent());
        }
    }

    protected OAuth2ClientFacade getOAuth2ClientFacade() {
        return new OltuOAuth2ClientFacade();
    }

    protected OAuth1ClientFacade getOAuth1ClientFacade() {
        return new GoogleOAuth1ClientFacade();
    }

    private boolean accessTokenIsExpired(OAuth2Profile profile) {
        long currentTime = TimeUtils.getCurrentTimeInSeconds();
        long issuedTime = profile.getAccessTokenIssuedTime();
        long expirationTime;

        if (profile.useManualAccessTokenExpirationTime()) {
            String expirationTimeString = profile.getManualAccessTokenExpirationTime() == null ? "" : profile.getManualAccessTokenExpirationTime();
            String expandedValue = PropertyExpander.expandProperties(profile.getContainer().getProject(), expirationTimeString);
            expirationTime = convertExpirationTimeToSeconds(expandedValue, profile.getManualAccessTokenExpirationTimeUnit());
        } else {
            expirationTime = profile.getAccessTokenExpirationTime();
        }

        //10 second buffer to make sure that the access token doesn't expire by the time request is sent
        return !(issuedTime <= 0 || expirationTime <= 0) && expirationTime < (currentTime + 10) - issuedTime;
    }

    private long convertExpirationTimeToSeconds(String expirationTimeString, TimeUnitConfig.Enum timeUnit) throws IllegalArgumentException {
        long expirationTime;
        try {
            expirationTime = Long.valueOf(expirationTimeString.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Manual expiration time cannot be parsed due to invalid characters." +
                    "Please review it and make sure it is set correctly.", e);
        }
        if (timeUnit.equals(TimeUnitConfig.HOURS)) {
            return expirationTime * 3600;
        } else if (timeUnit.equals(TimeUnitConfig.MINUTES)) {
            return expirationTime * 60;
        } else {
            return expirationTime;
        }
    }

    private void reloadAccessToken(OAuth2Profile profile, OAuth2ClientFacade oAuth2Client) {
        try {
            if (profile.getRefreshToken() != null) {
                log.info("The access token has expired, trying to refresh it.");
                oAuth2Client.refreshAccessToken(profile);
                log.info("The access token has been refreshed successfully.");
            } else {
                if (profile.hasAutomationJavaScripts()) {
                    log.info("The access token has expired, trying to retrieve a new one with JavaScript automation.");
                    oAuth2Client.requestAccessToken(profile);
                    profile.waitForAccessTokenStatus(OAuth2Profile.AccessTokenStatus.RETRIEVED_FROM_SERVER,
                            ACCESS_TOKEN_RETRIEVAL_TIMEOUT);
                    if (profile.getAccessTokenStatus() == OAuth2Profile.AccessTokenStatus.RETRIEVED_FROM_SERVER) {
                        log.info("A new access token has been retrieved successfully.");
                    } else {
                        log.warn("OAuth2 access token retrieval timed out after " + ACCESS_TOKEN_RETRIEVAL_TIMEOUT + " ms");
                        throw new RuntimeException("OAuth2 access token retrieval timed out after " + ACCESS_TOKEN_RETRIEVAL_TIMEOUT + " ms");
                    }
                } else {
                    log.warn("No automation JavaScripts added to OAuth2 profile – cannot retrieve new access token");
                    throw new RuntimeException("No automation JavaScripts added to OAuth2 profile – cannot retrieve new access token");
                }
            }
        } catch (Exception e) {
            //Propagate it up so that it is shown as a failure message in test case log
            throw new RuntimeException("Unable to refresh expired access token.", e);
        }
    }
}
