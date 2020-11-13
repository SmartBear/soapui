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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.impl.support.AbstractMockResponse;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.WsdlContentPart;
import com.eviware.soapui.impl.wsdl.WsdlHeaderPart;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.filters.RemoveEmptyContentRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.AttachmentUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.BodyPartAttachment;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.FileAttachment;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.IconAnimator;
import com.eviware.soapui.impl.wsdl.support.MockFileAttachment;
import com.eviware.soapui.impl.wsdl.support.WsdlAttachment;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaConfig;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils.SoapHeader;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Attachment.AttachmentEncoding;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.settings.CommonSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ws.security.WSSecurityException;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;

import javax.swing.ImageIcon;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Message;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A WsdlMockResponse contained by a WsdlMockOperation
 *
 * @author ole.matzura
 */

public class WsdlMockResponse extends AbstractMockResponse<MockResponseConfig> implements WsaContainer {
    private final static Logger log = LogManager.getLogger(WsdlMockResponse.class);

    public final static String MOCKRESULT_PROPERTY = WsdlMockResponse.class.getName() + "@mockresult";
    public final static String HEADERS_PROPERTY = WsdlMockResponse.class.getName() + "@headers";
    public final static String DISABLE_MULTIPART_ATTACHMENTS = WsdlMockResponse.class.getName()
            + "@disable-multipart-attachments";
    public static final String FORCE_MTOM = WsdlMockResponse.class.getName() + "@force_mtom";
    public static final String ENABLE_INLINE_FILES = WsdlMockResponse.class.getName() + "@enable_inline_files";
    public final static String RESPONSE_DELAY_PROPERTY = WsdlMockResponse.class.getName() + "@response-delay";
    public static final String STRIP_WHITESPACES = WsdlMockResponse.class.getName() + "@strip-whitespaces";
    public static final String REMOVE_EMPTY_CONTENT = WsdlMockResponse.class.getName() + "@remove_empty_content";
    public static final String ENCODE_ATTACHMENTS = WsdlMockResponse.class.getName() + "@encode_attachments";
    public static final String OUGOING_WSS = WsdlMockResponse.class.getName() + "@outgoing-wss";
    public static final String ICON_NAME = "/mockResponse.gif";

    protected List<FileAttachment<WsdlMockResponse>> attachments = new ArrayList<FileAttachment<WsdlMockResponse>>();
    private List<HttpAttachmentPart> definedAttachmentParts;
    private IconAnimator<WsdlMockResponse> iconAnimator;
    private WsaConfig wsaConfig;

    public WsdlMockResponse(WsdlMockOperation operation, MockResponseConfig config) {
        super(config, operation, ICON_NAME);

        for (AttachmentConfig ac : getConfig().getAttachmentList()) {
            attachments.add(new MockFileAttachment(ac, this));
        }

        if (!config.isSetEncoding()) {
            config.setEncoding("UTF-8");
        }

        iconAnimator = new IconAnimator<WsdlMockResponse>(this, "/mockResponse.gif", "/exec_request.png", 4);
    }

    @Override
    public void setConfig(MockResponseConfig config) {
        super.setConfig(config);

        if (wsaConfig != null) {
            if (config.isSetWsaConfig()) {
                wsaConfig.setConfig(config.getWsaConfig());
            } else {
                wsaConfig = null;
            }
        }
    }

    public Attachment[] getAttachments() {
        return attachments.toArray(new Attachment[attachments.size()]);
    }

    protected String getContentType(Operation operation, String encoding) {
        SoapVersion soapVersion = ((WsdlOperation) operation).getInterface().getSoapVersion();
        return soapVersion.getContentTypeHttpHeader(encoding, null);
    }

    @Override
    public ImageIcon getIcon() {
        return iconAnimator.getIcon();
    }

    public WsdlMockOperation getMockOperation() {
        return (WsdlMockOperation) getParent();
    }

    @Override
    public String getScriptHelpUrl() {
        return HelpUrls.MOCKRESPONSE_SCRIPT_HELP_URL;
    }

    public MessagePart[] getRequestParts() {
        try {
            List<MessagePart> result = new ArrayList<MessagePart>();
            result.addAll(Arrays.asList(getMockOperation().getOperation().getDefaultRequestParts()));

            if (getMockResult() != null) {
                result.addAll(AttachmentUtils.extractAttachmentParts((WsdlOperation) getMockOperation().getOperation(), getMockResult()
                        .getMockRequest().getRequestContent(), true, false, isMtomEnabled()));
            }

            return result.toArray(new MessagePart[result.size()]);
        } catch (Exception e) {
            SoapUI.logError(e);
            return new MessagePart[0];
        }
    }

    public MessagePart[] getResponseParts() {
        try {
            // init
            WsdlOperation op = (WsdlOperation) getMockOperation().getOperation();
            if (op == null || op.isUnidirectional()) {
                return new MessagePart[0];
            }

            List<MessagePart> result = new ArrayList<MessagePart>();
            WsdlContext wsdlContext = op.getInterface().getWsdlContext();
            BindingOperation bindingOperation = op.findBindingOperation(wsdlContext.getDefinition());

            if (bindingOperation == null) {
                return new MessagePart[0];
            }

            // header parts
            BindingOutput bindingOutput = bindingOperation.getBindingOutput();
            List<SoapHeader> headers = bindingOutput == null ? new ArrayList<SoapHeader>() : WsdlUtils
                    .getSoapHeaders(bindingOutput.getExtensibilityElements());

            for (int i = 0; i < headers.size(); i++) {
                SoapHeader header = headers.get(i);

                Message message = wsdlContext.getDefinition().getMessage(header.getMessage());
                if (message == null) {
                    log.error("Missing message for header: " + header.getMessage());
                    continue;
                }

                javax.wsdl.Part part = message.getPart(header.getPart());

                if (part != null) {
                    SchemaType schemaType = WsdlUtils.getSchemaTypeForPart(wsdlContext, part);
                    SchemaGlobalElement schemaElement = WsdlUtils.getSchemaElementForPart(wsdlContext, part);
                    if (schemaType != null) {
                        result.add(new WsdlHeaderPart(part.getName(), schemaType, part.getElementName(), schemaElement));
                    }
                } else {
                    log.error("Missing part for header; " + header.getPart());
                }
            }

            // content parts
            javax.wsdl.Part[] parts = WsdlUtils.getOutputParts(bindingOperation);

            for (int i = 0; i < parts.length; i++) {
                javax.wsdl.Part part = parts[i];

                if (!WsdlUtils.isAttachmentOutputPart(part, bindingOperation)) {
                    SchemaType schemaType = WsdlUtils.getSchemaTypeForPart(wsdlContext, part);
                    SchemaGlobalElement schemaElement = WsdlUtils.getSchemaElementForPart(wsdlContext, part);
                    if (schemaType != null) {
                        result.add(new WsdlContentPart(part.getName(), schemaType, part.getElementName(), schemaElement));
                    }
                }
            }

            result.addAll(Arrays.asList(getDefinedAttachmentParts()));

            return result.toArray(new MessagePart[result.size()]);
        } catch (Exception e) {
            SoapUI.logError(e);
            return new MessagePart[0];
        }
    }

    public Attachment attachFile(File file, boolean cache) throws IOException {
        FileAttachment<WsdlMockResponse> fileAttachment = new MockFileAttachment(file, cache, this);
        attachments.add(fileAttachment);
        notifyPropertyChanged(ATTACHMENTS_PROPERTY, null, fileAttachment);
        return fileAttachment;
    }

    public int getAttachmentCount() {
        return attachments.size();
    }

    public WsdlAttachment getAttachmentAt(int index) {
        return attachments.get(index);
    }

    public void removeAttachment(Attachment attachment) {
        int ix = attachments.indexOf(attachment);
        attachments.remove(ix);

        try {
            notifyPropertyChanged(ATTACHMENTS_PROPERTY, attachment, null);
        } finally {
            getConfig().removeAttachment(ix);
        }
    }

    public HttpAttachmentPart[] getDefinedAttachmentParts() {
        if (definedAttachmentParts == null) {
            try {
                WsdlOperation operation = (WsdlOperation) getMockOperation().getOperation();
                if (operation == null) {
                    definedAttachmentParts = new ArrayList<HttpAttachmentPart>();
                } else {
                    UISupport.setHourglassCursor();
                    definedAttachmentParts = AttachmentUtils.extractAttachmentParts(operation, getResponseContent(), true,
                            true, isMtomEnabled());
                }
            } catch (Exception e) {
                log.warn(e.toString());
            } finally {
                UISupport.resetCursor();
            }
        }

        return definedAttachmentParts.toArray(new HttpAttachmentPart[definedAttachmentParts.size()]);
    }

    public HttpAttachmentPart getAttachmentPart(String partName) {
        HttpAttachmentPart[] parts = getDefinedAttachmentParts();
        for (HttpAttachmentPart part : parts) {
            if (part.getName().equals(partName)) {
                return part;
            }
        }

        return null;
    }

    public Attachment[] getAttachmentsForPart(String partName) {
        List<Attachment> result = new ArrayList<Attachment>();

        for (Attachment attachment : attachments) {
            if (attachment.getPart().equals(partName)) {
                result.add(attachment);
            }
        }

        return result.toArray(new Attachment[result.size()]);
    }

    @Override
    public String getContentType() {
        return getContentType(getOperation(), getEncoding());
    }

    public boolean isMtomEnabled() {
        return getSettings().getBoolean(WsdlSettings.ENABLE_MTOM);
    }

    public void setMtomEnabled(boolean mtomEnabled) {
        boolean old = isMtomEnabled();
        getSettings().setBoolean(WsdlSettings.ENABLE_MTOM, mtomEnabled);
        definedAttachmentParts = null;
        notifyPropertyChanged(MTOM_NABLED_PROPERTY, old, mtomEnabled);
    }

    protected String executeSpecifics(MockRequest request, String responseContent, WsdlMockRunContext context) throws IOException, WSSecurityException {
        if (this.getWsaConfig().isWsaEnabled()) {
            WsdlOperation operation = getMockOperation().getOperation();
            WsaUtils wsaUtils = new WsaUtils(responseContent, getSoapVersion(), operation, context);
            responseContent = wsaUtils.addWSAddressingMockResponse(this, (WsdlMockRequest) request);
        }

        String outgoingWss = getOutgoingWss();
        if (StringUtils.isNullOrEmpty(outgoingWss)) {
            outgoingWss = getMockOperation().getMockService().getOutgoingWss();
        }

        if (StringUtils.hasContent(outgoingWss)) {
            OutgoingWss outgoing = getMockOperation().getMockService().getProject().getWssContainer()
                    .getOutgoingWssByName(outgoingWss);
            if (outgoing != null) {
                Document dom = XmlUtils.parseXml(responseContent);
                outgoing.processOutgoing(dom, context);
                StringWriter writer = new StringWriter();
                XmlUtils.serialize(dom, writer);
                responseContent = writer.toString();
            }
        }
        return responseContent;
    }

    @SuppressWarnings("unchecked")
    public Attachment addAttachment(Attachment attachment) {
        if (attachment instanceof BodyPartAttachment) {
            try {
                BodyPartAttachment att = (BodyPartAttachment) attachment;

                AttachmentConfig newConfig = getConfig().addNewAttachment();
                newConfig.setData(Tools.readAll(att.getInputStream(), 0).toByteArray());
                newConfig.setContentId(att.getContentID());
                newConfig.setContentType(att.getContentType());
                newConfig.setName(att.getName());

                FileAttachment<WsdlMockResponse> newAttachment = new MockFileAttachment(newConfig, this);
                attachments.add(newAttachment);
                return newAttachment;
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        } else if (attachment instanceof FileAttachment) {
            AttachmentConfig oldConfig = ((FileAttachment<WsdlMockResponse>) attachment).getConfig();
            AttachmentConfig newConfig = (AttachmentConfig) getConfig().addNewAttachment().set(oldConfig);
            FileAttachment<WsdlMockResponse> newAttachment = new MockFileAttachment(newConfig, this);
            attachments.add(newAttachment);
            return newAttachment;
        }

        return null;
    }

    public void setResponseDelay(long delay) {
        long oldDelay = getResponseDelay();

        if (delay == 0) {
            getSettings().clearSetting(RESPONSE_DELAY_PROPERTY);
        } else {
            getSettings().setLong(RESPONSE_DELAY_PROPERTY, delay);
        }

        notifyPropertyChanged(RESPONSE_DELAY_PROPERTY, oldDelay, delay);
    }

    public long getResponseDelay() {
        return getSettings().getLong(RESPONSE_DELAY_PROPERTY, 0);
    }

    public long getContentLength() {
        return getResponseContent() == null ? 0 : getResponseContent().length();
    }

    public boolean isMultipartEnabled() {
        return !getSettings().getBoolean(DISABLE_MULTIPART_ATTACHMENTS);
    }

    public void setMultipartEnabled(boolean multipartEnabled) {
        getSettings().setBoolean(DISABLE_MULTIPART_ATTACHMENTS, multipartEnabled);
    }

    public void setEntitizeProperties(boolean entitizeProperties) {
        getSettings().setBoolean(CommonSettings.ENTITIZE_PROPERTIES, entitizeProperties);
    }

    public boolean isForceMtom() {
        return getSettings().getBoolean(FORCE_MTOM);
    }

    public void setForceMtom(boolean forceMtom) {
        boolean old = getSettings().getBoolean(FORCE_MTOM);
        getSettings().setBoolean(FORCE_MTOM, forceMtom);
        notifyPropertyChanged(FORCE_MTOM, old, forceMtom);
    }

    protected String removeEmptyContent(String responseContent) {
        if (isRemoveEmptyContent()) {
            responseContent = RemoveEmptyContentRequestFilter.removeEmptyContent(responseContent, getSoapVersion()
                    .getEnvelopeNamespace(), true);
        }
        return responseContent;
    }

    @Override
    public void setResponseContent(String responseContent) {
        super.setResponseContent(responseContent);

        handleFault(responseContent);
    }

    private void handleFault(String responseContent) {
        try {
            if (SoapUtils.isSoapFault(responseContent)) {
                setResponseHttpStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (XmlException e) {
            SoapUI.logError(e);
        }
    }


    public boolean isRemoveEmptyContent() {
        return getSettings().getBoolean(REMOVE_EMPTY_CONTENT);
    }

    public void setRemoveEmptyContent(boolean removeEmptyContent) {
        boolean old = getSettings().getBoolean(REMOVE_EMPTY_CONTENT);
        getSettings().setBoolean(REMOVE_EMPTY_CONTENT, removeEmptyContent);
        notifyPropertyChanged(REMOVE_EMPTY_CONTENT, old, removeEmptyContent);
    }

    public boolean isEncodeAttachments() {
        return getSettings().getBoolean(ENCODE_ATTACHMENTS);
    }

    public void setEncodeAttachments(boolean encodeAttachments) {
        boolean old = getSettings().getBoolean(ENCODE_ATTACHMENTS);
        getSettings().setBoolean(ENCODE_ATTACHMENTS, encodeAttachments);
        notifyPropertyChanged(ENCODE_ATTACHMENTS, old, encodeAttachments);
    }

    public boolean isStripWhitespaces() {
        return getSettings().getBoolean(STRIP_WHITESPACES);
    }

    public void setStripWhitespaces(boolean stripWhitespaces) {
        boolean old = getSettings().getBoolean(STRIP_WHITESPACES);
        getSettings().setBoolean(STRIP_WHITESPACES, stripWhitespaces);
        notifyPropertyChanged(STRIP_WHITESPACES, old, stripWhitespaces);
    }

    public boolean isInlineFilesEnabled() {
        return getSettings().getBoolean(WsdlMockResponse.ENABLE_INLINE_FILES);
    }

    public void setInlineFilesEnabled(boolean inlineFilesEnabled) {
        getSettings().setBoolean(WsdlMockResponse.ENABLE_INLINE_FILES, inlineFilesEnabled);
    }

    @Override
    public void beforeSave() {
        super.beforeSave();

        CompressedStringSupport.setString(getConfig().getResponseContent(), getResponseContent());
    }

    public void addAttachmentsChangeListener(PropertyChangeListener listener) {
        addPropertyChangeListener(ATTACHMENTS_PROPERTY, listener);
    }

    public boolean isReadOnly() {
        return false;
    }

    public void removeAttachmentsChangeListener(PropertyChangeListener listener) {
        removePropertyChangeListener(ATTACHMENTS_PROPERTY, listener);
    }

    public SoapVersion getSoapVersion() {
        if( getMockOperation().getOperation() == null ) {
            return SoapVersion.Soap11;
        }
        return getMockOperation().getOperation().getInterface().getSoapVersion();
    }

    public PropertyExpansion[] getPropertyExpansions() {
        List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this, this, "responseContent"));

        StringToStringsMap responseHeaders = getResponseHeaders();
        for (Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
            for (String value : headerEntry.getValue()) {
                result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this,
                        new ResponseHeaderHolder(headerEntry.getKey(), value, this), "value"));
            }
        }

        addWsaPropertyExpansions(result, getWsaConfig(), this);
        return result.toArray(new PropertyExpansion[result.size()]);
    }

    public void addWsaPropertyExpansions(List<PropertyExpansion> result, WsaConfig wsaConfig, ModelItem modelItem) {
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

    public static class ResponseHeaderHolder {
        private final String key;
        private final String oldValue;
        private WsdlMockResponse mockResponse;

        public ResponseHeaderHolder(String key, String oldValue, WsdlMockResponse mockResponse) {
            this.key = key;
            this.oldValue = oldValue;
            this.mockResponse = mockResponse;
        }

        public String getValue() {
            return oldValue;
        }

        public void setValue(String value) {
            StringToStringsMap valueMap = mockResponse.getResponseHeaders();
            valueMap.replace(key, oldValue, value);
            mockResponse.setResponseHeaders(valueMap);
        }
    }


    public String getOutgoingWss() {
        return getConfig().getOutgoingWss();
    }

    public void setOutgoingWss(String outgoingWss) {
        String old = getOutgoingWss();
        getConfig().setOutgoingWss(outgoingWss);
        notifyPropertyChanged(OUGOING_WSS, old, outgoingWss);
    }

    public String getPropertiesLabel() {
        return "Custom Properties";
    }

    public AttachmentEncoding getAttachmentEncoding(String partName) {
        HttpAttachmentPart attachmentPart = getAttachmentPart(partName);
        if (attachmentPart == null) {
            return AttachmentUtils.getAttachmentEncoding(getOperation(), partName, true);
        } else {
            return AttachmentUtils.getAttachmentEncoding(getOperation(), attachmentPart, true);
        }
    }

    public WsaConfig getWsaConfig() {
        if (wsaConfig == null) {
            if (!getConfig().isSetWsaConfig()) {
                getConfig().addNewWsaConfig();
            }
            wsaConfig = new WsaConfig(getConfig().getWsaConfig(), this);
            // wsaConfig.setGenerateMessageId(true);
        }
        return wsaConfig;
    }

    public boolean isWsAddressing() {
        return getConfig().getUseWsAddressing();
    }

    public void setWsAddressing(boolean wsAddressing) {
        boolean old = getConfig().getUseWsAddressing();
        getConfig().setUseWsAddressing(wsAddressing);
        notifyPropertyChanged("wsAddressing", old, wsAddressing);
    }

    public boolean isWsaEnabled() {
        return isWsAddressing();
    }

    public void setWsaEnabled(boolean arg0) {
        setWsAddressing(arg0);

    }

    public WsdlOperation getOperation() {
        return ((WsdlMockOperation) getMockOperation()).getOperation();
    }

    public void setOperation(WsdlOperation operation) {
        ((WsdlMockOperation) getMockOperation()).setOperation(operation);
    }

    protected String mockresultProperty() {
        return MOCKRESULT_PROPERTY;
    }
}
