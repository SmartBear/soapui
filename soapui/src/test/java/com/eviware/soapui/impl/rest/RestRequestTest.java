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
import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.StringListConfig;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.eviware.soapui.utils.ModelItemMatchers.hasARestParameterNamed;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * Unit tests for RestRequest
 */
public class RestRequestTest {

    private static final String PARAMETER_NAME = "paramName";
    private static final String PARAMETER_VALUE = "paramValue";

    private RestRequest request;


    @Before
    public void setUp() throws Exception {
        request = new RestRequest(ModelItemFactory.makeRestMethod(), RestRequestConfig.Factory.newInstance(), false);
    }

    @Test
    public void migratesAcceptValue() throws Exception {
        RestRequestConfig requestConfig = RestRequestConfig.Factory.newInstance();
        String contentType = "image/jpeg";
        requestConfig.setAccept(contentType);
        request = new RestRequest(ModelItemFactory.makeRestMethod(), requestConfig, false);

        assertThat(request.getRequestHeaders().get(RestRequest.ACCEPT_HEADER_NAME), hasItem(contentType));
        assertThat(requestConfig.getAccept(), is(nullValue()));
    }

    @Test
    public void holdsAndReturnsParameters() {
        addRequestParameter(PARAMETER_NAME, PARAMETER_VALUE);
        assertThat(request, hasARestParameterNamed(PARAMETER_NAME).withValue(PARAMETER_VALUE));
    }

    @Test
    public void retainsParameterValueWhenChangingItsLevel() {
        RestParamProperty parameter = request.getParams().addProperty(PARAMETER_NAME);
        parameter.setValue(PARAMETER_VALUE);
        parameter.setParamLocation(NewRestResourceActionBase.ParamLocation.RESOURCE);
        RestParamProperty returnedParameter = request.getParams().getProperty(PARAMETER_NAME);
        returnedParameter.setParamLocation(NewRestResourceActionBase.ParamLocation.METHOD);

        assertThat(request, hasARestParameterNamed(PARAMETER_NAME).withValue(PARAMETER_VALUE));
    }

    @Test
    public void updatesConfigWhenParameterOrderIsModified() throws Exception {
        addRequestParameter("someName", "someValue");
        String lastParameterName = "otherName";
        addRequestParameter(lastParameterName, "someOtherValue");
        request.getParams().moveProperty(lastParameterName, 0);

        StringListConfig parameterOrder = request.getConfig().getParameterOrder();
        assertThat(parameterOrder, is(notNullValue()));
        assertThat(parameterOrder.getEntryArray(0), is(lastParameterName));
    }

    @Test
    public void selectedProfileIsNoAuthorizationWhenAuthTypeIsNotSet() {
        assertThat(request.getSelectedAuthProfile(), is(CredentialsConfig.AuthType.NO_AUTHORIZATION.toString()));
    }

    @Test
    public void selectedProfileIsAdded() {
        assertThat(request.getSelectedAuthProfile(), is(CredentialsConfig.AuthType.NO_AUTHORIZATION.toString()));
    }


    /* Backward compatibility tests */
    @Test
    public void selectedProfileIsBasicWhenAuthTypeIsPreemptive() {
        setAuthTypeAndSelectedProfile(CredentialsConfig.AuthType.PREEMPTIVE, null);
        assertThat(request.getSelectedAuthProfile(), is(AbstractHttpRequest.BASIC_AUTH_PROFILE));
        assertThat(request.getPreemptive(), is(true));
    }

    @Test
    public void selectedProfileIsBasicWhenAuthTypeIsGlobalHttpSettings() {
        setAuthTypeAndSelectedProfile(CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS, null);
        assertThat(request.getSelectedAuthProfile(), is(AbstractHttpRequest.BASIC_AUTH_PROFILE));
        assertThat(request.getPreemptive(), is(false));
    }

    @Test
    public void selectedProfileIsBasicWhenAuthTypeAndSelectedProfileArePreemptive() {
        setAuthTypeAndSelectedProfile(CredentialsConfig.AuthType.PREEMPTIVE,
                CredentialsConfig.AuthType.PREEMPTIVE.toString());
        assertThat(request.getSelectedAuthProfile(), is(AbstractHttpRequest.BASIC_AUTH_PROFILE));
        assertThat(request.getPreemptive(), is(true));
    }

    @Test
    public void selectedProfileIsBasicWhenAuthTypeAndSelectedProfileAreGlobalHttpSettings() {
        setAuthTypeAndSelectedProfile(CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS,
                CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS.toString());
        assertThat(request.getSelectedAuthProfile(), is(AbstractHttpRequest.BASIC_AUTH_PROFILE));
        assertThat(request.getPreemptive(), is(false));
    }

    @Test
    public void selectedProfileIsNTLMWhenAuthTypeIsNTLM() {
        setAuthTypeAndSelectedProfile(CredentialsConfig.AuthType.NTLM, null);
        assertThat(request.getSelectedAuthProfile(), is(CredentialsConfig.AuthType.NTLM.toString()));
    }

    @Test
    public void selectedProfileIsSpnegoWhenAuthTypeIsSpnegoKerberos() {
        setAuthTypeAndSelectedProfile(CredentialsConfig.AuthType.SPNEGO_KERBEROS, null);
        assertThat(request.getSelectedAuthProfile(), is(CredentialsConfig.AuthType.SPNEGO_KERBEROS.toString()));
    }

    @Test
    public void savesCarriageReturnInBodyCorrectly() throws Exception {
        WsdlProject project = ModelItemFactory.makeWsdlProject();
        RestService restService = (RestService) project.addNewInterface("RestService", RestServiceFactory.REST_TYPE);
        RestResource restResource = restService.addNewResource("Root", "/resource");
        RestMethod restMethod = restResource.addNewMethod("POST");
        RestRequest restRequest = restMethod.addNewRequest("TestRequest");
        String originalContent = "First line\r\nSecond \\rline";
        restRequest.setRequestContent(originalContent);
        File saveFile = File.createTempFile("soapui", "xml");
        saveFile.deleteOnExit();
        project.saveIn(saveFile);
        WsdlProject loadedProject = new WsdlProject(saveFile.getAbsolutePath());
        loadedProject.loadProject(saveFile.toURL());
        RestRequest loadedRequest = (RestRequest) loadedProject.getInterfaceAt(0).getOperationAt(0).getRequestAt(0);
        assertThat(loadedRequest.getRequestContent(), is(originalContent));
    }

    /* Backward compatibility tests end */

    private RestParamProperty addRequestParameter(String name, String value) {
        RestParamProperty parameter = request.getParams().addProperty(name);
        parameter.setValue(value);
        return parameter;
    }

    private void setAuthTypeAndSelectedProfile(CredentialsConfig.AuthType.Enum authType, String selectedProfile) {
        if (selectedProfile != null) {
            request.setSelectedAuthProfileAndAuthType(selectedProfile, authType);
        } else {
            request.getConfig().addNewCredentials();
            request.getConfig().getCredentials().setAuthType(authType);
        }
    }
}
