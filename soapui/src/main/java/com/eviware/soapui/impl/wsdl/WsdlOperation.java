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
import com.eviware.soapui.config.AnonymousTypeConfig;
import com.eviware.soapui.config.OperationConfig;
import com.eviware.soapui.config.OperationTypesConfig;
import com.eviware.soapui.config.PartsConfig.Part;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.config.WsdlRequestConfig;
import com.eviware.soapui.impl.support.AbstractHttpOperation;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.AttachmentUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils.SoapHeader;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment.AttachmentEncoding;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.MessagePart.FaultPart;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.support.UISupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;

import javax.swing.ImageIcon;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.OperationType;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WSDL implementation of Operation, maps to a WSDL BindingOperation
 *
 * @author Ole.Matzura
 */

public class WsdlOperation extends AbstractWsdlModelItem<OperationConfig> implements AbstractHttpOperation {
    public static final String STYLE_DOCUMENT = "Document";
    public static final String STYLE_RPC = "RPC";

    public static final String ONE_WAY = "One-Way";
    public static final String NOTIFICATION = "Notification";
    public static final String REQUEST_RESPONSE = "Request-Response";
    public static final String SOLICIT_RESPONSE = "Solicit-Response";

    public final static Logger log = LogManager.getLogger(WsdlOperation.class);
    public static final String ICON_NAME = "/operation.png";
    private List<WsdlRequest> requests = new ArrayList<WsdlRequest>();
    private WsdlInterface iface;
    private ImageIcon oneWayIcon;

    private ImageIcon notificationIcon;

    private ImageIcon solicitResponseIcon;

    public WsdlOperation(WsdlInterface iface, OperationConfig operationConfig) {
        super(operationConfig, iface, ICON_NAME);
        this.iface = iface;

        if (!operationConfig.isSetIsOneWay()) {
            operationConfig.setIsOneWay(false);
        }

        List<WsdlRequestConfig> requestConfigs = getConfig().getCallList();
        for (WsdlRequestConfig config : requestConfigs) {
            requests.add(new WsdlRequest(this, config));
        }

        oneWayIcon = UISupport.createImageIcon("/onewayoperation.gif");
        notificationIcon = UISupport.createImageIcon("/notificationoperation.gif");
        solicitResponseIcon = UISupport.createImageIcon("/solicitresponseoperation.gif");
    }

    public String getAction() {
        String action = getConfig().getAction();
        return action == null ? "" : action;
    }

    public WsdlRequest getRequestAt(int index) {
        return requests.get(index);
    }

    public WsdlRequest getRequestByName(String requestName) {
        return (WsdlRequest) getWsdlModelItemByName(requests, requestName);
    }

    public int getRequestCount() {
        return requests.size();
    }

    @Override
    public ImageIcon getIcon() {
        if (isOneWay()) {
            return oneWayIcon;
        } else if (isSolicitResponse()) {
            return solicitResponseIcon;
        } else if (isNotification()) {
            return notificationIcon;
        } else {
            return super.getIcon();
        }
    }

    public WsdlRequest addNewRequest(String name) {
        WsdlRequest requestImpl = new WsdlRequest(this, getConfig().addNewCall());
        requestImpl.setName(name);
        requests.add(requestImpl);

        if (!getInterface().getWsaVersion().equals(WsaVersionTypeConfig.NONE.toString())) {
            requestImpl.setWsAddressing(true);
        }
        WsdlUtils.setDefaultWsaAction(requestImpl.getWsaConfig(), false);
        WsdlUtils.getAnonymous(this);

        (getInterface()).fireRequestAdded(requestImpl);
        return requestImpl;
    }

    public WsdlInterface getInterface() {
        return iface;
    }

    public void setAction(String soapAction) {
        String old = getAction();
        getConfig().setAction(soapAction);
        notifyPropertyChanged(ACTION_PROPERTY, old, soapAction);
    }

    public String createRequest(boolean buildOptional) {
        if (iface.getBindingName() == null) {
            UISupport.showErrorMessage("Missing binding name, please try to refresh "
                    + "Interface\nfor request generation to work correctly");
            return null;
        }

        if (getBindingOperationName() == null) {
            UISupport.showErrorMessage("Missing bindingOperation name, please try to refresh "
                    + "Interface\nfor request generation to work correctly");
            return null;
        }

        try {
            SoapMessageBuilder builder = iface.getMessageBuilder();
            BindingOperation bindingOperation = findBindingOperation(iface.getWsdlContext().getDefinition());

            if (bindingOperation == null) {
                UISupport.showErrorMessage("Failed to find bindingOperation, please try to refresh "
                        + "Interface\nfor request generation to work correctly");
                return null;
            }

            OperationType type = bindingOperation.getOperation().getStyle();
            if (OperationType.ONE_WAY.equals(type) || OperationType.REQUEST_RESPONSE.equals(type)) {
                return builder.buildSoapMessageFromInput(bindingOperation, buildOptional);
            } else {
                return builder.buildSoapMessageFromOutput(bindingOperation, buildOptional);
            }
        } catch (Exception e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public String createResponse(boolean buildOptional) {
        if (isUnidirectional()) {
            return null;
        }

        if (iface.getBindingName() == null) {
            UISupport.showErrorMessage("Missing binding name, please try to refresh "
                    + "Interface\nfor request generation to work correctly");
            return null;
        }

        if (getBindingOperationName() == null) {
            UISupport.showErrorMessage("Missing bindingOperation name, please try to refresh "
                    + "Interface\nfor request generation to work correctly");
            return null;
        }

        try {
            SoapMessageBuilder builder = iface.getMessageBuilder();
            BindingOperation bindingOperation = findBindingOperation(iface.getWsdlContext().getDefinition());

            if (bindingOperation == null) {
                UISupport.showErrorMessage("Failed to find bindingOperation, please try to refresh "
                        + "Interface\nfor request generation to work correctly");
                return null;
            }

            if (isRequestResponse()) {
                return builder.buildSoapMessageFromOutput(bindingOperation, buildOptional);
            } else {
                return builder.buildSoapMessageFromInput(bindingOperation, buildOptional);
            }
        } catch (Exception e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public BindingOperation findBindingOperation(Definition definition) {
        String bindingOperationName = getConfig().getBindingOperationName();
        return iface.findBindingOperation(definition, bindingOperationName, getInputName(), getOutputName());
    }

    public void removeRequest(WsdlRequest request) {
        int ix = requests.indexOf(request);
        requests.remove(ix);

        try {
            (getInterface()).fireRequestRemoved(request);
        } finally {
            request.release();
            getConfig().removeCall(ix);
        }
    }

    public OperationType getOperationType() {
        OperationConfig config = getConfig();

        // Backwards compatibility:
        if (!config.isSetType()) {
            if (config.getIsOneWay()) {
                config.setType(OperationTypesConfig.ONE_WAY);
                return OperationType.ONE_WAY;
            } else {
                config.setType(OperationTypesConfig.REQUEST_RESPONSE);
                return OperationType.REQUEST_RESPONSE;
            }
        }

        OperationTypesConfig.Enum type = config.getType();
        if (OperationTypesConfig.ONE_WAY.equals(type)) {
            return OperationType.ONE_WAY;
        } else if (OperationTypesConfig.NOTIFICATION.equals(type)) {
            return OperationType.NOTIFICATION;
        } else if (OperationTypesConfig.SOLICIT_RESPONSE.equals(type)) {
            return OperationType.SOLICIT_RESPONSE;
        } else {
            return OperationType.REQUEST_RESPONSE;
        }
    }

    public void setOperationType(OperationType type) {
        OperationConfig config = getConfig();
        if (type == null) {
            if (config.isSetType()) {
                config.unsetType();
            }
        } else {
            if (OperationType.ONE_WAY.equals(type)) {
                config.setType(OperationTypesConfig.ONE_WAY);
            } else if (OperationType.NOTIFICATION.equals(type)) {
                config.setType(OperationTypesConfig.NOTIFICATION);
            } else if (OperationType.SOLICIT_RESPONSE.equals(type)) {
                config.setType(OperationTypesConfig.SOLICIT_RESPONSE);
            } else {
                config.setType(OperationTypesConfig.REQUEST_RESPONSE);
            }
        }
    }

    public String getBindingOperationName() {
        return getConfig().getBindingOperationName();
    }

    public void setBindingOperationName(String name) {
        getConfig().setBindingOperationName(name);
    }

    public void setInputName(String name) {
        getConfig().setInputName(name);
    }

    public String getInputName() {
        String inputName = getConfig().getInputName();
        return inputName == null || inputName.trim().length() == 0 ? null : inputName;
    }

    public void setOutputName(String name) {
        if (name == null) {
            if (getConfig().isSetOutputName()) {
                getConfig().unsetOutputName();
            }
        } else {
            getConfig().setOutputName(name);
        }
    }

    public String getOutputName() {
        String outputName = getConfig().getOutputName();
        return outputName == null || outputName.trim().length() == 0 ? null : outputName;
    }

    public String getAnonymous() {
        if (getConfig().getAnonymous() != null) {
            if (getConfig().getAnonymous().equals(AnonymousTypeConfig.PROHIBITED)) {
                return AnonymousTypeConfig.PROHIBITED.toString();
            } else if (getConfig().getAnonymous().equals(AnonymousTypeConfig.REQUIRED)) {
                return AnonymousTypeConfig.REQUIRED.toString();
            }
        }

        return AnonymousTypeConfig.OPTIONAL.toString();
    }

    public void setAnonymous(String anonymous) {
        // getConfig().setAnonymous(AnonymousTypeConfig.Enum.forString(arg0));
        if (anonymous.equals(AnonymousTypeConfig.REQUIRED.toString())) {
            getConfig().setAnonymous(AnonymousTypeConfig.REQUIRED);
        } else if (anonymous.equals(AnonymousTypeConfig.PROHIBITED.toString())) {
            getConfig().setAnonymous(AnonymousTypeConfig.PROHIBITED);
        } else {
            getConfig().setAnonymous(AnonymousTypeConfig.OPTIONAL);
        }

    }

    public boolean isOneWay() {
        return OperationType.ONE_WAY.equals(getOperationType());
    }

    public boolean isNotification() {
        return OperationType.NOTIFICATION.equals(getOperationType());
    }

    public boolean isSolicitResponse() {
        return OperationType.SOLICIT_RESPONSE.equals(getOperationType());
    }

    public boolean isRequestResponse() {
        return OperationType.REQUEST_RESPONSE.equals(getOperationType());
    }

    public boolean isUnidirectional() {
        return isOneWay() || isNotification();
    }

    public boolean isBidirectional() {
        return !isUnidirectional();
    }

    public void initFromBindingOperation(BindingOperation operation) {
        setAction(WsdlUtils.getSoapAction(operation));
        setName(operation.getOperation().getName());
        setBindingOperationName(operation.getName());
        setOperationType(operation.getOperation().getStyle());

        BindingInput bindingInput = operation.getBindingInput();
        BindingOutput bindingOutput = operation.getBindingOutput();

        setOutputName(bindingOutput != null ? bindingOutput.getName() : null);
        setInputName(bindingInput != null ? bindingInput.getName() : null);

        initAttachments(operation);
    }

    @SuppressWarnings("unchecked")
    private void initAttachments(BindingOperation operation) {
        if (getConfig().isSetRequestParts()) {
            getConfig().unsetRequestParts();
        }

        if (getConfig().isSetResponseParts()) {
            getConfig().unsetResponseParts();
        }

        BindingOutput bindingOutput = operation.getBindingOutput();
        BindingInput bindingInput = operation.getBindingInput();

        if (bindingOutput != null) {
            MIMEMultipartRelated multipartOutput = WsdlUtils.getExtensiblityElement(
                    bindingOutput.getExtensibilityElements(), MIMEMultipartRelated.class);

            getConfig().setReceivesAttachments(multipartOutput != null);
            if (multipartOutput != null) {
                List<MIMEPart> parts = multipartOutput.getMIMEParts();
                Map<String, Part> partMap = new HashMap<String, Part>();

                for (int c = 0; c < parts.size(); c++) {
                    List<MIMEContent> contentParts = WsdlUtils.getExtensiblityElements(parts.get(c)
                            .getExtensibilityElements(), MIMEContent.class);

                    for (MIMEContent content : contentParts) {
                        Part part = partMap.get(content.getPart());
                        if (part != null) {
                            if (!part.getContentTypeList().contains(content.getType())) {
                                part.addContentType(content.getType());
                            }
                        } else {
                            if (!getConfig().isSetResponseParts()) {
                                getConfig().addNewResponseParts();
                            }

                            Part responsePart = getConfig().getResponseParts().addNewPart();
                            responsePart.addContentType(content.getType());
                            responsePart.setName(content.getPart());

                            partMap.put(responsePart.getName(), responsePart);
                        }
                    }
                }
            }
        }

        if (bindingInput != null) {
            MIMEMultipartRelated multipartInput = WsdlUtils.getExtensiblityElement(
                    bindingInput.getExtensibilityElements(), MIMEMultipartRelated.class);

            getConfig().setSendsAttachments(multipartInput != null);
            if (multipartInput != null) {
                List<MIMEPart> parts = multipartInput.getMIMEParts();
                Map<String, Part> partMap = new HashMap<String, Part>();

                for (int c = 0; c < parts.size(); c++) {
                    List<MIMEContent> contentParts = WsdlUtils.getExtensiblityElements(parts.get(c)
                            .getExtensibilityElements(), MIMEContent.class);

                    for (MIMEContent content : contentParts) {
                        Part part = partMap.get(content.getPart());
                        if (part != null) {
                            if (!part.getContentTypeList().contains(content.getType())) {
                                part.addContentType(content.getType());
                            }
                        } else {
                            if (!getConfig().isSetRequestParts()) {
                                getConfig().addNewRequestParts();
                            }

                            Part requestPart = getConfig().getRequestParts().addNewPart();
                            requestPart.addContentType(content.getType());
                            requestPart.setName(content.getPart());

                            partMap.put(requestPart.getName(), requestPart);
                        }
                    }
                }
            }
        }
    }

    public boolean getReceivesAttachments() {
        return getConfig().getReceivesAttachments();
    }

    public boolean getSendsAttachments() {
        return getConfig().getSendsAttachments();
    }

    @SuppressWarnings("unchecked")
    public QName getRequestBodyElementQName() throws Exception {
        WsdlInterface iface = getInterface();

        Definition definition = iface.getWsdlContext().getDefinition();
        BindingOperation bindingOperation = findBindingOperation(definition);
        if (WsdlUtils.isRpc(definition, bindingOperation)) {
            BindingInput bindingInput = bindingOperation.getBindingInput();
            if (bindingInput == null) {
                return null;
            }

            String ns = WsdlUtils.getSoapBodyNamespace(bindingInput.getExtensibilityElements());
            if (ns == null) {
                ns = WsdlUtils.getTargetNamespace(definition);
            }

            return new QName(ns, bindingOperation.getName());
        } else {
            Message message = bindingOperation.getOperation().getInput().getMessage();
            List<javax.wsdl.Part> parts = message.getOrderedParts(null);
            if (parts == null || parts.isEmpty()) {
                return null;
            }

            int ix = 0;
            javax.wsdl.Part part = parts.get(0);

            while (part != null
                    && (WsdlUtils.isAttachmentInputPart(part, bindingOperation) || WsdlUtils.isHeaderInputPart(part,
                    message, bindingOperation))) {
                ix++;
                if (ix < parts.size()) {
                    part = parts.get(ix);
                } else {
                    part = null;
                }
            }

            if (part == null) {
                return null;
            }

            if (part.getElementName() != null) {
                return part.getElementName();
            } else {
                // return new QName( definition.getTargetNamespace(), part.getName()
                // );
                // changed to comply with soapmessagebuilder -> behaviour is not
                // really defined
                return new QName(part.getName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public QName getResponseBodyElementQName() throws Exception {
        if (isUnidirectional()) {
            return null;
        }

        WsdlInterface iface = getInterface();

        Definition definition = iface.getWsdlContext().getDefinition();
        BindingOperation bindingOperation = findBindingOperation(definition);
        if (WsdlUtils.isRpc(definition, bindingOperation)) {
            BindingOutput bindingOutput = bindingOperation.getBindingOutput();
            String ns = bindingOutput == null ? null : WsdlUtils.getSoapBodyNamespace(bindingOutput
                    .getExtensibilityElements());
            if (ns == null) {
                ns = WsdlUtils.getTargetNamespace(definition);
            }

            return new QName(ns, bindingOperation.getName() + "Response");
        } else {
            Message message = bindingOperation.getOperation().getOutput().getMessage();
            List<javax.wsdl.Part> parts = message.getOrderedParts(null);
            if (parts == null || parts.isEmpty()) {
                return null;
            }

            int ix = 0;
            javax.wsdl.Part part = parts.get(0);

            while (part != null
                    && (WsdlUtils.isAttachmentOutputPart(part, bindingOperation) || WsdlUtils.isHeaderOutputPart(part,
                    message, bindingOperation))) {
                ix++;
                if (ix < parts.size()) {
                    part = parts.get(ix);
                } else {
                    part = null;
                }
            }

            if (part == null) {
                return null;
            }

            if (part.getElementName() != null) {
                return part.getElementName();
            } else {
                // return new QName( definition.getTargetNamespace(), part.getName()
                // );
                return new QName(part.getName());
            }
        }
    }

    public String getStyle() {
        WsdlContext wsdlContext = iface.getWsdlContext();
        if (!wsdlContext.isLoaded()) {
            return "<not loaded>";
        }

        try {
            Definition definition = wsdlContext.getDefinition();
            BindingOperation bindingOperation = findBindingOperation(definition);

            if (bindingOperation == null) {
                return "<missing bindingOperation>";
            }

            if (WsdlUtils.isRpc(definition, bindingOperation)) {
                return WsdlOperation.STYLE_RPC;
            } else {
                return WsdlOperation.STYLE_DOCUMENT;
            }
        } catch (Exception e) {
            SoapUI.logError(e);
            return "<error>";
        }
    }

    public String getType() {
        if (isOneWay()) {
            return ONE_WAY;
        } else if (isNotification()) {
            return NOTIFICATION;
        } else if (isSolicitResponse()) {
            return SOLICIT_RESPONSE;
        } else {
            return REQUEST_RESPONSE;
        }
    }

    @Override
    public void release() {
        super.release();

        for (WsdlRequest request : requests) {
            request.release();
        }
    }

    public BindingOperation getBindingOperation() {
        try {
            return findBindingOperation(getInterface().getWsdlContext().getDefinition());
        } catch (Exception e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public List<Request> getRequestList() {
        return new ArrayList<Request>(requests);
    }

    public MessagePart[] getDefaultRequestParts() {
        try {
            // init
            List<MessagePart> result = new ArrayList<MessagePart>();
            WsdlContext wsdlContext = getInterface().getWsdlContext();
            BindingOperation bindingOperation = findBindingOperation(wsdlContext.getDefinition());

            if (bindingOperation == null) {
                return new MessagePart[0];
            }

            // header parts
            BindingInput bindingInput = bindingOperation.getBindingInput();
            if (bindingInput == null) {
                return new MessagePart[0];
            }

            List<SoapHeader> headers = WsdlUtils.getSoapHeaders(bindingInput.getExtensibilityElements());

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
            javax.wsdl.Part[] parts = WsdlUtils.getInputParts(bindingOperation);

            for (int i = 0; i < parts.length; i++) {
                javax.wsdl.Part part = parts[i];

                if (!WsdlUtils.isAttachmentInputPart(part, bindingOperation)) {
                    SchemaType schemaType = WsdlUtils.getSchemaTypeForPart(wsdlContext, part);
                    SchemaGlobalElement schemaElement = WsdlUtils.getSchemaElementForPart(wsdlContext, part);
                    if (schemaType != null) {
                        result.add(new WsdlContentPart(part.getName(), schemaType, part.getElementName(), schemaElement));
                    }
                }
            }

            return result.toArray(new MessagePart[result.size()]);
        } catch (Exception e) {
            SoapUI.logError(e);
            return new MessagePart[0];
        }
    }

    public MessagePart[] getDefaultResponseParts() {
        try {
            // init
            List<MessagePart> result = new ArrayList<MessagePart>();
            WsdlContext wsdlContext = getInterface().getWsdlContext();
            BindingOperation bindingOperation = findBindingOperation(wsdlContext.getDefinition());

            if (bindingOperation == null) {
                return new MessagePart[0];
            }

            // header parts
            BindingOutput bindingOutput = bindingOperation.getBindingOutput();
            if (bindingOutput == null) {
                return new MessagePart[0];
            }

            List<SoapHeader> headers = WsdlUtils.getSoapHeaders(bindingOutput.getExtensibilityElements());

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

            return result.toArray(new MessagePart[result.size()]);
        } catch (Exception e) {
            SoapUI.logError(e);
            return new MessagePart[0];
        }
    }

    public FaultPart[] getFaultParts() {
        BindingOperation bindingOperation = getBindingOperation();
        Map<?, ?> bindingFaults = bindingOperation.getBindingFaults();

        List<FaultPart> result = new ArrayList<FaultPart>();
        for (Object key : bindingFaults.keySet()) {
            result.add(new WsdlFaultPart((String) key));
        }

        return result.toArray(new FaultPart[result.size()]);
    }

    private class WsdlFaultPart extends FaultPart {
        private final String name;

        public WsdlFaultPart(String name) {
            this.name = name;
        }

        @Override
        public javax.wsdl.Part[] getWsdlParts() {
            try {
                return WsdlUtils.getFaultParts(getBindingOperation(), name);
            } catch (Exception e) {
                log.error(e.toString(), e);
            }

            return new javax.wsdl.Part[0];
        }

        @Override
        public QName getPartElementName() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public String getName() {
            return name;
        }

        @Override
        public SchemaType getSchemaType() {
            return null;
        }

        @Override
        public SchemaGlobalElement getPartElement() {
            return null;
        }
    }

    public List<? extends ModelItem> getChildren() {
        return getRequestList();
    }

    public AttachmentEncoding getAttachmentEncoding(String part, boolean isRequest) {
        return AttachmentUtils.getAttachmentEncoding(this, part, !isRequest);
    }
}
