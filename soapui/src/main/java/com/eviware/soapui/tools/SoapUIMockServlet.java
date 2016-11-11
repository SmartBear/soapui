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

package com.eviware.soapui.tools;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUICore;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ole
 */
public class SoapUIMockServlet extends HttpServlet {
    private WsdlMockRunner mockRunner;
    private WsdlMockService mockService;
    private WsdlProject project;
    private static Logger logger = Logger.getLogger(SoapUIMockServlet.class.getName());

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            logger.info("Initializing SoapUI Core");
            SoapUI.setSoapUICore(
                    createSoapUICore(getInitParameter("settingsFile"), getInitParameter("settingsPassword")), true);

            logger.info("Loading project");
            project = new WsdlProject(getInitParameter("projectFile"), getInitParameter("projectPassword"));

            logger.info("Starting MockService");
            mockService = project.getMockServiceByName(getInitParameter("mockService"));
            mockRunner = mockService.start();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        try {
            mockRunner.dispatchRequest(request, response);
        } catch (DispatchException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return mockService.getName();
    }

    // </editor-fold>

    protected SoapUICore createSoapUICore(String settingsFile, String soapUISettingsPassword) {
        return new DefaultSoapUICore(null, settingsFile, soapUISettingsPassword);
    }
}
