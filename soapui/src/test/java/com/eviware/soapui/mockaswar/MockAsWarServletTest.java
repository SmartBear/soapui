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

package com.eviware.soapui.mockaswar;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.model.mock.MockRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.eviware.soapui.utils.MockedServlet.mockHttpServletRequest;
import static com.eviware.soapui.utils.MockedServlet.mockHttpServletResponse;
import static com.eviware.soapui.utils.MockedServlet.stubbedServletContext;
import static com.eviware.soapui.utils.ResourceUtils.getFilePathFromResource;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MockAsWarServletTest {


    private StubMockAsWarServlet stubMockAsWarServlet;
    private HttpServletRequest reqeust;
    private HttpServletResponse response;

    @Before
    public void setUp() throws ServletException, IOException {
        stubMockAsWarServlet = new StubMockAsWarServlet();
        stubMockAsWarServlet.init();

        reqeust = mockHttpServletRequest();
        response = mockHttpServletResponse();
    }

    @After
    public void tearDown() {
        stubMockAsWarServlet.destroy();

    }

    @Test
    public void shouldStartAllMockServices() throws ServletException, IOException {

        int totalMockService = stubMockAsWarServlet.project.getRestMockServiceCount() + stubMockAsWarServlet.project.getMockServiceCount();
        stubMockAsWarServlet.service(reqeust, response);

        MockRunner[] mockRunners = ((MockAsWarServlet.MockServletSoapUICore) stubMockAsWarServlet.getMockServletCore()).getMockRunners();
        assertThat(mockRunners.length, is(totalMockService));
    }

    @Test
    public void shouldWriteResponse() throws Exception {

        String path = "/numeric/numericpath";
        when(reqeust.getPathInfo()).thenReturn(path);
        when(reqeust.getMethod()).thenReturn(RestRequestInterface.HttpMethod.GET.name());

        stubMockAsWarServlet.service(reqeust, response);

        verify(reqeust, atLeastOnce()).getPathInfo();
        verify( response, atLeastOnce()).getOutputStream();

    }
}

class StubMockAsWarServlet extends MockAsWarServlet {

    ServletContext stubbedServletContext = stubbedServletContext();

    public void init() throws ServletException {
        SoapUI.setSoapUICore(new MockServletSoapUICore(getServletContext()), true);
        super.init();
    }

    @Override
    public String getInitParameter(String name) {

        if (name.equals("projectFile")) {
            return getProjectFilePath();

        }

        return "";
    }

    @Override
    public javax.servlet.ServletContext getServletContext() {

        return stubbedServletContext;

    }

    private String getProjectFilePath() {
        try {
            // remove the initial "/" since on the servlet it always append a slash
            return getFilePathFromResource("/soapui-projects/REST-multiple-service-soapui-project.xml").substring(1);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
