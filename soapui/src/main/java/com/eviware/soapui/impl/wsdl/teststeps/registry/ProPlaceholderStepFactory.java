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

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepWithProperties;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.UISupport;

/**
 * Factory for creation of placeholder steps
 *
 * @author Ole.Matzura
 */

public class ProPlaceholderStepFactory extends WsdlTestStepFactory {
    public ProPlaceholderStepFactory(String type, String name, String image) {
        super(type, name, "Placeholder for SoapUI Pro " + name + " TestStep", image);
    }

    public WsdlTestStep buildTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        return new WsdlProPlaceholderTestStep(testCase, config, forLoadTest, getTestStepIconPath(),
                getTestStepDescription());
    }

    public TestStepConfig createNewTestStep(WsdlTestCase testCase, String name) {
        return null;
    }

    public boolean canCreate() {
        return false;
    }

    public static class WsdlProPlaceholderTestStep extends WsdlTestStepWithProperties {
        private final String description;

        protected WsdlProPlaceholderTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest,
                                             String iconPath, String description) {
            super(testCase, config, false, forLoadTest);
            this.description = description;

            if (!forLoadTest) {
                setIcon(UISupport.createImageIcon(iconPath));
            }
        }

        public TestStepResult run(TestCaseRunner testRunner, TestCaseRunContext testRunContext) {
            return new WsdlTestStepResult(this);
        }

        @Override
        public String getDescription() {
            return description;
        }
    }
}
