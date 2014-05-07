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

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;

public class RandomMockOperationDispatcher extends AbstractMockOperationDispatcher {
    public RandomMockOperationDispatcher(MockOperation mockOperation) {
        super(mockOperation);
    }

    public MockResponse selectMockResponse(MockRequest request, MockResult result) {
        synchronized (result.getMockOperation()) {
            synchronized (this) {
                int currentDispatchIndex = (int) ((Math.random() * getMockOperation().getMockResponseCount()) + 0.5F);

                if (currentDispatchIndex >= getMockOperation().getMockResponseCount()) {
                    currentDispatchIndex = 0;
                }

                return getMockOperation().getMockResponseAt(currentDispatchIndex);
            }
        }
    }

    @Override
    public boolean hasDefaultResponse() {
        return false;
    }

    public static class Factory implements MockOperationDispatchFactory {
        public MockOperationDispatcher build(MockOperation mockOperation) {
            return new RandomMockOperationDispatcher(mockOperation);
        }
    }
}
