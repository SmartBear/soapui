package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.config.OAuth1ProfileConfig;
import com.eviware.soapui.config.OAuth1ProfileContainerConfig;
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

public class OAuth1ProfileContainer {
    private final WsdlProject project;
    private final OAuth1ProfileContainerConfig configuration;
    private List<OAuth1Profile> OAuth1ProfileList = new ArrayList<OAuth1Profile>();
    private List<OAuth1ProfileListener> listeners = new CopyOnWriteArrayList<OAuth1ProfileListener>();

    public OAuth1ProfileContainer(WsdlProject project, OAuth1ProfileContainerConfig configuration) {
        this.project = project;
        this.configuration = configuration;

        buildOAuth1ProfileList();
    }

    public WsdlProject getProject() {
        return project;
    }

    public List<OAuth1Profile> getOAuth1ProfileList() {
        return OAuth1ProfileList;
    }

    public ArrayList<String> getOAuth1ProfileNameList() {
        ArrayList<String> profileNameList = new ArrayList<String>();
        for (OAuth1Profile profile : getOAuth1ProfileList()) {
            profileNameList.add(profile.getName());
        }
        return profileNameList;
    }

    public OAuth1Profile getProfileByName(String profileName) {
        for (OAuth1Profile profile : getOAuth1ProfileList()) {
            if (profile.getName().equals(profileName)) {
                return profile;
            }
        }
        return null;
    }

    public void renameProfile(String profileOldName, String newName) {
        getProfileByName(profileOldName).setName(newName);
        updateProfileForAllRequests(profileOldName, newName);
        fireOAuth1ProfileRenamed(profileOldName, newName);
    }

    public OAuth1Profile addNewOAuth1Profile(String profileName) {
        OAuth1ProfileConfig profileConfig = configuration.addNewOAuth1Profile();
        profileConfig.setName(profileName);

        OAuth1Profile OAuth1Profile = new OAuth1Profile(this, profileConfig);
        buildOAuth1ProfileList();
        fireOAuth1ProfileAdded(OAuth1Profile);

        return OAuth1Profile;
    }

    public void removeProfile(final String profileName) {
        for (int count = 0; count < configuration.sizeOfOAuth1ProfileArray(); count++) {
            if (configuration.getOAuth1ProfileArray(count).getName().equals(profileName)) {
                configuration.removeOAuth1Profile(count);
                break;
            }
        }
        buildOAuth1ProfileList();
        doForAllRestRequests(new RestRequestCallback() {
            @Override
            public void doit(RestRequest restRequest) {
                if (ObjectUtils.equals(restRequest.getSelectedAuthProfile(), profileName)) {
                    restRequest.setSelectedAuthProfileAndAuthType(CredentialsConfig.AuthType.NO_AUTHORIZATION.toString(), CredentialsConfig.AuthType.NO_AUTHORIZATION);
                }
            }
        });
        fireOAuth1ProfileRemoved(profileName);
    }

    public OAuth1ProfileContainerConfig getConfig() {
        return configuration;
    }

    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(project, this);

        for (OAuth1Profile OAuth1Profile : OAuth1ProfileList) {
            result.addAll(OAuth1Profile.getPropertyExpansions());
        }

        return result.toArray();
    }

    private void buildOAuth1ProfileList() {
        OAuth1ProfileList.clear();
        for (OAuth1ProfileConfig profileConfig : configuration.getOAuth1ProfileList()) {
            OAuth1ProfileList.add(new OAuth1Profile(this, profileConfig));
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

    public void addOAuth1ProfileListener(OAuth1ProfileListener listener) {
        listeners.add(listener);
    }

    public void removeOAuth1ProfileListener(OAuth1ProfileListener listener) {
        listeners.remove(listener);
    }

    private void fireOAuth1ProfileAdded(OAuth1Profile OAuth1Profile) {
        for (OAuth1ProfileListener listener : listeners) {
            listener.profileAdded(OAuth1Profile);
        }
    }

    private void fireOAuth1ProfileRenamed(String profileOldName, String newName) {
        for (OAuth1ProfileListener listener : listeners) {
            listener.profileRenamed(profileOldName, newName);
        }
    }

    private void fireOAuth1ProfileRemoved(String profileName) {
        for (OAuth1ProfileListener listener : listeners) {
            listener.profileRemoved(profileName);
        }
    }

    private interface RestRequestCallback {

        void doit(RestRequest restRequest);

    }
}
