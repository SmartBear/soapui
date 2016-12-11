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

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class RestRequestStepFactoryTest {
    @Test
    public void copiesRESTRequestBody() throws Exception {
        RestRequest restRequest = ModelItemFactory.makeRestRequest();
        restRequest.setMethod(RestRequestInterface.HttpMethod.POST);
        String requestBody = "Some meaningful data";
        restRequest.setRequestContent(requestBody);

        TestStepConfig testStepConfig = RestRequestStepFactory.createConfig(restRequest, "Rest Request");
        RestRequestStepConfig config = (RestRequestStepConfig) testStepConfig.getConfig();

        assertThat(config.getRestRequest().getRequest().getStringValue(), is(requestBody));
    }
}
