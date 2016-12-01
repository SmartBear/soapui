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

package com.eviware.soapui.impl.wsdl.panels.support;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;

public class MockTestSuiteRunContext extends AbstractSubmitContext<WsdlTestSuite> implements TestSuiteRunContext {
    private final MockTestSuiteRunner mockTestSuiteRunner;

    public MockTestSuiteRunContext(MockTestSuiteRunner mockTestSuiteRunner) {
        super(mockTestSuiteRunner.getTestRunnable());
        this.mockTestSuiteRunner = mockTestSuiteRunner;
    }

    public TestCase getCurrentTestCase() {
        return null;
    }

    public int getCurrentTestCaseIndex() {
        return -1;
    }

    public TestSuite getTestSuite() {
        return getModelItem();
    }

    public TestSuiteRunner getTestSuiteRunner() {
        return mockTestSuiteRunner;
    }

    public TestRunner getTestRunner() {
        return mockTestSuiteRunner;
    }

    public Object getProperty(String name) {
        return getProperties().get(name);
    }

}
