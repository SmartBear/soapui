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

package com.eviware.soapui.impl.wsdl.testcase;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.ProjectRunContext;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.support.types.StringToObjectMap;

public class WsdlProjectRunContext extends AbstractSubmitContext<WsdlProject> implements ProjectRunContext {
    private final WsdlProjectRunner testScenarioRunner;

    public WsdlProjectRunContext(WsdlProjectRunner testScenarioRunner, StringToObjectMap properties) {
        super(testScenarioRunner.getTestRunnable(), properties);
        this.testScenarioRunner = testScenarioRunner;
    }

    public WsdlProject getProject() {
        return getModelItem();
    }

    public ProjectRunner getProjectRunner() {
        return testScenarioRunner;
    }

    public TestRunner getTestRunner() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getProperty(String name) {
        // TODO Auto-generated method stub
        return null;
    }

}
