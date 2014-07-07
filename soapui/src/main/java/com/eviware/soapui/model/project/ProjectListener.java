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

import com.eviware.soapui.model.environment.Environment;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.SoapUIListener;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.testsuite.TestSuite;

/**
 * Listener for Project-related events
 *
 * @author Ole.Matzura
 */

public interface ProjectListener extends SoapUIListener {
    void interfaceAdded(Interface iface);

    void interfaceRemoved(Interface iface);

    void interfaceUpdated(Interface iface);

    void testSuiteAdded(TestSuite testSuite);

    void testSuiteRemoved(TestSuite testSuite);

    void testSuiteMoved(TestSuite testSuite, int index, int offset);

    void mockServiceAdded(MockService mockService);

    void mockServiceRemoved(MockService mockService);

    void afterLoad(Project project);

    void beforeSave(Project project);

    void environmentAdded(Environment env);

    void environmentRemoved(Environment env, int index);

    void environmentSwitched(Environment environment);

    void environmentRenamed(Environment environment, String oldName, String newName);
}
