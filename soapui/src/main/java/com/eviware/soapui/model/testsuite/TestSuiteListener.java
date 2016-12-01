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

package com.eviware.soapui.model.testsuite;

import com.eviware.soapui.security.SecurityTest;

/**
 * Listener for TestSuite-related events
 *
 * @author Ole.Matzura
 */

public interface TestSuiteListener {
    void testCaseAdded(TestCase testCase);

    void testCaseRemoved(TestCase testCase);

    void testCaseMoved(TestCase testCase, int index, int offset);

    void loadTestAdded(LoadTest loadTest);

    void loadTestRemoved(LoadTest loadTest);

    void testStepAdded(TestStep testStep, int index);

    void testStepRemoved(TestStep testStep, int index);

    void testStepMoved(TestStep testStep, int fromIndex, int offset);

    void securityTestAdded(SecurityTest securityTest);

    void securityTestRemoved(SecurityTest securityTest);
}
