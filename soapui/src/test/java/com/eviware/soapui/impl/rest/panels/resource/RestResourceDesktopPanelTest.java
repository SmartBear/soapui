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

package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.panels.request.RestRequestDesktopPanel;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JTable;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the RestResourceDesktopPanel class.
 */
public class RestResourceDesktopPanelTest {
    static final String PARAM_2 = "Param2";
    static final String PARAM_3 = "Param3";
    static final String PARAM_1 = "Param1";

    private RestResource parentResource;
    private RestResource childResource;
    private RestResourceDesktopPanel resourceDesktopPanel;

    @Before
    public void setUp() throws Exception {

        parentResource = ModelItemFactory.makeRestResource();
        parentResource.setPath("/parent");

        childResource = parentResource.addNewChildResource("child", "the_child");
        resourceDesktopPanel = new RestResourceDesktopPanel(childResource);
    }

    @Test
    public void displaysFullPathForChildResource() throws Exception {
        assertThat(resourceDesktopPanel.pathTextField.getText(), is(childResource.getFullPath()));
    }

    /**
     * This should be fixed as param location change listener should be present at each level
     *
     * @return
     * @throws SoapUIException
     */
    private RestRequestDesktopPanel openRestRequestDesktopPanelToAttachTheEventListenersForParameterlevelChange()
            throws SoapUIException {
        RestRequest restRequest = ModelItemFactory.makeRestRequest(childResource);
        restRequest.setMethod(RestRequestInterface.HttpMethod.GET);
        return new RestRequestDesktopPanel(restRequest);
    }

    private RestParamsPropertyHolder addParamsToChildResource() {
        RestParamsPropertyHolder params = childResource.getParams();
        params.addProperty(PARAM_1);
        params.addProperty(PARAM_2);
        params.addProperty(PARAM_3);
        return params;
    }

    private JTable getRestParameterTable() {
        return resourceDesktopPanel.getParamsTable().getParamsTable();
    }
}
