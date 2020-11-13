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

package com.eviware.soapui.impl.wsdl.panels.support;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.result.SecurityScanResult;
import org.apache.logging.log4j.Logger;

public class MockSecurityTestRunner extends AbstractMockTestRunner<SecurityTest> implements SecurityTestRunner {

    private SecurityTest securityTest;

    public MockSecurityTestRunner(SecurityTest modelItem) {
        super(modelItem, null);
    }

    public MockSecurityTestRunner(SecurityTest modelItem, Logger logger) {
        super(modelItem, logger);
        this.securityTest = modelItem;
    }

    @Override
    public SecurityTest getSecurityTest() {
        return securityTest;
    }

    @Override
    public SecurityScanResult runTestStepSecurityScan(SecurityTestRunContext runContext, TestStep testStep,
                                                      SecurityScan securityCheck) {
        return securityCheck.run(cloneForSecurityScan((WsdlTestStep) testStep), runContext, null);
    }

    private TestStep cloneForSecurityScan(WsdlTestStep sourceTestStep) {
        WsdlTestStep clonedTestStep = null;
        TestStepConfig testStepConfig = (TestStepConfig) sourceTestStep.getConfig().copy();
        WsdlTestStepFactory factory = WsdlTestStepRegistry.getInstance().getFactory(testStepConfig.getType());
        if (factory != null) {
            clonedTestStep = factory.buildTestStep(securityTest.getTestCase(), testStepConfig, false);
            if (clonedTestStep instanceof Assertable) {
                for (TestAssertion assertion : ((Assertable) clonedTestStep).getAssertionList()) {
                    ((Assertable) clonedTestStep).removeAssertion(assertion);
                }
            }
        }
        return clonedTestStep;
    }

}
