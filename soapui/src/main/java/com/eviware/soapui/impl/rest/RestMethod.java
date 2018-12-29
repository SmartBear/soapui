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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RestMethodConfig;
import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.RestResourceRepresentationConfig;
import com.eviware.soapui.impl.rest.RestRepresentation.Type;
import com.eviware.soapui.impl.rest.support.OverlayRestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.ui.desktop.AbstractSoapUIDesktop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestMethod extends AbstractWsdlModelItem<RestMethodConfig> implements MutableTestPropertyHolder,
        PropertyChangeListener {
    private List<RestRequest> requests = new ArrayList<RestRequest>();
    private List<RestRepresentation> representations = new ArrayList<RestRepresentation>();
    private RestResource resource;
    private XmlBeansRestParamsTestPropertyHolder params;
    private RestParamsPropertyHolder overlayParams;

    private PropertyChangeListener representationPropertyChangeListener = new RepresentationPropertyChangeListener();
    private TestPropertyListener testPropertyListener = new InternalTestPropertyListener();

    public RestMethod(RestResource service, RestMethodConfig methodConfig) {
        super(methodConfig, service, "/"
                + (StringUtils.isNullOrEmpty(methodConfig.getMethod()) ? "get" : methodConfig.getMethod().toLowerCase())
                + "_method.gif");
        this.resource = service;

        if (methodConfig.getParameters() == null) {
            methodConfig.addNewParameters();
        }

        params = new XmlBeansRestParamsTestPropertyHolder(this, methodConfig.getParameters());

        for (RestResourceRepresentationConfig config : methodConfig.getRepresentationList()) {
            RestRepresentation representation = new RestRepresentation(this, config);
            representations.add(representation);
            notifyPropertyChanged("representations", null, representation);
        }

        for (RestRequestConfig config : methodConfig.getRequestList()) {
            RestRequest request = new RestRequest(this, config, false);
            requests.add(request);
            notifyPropertyChanged("childRequests", null, request);
        }

        addTestPropertyListener(testPropertyListener);
    }

    public RestParamsPropertyHolder getOverlayParams() {
        if (overlayParams == null) {
            overlayParams = new OverlayRestParamsPropertyHolder(buildOverlay(getResource()), params);
        }
        return overlayParams;
    }

    private RestParamsPropertyHolder buildOverlay(RestResource resource) {
        return resource.getParentResource() == null ? resource.getParams() : new OverlayRestParamsPropertyHolder(
                buildOverlay(resource.getParentResource()), resource.getParams());
    }

    public RestResource getOperation() {
        return resource;
    }

    public RestParamProperty addProperty(String name) {
        return params.addProperty(name);
    }

    public void moveProperty(String propertyName, int targetIndex) {
        params.moveProperty(propertyName, targetIndex);
    }

    public RestParamProperty removeProperty(String propertyName) {
        return params.removeProperty(propertyName);
    }

    public boolean renameProperty(String name, String newName) {
        return params.renameProperty(name, newName);
    }

    public void addTestPropertyListener(TestPropertyListener listener) {
        params.addTestPropertyListener(listener);
    }

    public RestParamsPropertyHolder getParams() {
        return params;
    }

    public ModelItem getModelItem() {
        return this;
    }

    public Map<String, TestProperty> getProperties() {
        return params.getProperties();
    }

    public RestParamProperty getProperty(String name) {
        return params.getProperty(name);
    }

    public RestParamProperty getPropertyAt(int index) {
        return params.getPropertyAt(index);
    }

    public int getPropertyCount() {
        return params.getPropertyCount();
    }

    public String[] getPropertyNames() {
        return params.getPropertyNames();
    }

    public String getPropertyValue(String name) {
        return params.getPropertyValue(name);
    }

    public boolean hasProperty(String name) {
        return params.hasProperty(name);
    }

    public void removeTestPropertyListener(TestPropertyListener listener) {
        params.removeTestPropertyListener(listener);
    }

    public void setPropertyValue(String name, String value) {
        params.setPropertyValue(name, value);
    }

    public String getPropertiesLabel() {
        return "Method Params";
    }

    public boolean hasRequestBody() {
        RestRequestInterface.HttpMethod method = getMethod();
        return method == RestRequestInterface.HttpMethod.POST || method == RestRequestInterface.HttpMethod.PUT
                || method == RestRequestInterface.HttpMethod.PATCH || method == RestRequestInterface.HttpMethod.DELETE
                || method == RestRequestInterface.HttpMethod.PROPFIND || method == RestRequestInterface.HttpMethod.LOCK;
    }

    public void propertyChange(PropertyChangeEvent arg0) {
        // TODO Auto-generated method stub

    }

    public PropertyExpansion[] getPropertyExpansions() {
        return params.getPropertyExpansions();
    }

    public RestRequestInterface.HttpMethod getMethod() {
        String method = getConfig().getMethod();
        return method == null ? null : RestRequestInterface.HttpMethod.valueOf(method);
    }

    public void setMethod(RestRequestInterface.HttpMethod method) {
        RestRequestInterface.HttpMethod old = getMethod();
        getConfig().setMethod(method.toString());
        setIcon(UISupport.createImageIcon("/" + method.toString().toLowerCase() + "_method.gif"));
        notifyPropertyChanged("method", old, method);
    }

    public String getDefaultRequestMediaType() {
        RestRepresentation[] representations = getRepresentations(RestRepresentation.Type.REQUEST, null);
        if (representations.length >= 1) {
            return representations[0].getMediaType();
        }
        return "application/json";
    }

    public RestRepresentation[] getRepresentations() {
        return getRepresentations(null, null);
    }

    public RestRepresentation[] getRepresentations(RestRepresentation.Type type, String mediaType) {
        List<RestRepresentation> result = new ArrayList<RestRepresentation>();
        Set<String> addedTypes = new HashSet<String>();

        for (RestRepresentation representation : representations) {
            if ((type == null || type == representation.getType())
                    && (mediaType == null || mediaType.equals(representation.getMediaType()))) {
                result.add(representation);
                addedTypes.add(representation.getMediaType());
            }
        }

        if (type == RestRepresentation.Type.REQUEST) {
            for (RestRequest request : requests) {
                for (Attachment attachment : request.getAttachments()) {
                    if ((mediaType == null || mediaType.equals(attachment.getContentType()))
                            && !addedTypes.contains(attachment.getContentType())) {
                        RestRepresentation representation = new RestRepresentation(this,
                                RestResourceRepresentationConfig.Factory.newInstance());
                        representation.setType(RestRepresentation.Type.REQUEST);
                        representation.setMediaType(attachment.getContentType());
                        result.add(representation);
                    }
                }
            }
        }

        return result.toArray(new RestRepresentation[result.size()]);
    }

    public String[] getResponseMediaTypes() {
        StringList result = new StringList();

        for (RestRepresentation representation : getRepresentations(Type.RESPONSE, null)) {
            if (!result.contains(representation.getMediaType())) {
                result.add(representation.getMediaType());
            }
        }

        return result.toStringArray();
    }

    public RestRepresentation addNewRepresentation(Type type) {
        RestRepresentation representation = new RestRepresentation(this, getConfig().addNewRepresentation());
        representation.setType(type);

        representation.addPropertyChangeListener(representationPropertyChangeListener);

        representations.add(representation);

        notifyPropertyChanged("representations", null, representation);

        return representation;
    }

    public void removeRepresentation(RestRepresentation representation) {
        int ix = representations.indexOf(representation);

        representations.remove(ix);
        representation.removePropertyChangeListener(representationPropertyChangeListener);

        notifyPropertyChanged("representations", representation, null);
        getConfig().removeRepresentation(ix);
        representation.release();
    }

    public void removeRequest(RestRequest request) {
        int ix = requests.indexOf(request);
        requests.remove(ix);

        try {
            (getInterface()).fireRequestRemoved(request);
            notifyPropertyChanged("childRequests", request, null);
        } finally {
            request.release();
            getConfig().removeRequest(ix);
        }
    }

    public RestResource getResource() {
        return resource;
    }

    public List<RestRequest> getRequestList() {
        return new ArrayList<RestRequest>(requests);
    }

    public RestRequest getRequestAt(int index) {
        return requests.get(index);
    }

    public RestRequest getRequestByName(String name) {
        return (RestRequest) getWsdlModelItemByName(requests, name);
    }

    public int getRequestCount() {
        return requests.size();
    }

    public RestRequest addNewRequest(String name) {
        RestRequestConfig requestConfig = getConfig().addNewRequest();
        requestConfig.setName(name);

        RestRequest request = new RestRequest(this, requestConfig, false);
        requests.add(request);
        request.resetPropertyValues();

        String[] endpoints = getInterface().getEndpoints();
        if (endpoints.length > 0) {
            request.setEndpoint(endpoints[0]);
        }

        notifyPropertyChanged("childRequests", null, request);
        return request;
    }

    public RestRequest cloneRequest(RestRequest request, String name) {
        request.beforeSave();
        RestRequestConfig requestConfig = (RestRequestConfig) getConfig().addNewRequest().set(request.getConfig());
        requestConfig.setName(name);

        RestRequest newRequest = new RestRequest(this, requestConfig, false);
        requests.add(newRequest);

        notifyPropertyChanged("childRequests", null, newRequest);
        return newRequest;
    }

    public RestParamProperty[] getDefaultParams() {
        List<RestParamProperty> result = new ArrayList<RestParamProperty>();
        Set<String> names = new HashSet<String>();

        result.addAll(Arrays.asList(resource.getDefaultParams()));

        for (int c = 0; c < getPropertyCount(); c++) {
            if (names.contains(getPropertyAt(c).getName())) {
                continue;
            }

            result.add(getPropertyAt(c));
            names.add(getPropertyAt(c).getName());
        }

        return result.toArray(new RestParamProperty[result.size()]);
    }

    public RestService getInterface() {
        return resource.getInterface();
    }

    public List<? extends ModelItem> getChildren() {
        return getRequestList();
    }

    @Override
    public void release() {
        ((AbstractSoapUIDesktop) SoapUI.getDesktop()).closeDependantPanels(this);
        super.release();
        for (int i = requests.size(); i > 0; i--) {
            requests.get(i - 1).release();
        }
        getOperation().removePropertyChangeListener(this);
        params.release();

        removeTestPropertyListener(testPropertyListener);
    }

    public List<TestProperty> getPropertyList() {
        return params.getPropertyList();
    }

    private class RepresentationPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(Request.MEDIA_TYPE)
                    && ((RestRepresentation) evt.getSource()).getType() == Type.RESPONSE) {
                RestMethod.this.notifyPropertyChanged("responseMediaTypes", null, getResponseMediaTypes());
            }
        }
    }

    private class InternalTestPropertyListener implements TestPropertyListener {
        public void propertyAdded(String name) {
        }

        public void propertyMoved(String name, int oldIndex, int newIndex) {
        }

        public void propertyRemoved(String name) {
        }

        public void propertyRenamed(String oldName, String newName) {
        }

        public void propertyValueChanged(String name, String oldValue, String newValue) {
            getProperty(name).setDefaultValue(newValue);
        }

    }

}
