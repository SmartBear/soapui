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
import com.eviware.soapui.support.SoapUIException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class OAuth2ParametersTest {

    private static String AUTHORIZATION_URI_PROPERTY_NAME = "myAuthorizationURI";
    private static String REDIRECT_URI_PROPERTY_NAME = "myRedirectURI";
    private static String ACCESS_TOKEN_URI_PROPERTY_NAME = "myAccessTokenURI";
    private static String CLIENT_ID_PROPERTY_NAME = "myClientId";
    private static String CLIENT_SECRET_PROPERTY_NAME = "myClientSecret";
    private static String SCOPE_PROPERTY_NAME = "myScope";
    private static String REFRESH_TOKEN_PROPERTY_NAME = "myRefreshToken";

    private OAuth2Profile profile;

    @Before
    public void setUp() throws SoapUIException {
        profile = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
        profile.setRefreshToken("RefreshToken");
    }

    @Test
    public void performsPropertyExpansion() throws Exception {
        WsdlProject project = profile.getContainer().getProject();
        project.addProperty(AUTHORIZATION_URI_PROPERTY_NAME).setValue(profile.getAuthorizationURI());
        project.addProperty(REDIRECT_URI_PROPERTY_NAME).setValue(profile.getRedirectURI());
        project.addProperty(ACCESS_TOKEN_URI_PROPERTY_NAME).setValue(profile.getAccessTokenURI());
        project.addProperty(CLIENT_ID_PROPERTY_NAME).setValue(profile.getClientID());
        project.addProperty(CLIENT_SECRET_PROPERTY_NAME).setValue(profile.getClientSecret());
        project.addProperty(SCOPE_PROPERTY_NAME).setValue(profile.getScope());
        project.addProperty(REFRESH_TOKEN_PROPERTY_NAME).setValue(profile.getRefreshToken());

        profile.setAuthorizationURI("${#Project#" + AUTHORIZATION_URI_PROPERTY_NAME + "}");
        profile.setRedirectURI("${#Project#" + REDIRECT_URI_PROPERTY_NAME + "}");
        profile.setAccessTokenURI("${#Project#" + ACCESS_TOKEN_URI_PROPERTY_NAME + "}");
        profile.setClientID("${#Project#" + CLIENT_ID_PROPERTY_NAME + "}");
        profile.setClientSecret("${#Project#" + CLIENT_SECRET_PROPERTY_NAME + "}");
        profile.setScope("${#Project#" + SCOPE_PROPERTY_NAME + "}");
        profile.setRefreshToken("${#Project#" + REFRESH_TOKEN_PROPERTY_NAME + "}");

        OAuth2Parameters parameters = new OAuth2Parameters(profile);

        assertThat(parameters.authorizationUri, is(project.getPropertyValue(AUTHORIZATION_URI_PROPERTY_NAME)));
        assertThat(parameters.redirectUri, is(project.getPropertyValue(REDIRECT_URI_PROPERTY_NAME)));
        assertThat(parameters.accessTokenUri, is(project.getPropertyValue(ACCESS_TOKEN_URI_PROPERTY_NAME)));
        assertThat(parameters.clientId, is(project.getPropertyValue(CLIENT_ID_PROPERTY_NAME)));
        assertThat(parameters.clientSecret, is(project.getPropertyValue(CLIENT_SECRET_PROPERTY_NAME)));
        assertThat(parameters.scope, is(project.getPropertyValue(SCOPE_PROPERTY_NAME)));
        assertThat(parameters.refreshToken, is(project.getPropertyValue(REFRESH_TOKEN_PROPERTY_NAME)));
    }

    @Test
    public void performsPropertyExpansionInJavaScripts() throws Exception {
        final String userNamePropertyName = "OAuth_User";
        final String userName = "our.google.user";
        WsdlProject project = profile.getContainer().getProject();
        project.addProperty(userNamePropertyName).setValue(userName);
        String javaScriptWithExpansion = "document.getElementById('usr').value='${#Project#" + userNamePropertyName + "}'";
        profile.setAutomationJavaScripts(Arrays.asList(javaScriptWithExpansion));


        OAuth2Parameters parameters = new OAuth2Parameters(profile);

        String expectedExpandedJavaScript = "document.getElementById('usr').value='" + userName + "'";
        assertThat(parameters.getJavaScripts().get(0), is(expectedExpandedJavaScript));
    }


}
