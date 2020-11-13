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

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import org.apache.logging.log4j.Logger;

public class MockLoadTestRunner extends AbstractMockTestRunner<WsdlLoadTest> implements LoadTestRunner {
    public MockLoadTestRunner(WsdlLoadTest modelItem, Logger logger) {
        super(modelItem, logger);
    }

    public WsdlLoadTest getLoadTest() {
        return getTestRunnable();
    }

    public float getProgress() {
        return 0;
    }

    public int getRunningThreadCount() {
        return (int) getLoadTest().getThreadCount();
    }

    public boolean hasStopped() {
        return false;
    }
}
