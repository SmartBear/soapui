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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingOperation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class for validating SOAP requests/responses against their definition and
 * schema, requires that the messages follow basic-profile requirements
 *
 * @author Ole.Matzura
 */

public class WsdlValidator {
    private final WsdlContext wsdlContext;
    private final static Logger log = LogManager.getLogger(WsdlValidator.class);

    public WsdlValidator(WsdlContext wsdlContext) {
        this.wsdlContext = wsdlContext;
    }

    public AssertionError[] assertRequest(WsdlMessageExchange messageExchange, boolean envelopeOnly) {
        List<XmlError> errors = new ArrayList<XmlError>();
        try {
            String requestContent = messageExchange.getRequestContent();
            wsdlContext.getSoapVersion().validateSoapEnvelope(requestContent, errors);

            if (errors.isEmpty() && !envelopeOnly) {
                wsdlContext.getSoapVersion().validateSoapEnvelope(requestContent, errors);
                WsdlOperation operation = messageExchange.getOperation();
                BindingOperation bindingOperation = operation.getBindingOperation();
                if (bindingOperation == null) {
                    errors.add(XmlError.forMessage("Missing operation [" + operation.getBindingOperationName()
                            + "] in wsdl definition"));
                } else {
                    Part[] inputParts = WsdlUtils.getInputParts(bindingOperation);
                    validateMessage(messageExchange, requestContent, bindingOperation, inputParts, errors, false);
                    // validateInputAttachments(request, errors, bindingOperation,
                    // inputParts);
                }
            }
        } catch (Exception e) {
            errors.add(XmlError.forMessage(e.getMessage()));
        }

        return convertErrors(errors);
    }

    private void validateInputAttachments(WsdlMessageExchange messageExchange, List<XmlError> errors,
                                          BindingOperation bindingOperation, Part[] inputParts) {
        for (Part part : inputParts) {
            MIMEContent[] contents = WsdlUtils.getInputMultipartContent(part, bindingOperation);
            if (contents.length == 0) {
                continue;
            }

            Attachment[] attachments = messageExchange.getRequestAttachmentsForPart(part.getName());
            if (attachments.length == 0) {
                errors.add(XmlError.forMessage("Missing attachment for part [" + part.getName() + "]"));
            } else if (attachments.length == 1) {
                Attachment attachment = attachments[0];
                String types = "";
                for (MIMEContent content : contents) {
                    String type = content.getType();
                    if (type.equals(attachment.getContentType()) || type.toUpperCase().startsWith("MULTIPART")) {
                        types = null;
                        break;
                    }
                    if (types.length() > 0) {
                        types += ",";
                    }

                    types += type;
                }

                if (types != null) {
                    String msg = "Missing attachment for part [" + part.getName() + "] with content-type [" + types + "],"
                            + " content type is [" + attachment.getContentType() + "]";

                    if (SoapUI.getSettings().getBoolean(WsdlSettings.ALLOW_INCORRECT_CONTENTTYPE)) {
                        log.warn(msg);
                    } else {
                        errors.add(XmlError.forMessage(msg));
                    }
                }
            } else {
                String types = "";
                for (MIMEContent content : contents) {
                    String type = content.getType();
                    if (type.toUpperCase().startsWith("MULTIPART")) {
                        types = null;
                        break;
                    }
                    if (types.length() > 0) {
                        types += ",";
                    }

                    types += type;
                }

                if (types == null) {
                    String msg = "Too many attachments for part [" + part.getName() + "] with content-type [" + types + "]";
                    if (SoapUI.getSettings().getBoolean(WsdlSettings.ALLOW_INCORRECT_CONTENTTYPE)) {
                        log.warn(msg);
                    } else {
                        errors.add(XmlError.forMessage(msg));
                    }
                }
            }

            if (attachments.length > 0) {
                validateAttachmentsReadability(errors, attachments);
            }
        }
    }

    private void validateOutputAttachments(WsdlMessageExchange messageExchange, XmlObject xml, List<XmlError> errors,
                                           BindingOperation bindingOperation, Part[] outputParts) throws Exception {
        for (Part part : outputParts) {
            MIMEContent[] contents = WsdlUtils.getOutputMultipartContent(part, bindingOperation);
            if (contents.length == 0) {
                continue;
            }

            Attachment[] attachments = messageExchange.getResponseAttachmentsForPart(part.getName());

            // check for rpc
            if (attachments.length == 0 && WsdlUtils.isRpc(wsdlContext.getDefinition(), bindingOperation)) {
                XmlObject[] rpcBodyPart = getRpcBodyPart(bindingOperation, xml, true);
                if (rpcBodyPart.length == 1) {
                    XmlObject[] children = rpcBodyPart[0].selectChildren(new QName(part.getName()));
                    if (children.length == 1) {
                        String href = ((Element) children[0].getDomNode()).getAttribute("href");
                        if (href != null) {
                            if (href.startsWith("cid:")) {
                                href = href.substring(4);
                            }

                            attachments = messageExchange.getResponseAttachmentsForPart(href);
                        }
                    }
                }
            }

            if (attachments.length == 0) {
                errors.add(XmlError.forMessage("Missing attachment for part [" + part.getName() + "]"));
            } else if (attachments.length == 1) {
                Attachment attachment = attachments[0];
                String types = "";
                for (MIMEContent content : contents) {
                    String type = content.getType();
                    if (type.equals(attachment.getContentType()) || type.toUpperCase().startsWith("MULTIPART")) {
                        types = null;
                        break;
                    }

                    if (types.length() > 0) {
                        types += ",";
                    }

                    types += type;
                }

                if (types != null) {
                    String msg = "Missing attachment for part [" + part.getName() + "] with content-type [" + types
                            + "], content type is [" + attachment.getContentType() + "]";

                    if (SoapUI.getSettings().getBoolean(WsdlSettings.ALLOW_INCORRECT_CONTENTTYPE)) {
                        log.warn(msg);
                    } else {
                        errors.add(XmlError.forMessage(msg));
                    }
                }
            } else {
                String types = "";
                for (MIMEContent content : contents) {
                    String type = content.getType();
                    if (type.toUpperCase().startsWith("MULTIPART")) {
                        types = null;
                        break;
                    }

                    if (types.length() > 0) {
                        types += ",";
                    }

                    types += type;
                }

                if (types != null) {
                    String msg = "Too many attachments for part [" + part.getName() + "] with content-type [" + types + "]";

                    if (SoapUI.getSettings().getBoolean(WsdlSettings.ALLOW_INCORRECT_CONTENTTYPE)) {
                        log.warn(msg);
                    } else {
                        errors.add(XmlError.forMessage(msg));
                    }
                }
            }

            if (attachments.length > 0) {
                validateAttachmentsReadability(errors, attachments);
            }
        }
    }

    private void validateAttachmentsReadability(List<XmlError> errors, Attachment[] attachments) {
        for (Attachment attachment : attachments) {
            try {
                attachment.getInputStream();
            } catch (Exception e) {
                errors.add(XmlError.forMessage(e.toString()));
            }
        }
    }

    public XmlObject[] getMessageParts(String messageContent, String operationName, boolean isResponse)
            throws Exception {
        BindingOperation bindingOperation = findBindingOperation(operationName);
        if (bindingOperation == null) {
            throw new Exception("Missing operation [" + operationName + "] in wsdl definition");
        }

        if (!wsdlContext.hasSchemaTypes()) {
            throw new Exception("Missing schema types for message");
        }

        // XmlObject msgXml = XmlObject.Factory.parse( messageContent );
        XmlObject msgXml = XmlUtils.createXmlObject(messageContent);
        Part[] parts = isResponse ? WsdlUtils.getOutputParts(bindingOperation) : WsdlUtils
                .getInputParts(bindingOperation);
        if (parts == null || parts.length == 0) {
            throw new Exception("Missing parts for operation [" + operationName + "]");
        }

        List<XmlObject> result = new ArrayList<XmlObject>();

        if (WsdlUtils.isRpc(wsdlContext.getDefinition(), bindingOperation)) {
            // get root element
            XmlObject[] paths = msgXml.selectPath("declare namespace env='"
                    + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "';" + "declare namespace ns='"
                    + WsdlUtils.getTargetNamespace(wsdlContext.getDefinition()) + "';" + "$this/env:Envelope/env:Body/ns:"
                    + bindingOperation.getName() + (isResponse ? "Response" : ""));

            if (paths.length != 1) {
                throw new Exception("Missing message wrapper element ["
                        + WsdlUtils.getTargetNamespace(wsdlContext.getDefinition()) + "@" + bindingOperation.getName()
                        + (isResponse ? "Response]" : "]"));
            } else {
                XmlObject wrapper = paths[0];

                for (int i = 0; i < parts.length; i++) {
                    Part part = parts[i];
                    if ((isResponse && WsdlUtils.isAttachmentOutputPart(part, bindingOperation))
                            || (!isResponse && WsdlUtils.isAttachmentInputPart(part, bindingOperation))) {
                        continue;
                    }

                    QName partName = part.getElementName();
                    if (partName == null) {
                        partName = new QName(part.getName());
                    }

                    XmlObject[] children = wrapper.selectChildren(partName);
                    if (children.length != 1) {
                        log.error("Missing message part [" + part.getName() + "]");
                    } else {
                        QName typeName = part.getTypeName();
                        if (typeName == null) {
                            typeName = partName;
                            SchemaGlobalElement type = wsdlContext.getSchemaTypeLoader().findElement(typeName);

                            if (type != null) {
                                result.add(children[0].copy().changeType(type.getType()));
                            } else {
                                log.error("Missing element [" + typeName + "] in associated schema for part ["
                                        + part.getName() + "]");
                            }
                        } else {
                            SchemaType type = wsdlContext.getSchemaTypeLoader().findType(typeName);
                            if (type != null) {
                                result.add(children[0].copy().changeType(type));
                            } else {
                                log.error("Missing type [" + typeName + "] in associated schema for part [" + part.getName()
                                        + "]");
                            }
                        }
                    }
                }
            }
        } else {
            Part part = parts[0];
            QName elementName = part.getElementName();
            if (elementName != null) {
                // just check for correct message element, other elements are
                // avoided (should create an error)
                XmlObject[] paths = msgXml.selectPath("declare namespace env='"
                        + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "';" + "declare namespace ns='"
                        + elementName.getNamespaceURI() + "';" + "$this/env:Envelope/env:Body/ns:"
                        + elementName.getLocalPart());

                if (paths.length == 1) {
                    SchemaGlobalElement elm = wsdlContext.getSchemaTypeLoader().findElement(elementName);
                    if (elm != null) {
                        result.add(paths[0].copy().changeType(elm.getType()));
                    } else {
                        throw new Exception("Missing part type in associated schema");
                    }
                } else {
                    throw new Exception("Missing message part with name [" + elementName + "]");
                }
            }
        }

        return result.toArray(new XmlObject[result.size()]);
    }

    @SuppressWarnings("unchecked")
    public void validateXml(String request, List<XmlError> errors) {
        try {
            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setLoadLineNumbers();
            xmlOptions.setErrorListener(errors);
            xmlOptions.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT);
            // XmlObject.Factory.parse( request, xmlOptions );
            XmlUtils.createXmlObject(request, xmlOptions);
        } catch (XmlException e) {
            if (e.getErrors() != null) {
                errors.addAll(e.getErrors());
            }
            errors.add(XmlError.forMessage(e.getMessage()));
        } catch (Exception e) {
            errors.add(XmlError.forMessage(e.getMessage()));
        }
    }

    private AssertionError[] convertErrors(List<XmlError> errors) {
        if (errors.size() > 0) {
            List<AssertionError> response = new ArrayList<AssertionError>();
            for (Iterator<XmlError> i = errors.iterator(); i.hasNext(); ) {
                XmlError error = i.next();

                if (error instanceof XmlValidationError) {
                    XmlValidationError e = ((XmlValidationError) error);
                    QName offendingQName = e.getOffendingQName();
                    if (offendingQName != null) {
                        if (offendingQName.equals(new QName(wsdlContext.getSoapVersion().getEnvelopeNamespace(),
                                "encodingStyle"))) {
                            log.debug("ignoring encodingStyle validation..");
                            continue;
                        } else if (offendingQName.equals(new QName(wsdlContext.getSoapVersion().getEnvelopeNamespace(),
                                "mustUnderstand"))) {
                            log.debug("ignoring mustUnderstand validation..");
                            continue;
                        }
                    }
                }

                AssertionError assertionError = new AssertionError(error);
                if (!response.contains(assertionError)) {
                    response.add(assertionError);
                }
            }

            return response.toArray(new AssertionError[response.size()]);
        }

        return new AssertionError[0];
    }

    @SuppressWarnings("unchecked")
    public void validateMessage(WsdlMessageExchange messageExchange, String message, BindingOperation bindingOperation,
                                Part[] parts, List<XmlError> errors, boolean isResponse) {
        try {
            if (!wsdlContext.hasSchemaTypes()) {
                errors.add(XmlError.forMessage("Missing schema types for message"));
            } else {
                if (!WsdlUtils.isOutputSoapEncoded(bindingOperation)) {
                    XmlOptions xmlOptions = new XmlOptions();
                    xmlOptions.setLoadLineNumbers();
                    xmlOptions.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT);
                    // XmlObject xml = XmlObject.Factory.parse( message, xmlOptions
                    // );
                    XmlObject xml = XmlUtils.createXmlObject(message, xmlOptions);

                    XmlObject[] paths = xml.selectPath("declare namespace env='"
                            + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "';"
                            + "$this/env:Envelope/env:Body/env:Fault");

                    if (paths.length > 0) {
                        validateSoapFault(bindingOperation, paths[0], errors);
                    } else if (WsdlUtils.isRpc(wsdlContext.getDefinition(), bindingOperation)) {
                        validateRpcLiteral(bindingOperation, parts, xml, errors, isResponse);
                    } else {
                        validateDocLiteral(bindingOperation, parts, xml, errors, isResponse);
                    }

                    if (isResponse) {
                        validateOutputAttachments(messageExchange, xml, errors, bindingOperation, parts);
                    } else {
                        validateInputAttachments(messageExchange, errors, bindingOperation, parts);
                    }
                } else {
                    errors.add(XmlError.forMessage("Validation of SOAP-Encoded messages not supported"));
                }
            }
        } catch (XmlException e) {
            if (e.getErrors() != null) {
                errors.addAll(e.getErrors());
            }
            errors.add(XmlError.forMessage(e.getMessage()));
        } catch (Exception e) {
            errors.add(XmlError.forMessage(e.getMessage()));
        }
    }

    private BindingOperation findBindingOperation(String operationName) throws Exception {
        Map<?, ?> services = wsdlContext.getDefinition().getAllServices();
        Iterator<?> i = services.keySet().iterator();
        while (i.hasNext()) {
            Service service = (Service) wsdlContext.getDefinition().getService((QName) i.next());
            Map<?, ?> ports = service.getPorts();

            Iterator<?> iterator = ports.keySet().iterator();
            while (iterator.hasNext()) {
                Port port = (Port) service.getPort((String) iterator.next());
                Binding binding = port.getBinding();
                if (binding.getQName().equals(wsdlContext.getInterface().getBindingName())) {
                    BindingOperation bindingOperation = binding.getBindingOperation(operationName, null, null);
                    if (bindingOperation != null) {
                        return bindingOperation;
                    }
                }
            }
        }

        Map<?, ?> bindings = wsdlContext.getDefinition().getAllBindings();
        i = bindings.keySet().iterator();
        while (i.hasNext()) {
            Binding binding = (Binding) bindings.get(i.next());
            if (binding.getQName().equals(wsdlContext.getInterface().getBindingName())) {
                BindingOperation bindingOperation = binding.getBindingOperation(operationName, null, null);
                if (bindingOperation != null) {
                    return bindingOperation;
                }
            }
        }

        return null;
    }

    public AssertionError[] assertResponse(WsdlMessageExchange messageExchange, boolean envelopeOnly) {
        List<XmlError> errors = new ArrayList<XmlError>();
        try {
            String response = messageExchange.getResponseContent();

            if (StringUtils.isNullOrEmpty(response)) {
                if (!messageExchange.getOperation().isOneWay()) {
                    errors.add(XmlError.forMessage("Response is missing or empty"));
                }
            } else {
                wsdlContext.getSoapVersion().validateSoapEnvelope(response, errors);

                if (errors.isEmpty() && !envelopeOnly) {
                    WsdlOperation operation = messageExchange.getOperation();
                    BindingOperation bindingOperation = operation.getBindingOperation();
                    if (bindingOperation == null) {
                        errors.add(XmlError.forMessage("Missing operation [" + operation.getBindingOperationName()
                                + "] in wsdl definition"));
                    } else {
                        Part[] outputParts = WsdlUtils.getOutputParts(bindingOperation);
                        validateMessage(messageExchange, response, bindingOperation, outputParts, errors, true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errors.add(XmlError.forMessage(e.getMessage()));
        }

        return convertErrors(errors);
    }

    private void validateDocLiteral(BindingOperation bindingOperation, Part[] parts, XmlObject msgXml,
                                    List<XmlError> errors, boolean isResponse) throws Exception {
        Part part = null;

        // start by finding body part
        for (int c = 0; c < parts.length; c++) {
            // content part?
            if ((isResponse && !WsdlUtils.isAttachmentOutputPart(parts[c], bindingOperation))
                    || (!isResponse && !WsdlUtils.isAttachmentInputPart(parts[c], bindingOperation))) {
                // already found?
                if (part != null) {
                    errors.add(XmlError.forMessage("DocLiteral message must contain 1 body part definition"));
                    return;
                }

                part = parts[c];
            }
        }

        QName elementName = part.getElementName();
        if (elementName != null) {
            // just check for correct message element, other elements are avoided
            // (should create an error)
            XmlObject[] paths = msgXml.selectPath("declare namespace env='"
                    + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "';" + "declare namespace ns='"
                    + elementName.getNamespaceURI() + "';" + "$this/env:Envelope/env:Body/ns:" + elementName.getLocalPart());

            if (paths.length == 1) {
                SchemaGlobalElement elm = wsdlContext.getSchemaTypeLoader().findElement(elementName);
                if (elm != null) {
                    validateMessageBody(errors, elm.getType(), paths[0]);

                    // ensure no other elements in body
                    NodeList children = XmlUtils.getChildElements((Element) paths[0].getDomNode().getParentNode());
                    for (int c = 0; c < children.getLength(); c++) {
                        QName childName = XmlUtils.getQName(children.item(c));
                        if (!elementName.equals(childName)) {
                            XmlCursor cur = paths[0].newCursor();
                            cur.toParent();
                            cur.toChild(childName);
                            errors.add(XmlError.forCursor("Invalid element [" + childName + "] in SOAP Body", cur));
                            cur.dispose();
                        }
                    }
                } else {
                    errors.add(XmlError.forMessage("Missing part type [" + elementName + "] in associated schema"));
                }
            } else {
                errors.add(XmlError.forMessage("Missing message part with name [" + elementName + "]"));
            }
        } else if (part.getTypeName() != null) {
            QName typeName = part.getTypeName();

            XmlObject[] paths = msgXml.selectPath("declare namespace env='"
                    + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "';" + "declare namespace ns='"
                    + typeName.getNamespaceURI() + "';" + "$this/env:Envelope/env:Body/ns:" + part.getName());

            if (paths.length == 1) {
                SchemaType type = wsdlContext.getSchemaTypeLoader().findType(typeName);
                if (type != null) {
                    validateMessageBody(errors, type, paths[0]);
                    // XmlObject obj = paths[0].copy().changeType( type );
                    // obj.validate( new XmlOptions().setErrorListener( errors ));
                } else {
                    errors.add(XmlError.forMessage("Missing part type in associated schema"));
                }
            } else {
                errors.add(XmlError.forMessage("Missing message part with name:type [" + part.getName() + ":" + typeName
                        + "]"));
            }
        }
    }

    private void validateMessageBody(List<XmlError> errors, SchemaType type, XmlObject msg) throws XmlException {
        // need to create new body element of correct type from xml text
        // since we want to retain line-numbers
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        xmlOptions.setLoadLineNumbers(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT);

        XmlCursor cur = msg.newCursor();
        Map<String, String> map = new HashMap<String, String>();

        while (cur.hasNextToken()) {
            if (cur.toNextToken().isNamespace()) {
                map.put(cur.getName().getLocalPart(), cur.getTextValue());
            }
        }

        xmlOptions.setUseDefaultNamespace();
        xmlOptions.setSaveOuter();

        // problem: prefixes might get redefined/changed when saving which can
        // cause xsi:type refs to
        // reference wrong/non-existing namespace.. solution would probably be to
        // manually walk through document and
        // update xsi:type refs with new prefix. The setUseDefaultNamespace()
        // above helps here but is not a definitive fix

        String xmlText = msg.copy().changeType(type).xmlText(xmlOptions);

        xmlOptions.setLoadAdditionalNamespaces(map);

        XmlObject obj = type.getTypeSystem().parse(xmlText, type, xmlOptions);
        obj = obj.changeType(type);

        // create internal error list
        ArrayList<Object> list = new ArrayList<Object>();

        xmlOptions = new XmlOptions();
        xmlOptions.setErrorListener(list);
        xmlOptions.setValidateTreatLaxAsSkip();

        try {
            obj.validate(xmlOptions);
        } catch (Exception e) {
            SoapUI.logError(e);
            list.add("Internal Error - see error log for details - [" + e + "]");
        }

        // transfer errors for "real" line numbers
        for (int c = 0; c < list.size(); c++) {
            XmlError error = (XmlError) list.get(c);

            if (error instanceof XmlValidationError) {
                XmlValidationError validationError = ((XmlValidationError) error);

                if (wsdlContext.getSoapVersion().shouldIgnore(validationError)) {
                    continue;
                }

                // ignore cid: related errors
                if (validationError.getErrorCode().equals("base64Binary")
                        || validationError.getErrorCode().equals("hexBinary")) {
                    XmlCursor cursor = validationError.getCursorLocation();
                    if (cursor.toParent()) {
                        String text = cursor.getTextValue();

                        // special handling for soapui/MTOM -> add option for
                        // disabling?
                        if (text.startsWith("cid:") || text.startsWith("file:")) {
                            // ignore
                            continue;
                        }
                    }
                }
            }

            int line = error.getLine() == -1 ? 0 : error.getLine() - 1;
            errors.add(XmlError.forLocation(error.getMessage(), error.getSourceName(), getLine(msg) + line,
                    error.getColumn(), error.getOffset()));
        }
    }

    private int getLine(XmlObject object) {
        List<?> list = new ArrayList<Object>();
        object.newCursor().getAllBookmarkRefs(list);
        for (int c = 0; c < list.size(); c++) {
            if (list.get(c) instanceof XmlLineNumber) {
                return ((XmlLineNumber) list.get(c)).getLine();
            }
        }

        return -1;
    }

    private void validateRpcLiteral(BindingOperation bindingOperation, Part[] parts, XmlObject msgXml,
                                    List<XmlError> errors, boolean isResponse) throws Exception {
        if (parts.length == 0) {
            return;
        }

        XmlObject[] bodyParts = getRpcBodyPart(bindingOperation, msgXml, isResponse);

        if (bodyParts.length != 1) {
            errors.add(XmlError.forMessage("Missing message wrapper element ["
                    + WsdlUtils.getTargetNamespace(wsdlContext.getDefinition()) + "@" + bindingOperation.getName()
                    + (isResponse ? "Response" : "")));
        } else {
            XmlObject wrapper = bodyParts[0];

            for (int i = 0; i < parts.length; i++) {
                Part part = parts[i];

                // skip attachment parts
                if (isResponse) {
                    if (WsdlUtils.isAttachmentOutputPart(part, bindingOperation)) {
                        continue;
                    }
                } else {
                    if (WsdlUtils.isAttachmentInputPart(part, bindingOperation)) {
                        continue;
                    }
                }

                // find part in message
                XmlObject[] children = wrapper.selectChildren(new QName(part.getName()));

                // not found?
                if (children.length != 1) {
                    // try element name (loophole in basic-profile spec?)
                    QName elementName = part.getElementName();
                    if (elementName != null) {
                        bodyParts = msgXml.selectPath("declare namespace env='"
                                + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "';" + "declare namespace ns='"
                                + wsdlContext.getDefinition().getTargetNamespace() + "';" + "declare namespace ns2='"
                                + elementName.getNamespaceURI() + "';" + "$this/env:Envelope/env:Body/ns:"
                                + bindingOperation.getName() + (isResponse ? "Response" : "") + "/ns2:"
                                + elementName.getLocalPart());

                        if (bodyParts.length == 1) {
                            SchemaGlobalElement elm = wsdlContext.getSchemaTypeLoader().findElement(elementName);
                            if (elm != null) {
                                validateMessageBody(errors, elm.getType(), bodyParts[0]);
                            } else {
                                errors.add(XmlError.forMessage("Missing part type in associated schema for [" + elementName
                                        + "]"));
                            }
                        } else {
                            errors.add(XmlError.forMessage("Missing message part with name [" + elementName + "]"));
                        }
                    } else {
                        errors.add(XmlError.forMessage("Missing message part [" + part.getName() + "]"));
                    }
                } else {
                    QName typeName = part.getTypeName();
                    SchemaType type = wsdlContext.getSchemaTypeLoader().findType(typeName);
                    if (type != null) {
                        validateMessageBody(errors, type, children[0]);
                    } else {
                        errors.add(XmlError.forMessage("Missing type in associated schema for part [" + part.getName()
                                + "]"));
                    }
                }
            }
        }
    }

    private XmlObject[] getRpcBodyPart(BindingOperation bindingOperation, XmlObject msgXml, boolean isResponse)
            throws Exception {
        // rpc requests should use the operation name as root element and soapbind
        // namespaceuri attribute as ns
        String ns = WsdlUtils.getSoapBodyNamespace(isResponse ? bindingOperation.getBindingOutput()
                .getExtensibilityElements() : bindingOperation.getBindingInput().getExtensibilityElements());

        if (ns == null || ns.trim().length() == 0) {
            ns = WsdlUtils.getTargetNamespace(wsdlContext.getDefinition());
        }

        // get root element
        XmlObject[] paths = msgXml.selectPath("declare namespace env='"
                + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "';" + "declare namespace ns='" + ns + "';"
                + "$this/env:Envelope/env:Body/ns:" + bindingOperation.getName() + (isResponse ? "Response" : ""));
        return paths;
    }

    @SuppressWarnings("unchecked")
    private void validateSoapFault(BindingOperation bindingOperation, XmlObject msgXml, List<XmlError> errors)
            throws Exception {
        Map faults = bindingOperation.getBindingFaults();
        Iterator<BindingFault> i = faults.values().iterator();

        // create internal error list
        List<?> list = new ArrayList<Object>();

        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setErrorListener(list);
        xmlOptions.setValidateTreatLaxAsSkip();
        msgXml.validate(xmlOptions);

        for (Object o : list) {
            if (o instanceof XmlError) {
                errors.add((XmlError) o);
            } else {
                errors.add(XmlError.forMessage(o.toString()));
            }
        }

        while (i.hasNext()) {
            BindingFault bindingFault = i.next();
            String faultName = bindingFault.getName();

            Part[] faultParts = WsdlUtils.getFaultParts(bindingOperation, faultName);
            if (faultParts.length == 0) {
                log.warn("Missing fault parts in wsdl for fault [" + faultName + "] in bindingOperation ["
                        + bindingOperation.getName() + "]");
                continue;
            }

            if (faultParts.length != 1) {
                log.info("Too many fault parts in wsdl for fault [" + faultName + "] in bindingOperation ["
                        + bindingOperation.getName() + "]");
                continue;
            }

            Part part = faultParts[0];
            QName elementName = part.getElementName();

            if (elementName != null) {
                XmlObject[] paths = msgXml.selectPath("declare namespace env='"
                        + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "'; declare namespace flt='"
                        + wsdlContext.getSoapVersion().getFaultDetailNamespace() + "';" + "declare namespace ns='"
                        + elementName.getNamespaceURI() + "';" + "//env:Fault/flt:detail/ns:" + elementName.getLocalPart());

                if (paths.length == 1) {
                    SchemaGlobalElement elm = wsdlContext.getSchemaTypeLoader().findElement(elementName);
                    if (elm != null) {
                        validateMessageBody(errors, elm.getType(), paths[0]);
                    } else {
                        errors.add(XmlError.forMessage("Missing fault part element [" + elementName + "] for fault ["
                                + part.getName() + "] in associated schema"));
                    }

                    return;
                }
            }
            // this is not allowed by Basic Profile.. remove?
            else if (part.getTypeName() != null) {
                QName typeName = part.getTypeName();

                XmlObject[] paths = msgXml.selectPath("declare namespace env='"
                        + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "'; declare namespace flt='"
                        + wsdlContext.getSoapVersion().getFaultDetailNamespace() + "';" + "declare namespace ns='"
                        + typeName.getNamespaceURI() + "';" + "//env:Fault/flt:detail/ns:" + part.getName());

                if (paths.length == 1) {
                    SchemaType type = wsdlContext.getSchemaTypeLoader().findType(typeName);
                    if (type != null) {
                        validateMessageBody(errors, type, paths[0]);
                    } else {
                        errors.add(XmlError.forMessage("Missing fault part type [" + typeName + "] for fault ["
                                + part.getName() + "] in associated schema"));
                    }

                    return;
                }
            }
        }

        // if we get here, no matching fault was found.. this is not an error but
        // should be warned..
        XmlObject[] paths = msgXml.selectPath("declare namespace env='"
                + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "'; declare namespace flt='"
                + wsdlContext.getSoapVersion().getFaultDetailNamespace() + "';//env:Fault/flt:detail");

        if (paths.length == 0) {
            log.warn("Missing matching Fault in wsdl for bindingOperation [" + bindingOperation.getName() + "]");
        } else {
            String xmlText = paths[0].xmlText(new XmlOptions().setSaveOuter());
            log.warn("Missing matching Fault in wsdl for Fault Detail element ["
                    + XmlUtils.removeUnneccessaryNamespaces(xmlText) + "] in bindingOperation ["
                    + bindingOperation.getName() + "]");
        }
    }
}
