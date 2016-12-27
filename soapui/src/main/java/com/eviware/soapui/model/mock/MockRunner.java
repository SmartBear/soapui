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

package com.eviware.soapui.model.mock;

import com.eviware.soapui.model.Releasable;

/**
 * The mock runner is responsible for setting up a server on a port. It listens to requests and dispatches them
 * to the correct target.
 *
 * @author ole.matzura
 */

public interface MockRunner extends MockDispatcher, Releasable {
    /**
     * Start this runner. If already started - do nothing.
     */
    public void start() throws Exception;

    /**
     * Stop this runner. If not running - do nothing.
     */
    public void stop();

    /**
     * @return true if this runner is running - false otherwise.
     */
    public boolean isRunning();

    /**
     * @return The MockRunContext for this runner. This includes references to the mock service and responses for
     *         this runner.
     */
    public MockRunContext getMockContext();
}
