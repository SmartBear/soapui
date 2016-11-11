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

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.ProjectRunContext;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestRunner;

public class MockProjectRunContext extends AbstractSubmitContext<WsdlProject> implements ProjectRunContext {
    private final MockProjectRunner mockProjectRunner;

    public MockProjectRunContext(MockProjectRunner mockProjectRunner) {
        super(mockProjectRunner.getProject());
        this.mockProjectRunner = mockProjectRunner;
    }

    public WsdlProject getProject() {
        return getModelItem();
    }

    public ProjectRunner getProjectRunner() {
        return mockProjectRunner;
    }

    public TestRunner getTestRunner() {
        return mockProjectRunner;
    }

    public Object getProperty(String name) {
        return getProperties().get(name);
    }
}
