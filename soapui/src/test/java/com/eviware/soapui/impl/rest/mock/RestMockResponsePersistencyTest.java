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

package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RestMockResponsePersistencyTest {
    private MockResponse restMockResponse;
    private Integer httpStatusCode = HttpStatus.SC_NOT_FOUND;
    private String projectFileName = "RESTMockResponseTest.xml";
    private String restMockServiceName = "REST Mock Service";

    @Before
    public void setUp() throws XmlException, IOException, SoapUIException {
        RestMockAction restMockAction = ModelItemFactory.makeRestMockAction();
        WsdlProject project = restMockAction.getMockService().getProject();
        restMockAction.getMockService().setName(restMockServiceName);
        restMockResponse = restMockAction.addNewMockResponse("REST Mock Response");
        restMockResponse.setResponseHttpStatus(httpStatusCode);
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
    public void testResponseHttpStatusIsPersisted() throws Exception {
        WsdlProject retrievedProject = new WsdlProject(projectFileName);
        RestMockService restMockService = retrievedProject.getRestMockServiceByName(restMockServiceName);
        RestMockResponse mockResponse = restMockService.getMockOperationAt(0).getMockResponseAt(0);

        assertThat(mockResponse.getResponseHttpStatus(), is(httpStatusCode));

    }


}
