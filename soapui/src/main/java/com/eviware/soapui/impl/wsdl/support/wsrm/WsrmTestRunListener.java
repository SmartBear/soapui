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

package com.eviware.soapui.impl.wsdl.support.wsrm;

import com.eviware.soapui.config.WsrmVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsmc.WsmcInjection;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

import java.util.HashMap;

public class WsrmTestRunListener implements TestRunListener {

    private HashMap<String, WsrmSequence> wsrmMap;

    public void afterRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {

        if (wsrmMap != null) {
            for (String endpoint : wsrmMap.keySet()) {
                WsrmSequence sequence = wsrmMap.get(endpoint);
                WsrmUtils utils = new WsrmUtils(sequence.getSoapVersion());
                utils.closeSequence(endpoint, sequence.getSoapVersion(), sequence.getWsrmNameSpace(), sequence.getUuid(),
                        sequence.getIdentifier(), sequence.getLastMsgNumber(), sequence.getOperation());
            }
        }

        wsrmMap = null;
    }

    public void afterStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result) {

    }

    public void beforeRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
    }

    public void beforeStep(TestCaseRunner testRunner, TestCaseRunContext runContext) {
    }

    public void beforeStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep) {
        WsdlTestCase testCase = (WsdlTestCase) runContext.getTestCase();
        if (testStep instanceof WsdlTestRequestStep && testCase.getWsrmEnabled()) {
            if (wsrmMap == null) {
                wsrmMap = new HashMap<String, WsrmSequence>();
            }
            WsdlTestRequestStep requestStep = (WsdlTestRequestStep) testStep;
            String endpoint = requestStep.getHttpRequest().getEndpoint();
            SoapVersion soapVersion = requestStep.getOperation().getInterface().getSoapVersion();
            if (!wsrmMap.containsKey(endpoint)) {

                WsrmUtils utils = new WsrmUtils(soapVersion);
                WsrmSequence sequence = utils.createSequence(endpoint, soapVersion, testCase.getWsrmVersionNamespace(),
                        testCase.getWsrmAckTo(), testCase.getWsrmExpires(), requestStep.getOperation(), null, null);

                wsrmMap.put(endpoint, sequence);
            }

            WsrmSequence sequence = wsrmMap.get(endpoint);
            WsdlRequest wsdlRequest = requestStep.getHttpRequest();

            wsdlRequest.getWsrmConfig().setVersion(testCase.getWsrmVersion());
            wsdlRequest.getWsrmConfig().setSequenceIdentifier(sequence.getIdentifier());
            wsdlRequest.getWsrmConfig().setLastMessageId(sequence.incrementLastMsgNumber());
            wsdlRequest.getWsrmConfig().setUuid(sequence.getUuid());
            wsdlRequest.getWsrmConfig().setWsrmEnabled(true);

            if (!testCase.getWsrmVersion().equals(WsrmVersionTypeConfig.X_1_0.toString())) {
                WsmcInjection injection = new WsmcInjection(wsdlRequest.getEndpoint(), wsdlRequest.getOperation(),
                        soapVersion, wsdlRequest.getWsrmConfig().getUuid());
                wsdlRequest.setAfterRequestInjection(injection);
            }

        }

    }

}
