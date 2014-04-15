/*
 * Copyright 2004-2014 SmartBear Software
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

import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.eviware.soapui.utils.MockedServlet.mockHttpServletRequest;
import static com.eviware.soapui.utils.MockedServlet.mockHttpServletResponse;
import static com.eviware.soapui.utils.MockedServlet.stubbedServletContext;
import static com.eviware.soapui.utils.ResourceUtils.getFilePathFromResource;
import static org.mockito.Mockito.mock;

public class MockAsWarServletTest {


    @Test
    public void shouldGetResponse() throws IOException, ServletException {

        StubMockAsWarServlet servlet = new StubMockAsWarServlet();
        servlet.init();
        HttpServletRequest reqeust = mockHttpServletRequest();
        HttpServletResponse response = mockHttpServletResponse();
        servlet.service(reqeust, response);
    }

}

class StubMockAsWarServlet extends MockAsWarServlet {

    @Override
    public String getInitParameter(String name) {

        if (name.equals("projectFile")) {
            return getProjectFilePath();

        }

        return "";
    }

    @Override
    public javax.servlet.ServletContext getServletContext() {
        return stubbedServletContext();

    }

    private String getProjectFilePath() {
        try {
            return getFilePathFromResource("/soapui-projects/BasicMock-soapui-4.6.3-Project.xml").substring(1);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
