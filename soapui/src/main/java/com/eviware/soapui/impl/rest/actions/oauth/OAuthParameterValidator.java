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
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.editor.inspectors.auth.OAuth1GetTokenForm;
import com.eviware.soapui.support.editor.inspectors.auth.OAuth2GetAccessTokenForm;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 */
public class OAuthParameterValidator {

    static void validate(OAuth2Parameters parameters) {
        if (parameters.getOAuth2Flow() == OAuth2Profile.OAuth2Flow.RESOURCE_OWNER_PASSWORD_CREDENTIALS) {
            validateRequiredStringValue(parameters.resourceOwnerName, OAuth2GetAccessTokenForm.RESOURCE_OWNER_LOGIN);
            validateRequiredStringValue(parameters.resourceOwnerName, OAuth2GetAccessTokenForm.RESOURCE_OWNER_PASSWORD);
        }

        validateRequiredStringValue(parameters.clientId, OAuth2GetAccessTokenForm.CLIENT_ID_TITLE);
        if (parameters.getOAuth2Flow() != OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT) {
            validateRequiredStringValue(parameters.clientSecret, OAuth2GetAccessTokenForm.CLIENT_SECRET_TITLE);
            validateHttpUrl(parameters.accessTokenUri, OAuth2GetAccessTokenForm.ACCESS_TOKEN_URI_TITLE);
        }

        if (parameters.getOAuth2Flow() != OAuth2Profile.OAuth2Flow.RESOURCE_OWNER_PASSWORD_CREDENTIALS &&
                parameters.getOAuth2Flow() != OAuth2Profile.OAuth2Flow.CLIENT_CREDENTIALS_GRANT) {
            validateHttpUrl(parameters.authorizationUri, OAuth2GetAccessTokenForm.AUTHORIZATION_URI_TITLE);
            validateUri(parameters.redirectUri, OAuth2GetAccessTokenForm.REDIRECT_URI_TITLE);
        }
    }

    static void validate(OAuth1Parameters parameters) {
        validateRequiredStringValue(parameters.consumerKey, OAuth1GetTokenForm.CONSUMER_KEY_TITLE);
        validateRequiredStringValue(parameters.consumerSecret, OAuth1GetTokenForm.CONSUMER_SECRET_TITLE);
        validateHttpUrl(parameters.temporaryTokenUri, OAuth1GetTokenForm.TEMPORARY_TOKEN_URI_TITLE);
        validateHttpUrl(parameters.authorizationUri, OAuth1GetTokenForm.AUTHORIZATION_URI_TITLE);
        validateHttpUrl(parameters.accessTokenUri, OAuth1GetTokenForm.ACCESS_TOKEN_URI_TITLE);
        validateUri(parameters.redirectUri, OAuth1GetTokenForm.REDIRECT_URI_TITLE);
    }

    private static void validateUri(String uri, String uriName) {
        if (!StringUtils.hasContent(uri)) {
            throw new InvalidOAuthParametersException(uri + " is not a valid " + uriName);
        }

        try {
            new URI(uri);
        } catch (URISyntaxException e) {
            throw new InvalidOAuthParametersException(uri + " is not a valid " + uriName);
        }
    }

    private static void validateHttpUrl(String authorizationUri, String uriName) {
        if (!isValidHttpUrl(authorizationUri)) {
            throw new InvalidOAuthParametersException(uriName + " " + authorizationUri + " is not a valid HTTP URL");
        }
    }

    private static boolean isValidHttpUrl(String authorizationUri) {
        if (!StringUtils.hasContent(authorizationUri)) {
            return false;
        }
        try {
            URL url = new URL(authorizationUri);
            return url.getProtocol().startsWith("http");
        } catch (MalformedURLException e) {
            return false;
        }
    }

    static void validateRequiredStringValue(String value, String propertyName) {
        if (!StringUtils.hasContent(value)) {
            throw new InvalidOAuthParametersException(propertyName + " is empty");
        }
    }

}
