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
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.config.WsdlRequestConfig;
import com.eviware.soapui.config.WsrmVersionTypeConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.AttachmentUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSHeader;
import com.eviware.soapui.impl.wsdl.support.jms.header.JMSHeaderConfig;
import com.eviware.soapui.impl.wsdl.support.jms.property.JMSPropertiesConfig;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaConfig;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmConfig;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmContainer;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment.AttachmentEncoding;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.support.InterfaceListenerAdapter;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Request implementation holding a SOAP request
 *
 * @author Ole.Matzura
 */

public class WsdlRequest extends AbstractHttpRequest<WsdlRequestConfig> implements WsdlAttachmentContainer,
        PropertyExpansionContainer, WsaContainer, WsrmContainer, PropertyChangeListener {
    public final static Logger log = LogManager.getLogger(WsdlRequest.class);

    public static final String RESPONSE_CONTENT_PROPERTY = WsdlRequest.class.getName() + "@response-content";
    public static final String INLINE_RESPONSE_ATTACHMENTS = WsdlRequest.class.getName()
            + "@inline-response-attachments";
    public static final String EXPAND_MTOM_RESPONSE_ATTACHMENTS = WsdlRequest.class.getName()
            + "@expand-mtom-attachments";
    public static final String FORCE_MTOM = WsdlRequest.class.getName() + "@force_mtom";
    public static final String ENABLE_INLINE_FILES = WsdlRequest.class.getName() + "@enable_inline_files";
    public static final String SKIP_SOAP_ACTION = WsdlRequest.class.getName() + "@skip_soap_action";
    public static final String ENCODE_ATTACHMENTS = WsdlRequest.class.getName() + "@encode_attachments";
    public static final String WSS_TIMETOLIVE = WsdlRequest.class.getName() + "@wss-time-to-live";
    public static final String OPERATION_PROPERTY = WsdlRequest.class.getName() + "@operation";
    public static final String INCOMING_WSS = WsdlRequest.class.getName() + "@incoming-wss";
    public static final String OUGOING_WSS = WsdlRequest.class.getName() + "@outgoing-wss";

    public final static String PW_TYPE_NONE = "None";
    public final static String PW_TYPE_DIGEST = "PasswordDigest";
    public final static String PW_TYPE_TEXT = "PasswordText";

    private WsdlOperation operation;
    private List<HttpAttachmentPart> definedAttachmentParts;
    private InternalInterfaceListener interfaceListener = new InternalInterfaceListener();

    private WsaConfig wsaConfig;
    private WsrmConfig wsrmConfig;

    public WsdlRequest(WsdlOperation operation, WsdlRequestConfig callConfig) {
        this(operation, callConfig, false);
    }

    public WsdlRequest(WsdlOperation operation, WsdlRequestConfig callConfig, boolean forLoadTest) {
        super(callConfig, operation, null, forLoadTest);

        this.operation = operation;

        initEndpoints();

        // ensure encoding
        if (callConfig.getEncoding() == null || callConfig.getEncoding().length() == 0) {
            callConfig.setEncoding("UTF-8");
        }

        if (!forLoadTest) {
            operation.getInterface().addPropertyChangeListener(interfaceListener);
            operation.getInterface().addInterfaceListener(interfaceListener);
        }
    }

    public void updateConfig(WsdlRequestConfig request) {
        setConfig(request);

        if (wsaConfig != null) {
            wsaConfig.setConfig(request.getWsaConfig());
        }

        if (wsrmConfig != null) {
            wsrmConfig.setWsrmConfig(request.getWsrmConfig());
        }

        if (jmsHeaderConfig != null) {
            jmsHeaderConfig.setJMSHeaderConfConfig(request.getJmsConfig());
        }

        if (jmsPropertyConfig != null) {
            jmsPropertyConfig.setJmsPropertyConfConfig(request.getJmsPropertyConfig());
        }
    }

    protected void initEndpoints() {
        if (getEndpoint() == null) {
            String[] endpoints = operation.getInterface().getEndpoints();
            if (endpoints.length > 0) {
                setEndpoint(endpoints[0]);
            }
        }
    }

    public boolean isInlineResponseAttachments() {
        return getSettings().getBoolean(INLINE_RESPONSE_ATTACHMENTS);
    }

    public void setInlineResponseAttachments(boolean inlineResponseAttachments) {
        boolean old = getSettings().getBoolean(INLINE_RESPONSE_ATTACHMENTS);
        getSettings().setBoolean(INLINE_RESPONSE_ATTACHMENTS, inlineResponseAttachments);
        notifyPropertyChanged(INLINE_RESPONSE_ATTACHMENTS, old, inlineResponseAttachments);
    }

    public boolean isExpandMtomResponseAttachments() {
        return getSettings().getBoolean(EXPAND_MTOM_RESPONSE_ATTACHMENTS);
    }

    public void setExpandMtomResponseAttachments(boolean expandMtomResponseAttachments) {
        boolean old = getSettings().getBoolean(EXPAND_MTOM_RESPONSE_ATTACHMENTS);
        getSettings().setBoolean(EXPAND_MTOM_RESPONSE_ATTACHMENTS, expandMtomResponseAttachments);
        notifyPropertyChanged(EXPAND_MTOM_RESPONSE_ATTACHMENTS, old, expandMtomResponseAttachments);
    }

    /**
     * Use getResponse().getContentAsString();
     *
     * @deprecated
     */

    @Deprecated
    public String getResponseContent() {
        return getResponse() == null ? null : getResponse().getContentAsString();
    }

    public WsdlResponse getResponse() {
        return (WsdlResponse) super.getResponse();
    }

    public WsdlOperation getOperation() {
        return operation;
    }

    public void setOperation(WsdlOperation wsdlOperation) {
        WsdlOperation oldOperation = operation;
        this.operation = wsdlOperation;

        definedAttachmentParts = null;
        notifyPropertyChanged(OPERATION_PROPERTY, oldOperation, operation);
    }

    public void setRequestContent(String request) {
        definedAttachmentParts = null;
        super.setRequestContent(request);
    }

    // public void setResponse( WsdlResponse response, SubmitContext context )
    // {
    // WsdlResponse oldResponse = getResponse();
    // this.response = response;
    //
    // notifyPropertyChanged( RESPONSE_PROPERTY, oldResponse, response );
    // }

    public WsdlSubmit<WsdlRequest> submit(SubmitContext submitContext, boolean async) throws SubmitException {
        String endpoint = PropertyExpander.expandProperties(submitContext, getEndpoint());
        if (endpoint == null || endpoint.trim().length() == 0) {
            UISupport.showErrorMessage("Missing endpoint for request [" + getName() + "]");
            return null;
        }

        try {
            WsdlSubmit<WsdlRequest> submitter = new WsdlSubmit<WsdlRequest>(this, getSubmitListeners(),
                    RequestTransportRegistry.getTransport(endpoint, submitContext));
            submitter.submitRequest(submitContext, async);
            return submitter;
        } catch (Exception e) {
            throw new SubmitException(e.toString());
        }
    }

    private class InternalInterfaceListener extends InterfaceListenerAdapter implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(Interface.ENDPOINT_PROPERTY)) {
                String endpoint = getEndpoint();
                if (evt.getOldValue() != null && evt.getOldValue().equals(endpoint)) {
                    setEndpoint((String) evt.getNewValue());
                }
            }
        }
    }

    public String getWssPasswordType() {
        String wssPasswordType = getConfig().getWssPasswordType();
        return StringUtils.isNullOrEmpty(wssPasswordType) || PW_TYPE_NONE.equals(wssPasswordType) ? null
                : wssPasswordType;
    }

    public void setWssPasswordType(String wssPasswordType) {
        if (wssPasswordType == null || wssPasswordType.equals(PW_TYPE_NONE)) {
            if (getConfig().isSetWssPasswordType()) {
                getConfig().unsetWssPasswordType();
            }
        } else {
            getConfig().setWssPasswordType(wssPasswordType);
        }
    }

	/*
     * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.AttachmentContainer#getDefinedAttachmentParts
	 * ()
	 */

    public synchronized HttpAttachmentPart[] getDefinedAttachmentParts() {
        if (definedAttachmentParts == null) {
            try {
                UISupport.setHourglassCursor();
                definedAttachmentParts = AttachmentUtils.extractAttachmentParts(operation, getRequestContent(), true,
                        false, isMtomEnabled());
            } catch (Exception e) {
                log.warn(e.toString());
                definedAttachmentParts = new ArrayList<HttpAttachmentPart>();
            } finally {
                UISupport.resetCursor();
            }
        }

        return definedAttachmentParts.toArray(new HttpAttachmentPart[definedAttachmentParts.size()]);
    }

    public RestRequestInterface.HttpMethod getMethod() {
        return RestRequestInterface.HttpMethod.POST;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.impl.wsdl.AttachmentContainer#getAttachmentPart(java
     * .lang.String)
     */
    public HttpAttachmentPart getAttachmentPart(String partName) {
        HttpAttachmentPart[] parts = getDefinedAttachmentParts();
        for (HttpAttachmentPart part : parts) {
            if (part.getName().equals(partName)) {
                return part;
            }
        }

        return null;
    }

    public void copyTo(WsdlRequest newRequest, boolean copyAttachments, boolean copyHeaders) {
        newRequest.setEncoding(getEncoding());
        newRequest.setEndpoint(getEndpoint());
        newRequest.setRequestContent(getRequestContent());
        newRequest.setWssPasswordType(getWssPasswordType());

        CredentialsConfig credentials = getConfig().getCredentials();
        if (credentials != null) {
            newRequest.getConfig().setCredentials((CredentialsConfig) credentials.copy());
        }

        if (copyAttachments) {
            copyAttachmentsTo(newRequest);
        }

        if (copyHeaders) {
            newRequest.setRequestHeaders(getRequestHeaders());
        }

        // ((DefaultWssContainer)newRequest.getWssContainer()).updateConfig( (
        // WSSConfigConfig ) getConfig().getWssConfig().copy() );
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.impl.wsdl.AttachmentContainer#isMtomEnabled()
     */
    public boolean isMtomEnabled() {
        return getSettings().getBoolean(WsdlSettings.ENABLE_MTOM);
    }

    public void setMtomEnabled(boolean mtomEnabled) {
        getSettings().setBoolean(WsdlSettings.ENABLE_MTOM, mtomEnabled);
        definedAttachmentParts = null;
    }

    public boolean isInlineFilesEnabled() {
        return getSettings().getBoolean(WsdlRequest.ENABLE_INLINE_FILES);
    }

    public void setInlineFilesEnabled(boolean inlineFilesEnabled) {
        getSettings().setBoolean(WsdlRequest.ENABLE_INLINE_FILES, inlineFilesEnabled);
    }

    public boolean isSkipSoapAction() {
        return getSettings().getBoolean(WsdlRequest.SKIP_SOAP_ACTION);
    }

    public void setSkipSoapAction(boolean skipSoapAction) {
        getSettings().setBoolean(WsdlRequest.SKIP_SOAP_ACTION, skipSoapAction);
    }

    @Override
    public void release() {
        super.release();

        getOperation().getInterface().removeInterfaceListener(interfaceListener);
        getOperation().getInterface().removePropertyChangeListener(interfaceListener);
    }

    public MessagePart[] getRequestParts() {
        try {
            List<MessagePart> result = new ArrayList<MessagePart>();
            result.addAll(Arrays.asList(getOperation().getDefaultRequestParts()));
            result.addAll(Arrays.asList(getDefinedAttachmentParts()));

            return result.toArray(new MessagePart[result.size()]);
        } catch (Exception e) {
            SoapUI.logError(e);
            return new MessagePart[0];
        }
    }

    public MessagePart[] getResponseParts() {
        try {
            List<MessagePart> result = new ArrayList<MessagePart>();
            result.addAll(Arrays.asList(getOperation().getDefaultResponseParts()));

            if (getResponse() != null) {
                result.addAll(AttachmentUtils.extractAttachmentParts(getOperation(), getResponse().getContentAsString(),
                        true, true, isMtomEnabled()));
            }

            return result.toArray(new MessagePart[result.size()]);
        } catch (Exception e) {
            SoapUI.logError(e);
            return new MessagePart[0];
        }
    }

    public String getWssTimeToLive() {
        return getSettings().getString(WSS_TIMETOLIVE, null);
    }

    public void setWssTimeToLive(String ttl) {
        getSettings().setString(WSS_TIMETOLIVE, ttl);
    }

    public long getContentLength() {
        return getRequestContent().length();
    }

    public boolean isForceMtom() {
        return getSettings().getBoolean(FORCE_MTOM);
    }

    public void setForceMtom(boolean forceMtom) {
        boolean old = getSettings().getBoolean(FORCE_MTOM);
        getSettings().setBoolean(FORCE_MTOM, forceMtom);
        notifyPropertyChanged(FORCE_MTOM, old, forceMtom);
    }

    public boolean isEncodeAttachments() {
        return getSettings().getBoolean(ENCODE_ATTACHMENTS);
    }

    public void setEncodeAttachments(boolean encodeAttachments) {
        boolean old = getSettings().getBoolean(ENCODE_ATTACHMENTS);
        getSettings().setBoolean(ENCODE_ATTACHMENTS, encodeAttachments);
        notifyPropertyChanged(ENCODE_ATTACHMENTS, old, encodeAttachments);
    }

    public String getIncomingWss() {
        return getConfig().getIncomingWss();
    }

    public void setIncomingWss(String incomingWss) {
        String old = getIncomingWss();
        getConfig().setIncomingWss(incomingWss);
        notifyPropertyChanged(INCOMING_WSS, old, incomingWss);
    }

    public String getOutgoingWss() {
        return getConfig().getOutgoingWss();
    }

    public void setOutgoingWss(String outgoingWss) {
        String old = getOutgoingWss();
        getConfig().setOutgoingWss(outgoingWss);
        notifyPropertyChanged(OUGOING_WSS, old, outgoingWss);
    }

    public boolean isWsAddressing() {
        return getConfig().getUseWsAddressing();
    }

    public void setWsAddressing(boolean wsAddressing) {
        boolean old = getConfig().getUseWsAddressing();
        getConfig().setUseWsAddressing(wsAddressing);
        notifyPropertyChanged("wsAddressing", old, wsAddressing);
    }

    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(this, this);
        result.addAll(super.getPropertyExpansions());

        StringToStringsMap requestHeaders = getRequestHeaders();
        for (Map.Entry<String, List<String>> headerEntry : requestHeaders.entrySet()) {
            for (String value : headerEntry.getValue()) {
                result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this,
                        new HttpTestRequestStep.RequestHeaderHolder(headerEntry.getKey(), value, this), "value"));
            }
        }
        addWsaPropertyExpansions(result, getWsaConfig(), this);
        addJMSHeaderExpansions(result, getJMSHeaderConfig(), this);
        return result.toArray();
    }

    public void addWsaPropertyExpansions(PropertyExpansionsResult result, WsaConfig wsaConfig, ModelItem modelItem) {
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, wsaConfig, "action"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, wsaConfig, "from"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, wsaConfig, "to"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, wsaConfig, "replyTo"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, wsaConfig, "replyToRefParams"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, wsaConfig, "faultTo"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, wsaConfig, "faultToRefParams"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, wsaConfig, "relatesTo"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, wsaConfig, "relationshipType"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, wsaConfig, "messageID"));
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
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, jmsHeaderConfig,
                JMSHeader.DURABLE_SUBSCRIPTION_NAME));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, jmsHeaderConfig, JMSHeader.CLIENT_ID));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, jmsHeaderConfig,
                JMSHeader.SEND_AS_BYTESMESSAGE));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, jmsHeaderConfig,
                JMSHeader.SOAP_ACTION_ADD));
    }

    public AttachmentEncoding getAttachmentEncoding(String partName) {
        HttpAttachmentPart attachmentPart = getAttachmentPart(partName);
        if (attachmentPart == null) {
            return AttachmentUtils.getAttachmentEncoding(getOperation(), partName, false);
        } else {
            return AttachmentUtils.getAttachmentEncoding(getOperation(), attachmentPart, false);
        }
    }

    public WsaConfig getWsaConfig() {
        if (wsaConfig == null) {
            if (!getConfig().isSetWsaConfig()) {
                getConfig().addNewWsaConfig();
            }
            wsaConfig = new WsaConfig(getConfig().getWsaConfig(), this);
        }
        return wsaConfig;
    }

    public ModelItem getModelItem() {
        return this;
    }

    public boolean isWsaEnabled() {
        return isWsAddressing();
    }

    public void setWsaEnabled(boolean arg0) {
        setWsAddressing(arg0);
    }

    public boolean isWsReliableMessaging() {
        return getConfig().getUseWsReliableMessaging();
    }

    public void setWsReliableMessaging(boolean wsReliableMessaging) {
        boolean old = getConfig().getUseWsReliableMessaging();
        getConfig().setUseWsReliableMessaging(wsReliableMessaging);
        notifyPropertyChanged("wsReliableMessaging", old, wsReliableMessaging);
    }

    public WsrmConfig getWsrmConfig() {
        if (wsrmConfig == null) {
            if (!getConfig().isSetWsrmConfig()) {
                getConfig().addNewWsrmConfig();
            }
            wsrmConfig = new WsrmConfig(getConfig().getWsrmConfig(), this);
            wsrmConfig.addPropertyChangeListener("version", this);
        }
        return wsrmConfig;
    }

    public boolean isWsrmEnabled() {
        return isWsReliableMessaging();
    }

    public void setWsrmEnabled(boolean arg0) {
        setWsReliableMessaging(arg0);
    }

    public String getResponseContentAsXml() {
        return getResponse() == null ? null : getResponse().getContentAsString();
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

    public String getAction() {
        if (isWsaEnabled() && StringUtils.hasContent(getWsaConfig().getAction())) {
            return getWsaConfig().getAction();
        }

        return getOperation().getAction();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == wsrmConfig && evt.getPropertyName().equals("version")) {
            if (evt.getNewValue().equals(WsrmVersionTypeConfig.X_1_0.toString())) {
                getWsaConfig().setVersion(WsaVersionTypeConfig.X_200408.toString());
            }
        }
    }
}
