/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.security.support;

import com.eviware.soapui.model.iface.SoapUIListener;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.result.SecurityScanRequestResult;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;

/**
 * Listener for SecurityTestRun-related events
 *
 * @author Dragica
 */

public interface SecurityTestRunListener extends SoapUIListener {
    public void beforeRun(TestCaseRunner testRunner, SecurityTestRunContext runContext);

    public void afterRun(TestCaseRunner testRunner, SecurityTestRunContext runContext);

    public void beforeStep(TestCaseRunner testRunner, SecurityTestRunContext runContext, TestStepResult testStepResult);

    public void afterStep(TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result);

    public void afterOriginalStep(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                  SecurityTestStepResult result);

    public void beforeSecurityScan(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                   SecurityScan securityScan);

    public void afterSecurityScan(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                  SecurityScanResult securityScanResult);

    public void afterSecurityScanRequest(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                         SecurityScanRequestResult securityScanReqResult);
}
