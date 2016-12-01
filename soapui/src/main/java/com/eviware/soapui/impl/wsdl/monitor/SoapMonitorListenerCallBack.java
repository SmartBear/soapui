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

package com.eviware.soapui.impl.wsdl.monitor;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import org.apache.http.HttpRequest;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Prakash
 */
public class SoapMonitorListenerCallBack {
    private SoapUIListenerSupport<MonitorListener> listeners = new SoapUIListenerSupport<MonitorListener>(
            MonitorListener.class);

    public void fireAddMessageExchange(WsdlMonitorMessageExchange messageExchange) {
        fireOnMessageExchange(messageExchange);
    }

    public void fireOnMessageExchange(WsdlMonitorMessageExchange messageExchange) {
        for (MonitorListener listener : listeners.get()) {
            try {
                listener.onMessageExchange(messageExchange);
            } catch (Throwable t) {
                SoapUI.logError(t);
            }
        }
    }

    public void fireOnRequest(WsdlProject project, ServletRequest request, ServletResponse response) {
        for (MonitorListener listener : listeners.get()) {
            try {
                listener.onRequest(project, request, response);
            } catch (Throwable t) {
                SoapUI.logError(t);
            }
        }
    }

    public void fireBeforeProxy(WsdlProject project, ServletRequest request, ServletResponse response, HttpRequest httpRequest) {
        for (MonitorListener listener : listeners.get()) {
            try {
                listener.beforeProxy(project, request, response, httpRequest);
            } catch (Throwable t) {
                SoapUI.logError(t);
            }
        }
    }

    public void fireAfterProxy(WsdlProject project, ServletRequest request, ServletResponse response, HttpRequest httpRequest,
                               WsdlMonitorMessageExchange capturedData) {
        for (MonitorListener listener : listeners.get()) {
            try {
                listener.afterProxy(project, request, response, httpRequest, capturedData);
            } catch (Throwable t) {
                SoapUI.logError(t);
            }
        }
    }

    public void addSoapMonitorListener(MonitorListener listener) {
        listeners.add(listener);
    }

    public void removeSoapMonitorListener(MonitorListener listener) {
        listeners.remove(listener);
    }

    public static class SoapUIListenerSupport<T> {
        private Set<T> listeners = new HashSet<T>();
        @SuppressWarnings("unused")
        private final Class<T> listenerClass;

        public SoapUIListenerSupport(Class<T> listenerClass) {
            this.listenerClass = listenerClass;
            listeners.addAll(SoapUI.getListenerRegistry().getListeners(listenerClass));
        }

        public void add(T listener) {
            listeners.add(listener);
        }

        public void remove(T listener) {
            listeners.remove(listener);
        }

        public Collection<T> get() {
            return listeners;
        }
    }
}
