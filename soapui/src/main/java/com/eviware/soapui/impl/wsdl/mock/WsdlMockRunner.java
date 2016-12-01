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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.mock.MockDispatcher;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunListener;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * MockRunner that dispatches Http Requests to their designated
 * WsdlMockOperation if possible
 *
 * @author ole.matzura
 */

@SuppressWarnings("unchecked")
public class WsdlMockRunner implements MockRunner {
    private final WsdlMockRunContext mockContext;
    private boolean running;
    private MockDispatcher dispatcher;

    public WsdlMockRunner(MockService mockService, WsdlTestRunContext context) throws Exception {
        Set<WsdlInterface> interfaces = new HashSet<WsdlInterface>();

        // TODO: move this code elsewhere when the rest counterpoint is in place
        if (mockService instanceof WsdlMockService) {
            WsdlMockService wsdlMockService = (WsdlMockService) mockService;

            for (int i = 0; i < mockService.getMockOperationCount(); i++) {
                WsdlOperation operation = wsdlMockService.getMockOperationAt(i).getOperation();
                if (operation != null) {
                    interfaces.add(operation.getInterface());
                }
            }
        }

        for (WsdlInterface iface : interfaces) {
            iface.getWsdlContext().loadIfNecessary();
        }

        mockContext = new WsdlMockRunContext(mockService, context);
        dispatcher = mockService.createDispatcher(mockContext);

        start();
    }

    public WsdlMockRunContext getMockContext() {
        return mockContext;
    }

    private MockService getMockService() {
        return getMockContext().getMockService();
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        if (!isRunning()) {
            return;
        }

        SoapUI.getMockEngine().stopMockService(this);

        MockRunListener[] mockRunListeners = getMockService().getMockRunListeners();

        for (MockRunListener listener : mockRunListeners) {
            listener.onMockRunnerStop(this);
        }

        try {
            getMockService().runStopScript(mockContext, this);
            running = false;
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    public void release() {
        mockContext.clear();
        dispatcher = null;

    }

    @Override
    public int getMockResultCount() {
        return dispatcher.getMockResultCount();
    }

    @Override
    public MockResult getMockResultAt(int index) {
        return dispatcher.getMockResultAt(index);
    }

    @Override
    public MockResult dispatchRequest(HttpServletRequest request, HttpServletResponse response)
            throws DispatchException {
        for (MockRunListener listener : getMockService().getMockRunListeners()) {
            Object result = listener.onMockRequest(this, request, response);
            if (result instanceof MockResult) {
                return (MockResult) result;
            }
        }

        String qs = request.getQueryString();
        if (qs != null && qs.startsWith("cmd=")) {
            try {
                dispatchCommand(request.getParameter("cmd"), request, response);
            } catch (IOException e) {
                throw new DispatchException(e);
            }
        }

        return dispatcher.dispatchRequest(request, response);
    }

    private void dispatchCommand(String cmd, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if ("stop".equals(cmd)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.flushBuffer();

            SoapUI.getThreadPool().execute(new Runnable() {

                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    stop();
                }
            });
        } else if ("restart".equals(cmd)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.flushBuffer();

            SoapUI.getThreadPool().execute(new Runnable() {

                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    stop();

                    try {
                        getMockService().start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    // TODO remove this duplication. Look at WsdlMockDispatcher
    public String getOverviewUrl() {
        return getMockService().getPath() + "?WSDL";
    }

    public void start() throws Exception {
        if (running) {
            return;
        }

        mockContext.reset();
        getMockService().runStartScript(mockContext, this);

        SoapUI.getMockEngine().startMockService(this);
        running = true;

        MockRunListener[] mockRunListeners = getMockService().getMockRunListeners();

        for (MockRunListener listener : mockRunListeners) {
            listener.onMockRunnerStart(this);
        }

        Analytics.trackAction("Start Mock Service, ID: " + getMockService().getStringID());
    }

    public void setLogEnabled(boolean logEnabled) {
        dispatcher.setLogEnabled(logEnabled);
    }

    @Override
    public void clearResults() {
        dispatcher.clearResults();
    }


    public void setMaxResults(long maxNumberOfResults) {
        dispatcher.setMaxResults(maxNumberOfResults);
    }
}
