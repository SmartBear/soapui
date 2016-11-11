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

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunListener;
import com.eviware.soapui.model.mock.MockRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SequenceMockOperationDispatcher extends AbstractMockOperationDispatcher implements MockRunListener {
    private int currentDispatchIndex;

    public SequenceMockOperationDispatcher(MockOperation mockOperation) {
        super(mockOperation);

        mockOperation.getMockService().addMockRunListener(this);
    }

    public MockResponse selectMockResponse(MockRequest request, MockResult result) {
        MockResponse mockResponse = null;
        MockOperation mockOperation = getMockOperation();
        synchronized (mockOperation) {
            if (currentDispatchIndex >= mockOperation.getMockResponseCount()) {
                currentDispatchIndex = 0;
            }

            mockResponse = mockOperation.getMockResponseAt(currentDispatchIndex);

            currentDispatchIndex++;
    	}
        return mockResponse;
    }

    @Override
    public boolean hasDefaultResponse() {
        return false;
    }

    @Override
    public void release() {
        getMockOperation().getMockService().removeMockRunListener(this);
        super.release();
    }

    public void onMockRunnerStart(MockRunner mockRunner) {
        currentDispatchIndex = 0;
    }

    public void onMockResult(MockResult result) {
    }

    public void onMockRunnerStop(MockRunner mockRunner) {
    }

    public MockResult onMockRequest(MockRunner runner, HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    public static class Factory implements MockOperationDispatchFactory {
        public MockOperationDispatcher build(MockOperation mockOperation) {
            return new SequenceMockOperationDispatcher(mockOperation);
        }
    }
}
