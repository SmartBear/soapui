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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Submit implementation for submitting a WsdlRequest
 *
 * @author Ole.Matzura
 */

public final class WsdlSubmit<T extends AbstractHttpRequestInterface<?>> implements Runnable, Submit {
    private final static Logger logger = LogManager.getLogger(WsdlSubmit.class);
    private T request;
    private SubmitListener[] listeners;
    private Status status;
    private Exception error;
    private Response response;
    private volatile Future<?> future;
    private SubmitContext submitContext;
    private RequestTransport transport;

    public WsdlSubmit(T wsdlRequest, SubmitListener[] listeners, RequestTransport transport) {
        this.request = wsdlRequest;
        this.transport = transport;

        List<SubmitListener> regListeners = SoapUI.getListenerRegistry().getListeners(SubmitListener.class);

        this.listeners = new SubmitListener[listeners.length + regListeners.size()];
        for (int c = 0; c < listeners.length; c++) {
            this.listeners[c] = listeners[c];
        }

        for (int c = 0; c < regListeners.size(); c++) {
            this.listeners[listeners.length + c] = regListeners.get(c);
        }

        error = null;
        status = Status.INITIALIZED;
        future = null;
    }

    public void submitRequest(SubmitContext submitContext, boolean async) {
        this.submitContext = submitContext;

        if (async && future != null) {
            throw new RuntimeException("Submit already running");
        }

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

        logger.info("Canceling request...");
        if (status == Status.RUNNING) {
            transport.abortRequest(submitContext);
        }

        status = Status.CANCELED;

        notifyListenersAfterSubmit();
    }

    private void notifyListenersAfterSubmit() {
        for (SubmitListener listener : listeners) {
            try {
                listener.afterSubmit(this, submitContext);
            } catch (Throwable e) { //NOSONAR
                SoapUI.logError(e);
            }
        }
    }

    public void run() {
        try {
            submitContext.setProperty(RequestTransport.REQUEST_TRANSPORT, transport);
            submitContext.setProperty(RequestTransport.WSDL_REQUEST, request);

            boolean shouldAbort = notifyListenersBeforeSubmit();
            if (shouldAbort) {
                return;
            }

            status = Status.RUNNING;
            response = transport.sendRequest(submitContext, request);

            if (status != Status.CANCELED) {
                status = Status.FINISHED;
            }

            if (response != null) {
                if (response.getTimeTaken() == 0) {
                    logger.warn("Request took 0 in thread " + Thread.currentThread().getId() + ", response length = "
                            + response.getContentLength());
                }
            } else {
                logger.warn("Request does not have a response");
            }
        } catch (Exception e1) {
            error = e1;

            if (status != Status.CANCELED) {
                status = Status.ERROR;
                logger.error("Exception in request: " + e1);
                SoapUI.logError(e1);
            }
            if (response == null) {
                response = (Response) submitContext.getProperty(BaseHttpRequestTransport.RESPONSE);
            }
        } finally {
            if (status != Status.CANCELED) {
                notifyListenersAfterSubmit();
            }
        }
    }

    private boolean notifyListenersBeforeSubmit() {
        for (SubmitListener listener : listeners) {
            try {
                if (!listener.beforeSubmit(this, submitContext)) {
                    status = Status.CANCELED;
                    System.err.println("listener cancelled submit...");
                    return true;
                }
            } catch (Throwable e) {
                SoapUI.logError(e, "Error in SubmitListener");
            }
        }
        return false;
    }

    public T getRequest() {
        return request;
    }

    public Status getStatus() {
        return status;
    }

    public Exception getError() {
        return error;
    }

    public synchronized Status waitUntilFinished() {
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

    public Response getResponse() {
        return response;
    }
}
