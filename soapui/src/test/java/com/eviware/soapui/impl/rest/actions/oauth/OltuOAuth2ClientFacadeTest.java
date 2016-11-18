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
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.oltu.oauth2.common.OAuth;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for OltuAuth2ClientFacade
 */
public class OltuOAuth2ClientFacadeTest {

    private OAuth2Profile profile;
    private OAuth2Profile profileWithOnlyAccessToken;
    private OltuOAuth2ClientFacade oltuClientFacade;
    private ExtendedPostMethod httpRequest;

    @Before
    public void setUp() throws Exception {
        profile = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
        profileWithOnlyAccessToken = OAuth2TestUtils.getOAuth2ProfileWithOnlyAccessToken();
        httpRequest = new ExtendedPostMethod();
        httpRequest.setURI(new URI("endpoint/path"));
        oltuClientFacade = OAuth2TestUtils.getOltuOAuth2ClientFacadeWithMockedTokenExtractor(profile);
    }

    @Test
    public void getsTheAccessTokenForAuthorizationCodeGrantFlow() throws OAuth2Exception {
        oltuClientFacade.requestAccessToken(profile);
        assertThat(profile.getAccessToken(), is(OAuth2TestUtils.ACCESS_TOKEN));
    }

    @Test
    public void getsTheAccessTokenForImplicitGrantFlow() throws OAuth2Exception {
        profile.setOAuth2Flow(OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT);
        oltuClientFacade.requestAccessToken(profile);
        assertThat(profile.getAccessToken(), is(OAuth2TestUtils.ACCESS_TOKEN));
    }

    @Test
    public void refreshesAccessToken() throws Exception {
        profile.setAccessToken("expiredAccessToken");
        profile.setRefreshToken(OAuth2TestUtils.REFRESH_TOKEN);
        oltuClientFacade.refreshAccessToken(profile);
        assertThat(profile.getAccessToken(), is(OAuth2TestUtils.ACCESS_TOKEN));
    }

    @Test
    public void appendsAccessTokenToHeader() throws Exception {
        profileWithOnlyAccessToken.setAccessTokenPosition(OAuth2Profile.AccessTokenPosition.HEADER);
        String expectedAccessTokenValue = "Bearer " + profileWithOnlyAccessToken.getAccessToken();
        oltuClientFacade.applyAccessToken(profileWithOnlyAccessToken, httpRequest, "");

        assertThat(httpRequest.getHeaders(OAuth.HeaderType.AUTHORIZATION)[0].getValue(), is(expectedAccessTokenValue));
    }

    @Test
    public void appendsAccessTokenToHeaderByDefault() throws Exception {
        String expectedAccessTokenValue = "Bearer " + profileWithOnlyAccessToken.getAccessToken();
        oltuClientFacade.applyAccessToken(profileWithOnlyAccessToken, httpRequest, "");

        assertThat(httpRequest.getHeaders(OAuth.HeaderType.AUTHORIZATION)[0].getValue(), is(expectedAccessTokenValue));
    }

    @Test
    public void appendsAccessTokenToQuery() throws Exception {
        profileWithOnlyAccessToken.setAccessTokenPosition(OAuth2Profile.AccessTokenPosition.QUERY);
        oltuClientFacade.applyAccessToken(profileWithOnlyAccessToken, httpRequest, "");

        assertThat(httpRequest.getURI().getQuery(), is("access_token=" + profileWithOnlyAccessToken.getAccessToken()));
    }

    @Test
    public void appendsAccessTokenToBody() throws OAuth2Exception, IOException {
        String expectedBodyContent = "access_token=" + profileWithOnlyAccessToken.getAccessToken();
        profileWithOnlyAccessToken.setAccessTokenPosition(OAuth2Profile.AccessTokenPosition.BODY);
        oltuClientFacade.applyAccessToken(profileWithOnlyAccessToken, httpRequest, "");

        StringWriter writer = new StringWriter();
        IOUtils.copy(httpRequest.getEntity().getContent(), writer, "UTF-8");
        String actualContent = writer.toString();

        assertThat(actualContent, is(expectedBodyContent));
    }

/* Validation tests */

    @Test(expected = InvalidOAuthParametersException.class)
    public void rejectsUrnAsAuthorizationURI() throws Exception {
        profile.setAuthorizationURI(OAuth2TestUtils.OAUTH_2_OOB_URN);
        oltuClientFacade.requestAccessToken(profile);
    }

    @Test(expected = InvalidOAuthParametersException.class)
    public void rejectsNonHttpAuthorizationUrl() throws Exception {
        profile.setAuthorizationURI("ftp://ftp.sunet.se");
        oltuClientFacade.requestAccessToken(profile);
    }

    @Test(expected = InvalidOAuthParametersException.class)
    public void rejectsNonUriRedirectUri() throws Exception {
        profile.setRedirectURI("(/&#)!#%/(¤#!");
        oltuClientFacade.requestAccessToken(profile);
    }

    @Test(expected = InvalidOAuthParametersException.class)
    public void rejectsUrnAsAccessTokenURI() throws Exception {
        profile.setAccessTokenURI(OAuth2TestUtils.OAUTH_2_OOB_URN);
        oltuClientFacade.requestAccessToken(profile);
    }

    @Test(expected = InvalidOAuthParametersException.class)
    public void rejectsNonHttpAccessTokenURI() throws Exception {
        profile.setAccessTokenURI("ftp://ftp.sunet.se");
        oltuClientFacade.requestAccessToken(profile);
    }

    @Test(expected = InvalidOAuthParametersException.class)
    public void rejectsEmptyClientId() throws Exception {
        profile.setClientID("");
        oltuClientFacade.requestAccessToken(profile);
    }

    @Test(expected = InvalidOAuthParametersException.class)
    public void rejectsEmptyClientSecret() throws Exception {
        profile.setClientSecret("");
        oltuClientFacade.requestAccessToken(profile);
    }

    @Test(expected = InvalidOAuthParametersException.class)
    public void rejectsEmptyRefreshTokenOnRefresh() throws Exception {
        profile.setRefreshToken("");
        oltuClientFacade.refreshAccessToken(profile);
    }

    @Test(expected = InvalidOAuthParametersException.class)
    public void rejectsEmptyClientIdOnRefresh() throws Exception {
        profile.setRefreshToken("someRefreshToken");
        profile.setClientID("");
        oltuClientFacade.refreshAccessToken(profile);
    }

    @Test(expected = InvalidOAuthParametersException.class)
    public void rejectsEmptyClientSecretOnRefresh() throws Exception {
        profile.setRefreshToken("someRefreshToken");
        profile.setClientSecret("");
        oltuClientFacade.refreshAccessToken(profile);
    }
}
