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

package com.eviware.soapui.model.mock;

import com.eviware.soapui.model.iface.SoapUIListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Listener for MockRunner events
 *
 * @author ole.matzura
 */

public interface MockRunListener extends SoapUIListener {
    public void onMockRunnerStart(MockRunner mockRunner);

    public void onMockResult(MockResult result);

    public void onMockRunnerStop(MockRunner mockRunner);

    /**
     * Called before dispatching a request. If a MockResult is returned, further
     * dispatching is aborted and the returned result is used.
     *
     * @param runner
     * @param request
     * @param response
     * @return an optional MockResult, null if dispatching should move on as
     *         usual
     */

    public MockResult onMockRequest(MockRunner runner, HttpServletRequest request, HttpServletResponse response);
}
