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

package com.eviware.soapui.impl.rest.actions.support;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.actions.service.NewRestResourceAction;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.eviware.soapui.utils.CommonMatchers.aCollectionWithSize;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests generic functionality in  NewRestResourceActionBase using an instance of NewRestResourceAction
 */
public class NewRestResourceActionBaseTest {

    public static final String ENDPOINT = "http://sopranos.com";
    private static final String PARENT_RESOURCE_PATH = "godfather";
    private XDialogs originalDialogs;
    private StubbedDialogs stubbedDialogs;
    private RestService service;
    private NewRestResourceAction action;
    private RestResource parentResource;

    @Before
    public void setUp() throws Exception {
        originalDialogs = UISupport.getDialogs();
        stubbedDialogs = new StubbedDialogs();
        UISupport.setDialogs(stubbedDialogs);
        service = ModelItemFactory.makeRestService();
        service.addEndpoint(ENDPOINT);
        parentResource = service.addNewResource("parent", PARENT_RESOURCE_PATH);
        action = new NewRestResourceAction();
    }

    @After
    public void tearDown() throws Exception {
        UISupport.setDialogs(originalDialogs);
    }

    @Test
    public void createsResourceAsChildResourceOfPossibleParent() throws Exception {
        stubbedDialogs.mockConfirmWithReturnValue(true);
        String childResourcePath = "anthony_jr";

        action.createRestResource(service, ENDPOINT + "/" + PARENT_RESOURCE_PATH + "/" + childResourcePath);
        List<RestResource> rootLevelResources = service.getResourceList();
        assertThat(rootLevelResources, is(aCollectionWithSize(1)));
        RestResource newChildResource = rootLevelResources.get(0).getAllChildResources()[0];
        assertThat(newChildResource.getPath(), is(childResourcePath));
    }

    @Test
    public void handlesBasePathWhenFindingPossibleParent() throws Exception {
        String basePath = "/bada_bing/";
        service.setBasePath(basePath);
        stubbedDialogs.mockConfirmWithReturnValue(true);
        String childResourcePath = "anthony_jr";

        action.createRestResource(service, ENDPOINT + parentResource.getFullPath() + "/" + childResourcePath);
        List<RestResource> rootLevelResources = service.getResourceList();
        assertThat(rootLevelResources, is(aCollectionWithSize(1)));
        RestResource newChildResource = rootLevelResources.get(0).getAllChildResources()[0];
        assertThat(newChildResource.getPath(), is(childResourcePath));
    }

    @Test
    public void createsResourceAsRootLevelResourceWhenUserRejects() throws Exception {
        stubbedDialogs.mockConfirmWithReturnValue(false);
        String newResourcePath = "anthony_jr";

        action.createRestResource(service, ENDPOINT + "/" + PARENT_RESOURCE_PATH + "/" + newResourcePath);
        List<RestResource> rootLevelResources = service.getResourceList();
        assertThat(rootLevelResources, is(aCollectionWithSize(2)));
    }

    @Test
    public void showsCorrectConfirmationDialog() throws Exception {
        stubbedDialogs.mockConfirmWithReturnValue(true);
        String childResourcePath = "anthony_jr";

        action.createRestResource(service, ENDPOINT + "/" + PARENT_RESOURCE_PATH + "/" + childResourcePath);
        assertThat(stubbedDialogs.getConfirmations(), is(aCollectionWithSize(1)));
        StubbedDialogs.Confirmation confirmation = stubbedDialogs.getConfirmations().get(0);
        assertThat(confirmation.title, is(NewRestResourceAction.CONFIRM_DIALOG_TITLE));
    }
}
