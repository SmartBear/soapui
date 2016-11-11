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

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class OAuth2AccessTokenResponseTest {

    public static final String DUMMY_ACCESS_TOKEN = "dummy_access_token";
    public static final String DUMMY_REFRESH_TOKEN = "dummy_refresh_token";
    public static final long EXPIRES_IN = 3600L;
    private OAuth2AccessTokenResponse tokenResponse;

    @Before
    public void setUp() {
        tokenResponse = new OAuth2AccessTokenResponse();
    }

    @Test
    public void readsTokenFromJSONBody() throws OAuthProblemException {
        String jsonBody = "{\n" +
                "  \"access_token\" : \"" + DUMMY_ACCESS_TOKEN + "\",\n" +
                "  \"token_type\" : \"Bearer\",\n" +
                "  \"expires_in\" : " + EXPIRES_IN + ",\n" +
                "  \"refresh_token\" : \"" + DUMMY_REFRESH_TOKEN + "\"\n" +
                "}";

        tokenResponse.setBody(jsonBody);

        assertTokenResponseParameters(tokenResponse);
    }

    @Test
    public void readsTokenFromFormURLEncodedBody() throws OAuthProblemException {
        tokenResponse.setBody(getFormUrlEncodedresponseBody());

        assertTokenResponseParameters(tokenResponse);
    }

    @Test
    public void readsTokenFromFormURLEncodedBodyWithHash() throws OAuthProblemException {
        tokenResponse.setBody("#" + getFormUrlEncodedresponseBody());

        assertTokenResponseParameters(tokenResponse);
    }

    private void assertTokenResponseParameters(OAuth2AccessTokenResponse tokenResponse) {
        assertThat(tokenResponse.getAccessToken(), is(DUMMY_ACCESS_TOKEN));
        assertThat(tokenResponse.getExpiresIn(), is(EXPIRES_IN));
        assertThat(tokenResponse.getRefreshToken(), is(DUMMY_REFRESH_TOKEN));
    }

    private String getFormUrlEncodedresponseBody() {
        return "access_token=" + DUMMY_ACCESS_TOKEN + "&expires_in=" + EXPIRES_IN + "&refresh_token=" +
                DUMMY_REFRESH_TOKEN + "&token_type=Bearer";
    }

}
