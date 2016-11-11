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

import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class OAuth2TestUtils {
    public static String AUTHORIZATION_CODE = "some_code";
    public static String ACCESS_TOKEN = "expected_access_token";
    public static String REFRESH_TOKEN = "expected_refresh_token";

    public static final String OAUTH_2_OOB_URN = "urn:ietf:wg:oauth:2.0:oob";

    public static OAuth2Profile getOAuthProfileWithDefaultValues() throws SoapUIException {
        OAuth2ProfileConfig configuration = OAuth2ProfileConfig.Factory.newInstance();
        OAuth2Profile profile = new OAuth2Profile(ModelItemFactory.makeOAuth2ProfileContainer(), configuration);
        profile.setName("OAuth 2 -Profile");
        profile.setAuthorizationURI("http://localhost:8080/authorize");
        profile.setAccessTokenURI("http://localhost:8080/accesstoken");
        profile.setRedirectURI("http://localhost:8080/redirect");
        profile.setClientID("ClientId");
        profile.setClientSecret("ClientSecret");
        profile.setScope("ReadOnly");
        return profile;
    }
    public static OAuth2Profile getOAuthProfileForROPC() throws SoapUIException {
        OAuth2Profile profile = ModelItemFactory.makeOAuth2Profile();
        profile.setAccessTokenURI("http://localhost:8080/accesstoken");
        profile.setResourceOwnerName("Name");
        profile.setResourceOwnerPassword("Password");
        profile.setClientID("ClientId");
        profile.setClientSecret("ClientSecret");
        profile.setOAuth2Flow(OAuth2Profile.OAuth2Flow.RESOURCE_OWNER_PASSWORD_CREDENTIALS);
        return profile;
    }

    public static OAuth2Profile getOAuth2ProfileWithOnlyAccessToken() throws SoapUIException {
        OAuth2ProfileConfig configuration = OAuth2ProfileConfig.Factory.newInstance();
        OAuth2Profile profileWithOnlyAccessToken = new OAuth2Profile(ModelItemFactory.makeOAuth2ProfileContainer(),
                configuration);
        profileWithOnlyAccessToken.setAccessToken(ACCESS_TOKEN);

        return profileWithOnlyAccessToken;
    }

    public static OAuth2Profile getOAuthProfileWithRefreshToken() throws SoapUIException {
        OAuth2Profile profile = getOAuthProfileWithDefaultValues();
        profile.setRefreshToken("REFRESH#TOKEN");

        return profile;
    }

    public static OAuth2TokenExtractor mockOAuth2TokenExtractor(final OAuth2Profile profile)
            throws OAuthSystemException, MalformedURLException, URISyntaxException, OAuthProblemException {
        OAuth2TokenExtractor oAuth2TokenExtractor = mock(OAuth2TokenExtractor.class);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                profile.setAccessToken(OAuth2TestUtils.ACCESS_TOKEN);
                return profile;
            }
        }).when(oAuth2TokenExtractor).extractAccessToken(any(OAuth2Parameters.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                profile.setAccessToken(OAuth2TestUtils.ACCESS_TOKEN);
                return profile;
            }
        }).when(oAuth2TokenExtractor).refreshAccessToken(any(OAuth2Parameters.class));
        return oAuth2TokenExtractor;
    }

    public static OltuOAuth2ClientFacade getOltuOAuth2ClientFacadeWithMockedTokenExtractor(final OAuth2Profile profile) {
        return new OltuOAuth2ClientFacade() {
            @Override
            protected OAuth2TokenExtractor getOAuth2TokenExtractor() {
                try {
                    return mockOAuth2TokenExtractor(profile);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }
}
