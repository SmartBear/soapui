/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
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

package com.eviware.soapui.model.support;

import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.model.mock.MockDispatcher;
import com.eviware.soapui.model.mock.MockResult;
import org.apache.commons.collections.list.TreeList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

public abstract class AbstractMockDispatcher implements MockDispatcher {

    private final List<WsdlMockResult> mockResults = Collections.synchronizedList(new TreeList());
    private long maxResults = 100;
    private int removed = 0;
    private boolean logEnabled = true;


    public MockResult dispatchGetRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        throw new DispatchException("Unsupported HTTP Method: GET");
    }

    public MockResult dispatchPostRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        throw new DispatchException("Unsupported HTTP Method: POST");
    }

    public MockResult dispatchHeadRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        throw new DispatchException("Unsupported HTTP Method: HEAD");
    }

    public MockResult dispatchPutRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        throw new DispatchException("Unsupported HTTP Method: PUT");
    }

    public MockResult dispatchDeleteRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        throw new DispatchException("Unsupported HTTP Method: DELETE");
    }

    public MockResult dispatchPatchRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        throw new DispatchException("Unsupported HTTP Method: PATCH");
    }

    public MockResult dispatchPropfindRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        throw new DispatchException("Unsupported HTTP Method: PROPFIND");
    }

    public MockResult dispatchLockRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        throw new DispatchException("Unsupported HTTP Method: LOCK");
    }

    public MockResult dispatchUnlockRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        throw new DispatchException("Unsupported HTTP Method: UNLOCK");
    }

    public MockResult dispatchCopyRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        throw new DispatchException("Unsupported HTTP Method: COPY");
    }

    public MockResult dispatchPurgeRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        throw new DispatchException("Unsupported HTTP Method: PURGE");
    }

    public MockResult dispatchRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        String method = request.getMethod();

        if (method.equals("POST")) {
            return dispatchPostRequest(request, response);
        } else if (method.equals("GET")) {
            return dispatchGetRequest(request, response);
        } else if (method.equals("HEAD")) {
            return dispatchHeadRequest(request, response);
        } else if (method.equals("PUT")) {
            return dispatchPutRequest(request, response);
        } else if (method.equals("DELETE")) {
            return dispatchDeleteRequest(request, response);
        } else if (method.equals("PATCH")) {
            return dispatchPatchRequest(request, response);
        } else if (method.equals("PROPFIND")) {
            return dispatchPropfindRequest(request, response);
        } else if (method.equals("LOCK")) {
            return dispatchLockRequest(request, response);
        } else if (method.equals("UNLOCK")) {
            return dispatchUnlockRequest(request, response);
        } else if (method.equals("COPY")) {
            return dispatchCopyRequest(request, response);
        } else if (method.equals("PURGE")) {
            return dispatchPurgeRequest(request, response);
        }

        throw new DispatchException("Unsupported HTTP Method: " + method);
    }

    public synchronized void addMockResult(WsdlMockResult mockResult) {
        if (maxResults > 0 && logEnabled) {
            mockResults.add(mockResult);
        }

        while (mockResults.size() > maxResults) {
            mockResults.remove(0);
            removed++;
        }
    }

    public MockResult getMockResultAt(int index) {
        return index <= removed ? null : mockResults.get(index - removed);
    }

    public int getMockResultCount() {
        return mockResults.size() + removed;
    }

    public synchronized void clearResults() {
        mockResults.clear();
    }

    public long getMaxResults() {
        return maxResults;
    }

    public synchronized void setMaxResults(long maxNumberOfResults) {
        this.maxResults = maxNumberOfResults;

        while (mockResults.size() > maxNumberOfResults) {
            mockResults.remove(0);
            removed++;
        }
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }


}
