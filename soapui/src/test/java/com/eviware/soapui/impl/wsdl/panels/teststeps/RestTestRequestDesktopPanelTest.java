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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.config.RestMethodConfig;
import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.utils.ContainerWalker;
import com.eviware.soapui.utils.StatefulModelItemFactory;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JComboBox;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RestTestRequestDesktopPanelTest {

    public static final String PARAMETER_NAME = "jsessionid";
    public static final String PARAMETER_VALUE = "Da Value";
    public static final String ENDPOINT = "http://sunet.se/search";

    private RestTestRequestDesktopPanel restTestDesktopPanel;
    private RestRequest restRequest;
    private StubbedDialogs dialogs;
    private XDialogs originalDialogs;
    private JComboBox endpointsCombo;

    @Before
    public void setUp() throws Exception {
        buildRestTestPanel();
    }

    @After
    public void resetDialogs() {
        UISupport.setDialogs(originalDialogs);
    }

    @Test
    public void displaysEndpoint() {
        assertThat(restTestDesktopPanel.getEndpointsModel().getSelectedItem(), is((Object) ENDPOINT));
    }

    @Test
    public void reactsToEndpointChanges() {
        String anotherEndpoint = "http://google.com/search";
        restService().changeEndpoint(ENDPOINT, anotherEndpoint);
        assertThat(restTestDesktopPanel.getEndpointsModel().getSelectedItem(), is((Object) anotherEndpoint));
    }

    @Test
    public void displaysFullResourcePathInPathLabel() {
        String expectedPath = "[" + restRequest.getResource().getFullPath() + "]";
        assertThat(restTestDesktopPanel.pathLabel.getText(), is(expectedPath));
    }

    @Test
    public void displaysChangedPath() {
        restRequest.getResource().setPath("/newPath");
        String expectedPath = "[" + restRequest.getResource().getFullPath() + "]";
        assertThat(restTestDesktopPanel.pathLabel.getText(), is(expectedPath));
    }

    @Test
    public void displaysResourceMethodCombo() {
        Object expectedMethodResourceCombo = restRequest.getResource().getRestMethodByName("Get");
        assertThat(restTestDesktopPanel.methodResourceCombo.getSelectedItem(), is(expectedMethodResourceCombo));
    }

	/* Helpers */

    private JComboBox findEndpointsComboBox()

    {
        ContainerWalker finder = new ContainerWalker(restTestDesktopPanel);
        return finder.findComboBoxWithValue(ENDPOINT);
    }

    private RestService restService() {
        return restRequest.getOperation().getInterface();
    }

    private void buildRestTestPanel() throws SoapUIException, RestRequestStepFactory.ItemDeletedException {
        StatefulModelItemFactory modelItemFactory = new StatefulModelItemFactory();
        RestResource restResource = createRestRequestModel(modelItemFactory);
        mockPromptDiaglog(restResource);
        RestTestRequestStep testStep = createRestTestReqStep(modelItemFactory, restResource);
        restTestDesktopPanel = new RestTestRequestDesktopPanel(testStep);
        endpointsCombo = findEndpointsComboBox();
    }

    private RestResource createRestRequestModel(StatefulModelItemFactory modelItemFactory) throws SoapUIException {
        restRequest = modelItemFactory.makeRestRequest();
        restRequest.setMethod(RestRequestInterface.HttpMethod.GET);
        RestResource restResource = restRequest.getResource();
        restResource.getParams().addProperty(PARAMETER_NAME);
        restResource.addNewMethod(restRequest.getRestMethod().getName());
        RestMethodConfig restMethodConfig = restResource.getRestMethodList().get(0).getConfig();
        restMethodConfig.setMethod("GET");

        restService().addEndpoint(ENDPOINT);
        RestParamProperty restParamProperty = restRequest.getParams().getProperty(PARAMETER_NAME);
        restParamProperty.setValue(PARAMETER_VALUE);
        return restResource;
    }

    private RestTestRequestStep createRestTestReqStep(StatefulModelItemFactory modelItemFactory, RestResource restResource) throws RestRequestStepFactory.ItemDeletedException, SoapUIException {
        TestStepConfig config = TestStepConfig.Factory.newInstance();
        RestRequestStepConfig requestStepConfig = RestRequestStepConfig.Factory.newInstance();
        RestRequestConfig restRequestConfig = RestRequestConfig.Factory.newInstance();

        requestStepConfig.setMethodName(restRequest.getRestMethod().getName());
        requestStepConfig.setService(restService().getName());

        requestStepConfig.setRestRequest(restRequestConfig);
        requestStepConfig.getRestRequest().setEndpoint(ENDPOINT);
        config.setConfig(requestStepConfig);
        requestStepConfig.setResourcePath(restResource.getFullPath());

        return new RestTestRequestStep(modelItemFactory.makeTestCase(), config, false);
    }

    private void mockPromptDiaglog(RestResource restResource) {
        originalDialogs = UISupport.getDialogs();
        dialogs = new StubbedDialogs();
        UISupport.setDialogs(dialogs);
        String serviceName = restResource.getService().getName();
        String resourceName = restResource.getName();
        dialogs.mockPromptWithReturnValue(serviceName + " > " + resourceName);
    }

}
