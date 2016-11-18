package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AccessTokenPositionConfig;
import com.eviware.soapui.impl.rest.OAuth1Profile;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.TimeUtils;
import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCallbackUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.auth.oauth.OAuthSigner;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.apache.ApacheHttpTransport;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.oltu.oauth2.common.OAuth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

public class GoogleOAuth1ClientFacade implements OAuth1ClientFacade {
    private final static MessageSupport messages = MessageSupport.getMessages(GoogleOAuth1ClientFacade.class);

    private String tokenSecret;

    @Override
    public void requestAccessToken(OAuth1Profile profile) throws OAuth1Exception {
        try {
            OAuth1Parameters parameters = new OAuth1Parameters(profile);
            OAuthParameterValidator.validate(parameters);
            extractAccessToken(parameters);
        } catch (MalformedURLException | URISyntaxException e) {
            SoapUI.logError(e, messages.get("GoogleOAuth1ClientFacade.Error.WrongURL"));
            throw new OAuth1Exception(e);
        }
    }

    private void extractAccessToken(final OAuth1Parameters parameters) throws URISyntaxException,
            MalformedURLException, OAuth1Exception {
        tokenSecret = null;
        final UserBrowserFacade browserFacade = getBrowserFacade();
        browserFacade.addBrowserListener(new BrowserListenerAdapter() {
            @Override
            public void locationChanged(String newLocation) {
                getAccessTokenAndSaveToProfile(browserFacade, parameters, extractVerifierFromForm(newLocation));
            }

            @Override
            public void browserClosed() {
                super.browserClosed();
                if (!parameters.isAccessTokenRetrievedFromServer()) {
                    setRetrievedCanceledStatus(parameters);
                }
                tokenSecret = null;
            }
        });
        parameters.waitingForAuthorization();

        browserFacade.open(new URI(createAuthorizationURL(parameters)).toURL());
    }

    private void setRetrievedCanceledStatus(OAuth1Parameters parameters) {
        parameters.retrievalCanceled();
    }

    private OAuthCallbackUrl extractVerifierFromForm(String newLocation) {
        return StringUtils.hasContent(newLocation) ? new OAuthCallbackUrl(newLocation) : null;
    }

    private String createAuthorizationURL(OAuth1Parameters parameters) throws OAuth1Exception {
        OAuthGetTemporaryToken temporaryTokenGetter = new OAuthGetTemporaryToken(parameters.temporaryTokenUri) {
            @Override
            public OAuthParameters createParameters() {
                OAuthParameters result = super.createParameters();
                result.version = "1.0";
                return result;
            }
        };
        temporaryTokenGetter.consumerKey = parameters.consumerKey;
        temporaryTokenGetter.signer = getSigner(parameters.consumerSecret, tokenSecret);
        temporaryTokenGetter.callback = parameters.redirectUri;
        temporaryTokenGetter.transport = new ApacheHttpTransport();
        OAuthCredentialsResponse response = null;
        try {
            response = temporaryTokenGetter.execute();
        } catch (IOException e) {
            throw new OAuth1Exception(e);
        }

        OAuthAuthorizeTemporaryTokenUrl authorizeUrl = new OAuthAuthorizeTemporaryTokenUrl(parameters.authorizationUri);
        authorizeUrl.temporaryToken = response.token;
        tokenSecret = response.tokenSecret;
        return authorizeUrl.build();
    }

    private void getAccessTokenAndSaveToProfile(UserBrowserFacade browserFacade, OAuth1Parameters parameters, OAuthCallbackUrl callbackUrl) {
        if (callbackUrl != null && callbackUrl.token != null && callbackUrl.verifier != null) {
            try {
                parameters.receivedAuthorizationCode();
                OAuthGetAccessToken accessTokenGetter = new OAuthGetAccessToken(parameters.accessTokenUri);
                accessTokenGetter.temporaryToken = callbackUrl.token;
                accessTokenGetter.verifier = callbackUrl.verifier;
                accessTokenGetter.consumerKey = parameters.consumerKey;
                accessTokenGetter.signer = getSigner(parameters.consumerSecret, tokenSecret);
                accessTokenGetter.transport = new ApacheHttpTransport();

                OAuthCredentialsResponse response = accessTokenGetter.execute();

                if (response.token != null) {
                    parameters.setAccessTokenInProfile(response.token);
                    parameters.setAccessTokenIssuedTimeInProfile(TimeUtils.getCurrentTimeInSeconds());

                    browserFacade.close();
                }
                parameters.setTokenSecretInProfile(response.tokenSecret);
            } catch (IOException e) {
                SoapUI.logError(e);
            }
        }
    }

    private OAuthSigner getSigner(String clientSharedSecret, String tokenSharedSecret) {
        OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = clientSharedSecret;
        signer.tokenSharedSecret = tokenSharedSecret;
        return signer;
    }


    protected UserBrowserFacade getBrowserFacade() {
        WebViewUserBrowserFacade result = new WebViewUserBrowserFacade();
        return result;
    }

    @Override
    public void applyAccessToken(OAuth1Profile profile, HttpRequestBase request, String requestContent) {
        AccessTokenPositionConfig.Enum i = profile.getAccessTokenPosition();
        if (i == AccessTokenPositionConfig.QUERY) {
            appendAccessTokenToQuery(profile, request);
        } else if (i == AccessTokenPositionConfig.HEADER) {
            appendAccessTokenToHeader(profile, request);
        } else {
            assert false;
        }
    }

    private void appendAccessTokenToQuery(OAuth1Profile profile, HttpRequestBase request) {
        OAuthParameters oAuthParameters = getOAuthParameters(profile, request);
        String queryString = makeQueryStringWithOAuthParameters(oAuthParameters);
        URI oldUri = request.getURI();
        String requestQueryString = oldUri.getQuery() != null ? oldUri.getQuery() + "&" + queryString : queryString;

        try {
            request.setURI(URIUtils.createURI(oldUri.getScheme(), oldUri.getHost(), oldUri.getPort(),
                    oldUri.getRawPath(), requestQueryString, oldUri.getFragment()));
        } catch (URISyntaxException e) {
            SoapUI.logError(e);
        }
    }

    private String makeQueryStringWithOAuthParameters(OAuthParameters oAuthParameters) {
        StringBuilder buf = new StringBuilder();
        appendParameter(buf, "oauth_token", oAuthParameters.token);
        appendParameter(buf, "oauth_consumer_key", oAuthParameters.consumerKey);
        appendParameter(buf, "oauth_nonce", oAuthParameters.nonce);
        appendParameter(buf, "oauth_signature_method", oAuthParameters.signatureMethod);
        appendParameter(buf, "oauth_signature", oAuthParameters.signature);
        appendParameter(buf, "oauth_version", oAuthParameters.version);
        appendParameter(buf, "oauth_timestamp", oAuthParameters.timestamp);
        // hack: we have to remove the extra '&' at the end
        return buf.substring(0, buf.length() - 1);
    }

    private void appendParameter(StringBuilder buf, String name, String value) {
        if (value != null) {
            buf.append(OAuthParameters.escape(name)).append("=").append(OAuthParameters.escape(value)).append("&");
        }
    }

    private void appendAccessTokenToHeader(OAuth1Profile profile, HttpRequestBase request) {
        OAuthParameters oAuthParameters = getOAuthParameters(profile, request);
        request.removeHeaders(OAuth.HeaderType.AUTHORIZATION);
        request.addHeader(OAuth.HeaderType.AUTHORIZATION, oAuthParameters.getAuthorizationHeader());
    }

    private OAuthParameters getOAuthParameters(OAuth1Profile profile, HttpRequestBase request) {
        OAuthParameters oAuthParameters = new OAuthParameters();
        oAuthParameters.consumerKey = profile.getConsumerKey();
        oAuthParameters.token = profile.getAccessToken();
        oAuthParameters.signer = getSigner(profile.getConsumerSecret(), profile.getTokenSecret());
        oAuthParameters.version = "1.0";
        oAuthParameters.computeNonce();
        oAuthParameters.computeTimestamp();
        try {
            oAuthParameters.computeSignature(request.getMethod(), new GenericUrl(request.getURI()));
        } catch (GeneralSecurityException e) {
            SoapUI.logError(e);
        }
        return oAuthParameters;
    }
}
