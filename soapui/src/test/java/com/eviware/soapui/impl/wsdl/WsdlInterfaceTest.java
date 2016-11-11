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

import com.eviware.soapui.config.WsdlInterfaceConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WsdlInterfaceTest {

    private WsdlProject project;
    private WsdlInterfaceConfig interfaceConfig;
    private WsdlInterface iface;

    @Before
    public void setUp() throws Exception {
        project = new WsdlProject();
        interfaceConfig = WsdlInterfaceConfig.Factory.newInstance();
        iface = new WsdlInterface(project, interfaceConfig);

        assertEquals(0, iface.getEndpoints().length);
    }

    @Test
    public void testAddEndpoints() throws Exception {
        iface.addEndpoint("testEndpoint");
        assertEquals(1, iface.getEndpoints().length);
        assertEquals("testEndpoint", iface.getEndpoints()[0]);

        iface.addEndpoint("testEndpoint");
        assertEquals(1, iface.getEndpoints().length);
        assertEquals("testEndpoint", iface.getEndpoints()[0]);

        iface.addEndpoint("testEndpoint2");
        assertEquals(2, iface.getEndpoints().length);
        assertEquals("testEndpoint", iface.getEndpoints()[0]);
        assertEquals("testEndpoint2", iface.getEndpoints()[1]);
    }

    @Test
    public void testRemoveEndpoints() throws Exception {
        iface.addEndpoint("testEndpoint");
        iface.addEndpoint("testEndpoint2");

        iface.removeEndpoint("testEndpoint");
        assertEquals(1, iface.getEndpoints().length);

        iface.removeEndpoint("testEndpoint2");
        assertEquals(0, iface.getEndpoints().length);
    }
}
