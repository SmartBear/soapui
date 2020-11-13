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

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2ClientFacade;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2TestUtils;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.logging.log4j.Logger;
import org.apache.oltu.oauth2.common.OAuth;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static com.eviware.soapui.config.CredentialsConfig.AuthType.O_AUTH_2_0;
import static com.eviware.soapui.config.CredentialsConfig.AuthType.PREEMPTIVE;
import static com.eviware.soapui.utils.CommonMatchers.anEmptyArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OAuth2RequestFilterTest {

    public static final String EXPIRED_TOKEN = "EXPIRED#TOKEN";
    private static final String ACCESS_TOKEN = "ACDFECDSFKJFK#SDFSD8df#ACCESS-TOKEN";
    private static final String RETRIEVED_ACCESS_TOKEN = "yyCDFECDSFKJFK#dsfsddf#28317";

    private OAuth2RequestFilter oAuth2RequestFilter;
    private RestRequest restRequest;
    private ExtendedPostMethod httpRequest;
    private OAuth2ProfileContainer oAuth2ProfileContainer;
    @Mock
    private SubmitContext mockContext;
    @Mock
    private Logger mockLogger;
    private Logger realLogger;
    private OAuth2Profile oAuth2Profile;

    @Before
    public void setUp() throws SoapUIException, URISyntaxException {
        MockitoAnnotations.initMocks(this);

        oAuth2RequestFilter = new OAuth2RequestFilter();
        setupModelItems();
        setupRequest();
        replaceLogger();
    }

    @After
    public void restoreLogger() throws Exception {
        OAuth2RequestFilter.setLog(realLogger);
    }

	/* Tests */

    @Test
    public void appliesAccessToken() throws URISyntaxException {
        String expectedAccessTokenValue = "Bearer " + ACCESS_TOKEN;
        oAuth2RequestFilter.filterRestRequest(mockContext, restRequest);
        assertThat(httpRequest.getHeaders(OAuth.HeaderType.AUTHORIZATION)[0].getValue(), is(expectedAccessTokenValue));
    }

    @Test
    public void doesNotApplyNullAccessTokenToHeader() throws Exception {
        oAuth2Profile.setAccessToken(null);
        oAuth2RequestFilter.filterRestRequest(mockContext, restRequest);
        assertThat(httpRequest.getHeaders(OAuth.HeaderType.AUTHORIZATION), is(anEmptyArray()));
    }

    @Test
    public void doesNotApplyAccessTokenIfOAuthTypeIsNotOAuth2() {
        restRequest.setSelectedAuthProfileAndAuthType(PREEMPTIVE.toString(), PREEMPTIVE);
        oAuth2RequestFilter.filterRestRequest(mockContext, restRequest);
        assertThat(httpRequest.getHeaders(OAuth.HeaderType.AUTHORIZATION), is(anEmptyArray()));
    }

    @Test
    public void automaticallyRefreshAccessTokenIfExpired() throws Exception {
        OAuth2Profile profileWithRefreshToken = setProfileWithRefreshTokenAndExpiredAccessToken();
        oAuth2FilterWithMockOAuth2ClientFacade(profileWithRefreshToken);
        oAuth2RequestFilter.filterRestRequest(mockContext, restRequest);

        String actualAccessTokenHeader = httpRequest.getHeaders((OAuth.HeaderType.AUTHORIZATION))[0].getValue();
        assertThat(actualAccessTokenHeader, is("Bearer " + OAuth2TestUtils.ACCESS_TOKEN));
    }

    @Test
    public void automaticallyRefreshesAccessTokenIfExpired() throws Exception {
        setupProfileWithRefreshToken();

        oAuth2RequestFilter.filterRestRequest(mockContext, restRequest);

        String actualAccessTokenHeader = httpRequest.getHeaders((OAuth.HeaderType.AUTHORIZATION))[0].getValue();
        assertThat(actualAccessTokenHeader, is("Bearer " + OAuth2TestUtils.ACCESS_TOKEN));
    }

    @Test
    public void doesNotRefreshAccessTokenWhenRefreshMethodIsManual() throws SoapUIException {
        OAuth2Profile profileWithRefreshToken = setProfileWithRefreshTokenAndExpiredAccessToken();
        profileWithRefreshToken.setRefreshAccessTokenMethod(OAuth2Profile.RefreshAccessTokenMethods.MANUAL);
        oAuth2FilterWithMockOAuth2ClientFacade(profileWithRefreshToken);
        oAuth2RequestFilter.filterRestRequest(mockContext, restRequest);

        assertThat(profileWithRefreshToken.getAccessToken(), is(EXPIRED_TOKEN));
    }

    private OAuth2Profile setProfileWithRefreshTokenAndExpiredAccessToken() throws SoapUIException {
        final OAuth2Profile profileWithRefreshToken = OAuth2TestUtils.getOAuthProfileWithRefreshToken();
        setExpiredAccessToken(profileWithRefreshToken);

        oAuth2ProfileContainer.getOAuth2ProfileList().set(0, profileWithRefreshToken);
        restRequest.setSelectedAuthProfileAndAuthType(profileWithRefreshToken.getName(), O_AUTH_2_0);
        return profileWithRefreshToken;
    }

    @Test
    public void automaticallyReloadsAccessTokenWhenProfileHasAutomationScripts() throws Exception {
        setupProfileWithAutomationScripts();

        oAuth2RequestFilter.filterRestRequest(mockContext, restRequest);

        String actualAccessTokenHeader = httpRequest.getHeaders((OAuth.HeaderType.AUTHORIZATION))[0].getValue();
        assertThat(actualAccessTokenHeader, is("Bearer " + RETRIEVED_ACCESS_TOKEN));
    }

    @Test
    public void addsLogStatementsWhenRefreshingAccessToken() throws Exception {
        setupProfileWithRefreshToken();

        oAuth2RequestFilter.filterRestRequest(mockContext, restRequest);

        verify(mockLogger, times(2)).info(any(String.class));
    }

    @Test
    public void addsLogStatementsWhenReloadingAccessToken() throws Exception {
        setupProfileWithAutomationScripts();

        oAuth2RequestFilter.filterRestRequest(mockContext, restRequest);

        verify(mockLogger, times(2)).info(any(String.class));
    }

    @Test
    public void refreshAccessTokenIfManualExpirationTimeIsSetAndManualExpirationTimeHasPassed() throws Exception {
        setupProfileWithRefreshToken();
        oAuth2Profile.setAccessTokenExpirationTime(3600);
        oAuth2Profile.setManualAccessTokenExpirationTime("1");
        oAuth2Profile.setUseManualAccessTokenExpirationTime(true);

        oAuth2RequestFilter.filterRestRequest(mockContext, restRequest);

        String actualAccessTokenHeader = httpRequest.getHeaders((OAuth.HeaderType.AUTHORIZATION))[0].getValue();
        assertThat(actualAccessTokenHeader, is("Bearer " + OAuth2TestUtils.ACCESS_TOKEN));
    }

    @Test
    public void doesNotRefreshAccessTokenEvenIfExpiredWhenManualExpirationTimeIsSelected() throws Exception {
        setupProfileWithRefreshToken();
        oAuth2Profile.setAccessTokenIssuedTime(System.currentTimeMillis());
        oAuth2Profile.setAccessTokenExpirationTime(1);        //We use a 10 second buffer when checking for expiration, so this will count as expired.
        oAuth2Profile.setUseManualAccessTokenExpirationTime(true);
        oAuth2Profile.setManualAccessTokenExpirationTime("3600");

        String originalAccessToken = oAuth2Profile.getAccessToken();

        oAuth2RequestFilter.filterRestRequest(mockContext, restRequest);

        assertThat(oAuth2Profile.getAccessToken(), is(originalAccessToken));
    }

	/*
    Setup helpers.
	 */

    private void setupRequest() throws URISyntaxException {
        httpRequest = new ExtendedPostMethod();
        httpRequest.setURI(new URI("endpoint/path"));
        when(mockContext.getProperty(BaseHttpRequestTransport.HTTP_METHOD)).thenReturn(httpRequest);
    }

    private void setupModelItems() throws SoapUIException {
        restRequest = ModelItemFactory.makeRestRequest();

        WsdlProject project = restRequest.getOperation().getInterface().getProject();
        oAuth2ProfileContainer = project.getOAuth2ProfileContainer();
        List<OAuth2Profile> oAuth2ProfileList = oAuth2ProfileContainer.getOAuth2ProfileList();
        if (oAuth2ProfileList.isEmpty()) {
            oAuth2Profile = oAuth2ProfileContainer.addNewOAuth2Profile("OAuth 2 - Profile");
        } else {
            oAuth2Profile = oAuth2ProfileList.get(0);
        }
        restRequest.setSelectedAuthProfileAndAuthType(oAuth2Profile.getName(), O_AUTH_2_0);
        oAuth2Profile.setAccessToken(ACCESS_TOKEN);
    }

    private void replaceLogger() {
        realLogger = OAuth2RequestFilter.getLog();
        OAuth2RequestFilter.setLog(mockLogger);
    }

    private void setupProfileWithRefreshToken() throws SoapUIException {
        final OAuth2Profile profileWithRefreshToken = OAuth2TestUtils.getOAuthProfileWithRefreshToken();
        setExpiredAccessToken(profileWithRefreshToken);
        injectProfile(profileWithRefreshToken);

        oAuth2ProfileContainer.getOAuth2ProfileList().set(0, profileWithRefreshToken);
        oAuth2Profile = profileWithRefreshToken;
        oAuth2FilterWithMockOAuth2ClientFacade(oAuth2Profile);
    }

    private void oAuth2FilterWithMockOAuth2ClientFacade(final OAuth2Profile profileWithRefreshToken) {
        oAuth2RequestFilter = new OAuth2RequestFilter() {
            @Override
            protected OAuth2ClientFacade getOAuth2ClientFacade() {
                return OAuth2TestUtils.getOltuOAuth2ClientFacadeWithMockedTokenExtractor(profileWithRefreshToken);
            }
        };
    }

    private void setupProfileWithAutomationScripts() throws SoapUIException {
        final OAuth2Profile profileWithAutomationScripts = makeProfileWithAutomationScripts();
        setExpiredAccessToken(profileWithAutomationScripts);
        injectProfile(profileWithAutomationScripts);
        oAuth2FilterWithMockOAuth2ClientFacade(profileWithAutomationScripts);
        Runnable browserCallbackSimulator = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignore) {

                }
                profileWithAutomationScripts.applyRetrievedAccessToken(RETRIEVED_ACCESS_TOKEN);
            }
        };
        new Thread(browserCallbackSimulator).start();
    }

    private void injectProfile(final OAuth2Profile profileWithAutomationScripts) {
        oAuth2ProfileContainer.getOAuth2ProfileList().set(0, profileWithAutomationScripts);
        restRequest.setSelectedAuthProfileAndAuthType(profileWithAutomationScripts.getName(),
                CredentialsConfig.AuthType.O_AUTH_2_0);
        oAuth2Profile = profileWithAutomationScripts;
    }

    private OAuth2Profile makeProfileWithAutomationScripts() throws SoapUIException {
        final OAuth2Profile profileWithAutomationScripts = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
        profileWithAutomationScripts.setAutomationJavaScripts(Arrays.asList("doLoginAndConsent()"));
        return profileWithAutomationScripts;
    }

    private void setExpiredAccessToken(OAuth2Profile profileWithRefreshToken) {
        profileWithRefreshToken.setAccessToken(EXPIRED_TOKEN);
        profileWithRefreshToken.setAccessTokenIssuedTime(1);         //Token was issued Jan 1 1970
        profileWithRefreshToken.setAccessTokenExpirationTime(10);      //and expired 10 seconds later.
    }
}
