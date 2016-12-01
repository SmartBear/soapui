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

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.environment.Environment;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.project.ProjectListener;
import com.eviware.soapui.model.testsuite.TestSuite;

/**
 * Adapter for ProjectListener implementations
 *
 * @author Ole.Matzura
 */

public class ProjectListenerAdapter implements ProjectListener {
    public void interfaceAdded(Interface iface) {
    }

    public void interfaceRemoved(Interface iface) {
    }

    public void testSuiteAdded(TestSuite testSuite) {
    }

    public void testSuiteRemoved(TestSuite testSuite) {
    }

    public void testSuiteMoved(TestSuite testSuite, int index, int offset) {

    }

    public void mockServiceAdded(MockService mockService) {
    }

    public void mockServiceRemoved(MockService mockService) {
    }

    public void interfaceUpdated(Interface iface) {
    }

    public void afterLoad(Project project) {
    }

    public void beforeSave(Project project) {
    }

    public void environmentAdded(Environment env) {
    }

    public void environmentRemoved(Environment env, int index) {
    }

    public void environmentSwitched(Environment environment) {
    }

    @Override
    public void environmentRenamed(Environment environment, String oldName, String newName) {
        // TODO Auto-generated method stub

    }
}
