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

package com.eviware.soapui.impl.support.http;

import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.impl.rest.RestRequest.ParameterMessagePart;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.AbstractHttpOperation;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.jms.header.JMSHeaderConfig;
import com.eviware.soapui.impl.wsdl.support.jms.property.JMSPropertiesConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.MessagePart.ContentPart;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpRequest extends AbstractHttpRequest<HttpRequestConfig> implements
        HttpRequestInterface<HttpRequestConfig> {

    private XmlBeansRestParamsTestPropertyHolder params;

    protected HttpRequest(HttpRequestConfig config, boolean forLoadTest) {
        super(config, null, "/http_request_step.png", forLoadTest);

        if (config.getParameters() == null) {
            config.addNewParameters();
        }

        params = new XmlBeansRestParamsTestPropertyHolder(this, config.getParameters());
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

    @Override
    public void addTestPropertyListener(TestPropertyListener listener) {
        params.addTestPropertyListener(listener);
    }

    public ModelItem getModelItem() {
        return this;
    }

    public String getMediaType() {
        return getConfig().getMediaType() != null ? getConfig().getMediaType() : "application/xml";
    }

    public String getPath() {
        return getEndpoint();
    }

    public boolean hasRequestBody() {
        RestRequestInterface.HttpMethod method = getMethod();
        return method == RestRequestInterface.HttpMethod.POST || method == RestRequestInterface.HttpMethod.PUT
                || method == RestRequestInterface.HttpMethod.PATCH || method == RestRequestInterface.HttpMethod.DELETE
                || method == RestRequestInterface.HttpMethod.PROPFIND || method == RestRequestInterface.HttpMethod.LOCK;
    }

    @Override
    public RestParamsPropertyHolder getParams() {
        return params;
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

    public boolean isPostQueryString() {
        return hasRequestBody() && getConfig().getPostQueryString();
    }

    public boolean hasProperty(String name) {
        return params.hasProperty(name);
    }

    public void setPropertyValue(String name, String value) {
        params.setPropertyValue(name, value);
    }

    public void setMediaType(String mediaType) {
        String old = getMediaType();
        getConfig().setMediaType(mediaType);
        notifyPropertyChanged(MEDIA_TYPE, old, mediaType);
    }

    public void setPostQueryString(boolean b) {
        boolean old = isPostQueryString();
        getConfig().setPostQueryString(b);
        notifyPropertyChanged("postQueryString", old, b);

        if (!("multipart/form-data".equals(getMediaType()) || "multipart/mixed".equals(getMediaType()))) {
            setMediaType(b ? "application/x-www-form-urlencoded" : getMediaType());
        }
    }

    public void setMethod(RestRequestInterface.HttpMethod method) {
        RestRequestInterface.HttpMethod old = getMethod();
        getConfig().setMethod(method.toString());
        setIcon(UISupport.createImageIcon("/" + method.toString().toLowerCase() + "_method.gif"));
        notifyPropertyChanged("method", old, method);
    }

    public void setDownloadIncludedResources(boolean downloadIncludedResources) {
        boolean old = getDownloadIncludedResources();
        getConfig().setDownloadIncludedResources(downloadIncludedResources);
        notifyPropertyChanged("downloadIncludedResources", old, downloadIncludedResources);
    }

    public boolean getDownloadIncludedResources() {
        return getConfig().getDownloadIncludedResources();
    }

    public String getPropertiesLabel() {
        return "HTTP Params";
    }

    public void removeTestPropertyListener(TestPropertyListener listener) {
        params.removeTestPropertyListener(listener);
    }

    public HttpAttachmentPart getAttachmentPart(String partName) {
        return null;
    }

    public HttpAttachmentPart[] getDefinedAttachmentParts() {
        return new HttpAttachmentPart[0];
    }

    @Override
    public RestRequestInterface.HttpMethod getMethod() {
        String method = getConfig().getMethod();
        return method == null ? null : RestRequestInterface.HttpMethod.valueOf(method);
    }

    public MessagePart[] getRequestParts() {
        List<MessagePart> result = new ArrayList<MessagePart>();

        for (int c = 0; c < getPropertyCount(); c++) {
            result.add(new ParameterMessagePart(getPropertyAt(c)));
        }

        if (getMethod() == RestRequestInterface.HttpMethod.POST
                || getMethod() == RestRequestInterface.HttpMethod.PUT
                || getMethod() == RestRequestInterface.HttpMethod.PATCH
                || getMethod() == RestRequestInterface.HttpMethod.DELETE
                || getMethod() == RestRequestInterface.HttpMethod.PROPFIND
                || getMethod() == RestRequestInterface.HttpMethod.LOCK) {
            result.add(new HttpContentPart());
        }

        return result.toArray(new MessagePart[result.size()]);
    }

    public MessagePart[] getResponseParts() {
        return new MessagePart[0];
    }

    public String getResponseContentAsXml() {
        HttpResponse response = getResponse();
        if (response == null) {
            return null;
        }

        return response.getContentAsXml();
    }

    public WsdlSubmit<HttpRequest> submit(SubmitContext submitContext, boolean async) throws SubmitException {
        String endpoint = PropertyExpander.expandProperties(submitContext, getEndpoint());

        if (StringUtils.isNullOrEmpty(endpoint)) {
            UISupport.showErrorMessage("Missing endpoint for request [" + getName() + "]");
            return null;
        }

        try {
            WsdlSubmit<HttpRequest> submitter = new WsdlSubmit<HttpRequest>(this, getSubmitListeners(),
                    RequestTransportRegistry.getTransport(endpoint, submitContext));
            submitter.submitRequest(submitContext, async);
            return submitter;
        } catch (Exception e) {
            throw new SubmitException(e.toString());
        }
    }

    public void updateConfig(HttpRequestConfig request) {
        setConfig(request);
        if (params == null) {
            params = new XmlBeansRestParamsTestPropertyHolder(this, request.getParameters());
        } else {
            params.resetPropertiesConfig(request.getParameters());
        }

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

    public AbstractHttpOperation getOperation() {
        return null;
    }

    public class HttpContentPart extends ContentPart implements MessagePart {
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

    public List<TestProperty> getPropertyList() {
        return params.getPropertyList();
    }

    private JMSHeaderConfig jmsHeaderConfig;
    private JMSPropertiesConfig jmsPropertyConfig;

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

    public void notifyPropertyChanged(String responseContentProperty, String oldContent, String responseContent) {
        notifyPropertyChanged(responseContentProperty, (Object) oldContent, (Object) responseContent);
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

    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(this, this);
        result.addAll(super.getPropertyExpansions());
        result.addAll(params.getPropertyExpansions());

        return result.toArray();
    }

    public boolean isSendEmptyParameters() {
        return getSettings().getBoolean("sendEmptyParameters");
    }

    public void setSendEmptyParameters(boolean sendEmptyParameters) {
        getSettings().setBoolean("sendEmptyParameters", sendEmptyParameters);
    }
}
