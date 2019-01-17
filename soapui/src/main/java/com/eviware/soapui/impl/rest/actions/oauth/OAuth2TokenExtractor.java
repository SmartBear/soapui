/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.TimeUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.httpclient4.HttpClient4;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OAuth2TokenExtractor {

    public static final String CODE = "code";
    public static final String TITLE = "<TITLE>";
    public static final String TOKEN = "token";
    public static final String ACCESS_TOKEN = "access_token";

    protected List<BrowserListener> browserListeners = new ArrayList<BrowserListener>();

    public void extractAccessToken(final OAuth2Parameters parameters) throws OAuthSystemException, MalformedURLException, URISyntaxException, OAuthProblemException {
        OAuth2Profile.OAuth2Flow i = parameters.getOAuth2Flow();
        if (i.equals(OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT)) {
            extractAccessTokenForImplicitGrantFlow(parameters);

        } else if (i.equals(OAuth2Profile.OAuth2Flow.RESOURCE_OWNER_PASSWORD_CREDENTIALS)) {
            extractAccessTokenForROPC(parameters);

        } else if (i.equals(OAuth2Profile.OAuth2Flow.AUTHORIZATION_CODE_GRANT)) {
            extractAccessTokenForAuthorizationCodeGrantFlow(parameters);

        } else if (i.equals(OAuth2Profile.OAuth2Flow.CLIENT_CREDENTIALS_GRANT)) {
            extractAccessTokenForClientCredentialsGrant(parameters);

        } else {
            throw OAuthProblemException.error("Unsupported OAuth 2.0 grant flow");
        }
    }

    void extractAccessTokenForAuthorizationCodeGrantFlow(final OAuth2Parameters parameters) throws URISyntaxException,
            MalformedURLException, OAuthSystemException {
        final UserBrowserFacade browserFacade = getBrowserFacade();
        addBrowserInteractionHandler(browserFacade, parameters);
        addExternalListeners(browserFacade);
        browserFacade.addBrowserListener(new BrowserListenerAdapter() {
            @Override
            public void locationChanged(String newLocation) {
                getAccessTokenAndSaveToProfile(browserFacade, parameters, extractAuthorizationCodeFromForm(extractFormData(newLocation), CODE));
            }

            @Override
            public void contentChanged(String newContent) {
                int titlePosition = newContent.indexOf(TITLE);
                if (titlePosition != -1) {
                    String title = newContent.substring(titlePosition + TITLE.length(), newContent.indexOf("</TITLE>"));
                    getAccessTokenAndSaveToProfile(browserFacade, parameters, extractAuthorizationCodeFromTitle(title));
                }
            }

            @Override
            public void browserClosed() {
                super.browserClosed();
                if (!parameters.isAccessTokenRetrivedFromServer()) {
                    setRetrievedCanceledStatus(parameters);
                }
            }
        });
        browserFacade.open(new URI(createAuthorizationURL(parameters, CODE)).toURL());
        parameters.waitingForAuthorization();
    }

    void extractAccessTokenForROPC(final OAuth2Parameters parameters) throws OAuthProblemException, OAuthSystemException {
        OAuthClientRequest accessTokenRequest = getClientRequestForROPC(parameters);
        OAuthClient oAuthClient = getOAuthClient();

        OAuthToken oAuthToken = oAuthClient.accessToken(accessTokenRequest, OAuthJSONAccessTokenResponse.class).getOAuthToken();
        parameters.applyRetrievedAccessToken(oAuthToken.getAccessToken());
        parameters.setAccessTokenIssuedTimeInProfile(TimeUtils.getCurrentTimeInSeconds());
        parameters.setAccessTokenExpirationTimeInProfile(oAuthToken.getExpiresIn());
        parameters.setRefreshTokenInProfile(oAuthToken.getRefreshToken());
    }

    public OAuthClientRequest getClientRequestForROPC(final OAuth2Parameters parameters) throws OAuthSystemException {
        OAuthClientRequest accessTokenRequest = OAuthClientRequest
                .tokenLocation(parameters.accessTokenUri)
                .setGrantType(GrantType.PASSWORD)
                .setClientId(parameters.clientId)
                .setClientSecret(parameters.clientSecret)
                .setUsername(parameters.resourceOwnerName)
                .setPassword(parameters.resourceOwnerPassword)
                .setScope(parameters.scope)
                .buildBodyMessage();

        return accessTokenRequest;
    }

    public void extractAccessTokenForClientCredentialsGrant(final OAuth2Parameters parameters) throws OAuthProblemException, OAuthSystemException {
        OAuthClientRequest accessTokenRequest = getClientRequestForClientCredentialsGrant(parameters);
        OAuthClient oAuthClient = getOAuthClient();

        OAuthToken oAuthToken = oAuthClient.accessToken(accessTokenRequest, OAuthJSONAccessTokenResponse.class).getOAuthToken();
        parameters.applyRetrievedAccessToken(oAuthToken.getAccessToken());
        parameters.setAccessTokenIssuedTimeInProfile(TimeUtils.getCurrentTimeInSeconds());
        parameters.setAccessTokenExpirationTimeInProfile(oAuthToken.getExpiresIn());
        parameters.setRefreshTokenInProfile(oAuthToken.getRefreshToken());
    }

    public OAuthClientRequest getClientRequestForClientCredentialsGrant(final OAuth2Parameters parameters) throws OAuthSystemException {
        OAuthClientRequest accessTokenRequest = OAuthClientRequest
                .tokenLocation(parameters.accessTokenUri)
                .setGrantType(GrantType.CLIENT_CREDENTIALS)
                .setClientId(parameters.clientId)
                .setClientSecret(parameters.clientSecret)
                .setScope(parameters.scope)
                .buildBodyMessage();

        return accessTokenRequest;
    }

    void extractAccessTokenForImplicitGrantFlow(final OAuth2Parameters parameters) throws OAuthSystemException,
            URISyntaxException, MalformedURLException {
        final UserBrowserFacade browserFacade = getBrowserFacade();
        addBrowserInteractionHandler(browserFacade, parameters);
        addExternalListeners(browserFacade);
        browserFacade.addBrowserListener(new BrowserListenerAdapter() {
            @Override
            public void locationChanged(String newLocation) {
                String accessToken = extractAuthorizationCodeFromForm(extractFormData(newLocation), ACCESS_TOKEN);
                if (!StringUtils.isNullOrEmpty(accessToken)) {
                    parameters.setAccessTokenInProfile(accessToken);
                    parameters.setRefreshTokenInProfile(null);
                    parameters.setAccessTokenExpirationTimeInProfile(0);
                    parameters.setAccessTokenIssuedTimeInProfile(TimeUtils.getCurrentTimeInSeconds());
                    browserFacade.close();
                }
            }

            @Override
            public void browserClosed() {
                super.browserClosed();
                if (!parameters.isAccessTokenRetrivedFromServer()) {
                    setRetrievedCanceledStatus(parameters);
                }
            }
        });
        browserFacade.open(new URI(createAuthorizationURL(parameters, TOKEN)).toURL());
        parameters.waitingForAuthorization();
    }

    void refreshAccessToken(OAuth2Parameters parameters) throws OAuthProblemException, OAuthSystemException {
        OAuthClientRequest accessTokenRequest = OAuthClientRequest
                .tokenLocation(parameters.accessTokenUri)
                .setGrantType(GrantType.REFRESH_TOKEN)
                .setClientId(parameters.clientId)
                .setClientSecret(parameters.clientSecret)
                .setRefreshToken(parameters.refreshToken)
                .buildBodyMessage();

        OAuthClient oAuthClient = getOAuthClient();

        OAuthToken oAuthToken = oAuthClient.accessToken(accessTokenRequest, OAuthJSONAccessTokenResponse.class).getOAuthToken();
        parameters.applyRetrievedAccessToken(oAuthToken.getAccessToken());
        parameters.setAccessTokenIssuedTimeInProfile(TimeUtils.getCurrentTimeInSeconds());
	// Due to RFC6749 6. the client MUST discard the old refresh token if the server issues a new token
	// and replace it with the new one.
	String newRefreshToken = oAuthToken.getRefreshToken();
	if (newRefreshToken!=null && !newRefreshToken.equals(parameters.refreshToken)) {
           parameters.setRefreshTokenInProfile(newRefreshToken);
	}
    }

    public void addBrowserListener(BrowserListener listener) {
        browserListeners.add(listener);
    }

    protected OAuthClient getOAuthClient() {
        return new OAuthClient(new HttpClient4(HttpClientSupport.getHttpClient()));
    }

    protected UserBrowserFacade getBrowserFacade() {
        return new WebViewUserBrowserFacade();
    }

	/* Helper methods */

    private void setRetrievedCanceledStatus(OAuth2Parameters parameters) {
        parameters.retrivalCanceled();
    }

    private void addExternalListeners(UserBrowserFacade browserFacade) {
        for (BrowserListener browserListener : browserListeners) {
            browserFacade.addBrowserListener(browserListener);
        }
    }

    private void addBrowserInteractionHandler(UserBrowserFacade browserFacade, OAuth2Parameters parameters) {
        if (parameters.getJavaScripts().isEmpty()) {
            return;
        }
        browserFacade.addBrowserListener(new BrowserInteractionMonitor(browserFacade, parameters.getJavaScripts()));
    }

    private String createAuthorizationURL(OAuth2Parameters parameters, String responseType)
            throws OAuthSystemException {
        return OAuthClientRequest
                .authorizationLocation(parameters.authorizationUri)
                .setClientId(parameters.clientId)
                .setResponseType(responseType)
                .setScope(parameters.scope)
                .setRedirectURI(parameters.redirectUri)
                .buildQueryMessage().getLocationUri();
    }

    private String extractFormData(String url) {
        int questionMarkIndex = url.indexOf('?');
        if (questionMarkIndex != -1) {
            return url.substring(questionMarkIndex + 1);
        }

        int hashIndex = url.indexOf("#");
        if (hashIndex != -1) {
            return url.substring(hashIndex + 1);
        }
        return "";
    }

    private String extractAuthorizationCodeFromTitle(String title) {
        if (title.contains("code=")) {
            return title.substring(title.indexOf("code=") + 5);
        }
        return null;
    }

    private String extractAuthorizationCodeFromForm(String formData, String parameterName) {
        return (String) OAuthUtils.decodeForm(formData).get(parameterName);
    }

    private void getAccessTokenAndSaveToProfile(UserBrowserFacade browserFacade, OAuth2Parameters parameters, String authorizationCode) {
        if (authorizationCode != null) {
            try {
                parameters.receivedAuthorizationCode();
                OAuthClientRequest accessTokenRequest = OAuthClientRequest
                        .tokenLocation(parameters.accessTokenUri)
                        .setGrantType(GrantType.AUTHORIZATION_CODE)
                        .setClientId(parameters.clientId)
                        .setClientSecret(parameters.clientSecret)
                        .setRedirectURI(parameters.redirectUri)
                        .setCode(authorizationCode)
                        .buildBodyMessage();
                OAuthToken token = getOAuthClient().accessToken(accessTokenRequest, OAuth2AccessTokenResponse.class)
                        .getOAuthToken();
                if (token != null && token.getAccessToken() != null) {
                    parameters.setAccessTokenInProfile(token.getAccessToken());
                    parameters.setRefreshTokenInProfile(token.getRefreshToken());
                    if (token.getExpiresIn() != null) {
                        parameters.setAccessTokenExpirationTimeInProfile(token.getExpiresIn());
                    }
                    parameters.setAccessTokenIssuedTimeInProfile(TimeUtils.getCurrentTimeInSeconds());

                    browserFacade.close();
                }
            } catch (OAuthSystemException e) {
                SoapUI.logError(e);
            } catch (OAuthProblemException e) {
                SoapUI.logError(e);
            }
        }
    }

	/* Helper class that runs automation JavaScripts registered in the OAuth2 profile */

    private class BrowserInteractionMonitor extends BrowserListenerAdapter {
        private final List<String> javaScripts;
        int pageIndex = 0;
        private UserBrowserFacade browserFacade;

        public BrowserInteractionMonitor(UserBrowserFacade browserFacade, List<String> javaScripts) {
            this.browserFacade = browserFacade;
            this.javaScripts = javaScripts;
        }

        @Override
        public void contentChanged(String newContent) {
            if (javaScripts.size() > pageIndex) {
                String script = javaScripts.get(pageIndex);
                try {
                    browserFacade.executeJavaScript(script);
                } catch (Exception e) {
                    SoapUI.log.warn("Error when running JavaScript [" + script + "]: " + e.getMessage());
                }
                pageIndex++;
            }
        }

    }
}
