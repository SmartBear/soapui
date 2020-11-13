/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MockRunnerManagerImpl implements MockRunnerManager {
    private final static Logger log = LogManager.getLogger(MockRunnerManagerImpl.class);

    private static Map<String, MockRunnerManager> managers = new HashMap<String, MockRunnerManager>();

    private Map<String, WsdlMockService> mockServices = new HashMap<String, WsdlMockService>();

    private Vector<WsdlMockRunner> mockRunners = new Vector<WsdlMockRunner>();

    private Project project;

    private MockRunnerManagerImpl(Project project) {
        this.project = project;
    }

    public static MockRunnerManager getInstance(TestCase testCase) {
        if (managers.containsKey(testCase.getId())) {
            return managers.get(testCase.getId());
        } else {
            MockRunnerManager manager = new MockRunnerManagerImpl(testCase.getTestSuite().getProject());
            managers.put(testCase.getId(), manager);

            return manager;
        }
    }

    public WsdlMockService getMockService(int port, String path) {
        String key = port + path;

        WsdlMockService service = mockServices.get(key);
        if (service == null) {
            MockServiceConfig mockServiceConfig = MockServiceConfig.Factory.newInstance();
            mockServiceConfig.setPath(path);
            mockServiceConfig.setPort(port);
            mockServiceConfig.setName(port + ":" + path);
            service = new WsdlMockService(project, mockServiceConfig);
            mockServices.put(key, service);
        }

        return service;
    }

    public void start() throws MockRunnerManagerException {
        if (log.isDebugEnabled()) {
            log.debug("Starting MockRunnerManager");
        }

        for (WsdlMockService mockService : mockServices.values()) {
            try {
                mockRunners.add(mockService.start());
            } catch (Exception e) {
                throw new MockRunnerManagerException("Failed to create a WsdlMockRunner", e);
            }
        }
    }

    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Stopping MockRunnerManager");
        }

        for (WsdlMockRunner runner : mockRunners) {
            try {
                runner.stop();
            } catch (Exception e) {
                log.error(e);
            }
        }

        mockServices.clear();
        mockRunners.clear();
    }

    public boolean isStarted() {
        for (WsdlMockRunner runner : mockRunners) {
            if (runner.isRunning()) {
                return true;
            }
        }

        return false;
    }
}
