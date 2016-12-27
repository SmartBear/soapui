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

package com.eviware.soapui.monitor;

import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.security.SecurityTestRunner;

/**
 * Listener for TestMonitor events
 *
 * @author Ole.Matzura
 */

public interface TestMonitorListener {
    public void loadTestStarted(LoadTestRunner runner);

    public void loadTestFinished(LoadTestRunner runner);

    public void securityTestStarted(SecurityTestRunner runner);

    public void securityTestFinished(SecurityTestRunner runner);

    public void testCaseStarted(TestCaseRunner runner);

    public void testCaseFinished(TestCaseRunner runner);

    public void mockServiceStarted(MockRunner runner);

    public void mockServiceStopped(MockRunner runner);
}
