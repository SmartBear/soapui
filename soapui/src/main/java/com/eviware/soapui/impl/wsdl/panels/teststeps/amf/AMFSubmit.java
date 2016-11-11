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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringsMap;
import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;

import java.util.List;
import java.util.concurrent.Future;

public class AMFSubmit implements Submit, Runnable {
    public static final String AMF_CONNECTION = "AMF_CONNECTION";
    private volatile Future<?> future;
    private SubmitContext context;
    private Status status;
    private SubmitListener[] listeners;
    private Exception error;
    private long timestamp;
    private final AMFRequest request;
    private AMFResponse response;
    private AMFCredentials credentials;

    public AMFSubmit(AMFRequest request, SubmitContext submitContext, boolean async) {
        this.request = request;
        this.context = submitContext;

        List<SubmitListener> regListeners = SoapUI.getListenerRegistry().getListeners(SubmitListener.class);

        SubmitListener[] submitListeners = request.getSubmitListeners();
        this.listeners = new SubmitListener[submitListeners.length + regListeners.size()];
        for (int c = 0; c < submitListeners.length; c++) {
            this.listeners[c] = submitListeners[c];
        }

        for (int c = 0; c < regListeners.size(); c++) {
            this.listeners[submitListeners.length + c] = regListeners.get(c);
        }

        error = null;
        status = Status.INITIALIZED;
        timestamp = System.currentTimeMillis();

        if (async) {
            future = SoapUI.getThreadPool().submit(this);
        } else {
            run();
        }
    }

    public void cancel() {
        if (status == Status.CANCELED) {
            return;
        }

        SoapUI.log.info("Canceling request..");

        status = Status.CANCELED;

        for (int i = 0; i < listeners.length; i++) {
            try {
                listeners[i].afterSubmit(this, context);
            } catch (Throwable e) {
                SoapUI.logError(e);
            }
        }
    }

    public Status waitUntilFinished() {
        if (future != null) {
            if (!future.isDone()) {
                try {
                    future.get();
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }
        } else {
            throw new RuntimeException("cannot wait on null future");
        }

        return getStatus();
    }

    public void run() {
        try {
            for (int i = 0; i < listeners.length; i++) {
                if (!listeners[i].beforeSubmit(this, context)) {
                    status = Status.CANCELED;
                    SoapUI.log.error("listener cancelled submit..");
                    return;
                }
            }

            status = Status.RUNNING;
            Object responseContent = executeAmfCall(getRequest());
            createResponse(responseContent);

            if (status != Status.CANCELED && status != Status.ERROR) {
                status = Status.FINISHED;
            }
        } catch (Exception e) {
            UISupport.showErrorMessage("There's been an error in executing query " + e.toString());
            error = e;
        } finally {

            if (status != Status.CANCELED) {
                for (int i = 0; i < listeners.length; i++) {
                    try {
                        listeners[i].afterSubmit(this, context);
                    } catch (Throwable e) {
                        SoapUI.logError(e);
                    }
                }
            }
        }
    }

    protected void createResponse(Object responseContent) {
        try {
            response = new AMFResponse(request, context, responseContent);
            response.setTimestamp(timestamp);
            response.setTimeTaken(System.currentTimeMillis() - timestamp);
        } catch (Exception e) {
            SoapUI.logError(e);
        }

    }

    private Object executeAmfCall(AMFRequest amfRequest) throws ClientStatusException, ServerStatusException {
        SoapUIAMFConnection amfConnection = null;
        try {
            amfConnection = getConnection(amfRequest);
            addAmfHeaders(amfRequest, amfConnection);
            addHttpHeaders(amfRequest, amfConnection);
            Object result = amfConnection.call(context, amfRequest.getAmfCall(), amfRequest.argumentsToArray());

            return result;
        } catch (Exception e) {
            SoapUI.logError(e);
            error = e;
            status = Status.ERROR;
        } finally {
            amfRequest.clearArguments();
            if (context.getModelItem() instanceof AMFRequestTestStep) {
                if (credentials != null && credentials.isLoggedIn()) {
                    credentials.logout();
                    credentials = null;
                } else {
                    amfConnection.close();
                }
            }
        }
        return null;

    }

    private SoapUIAMFConnection getConnection(AMFRequest amfRequest) throws Exception {
        SoapUIAMFConnection amfConnection = null;
        if (isAuthorisationEnabled(amfRequest) && (context.getModelItem() instanceof WsdlTestCase)) {
            if ((amfConnection = (SoapUIAMFConnection) context.getProperty(AMF_CONNECTION)) != null) {
                return amfConnection;
            } else {
                throw new Exception("amf session connection error! ");
            }
        } else if (isAuthorisationEnabled(amfRequest) && (context.getModelItem() instanceof AMFRequestTestStep)) {
            String endpoint = context.expand(getTestCaseConfig(amfRequest).getAmfEndpoint());
            String username = context.expand(getTestCaseConfig(amfRequest).getAmfLogin());
            String password = context.expand(getTestCaseConfig(amfRequest).getAmfPassword());

            if (StringUtils.hasContent(endpoint) && StringUtils.hasContent(username)) {
                credentials = new AMFCredentials(endpoint, username, password, context);
                amfConnection = credentials.login();
            } else {
                amfConnection = new SoapUIAMFConnection();
                amfConnection.connect(context.expand(amfRequest.getEndpoint()));
            }

            context.setProperty(AMF_CONNECTION, amfConnection);
            return amfConnection;
        } else {
            amfConnection = new SoapUIAMFConnection();
            amfConnection.connect(context.expand(amfRequest.getEndpoint()));
            return amfConnection;
        }
    }

    private boolean isAuthorisationEnabled(AMFRequest amfRequest) {
        return getTestCaseConfig(amfRequest).getAmfAuthorisation();
    }

    private TestCaseConfig getTestCaseConfig(AMFRequest amfRequest) {
        return amfRequest.getTestStep().getTestCase().getConfig();
    }

    private void addHttpHeaders(AMFRequest amfRequest, SoapUIAMFConnection amfConnection) {
        StringToStringsMap httpHeaders = amfRequest.getHttpHeaders();
        if (httpHeaders != null) {
            for (String key : httpHeaders.getKeys()) {
                for (String value : httpHeaders.get(key)) {
                    amfConnection.addHttpRequestHeader(key, context.expand(value));
                }
            }
        }
    }

    private void addAmfHeaders(AMFRequest amfRequest, SoapUIAMFConnection amfConnection) {
        if (amfRequest.getAmfHeaders() != null) {
            for (String key : amfRequest.getAmfHeaders().keySet()) {
                Object data = amfRequest.getAmfHeaders().get(key);
                if (data instanceof String) {
                    data = context.expand((String) data);
                }

                amfConnection.addAmfHeader(key, data);
            }
        }
    }

    public Exception getError() {
        return error;
    }

    public AMFRequest getRequest() {
        return request;
    }

    public AMFResponse getResponse() {
        return response;
    }

    public Status getStatus() {
        return status;
    }

}
