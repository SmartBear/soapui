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

package com.eviware.soapui.model.project;

import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.workspace.Workspace;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * A SoapUI project
 *
 * @author Ole.Matzura
 */

public interface Project extends TestModelItem {
    /**
     * The id of the JBossWS project nature
     */
    public static final String JBOSSWS_NATURE_ID = "com.eviware.soapui.jbosside.jbosswsNature";

    /**
     * The id of the SoapUI project nature
     */
    public static final String SOAPUI_NATURE_ID = "com.eviware.soapui.soapuiNature";

    public Workspace getWorkspace();

    public Interface getInterfaceAt(int index);

    public Interface getInterfaceByName(String interfaceName);

    public int getInterfaceCount();

    public void addProjectListener(ProjectListener listener);

    public void removeProjectListener(ProjectListener listener);

    public int getTestSuiteCount();

    public TestSuite getTestSuiteAt(int index);

    public TestSuite getTestSuiteByName(String testSuiteName);

    public TestSuite getTestSuiteById(UUID testSuiteId);

    public TestSuite addNewTestSuite(String name);

    public int getMockServiceCount();

    public int getRestMockServiceCount();

    public MockService getMockServiceAt(int index);

    public MockService getRestMockServiceAt(int index);

    public MockService getMockServiceByName(String mockServiceName);

    public RestMockService getRestMockServiceByName(String mockServiceName);

    public MockService addNewMockService(String name);

    public RestMockService addNewRestMockService(String name);

    public void removeMockService(MockService service);

    public SaveStatus save() throws IOException;

    public List<TestSuite> getTestSuiteList();

    public List<WsdlMockService> getMockServiceList();

    public List<RestMockService> getRestMockServiceList();

    public List<Interface> getInterfaceList();

    public boolean hasNature(String natureId);

    public EndpointStrategy getEndpointStrategy();

    public void release();

    public boolean isOpen();

    public boolean isDisabled();

    public String getPath();

    public String getResourceRoot();

    public String getShadowPassword();

    public void setShadowPassword(String password);

    public void inspect();

    public int getIndexOfTestSuite(TestSuite testSuite);

    OAuth2ProfileContainer getOAuth2ProfileContainer();
}
