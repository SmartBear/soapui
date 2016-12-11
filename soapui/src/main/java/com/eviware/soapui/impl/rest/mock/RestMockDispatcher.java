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

package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.support.AbstractMockDispatcher;
import org.apache.commons.httpclient.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestMockDispatcher extends AbstractMockDispatcher {

    private RestMockService mockService;
    private WsdlMockRunContext mockContext;

    public RestMockDispatcher(RestMockService mockService, WsdlMockRunContext mockContext) {
        this.mockService = mockService;
        this.mockContext = mockContext;
    }

    @Override
    public MockResult dispatchRequest(HttpServletRequest request, HttpServletResponse response) {

        RestMockRequest restMockRequest = null;
        Object result = null;
        try {
            restMockRequest = new RestMockRequest(request, response, mockContext);
            result = mockService.runOnRequestScript(mockContext, restMockRequest);

            if (!(result instanceof MockResult)) {
                result = getMockResult(restMockRequest);
            }

            mockService.runAfterRequestScript(mockContext, (MockResult) result);
            return (MockResult) result;
        } catch (Exception e) {
            SoapUI.logError(e, "got an exception while dispatching - returning a default 500 response");
            return createServerErrorMockResult(restMockRequest);
        } finally {
            mockService.fireOnMockResult(result);
        }
    }

    private MockResult createServerErrorMockResult(RestMockRequest restMockRequest) {
        restMockRequest.getHttpResponse().setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        return new RestMockResult(restMockRequest);
    }

    private MockResult getMockResult(RestMockRequest restMockRequest) throws DispatchException {

        String pathToFind = getPathRemainder(restMockRequest);

        RestMockAction mockAction = (RestMockAction) mockService.findBestMatchedOperation(pathToFind, restMockRequest.getMethod());

        if (mockAction != null) {
            return mockAction.dispatchRequest(restMockRequest);
        } else {
            return createNotFoundResponse(restMockRequest);
        }

    }

    private String getPathRemainder(RestMockRequest restMockRequest) {
        String pathToFind = restMockRequest.getPath();

        if (!mockService.getPath().equals("/")) {
            pathToFind = restMockRequest.getPath().substring(mockService.getPath().length());
        }
        return pathToFind;
    }

    private RestMockResult createNotFoundResponse(RestMockRequest restMockRequest) {
        restMockRequest.getHttpResponse().setStatus(HttpStatus.SC_NOT_FOUND);
        return new RestMockResult(restMockRequest);
    }
}
