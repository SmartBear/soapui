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

package com.eviware.soapui.model.propertyexpansion.resolvers.providers;

import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.resolvers.DynamicPropertyResolver;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

import java.util.List;

/**
 * Returns the current teststep index
 */

public class CurrentStepRunIndexProvider implements DynamicPropertyResolver.ValueProvider {
    @Override
    public String getValue(PropertyExpansionContext context) {
        if( context instanceof WsdlTestRunContext)
        {
            TestRunner runner = ((WsdlTestRunContext) context).getTestRunner();
            if( runner instanceof AbstractTestCaseRunner) {
                List<TestStepResult> resultList = ((AbstractTestCaseRunner) runner).getResults();
                TestStep currentStep = ((WsdlTestRunContext) context).getCurrentStep();

                int ix = 0;
                for( TestStepResult result : resultList )
                    if( result.getTestStep().getId().equals( currentStep.getId()))
                        ix++;

                return String.valueOf( ix );
            }
        }

        return null;
    }
}
