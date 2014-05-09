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

package com.eviware.soapui.model.testsuite;

import com.eviware.soapui.model.iface.SoapUIListener;

/**
 * Listener for TestRun-related events, schedule events will only be triggered
 * for LoadTest runs.
 *
 * @author Ole.Matzura
 */

public interface TestRunListener extends SoapUIListener {
    public void beforeRun(TestCaseRunner testRunner, TestCaseRunContext runContext);

    public void afterRun(TestCaseRunner testRunner, TestCaseRunContext runContext);

    /**
     * @deprecated use
     *             {@link #beforeStep(TestCaseRunner, TestCaseRunContext, TestStep)}
     *             instead
     */
    @Deprecated
    public void beforeStep(TestCaseRunner testRunner, TestCaseRunContext runContext);

    public void beforeStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep);

    public void afterStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result);
}
