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

package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitorListenerCallBack;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import org.apache.http.HttpVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TunnelServletUnitTest {

    private TunnelServlet tunnelServlet;

    @Before
    public void setUp() {
        WsdlProject project = Mockito.mock(WsdlProject.class);
        XmlBeansSettingsImpl settings = Mockito.mock(XmlBeansSettingsImpl.class);
        Mockito.when(project.getSettings()).thenReturn(settings);

        String sslEndPoint = "Dummy End point";
        SoapMonitorListenerCallBack listenerCallBack = Mockito.mock(SoapMonitorListenerCallBack.class);
        tunnelServlet = new TunnelServlet(project, sslEndPoint, listenerCallBack);
    }

    @Test
    public void shouldSetHttpProtocolVersion_1_1_ToRequest() {
        ExtendedPostMethod extendedPostMethod = new ExtendedPostMethod();
        tunnelServlet.setProtocolversion(extendedPostMethod, HttpVersion.HTTP_1_1.toString());
        Assert.assertEquals("The copied version doesn't match with the provided version string",
                HttpVersion.HTTP_1_1, extendedPostMethod.getProtocolVersion());
    }

    @Test
    public void shouldSetHttpProtocolVersion_1_0_ToRequest() {
        ExtendedPostMethod extendedPostMethod = new ExtendedPostMethod();
        tunnelServlet.setProtocolversion(extendedPostMethod, HttpVersion.HTTP_1_0.toString());
        Assert.assertEquals("The copied version doesn't match with the provided version string",
                HttpVersion.HTTP_1_0, extendedPostMethod.getProtocolVersion());
    }

}
