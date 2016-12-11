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

package com.eviware.soapui.impl.wsdl.loadtest;

import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunner;

/**
 * LoadTestRunContext implementation for WsdlLoadTests
 *
 * @author Ole.Matzura
 */

public class WsdlLoadTestContext extends DefaultPropertyExpansionContext implements LoadTestRunContext {
    private final LoadTestRunner runner;

    public WsdlLoadTestContext(LoadTestRunner runner) {
        super(runner.getLoadTest().getTestCase());
        this.runner = runner;
    }

    public LoadTestRunner getLoadTestRunner() {
        return runner;
    }

    @Override
    public Object get(Object key) {
        if ("loadTestRunner".equals(key)) {
            return runner;
        }

        return super.get(key);
    }

    public Object getProperty(String testStep, String propertyName) {
        return null;
    }

    public TestCaseRunner getTestRunner() {
        return null;
    }
}
