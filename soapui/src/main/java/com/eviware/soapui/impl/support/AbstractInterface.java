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

package com.eviware.soapui.impl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.EndpointsConfig;
import com.eviware.soapui.config.InterfaceConfig;
import com.eviware.soapui.impl.support.definition.support.AbstractDefinitionContext;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.InterfaceListener;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractInterface<T extends InterfaceConfig> extends AbstractWsdlModelItem<T> implements
        Interface {
    private Set<InterfaceListener> interfaceListeners = new HashSet<InterfaceListener>();

    protected AbstractInterface(T config, ModelItem parent, String icon) {
        super(config, parent, icon);

        if (config.getEndpoints() == null) {
            config.addNewEndpoints();
        }

        for (InterfaceListener listener : SoapUI.getListenerRegistry().getListeners(InterfaceListener.class)) {
            addInterfaceListener(listener);
        }

        if (!config.isSetDefinitionCache()) {
            config.addNewDefinitionCache();
        }
    }

    public WsdlProject getProject() {
        return (WsdlProject) getParent();
    }

    public T getConfig() {
        return super.getConfig();
    }

    public List<? extends ModelItem> getChildren() {
        return getOperationList();
    }

    public String[] getEndpoints() {
        EndpointsConfig endpoints = getConfig().getEndpoints();
        List<String> endpointArray = endpoints.getEndpointList();
        Collections.sort(endpointArray);
        return endpointArray.toArray(new String[endpointArray.size()]);
    }

    public void addEndpoint(String endpoint) {
        if (endpoint == null || endpoint.trim().length() == 0) {
            return;
        }

        endpoint = endpoint.trim();
        String[] endpoints = getEndpoints();

        // dont add the same endpoint twice
        if (Arrays.asList(endpoints).contains(endpoint)) {
            return;
        }

        getConfig().getEndpoints().addNewEndpoint().setStringValue(endpoint);

        notifyPropertyChanged(ENDPOINT_PROPERTY, null, endpoint);
    }

    public void changeEndpoint(String oldEndpoint, String newEndpoint) {
        if (oldEndpoint == null || oldEndpoint.trim().length() == 0) {
            return;
        }
        if (newEndpoint == null || newEndpoint.trim().length() == 0) {
            return;
        }

        EndpointsConfig endpoints = getConfig().getEndpoints();

        for (int c = 0; c < endpoints.sizeOfEndpointArray(); c++) {
            if (endpoints.getEndpointArray(c).equals(oldEndpoint)) {
                endpoints.setEndpointArray(c, newEndpoint);
                notifyPropertyChanged(ENDPOINT_PROPERTY, oldEndpoint, newEndpoint);
                break;
            }
        }
    }

    public void removeEndpoint(String endpoint) {
        EndpointsConfig endpoints = getConfig().getEndpoints();

        for (int c = 0; c < endpoints.sizeOfEndpointArray(); c++) {
            if (endpoints.getEndpointArray(c).equals(endpoint)) {
                endpoints.removeEndpoint(c);
                notifyPropertyChanged(ENDPOINT_PROPERTY, endpoint, null);
                break;
            }
        }
    }

    public void fireOperationAdded(Operation operation) {
        InterfaceListener[] a = interfaceListeners.toArray(new InterfaceListener[interfaceListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].operationAdded(operation);
        }
    }

    public void fireOperationUpdated(Operation operation) {
        InterfaceListener[] a = interfaceListeners.toArray(new InterfaceListener[interfaceListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].operationUpdated(operation);
        }
    }

    public void fireOperationRemoved(Operation operation) {
        InterfaceListener[] a = interfaceListeners.toArray(new InterfaceListener[interfaceListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].operationRemoved(operation);
        }
    }

    public void fireRequestAdded(Request request) {
        InterfaceListener[] a = interfaceListeners.toArray(new InterfaceListener[interfaceListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].requestAdded(request);
        }
    }

    public void fireRequestRemoved(Request request) {
        InterfaceListener[] a = interfaceListeners.toArray(new InterfaceListener[interfaceListeners.size()]);

        for (int c = 0; c < a.length; c++) {
            a[c].requestRemoved(request);
        }
    }

    public void addInterfaceListener(InterfaceListener listener) {
        interfaceListeners.add(listener);
    }

    public void removeInterfaceListener(InterfaceListener listener) {
        interfaceListeners.remove(listener);
    }

    @Override
    public void release() {
        super.release();

        interfaceListeners.clear();
    }

    @SuppressWarnings("unchecked")
    public abstract AbstractDefinitionContext getDefinitionContext();

    /**
     * Return the URL for the current definition (ie a WSDL or WADL url)
     */

    public abstract String getDefinition();

    public abstract String getType();

    public abstract boolean isDefinitionShareble();

    public Operation[] getAllOperations() {
        return getOperationList().toArray(new Operation[getOperationCount()]);
    }
}
