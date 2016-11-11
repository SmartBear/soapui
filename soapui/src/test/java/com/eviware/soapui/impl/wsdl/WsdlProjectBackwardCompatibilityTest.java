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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class WsdlProjectBackwardCompatibilityTest {

    WsdlProject project;

    @Before
    public void setUp() throws URISyntaxException, XmlException, IOException, SoapUIException {

        String fileName = SoapUI.class.getResource("/soapui-projects/BasicMock-soapui-4.6.3-Project.xml").toURI().toString();
        project = new WsdlProject(fileName);
    }

    @Test
    public void verifyBackwardCompatibilityForBasic462ProjectWithMockService() throws XmlException, IOException, SoapUIException, URISyntaxException {

        assertThat(project.getMockServiceCount(), is(2));
        MockService mockService = project.getMockServiceByName("MockService 1");
        MockOperation mockOperation = mockService.getMockOperationByName("ConversionRate");
        MockResponse mockResponse = mockOperation.getMockResponseByName("Response 1");
        assertTrue(mockResponse.getResponseContent().contains("10"));
    }
}
