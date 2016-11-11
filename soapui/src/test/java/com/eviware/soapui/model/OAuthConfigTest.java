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

package com.eviware.soapui.model;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class OAuthConfigTest {
    public static final String PROFILE_NAME = "Profile";
    private OAuth2Profile oAuth2Profile;
    private String projectFileName = "OAuthTestProject.xml";

    @Before
    public void setUp() throws XmlException, IOException, SoapUIException {
        WsdlProject project = createNewProjectWithRESTInterface();

        oAuth2Profile = project.getOAuth2ProfileContainer().addNewOAuth2Profile(PROFILE_NAME);
        oAuth2Profile.setClientID("google");
        oAuth2Profile.setAccessTokenURI("http://google.com/accessTokenURI");
        oAuth2Profile.setAuthorizationURI("http://google.com/auth");
        oAuth2Profile.setClientSecret("XYSDKMLL");
        oAuth2Profile.setAccessToken("ACDFECDSFKJFK#SDFSD8df");
        oAuth2Profile.setScope("google.com/calendar/read");

        project.saveAs(projectFileName);
    }

    @After
    public void tearDown() {
        File file = new File(projectFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void theProfileListIsEmptyByDefault() throws XmlException, IOException, SoapUIException {
        WsdlProject project = createNewProjectWithRESTInterface();

        assertThat(project.getOAuth2ProfileContainer().getOAuth2ProfileList(), is(empty()));
    }

    @Test
    public void savesProfileWithName() {
        assertThat(oAuth2Profile.getName(), is(PROFILE_NAME));
    }


    @Test
    public void basicOAuthConfigIsProjectSpecific() throws Exception {
        WsdlProject retrievedProject = new WsdlProject(projectFileName);

        assertThat(retrievedProject.getOAuth2ProfileContainer().getOAuth2ProfileList().size(), is(1));
        OAuth2Profile savedOAuth2Profile = retrievedProject.getOAuth2ProfileContainer().getOAuth2ProfileList().get(0);

        assertOAuth2ProfileFields(savedOAuth2Profile);

    }

    @Test
    public void basicOAuthConfigIsSaved() throws Exception {
        WsdlProject retrievedProject = new WsdlProject(projectFileName);

        assertThat(retrievedProject.getOAuth2ProfileContainer().getOAuth2ProfileList().size(), is(1));
        OAuth2Profile savedOAuth2Profile = retrievedProject.getOAuth2ProfileContainer().getOAuth2ProfileList().get(0);

        assertOAuth2ProfileFields(savedOAuth2Profile);
    }

    private void assertOAuth2ProfileFields(OAuth2Profile savedOAuth2Profile) {
        assertThat(savedOAuth2Profile.getName(), is(oAuth2Profile.getName()));
        assertThat(savedOAuth2Profile.getClientID(), is(oAuth2Profile.getClientID()));
        assertThat(savedOAuth2Profile.getAccessTokenURI(), is(oAuth2Profile.getAccessTokenURI()));
        assertThat(savedOAuth2Profile.getAuthorizationURI(), is(oAuth2Profile.getAuthorizationURI()));
        assertThat(savedOAuth2Profile.getClientSecret(), is(oAuth2Profile.getClientSecret()));
        assertThat(savedOAuth2Profile.getAccessToken(), is(oAuth2Profile.getAccessToken()));
        assertThat(savedOAuth2Profile.getScope(), is(oAuth2Profile.getScope()));
        assertThat(savedOAuth2Profile.getResourceOwnerName(), is(oAuth2Profile.getResourceOwnerName()));
        assertThat(savedOAuth2Profile.getResourceOwnerPassword(), is(oAuth2Profile.getResourceOwnerPassword()));
    }

    private WsdlProject createNewProjectWithRESTInterface() throws XmlException, IOException, SoapUIException {
        WsdlProject project = new WsdlProject();

        RestService restService = (RestService) project.addNewInterface("Test", RestServiceFactory.REST_TYPE);
        restService.addNewResource("Resource", "/test");
        return project;
    }
}
