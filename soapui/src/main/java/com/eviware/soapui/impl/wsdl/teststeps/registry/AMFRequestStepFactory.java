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

import com.eviware.soapui.config.AMFRequestTestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.monitor.WsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;

/**
 * Factory for creation TransferValue steps
 *
 * @author Ole.Matzura
 */

public class AMFRequestStepFactory extends WsdlTestStepFactory {
    public static final String AMF_REQUEST_TYPE = "amfrequest";

    public AMFRequestStepFactory() {
        super(AMF_REQUEST_TYPE, "AMF Request", "Submits a AMF Request and validates its response", "/amf_request_step.gif");
    }

    public WsdlTestStep buildTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        return new AMFRequestTestStep(testCase, config, forLoadTest);
    }

    public TestStepConfig createNewTestStep(WsdlTestCase testCase, String name) {
        TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
        testStepConfig.setType(AMF_REQUEST_TYPE);
        testStepConfig.setName(name);
        return testStepConfig;
    }

    public boolean canCreate() {
        return true;
    }

    public TestStepConfig createConfig(WsdlMonitorMessageExchange me, String stepName) {
        AMFRequestTestStepConfig testRequestConfig = AMFRequestTestStepConfig.Factory.newInstance();

        testRequestConfig.setEndpoint(me.getEndpoint());

        TestStepConfig testStep = TestStepConfig.Factory.newInstance();
        testStep.setType(AMF_REQUEST_TYPE);
        testStep.setConfig(testRequestConfig);
        testStep.setName(stepName);
        return testStep;
    }
}
