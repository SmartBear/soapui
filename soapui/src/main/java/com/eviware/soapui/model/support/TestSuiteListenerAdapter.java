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

import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuiteListener;
import com.eviware.soapui.security.SecurityTest;

/**
 * Adapter for TestSuiteListener implementations
 *
 * @author Ole.Matzura
 */

public class TestSuiteListenerAdapter implements TestSuiteListener {
    public void testCaseAdded(TestCase testCase) {
    }

    public void testCaseRemoved(TestCase testCase) {
    }

    public void testStepAdded(TestStep testStep, int index) {
    }

    public void testStepRemoved(TestStep testStep, int index) {
    }

    public void loadTestAdded(LoadTest loadTest) {
    }

    public void loadTestRemoved(LoadTest loadTest) {
    }

    public void securityTestAdded(SecurityTest securityTest) {
    }

    public void securityTestRemoved(SecurityTest securityTest) {
    }

    public void testStepMoved(TestStep testStep, int fromIndex, int offset) {
    }

    public void testCaseMoved(TestCase testCase, int index, int offset) {
    }
}
