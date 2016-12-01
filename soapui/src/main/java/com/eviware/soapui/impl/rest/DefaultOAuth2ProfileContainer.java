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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.config.OAuth2ProfileContainerConfig;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultOAuth2ProfileContainer implements OAuth2ProfileContainer {
    private final WsdlProject project;
    private final OAuth2ProfileContainerConfig configuration;
    private List<OAuth2Profile> oAuth2ProfileList = new ArrayList<OAuth2Profile>();
    private List<OAuth2ProfileListener> listeners = new CopyOnWriteArrayList<OAuth2ProfileListener>();

    public DefaultOAuth2ProfileContainer(WsdlProject project, OAuth2ProfileContainerConfig configuration) {
        this.project = project;
        this.configuration = configuration;

        buildOAuth2ProfileList();
    }

    @Override
    public WsdlProject getProject() {
        return project;
    }

    @Override
    public List<OAuth2Profile> getOAuth2ProfileList() {
        return oAuth2ProfileList;
    }

    @Override
    public ArrayList<String> getOAuth2ProfileNameList() {
        ArrayList<String> profileNameList = new ArrayList<String>();
        for (OAuth2Profile profile : getOAuth2ProfileList()) {
            profileNameList.add(profile.getName());
        }
        return profileNameList;
    }

    @Override
    public OAuth2Profile getProfileByName(String profileName) {
        for (OAuth2Profile profile : getOAuth2ProfileList()) {
            if (profile.getName().equals(profileName)) {
                return profile;
            }
        }
        return null;
    }

    @Override
    public void renameProfile(String profileOldName, String newName) {
        getProfileByName(profileOldName).setName(newName);
        updateProfileForAllRequests(profileOldName, newName);
        fireOAuth2ProfileRenamed(profileOldName, newName);
    }

    @Override
    public void release() {
        //FIXME: Add implementation when we implement the GUI with listeners
    }

    @Override
    public OAuth2Profile addNewOAuth2Profile(String profileName) {
        OAuth2ProfileConfig profileConfig = configuration.addNewOAuth2Profile();
        profileConfig.setName(profileName);

        OAuth2Profile oAuth2Profile = new OAuth2Profile(this, profileConfig);
        buildOAuth2ProfileList();
        fireOAuth2ProfileAdded(oAuth2Profile);

        return oAuth2Profile;
    }

    @Override
    public void removeProfile(final String profileName) {
        for (int count = 0; count < configuration.sizeOfOAuth2ProfileArray(); count++) {
            if (configuration.getOAuth2ProfileArray(count).getName().equals(profileName)) {
                configuration.removeOAuth2Profile(count);
                break;
            }
        }
        buildOAuth2ProfileList();
        doForAllRestRequests(new RestRequestCallback() {
            @Override
            public void doit(RestRequest restRequest) {
                if (ObjectUtils.equals(restRequest.getSelectedAuthProfile(), profileName)) {
                    restRequest.setSelectedAuthProfileAndAuthType(CredentialsConfig.AuthType.NO_AUTHORIZATION.toString(), CredentialsConfig.AuthType.NO_AUTHORIZATION);
                }
            }
        });
        fireOAuth2ProfileRemoved(profileName);
    }

    @Override
    public OAuth2ProfileContainerConfig getConfig() {
        return configuration;
    }


    @Override
    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(project, this);

        for (OAuth2Profile oAuth2Profile : oAuth2ProfileList) {
            result.addAll(oAuth2Profile.getPropertyExpansions());
        }

        return result.toArray();
    }

    private void buildOAuth2ProfileList() {
        oAuth2ProfileList.clear();
        for (OAuth2ProfileConfig profileConfig : configuration.getOAuth2ProfileList()) {
            oAuth2ProfileList.add(new OAuth2Profile(this, profileConfig));
        }
    }

    private void updateProfileForAllRequests(final String profileOldName, final String newName) {
        doForAllRestRequests(new RestRequestCallback() {
            @Override
            public void doit(RestRequest restRequest) {
                if (ObjectUtils.equals(restRequest.getSelectedAuthProfile(), profileOldName)) {
                    restRequest.setSelectedAuthProfileAndAuthType(newName, CredentialsConfig.AuthType.Enum.forString(restRequest.getAuthType()));
                }
            }
        });
    }

    private void doForAllRestRequests(RestRequestCallback callback) {
        for (Interface iface : project.getInterfaceList()) {
            if (iface instanceof RestService) {
                for (RestResource restResource : ((RestService) iface).getAllResources()) {
                    for (RestMethod restMethod : restResource.getRestMethodList()) {
                        for (RestRequest restRequest : restMethod.getRequestList()) {
                            callback.doit(restRequest);
                        }
                    }
                }
            }
        }
        for (TestSuite testSuite : project.getTestSuiteList()) {
            for (TestCase testCase : testSuite.getTestCaseList()) {
                for (RestTestRequestStep restTestRequestStep : testCase.getTestStepsOfType(RestTestRequestStep.class)) {
                    callback.doit(restTestRequestStep.getTestRequest());
                }
            }
        }
    }

    @Override
    public void addOAuth2ProfileListener(OAuth2ProfileListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeOAuth2ProfileListener(OAuth2ProfileListener listener) {
        listeners.remove(listener);
    }

    private void fireOAuth2ProfileAdded(OAuth2Profile oAuth2Profile) {
        for (OAuth2ProfileListener listener : listeners) {
            listener.profileAdded(oAuth2Profile);
        }
    }

    private void fireOAuth2ProfileRenamed(String profileOldName, String newName) {
        for (OAuth2ProfileListener listener : listeners) {
            listener.profileRenamed(profileOldName, newName);
        }
    }

    private void fireOAuth2ProfileRemoved(String profileName) {
        for (OAuth2ProfileListener listener : listeners) {
            listener.profileRemoved(profileName);
        }
    }

    private interface RestRequestCallback {

        void doit(RestRequest restRequest);

    }
}
