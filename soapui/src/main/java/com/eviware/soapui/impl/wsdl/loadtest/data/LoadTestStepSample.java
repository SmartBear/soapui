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

package com.eviware.soapui.impl.wsdl.loadtest.data;

import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;

/**
 * Base class for a LoadTest sample
 *
 * @author Ole.Matzura
 */

public class LoadTestStepSample {
    private long size;
    private TestStepStatus status;
    private long timeTaken;
    private String[] messages;
    private long timeStamp;

    LoadTestStepSample(TestStepResult result) {
        size = result.getSize();
        status = result.getStatus();
        timeTaken = result.getTimeTaken();
        messages = result.getMessages();
        timeStamp = result.getTimeStamp();
    }

    public String[] getMessages() {
        return messages.clone();
    }

    public long getSize() {
        return size;
    }

    public TestStepStatus getStatus() {
        return status;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getTimeTaken() {
        return timeTaken;
    }
}
