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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.StringToStringMapConfig;
import com.eviware.soapui.impl.rest.RestRepresentation.Type;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestRequestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.handlers.JsonMediaTypeHandler;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSHeader;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.jms.header.JMSHeaderConfig;
import com.eviware.soapui.impl.wsdl.support.jms.property.JMSPropertiesConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.MessagePart.ContentPart;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlString;

import javax.xml.namespace.QName;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Request implementation holding a SOAP request
 *
 * @author Ole.Matzura
 */

public class RestRequest extends AbstractHttpRequest<RestRequestConfig> implements RestRequestInterface {
    static final String ACCEPT_HEADER_NAME = "Accept";

    private RestMethod method;
    private RestRequestParamsPropertyHolder params;
    private ParamUpdater paramUpdater;
    private JMSHeaderConfig jmsHeaderConfig;
    private JMSPropertiesConfig jmsPropertyConfig;

    public RestRequest(RestMethod method, RestRequestConfig requestConfig, boolean forLoadTest) {
        super(requestConfig, method.getOperation(), "/rest_request_step.png", forLoadTest);
        this.method = method;

        if (requestConfig.getParameters() == null) {
            requestConfig.addNewParameters();
        }

        StringToStringMap paramValues = StringToStringMap.fromXml(requestConfig.getParameters());
        params = new RestRequestParamsPropertyHolder(method.getOverlayParams(), this, paramValues);
        paramUpdater = new ParamUpdater(paramValues);
        params.addTestPropertyListener(paramUpdater);

        method.addPropertyChangeListener(this);
        if (requestConfig.getMediaType() == null) {
            String defaultMediaType = getRestMethod().getDefaultRequestMediaType();
            getConfig().setMediaType(defaultMediaType);
        }
        cleanUpAcceptEncoding();
    }

    public ModelItem getParent() {
        return getRestMethod();
    }

    public RestMethod getRestMethod() {
        return method;
    }

    protected RequestIconAnimator<?> initIconAnimator() {
        return new RequestIconAnimator<AbstractHttpRequest<?>>(this, "/rest_request.gif", "/exec_rest_request.gif", 4);
    }

    public MessagePart[] getRequestParts() {
        List<MessagePart> result = new ArrayList<MessagePart>();

        for (int c = 0; c < getPropertyCount(); c++) {
            result.add(new ParameterMessagePart(getPropertyAt(c)));
        }

        if (getMethod() == HttpMethod.POST
                || getMethod() == HttpMethod.PUT
                || getMethod() == HttpMethod.PATCH) {
            result.add(new RestContentPart());
        }

        return result.toArray(new MessagePart[result.size()]);
    }

    public RestRepresentation[] getRepresentations() {
        return getRepresentations(null, null);
    }

    public RestRepresentation[] getRepresentations(RestRepresentation.Type type) {
        return getRepresentations(type, null);
    }

    public RestRepresentation[] getRepresentations(RestRepresentation.Type type, String mediaType) {
        return getRestMethod().getRepresentations(type, mediaType);
    }

    public MessagePart[] getResponseParts() {
        return new MessagePart[0];
    }

    public HttpMethod getMethod() {
        return getRestMethod().getMethod();
    }

    public String getAccept() {
        String accept = getConfig().getAccept();
        return accept == null ? "" : accept;
    }

    public void setAccept(String acceptEncoding) {
        String old = getAccept();
        getConfig().setAccept(acceptEncoding);
        notifyPropertyChanged("accept", old, acceptEncoding);
    }

    public void setMediaType(String mediaType) {
        String old = getMediaType();
        getConfig().setMediaType(mediaType);
        notifyPropertyChanged(MEDIA_TYPE, old, mediaType);
    }

    public String getMediaType() {
        return getConfig().getMediaType();
    }

    public void setMethod(HttpMethod method) {
        getRestMethod().setMethod(method);
    }

    public WsdlSubmit<RestRequest> submit(SubmitContext submitContext, boolean async) throws SubmitException {
        String endpoint = PropertyExpander.expandProperties(submitContext, getEndpoint());

        if (StringUtils.isNullOrEmpty(endpoint)) {
            try {
                endpoint = new URL(getPath()).toString();
            } catch (MalformedURLException ignore) {
            }
        }

        if (StringUtils.isNullOrEmpty(endpoint)) {
            UISupport.showErrorMessage("Missing endpoint for request [" + getName() + "]");
            return null;
        }

        try {
            WsdlSubmit<RestRequest> submitter = new WsdlSubmit<RestRequest>(this, getSubmitListeners(),
                    RequestTransportRegistry.getTransport(endpoint, submitContext));
            submitter.submitRequest(submitContext, async);
            addPropertyChangeListener(AbstractHttpRequest.RESPONSE_PROPERTY, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getNewValue() != null) {
                        setOriginalUriInConfig((HttpResponse) evt.getNewValue());
                    }
                }
            });
            return submitter;
        } catch (Exception e) {
            throw new SubmitException(e.toString());
        }
    }

    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(this, this);
        result.addAll(super.getPropertyExpansions());
        result.addAll(getRestMethod().getPropertyExpansions());
        result.addAll(params.getPropertyExpansions());
        addJMSHeaderExpansions(result, getJMSHeaderConfig(), this);

        return result.toArray();
    }

    public void addJMSHeaderExpansions(PropertyExpansionsResult result, JMSHeaderConfig jmsHeaderConfig,
                                       ModelItem modelItem) {
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, jmsHeaderConfig,
                JMSHeader.JMSCORRELATIONID));
        result.addAll(PropertyExpansionUtils
                .extractPropertyExpansions(modelItem, jmsHeaderConfig, JMSHeader.JMSREPLYTO));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, jmsHeaderConfig, JMSHeader.JMSTYPE));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, jmsHeaderConfig,
                JMSHeader.JMSPRIORITY));
        result.addAll(PropertyExpansionUtils
                .extractPropertyExpansions(modelItem, jmsHeaderConfig, JMSHeader.TIMETOLIVE));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, jmsHeaderConfig,
                JMSHeader.DURABLE_SUBSCRIPTION_NAME));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, jmsHeaderConfig, JMSHeader.CLIENT_ID));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, jmsHeaderConfig,
                JMSHeader.SEND_AS_BYTESMESSAGE));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, jmsHeaderConfig,
                JMSHeader.SOAP_ACTION_ADD));

    }

    public TestProperty addProperty(String name) {
        return params.addProperty(name);
    }

    public void moveProperty(String propertyName, int targetIndex) {
        params.moveProperty(propertyName, targetIndex);
    }

    public TestProperty removeProperty(String propertyName) {
        return params.removeProperty(propertyName);
    }

    public boolean renameProperty(String name, String newName) {
        return params.renameProperty(name, newName);
    }

    public void addTestPropertyListener(TestPropertyListener listener) {
        params.addTestPropertyListener(listener);
    }

    public ModelItem getModelItem() {
        return this;
    }

    @Override
    public RestResource getOperation() {
        return method.getOperation();
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

    public void resetPropertyValues() {
        params.clear();
        for (String name : params.getPropertyNames()) {
            params.getProperty(name).setValue(params.getProperty(name).getDefaultValue());
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("path")) {
            notifyPropertyChanged("path", null, getPath());
        } else if (evt.getPropertyName().equals("method")) {
            notifyPropertyChanged("method", evt.getOldValue(), evt.getNewValue());
        }
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

    public boolean isPostQueryString() {
        return hasRequestBody() && getConfig().getPostQueryString();
    }

    public void setPostQueryString(boolean b) {
        boolean old = isPostQueryString();
        getConfig().setPostQueryString(b);
        notifyPropertyChanged("postQueryString", old, b);

        if (!("multipart/form-data".equals(getMediaType()) || "multipart/mixed".equals(getMediaType()))) {
            setMediaType(b ? "application/x-www-form-urlencoded" : getMediaType());
        }
    }

    public final static class ParameterMessagePart extends MessagePart.ParameterPart {
        private String name;

        public ParameterMessagePart(TestProperty propertyAt) {
            this.name = propertyAt.getName();
        }

        @Override
        public SchemaType getSchemaType() {
            return XmlString.type;
        }

        @Override
        public SchemaGlobalElement getPartElement() {
            return null;
        }

        @Override
        public QName getPartElementName() {
            return new QName(getName());
        }

        public String getDescription() {
            return null;
        }

        public String getName() {
            return name;
        }
    }

    public String getPropertiesLabel() {
        return "Request Params";
    }

    public RestParamsPropertyHolder getParams() {
        return params;
    }

    public HttpAttachmentPart getAttachmentPart(String partName) {
        return null;
    }

    public HttpAttachmentPart[] getDefinedAttachmentParts() {
        return new HttpAttachmentPart[0];
    }

    public class RestContentPart extends ContentPart implements MessagePart {
        @Override
        public SchemaGlobalElement getPartElement() {
            return null;
        }

        @Override
        public QName getPartElementName() {
            return null;
        }

        @Override
        public SchemaType getSchemaType() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public String getName() {
            return null;
        }

        public String getMediaType() {
            return getConfig().getMediaType();
        }
    }

    public boolean hasRequestBody() {
        return getRestMethod().hasRequestBody();
    }

    public RestResource getResource() {
        return getOperation();
    }

    public String getPath() {
        if (!StringUtils.isNullOrEmpty(getConfig().getFullPath()) || getResource() == null) {
            return getConfig().getFullPath();
        } else {
            return getResource().getFullPath();
        }
    }

    public void setPath(String fullPath) {
        String old = getPath();

        if (getResource() != null && getResource().getFullPath().equals(fullPath)) {
            getConfig().unsetFullPath();
        } else {
            getConfig().setFullPath(fullPath);
        }

        notifyPropertyChanged("path", old, fullPath);
    }

    public String getResponseContentAsXml() {
        HttpResponse response = getResponse();
        if (response == null) {
            return null;
        }

        return response.getContentAsXml();
    }

    @Override
    public void release() {
        super.release();

        if (method != null) {
            method.removePropertyChangeListener(this);
        }

        params.removeTestPropertyListener(paramUpdater);
        params.release();
    }

    public void updateConfig(RestRequestConfig request) {
        setConfig(request);

        updateParams();

        List<AttachmentConfig> attachmentConfigs = getConfig().getAttachmentList();
        for (int i = 0; i < attachmentConfigs.size(); i++) {
            AttachmentConfig config = attachmentConfigs.get(i);
            getAttachmentsList().get(i).updateConfig(config);
        }

        if (jmsHeaderConfig != null) {
            jmsHeaderConfig.setJMSHeaderConfConfig(request.getJmsConfig());
        }

        if (jmsPropertyConfig != null) {
            jmsPropertyConfig.setJmsPropertyConfConfig(request.getJmsPropertyConfig());
        }
    }

    protected void updateParams() {
        StringToStringMap paramValues = StringToStringMap.fromXml(getConfig().getParameters());
        params.reset(getRestMethod().getOverlayParams(), paramValues);
        paramUpdater.setValues(paramValues);
    }

    public boolean hasEndpoint() {
        return super.hasEndpoint() || PathUtils.isHttpPath(getPath());
    }

    private class ParamUpdater implements TestPropertyListener {
        private StringToStringMap values;

        public ParamUpdater(StringToStringMap paramValues) {
            values = paramValues;
        }

        public void setValues(StringToStringMap paramValues) {
            values = paramValues;
        }

        private void sync() {
            try {
                RestRequestConfig requestConfig = getConfig();
                requestConfig.setParameters(StringToStringMapConfig.Factory.parse(values.toXml()));
            } catch (XmlException e) {
                e.printStackTrace();
            }
        }

        public void propertyAdded(String name) {
            sync();
        }

        public void propertyMoved(String name, int oldIndex, int newIndex) {
        }

        public void propertyRemoved(String name) {
            sync();
        }

        public void propertyRenamed(String oldName, String newName) {
            sync();
        }

        public void propertyValueChanged(String name, String oldValue, String newValue) {
            sync();
        }

    }

    public List<TestProperty> getPropertyList() {
        return params.getPropertyList();
    }

    protected void setRestMethod(RestMethod restMethod) {
        if (this.method != null) {
            this.method.removePropertyChangeListener(this);
        }

        this.method = restMethod;

        if (method != null) {
            method.addPropertyChangeListener(this);
        }

        updateParams();
    }

    public JMSHeaderConfig getJMSHeaderConfig() {
        if (jmsHeaderConfig == null) {
            if (!getConfig().isSetJmsConfig()) {
                getConfig().addNewJmsConfig();
            }
            jmsHeaderConfig = new JMSHeaderConfig(getConfig().getJmsConfig(), this);
        }
        return jmsHeaderConfig;
    }

    public JMSPropertiesConfig getJMSPropertiesConfig() {
        if (jmsPropertyConfig == null) {
            if (!getConfig().isSetJmsPropertyConfig()) {
                getConfig().addNewJmsPropertyConfig();
            }
            jmsPropertyConfig = new JMSPropertiesConfig(getConfig().getJmsPropertyConfig(), this);
        }
        return jmsPropertyConfig;
    }

    @Override
    public String getMultiValueDelimiter() {
        return getConfig().getMultiValueDelimiter();
    }

    public void setMultiValueDelimiter(String delimiter) {
        String old = getMultiValueDelimiter();
        getConfig().setMultiValueDelimiter(delimiter);

        notifyPropertyChanged("multiValueDelimiter", old, delimiter);
    }


	/*
    Helper methods
	 */

    private void setOriginalUriInConfig(HttpResponse response) {
        if (getConfig().getOriginalUri() == null && response.getURL() != null) {
            getConfig().setOriginalUri(JsonMediaTypeHandler.makeNamespaceUriFrom(response.getURL()));
        }
    }

    private void cleanUpAcceptEncoding() {
        if (StringUtils.hasContent(getAccept())) {
            StringToStringsMap requestHeaders = getRequestHeaders();
            requestHeaders.add(ACCEPT_HEADER_NAME, getAccept());
            setRequestHeaders(requestHeaders);
            setAccept(null);
        }
    }
}
