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

package com.eviware.soapui.impl.wsdl.support.soap;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils.SoapHeader;
import com.eviware.soapui.impl.wsdl.support.xsd.SampleXmlUtil;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.MessageBuilder;
import com.eviware.soapui.model.iface.MessagePart.FaultPart;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Builds SOAP requests according to WSDL/XSD definitions
 *
 * @author Ole.Matzura
 */

public class SoapMessageBuilder implements MessageBuilder {
    private final static Logger log = LogManager.getLogger(SoapMessageBuilder.class);

    private WsdlContext wsdlContext;
    private WsdlInterface iface;
    private Map<QName, String[]> multiValues = null;

    public SoapMessageBuilder(WsdlInterface iface) throws Exception {
        this.iface = iface;
        this.wsdlContext = iface.getWsdlContext();
    }

    public SoapMessageBuilder(WsdlContext wsdlContext) {
        this.wsdlContext = wsdlContext;
    }

    public void setMultiValues(Map<QName, String[]> multiValues) {
        this.multiValues = multiValues;
    }

    public Interface getInterface() {
        return iface;
    }

    public String buildFault(String faultcode, String faultstring) {
        return buildFault(faultcode, faultstring, getSoapVersion());
    }

    public SoapVersion getSoapVersion() {
        return iface == null ? wsdlContext.getSoapVersion() : iface.getSoapVersion();
    }

    public static String buildFault(String faultcode, String faultstring, SoapVersion soapVersion) {
        SampleXmlUtil generator = new SampleXmlUtil(false);
        generator.setTypeComment(false);
        generator.setIgnoreOptional(true);

        String emptyResponse = buildEmptyFault(generator, soapVersion);

        if (soapVersion == SoapVersion.Soap11) {
            emptyResponse = XmlUtils.setXPathContent(emptyResponse, "//faultcode", faultcode);
            emptyResponse = XmlUtils.setXPathContent(emptyResponse, "//faultstring", faultstring);
        } else if (soapVersion == SoapVersion.Soap12) {
            emptyResponse = XmlUtils.setXPathContent(emptyResponse, "//soap:Value", faultcode);
            emptyResponse = XmlUtils.setXPathContent(emptyResponse, "//soap:Text", faultstring);
            emptyResponse = XmlUtils.setXPathContent(emptyResponse, "//soap:Text/@xml:lang", "en");
        }

        return emptyResponse;
    }

    public String buildEmptyFault() {
        return buildEmptyFault(getSoapVersion());
    }

    public static String buildEmptyFault(SoapVersion soapVersion) {
        SampleXmlUtil generator = new SampleXmlUtil(false);

        String emptyResponse = buildEmptyFault(generator, soapVersion);

        return emptyResponse;
    }

    private static String buildEmptyFault(SampleXmlUtil generator, SoapVersion soapVersion) {
        String emptyResponse = buildEmptyMessage(soapVersion);
        try {
            // XmlObject xmlObject = XmlObject.Factory.parse( emptyResponse );
            XmlObject xmlObject = XmlUtils.createXmlObject(emptyResponse);
            XmlCursor cursor = xmlObject.newCursor();

            if (cursor.toChild(soapVersion.getEnvelopeQName()) && cursor.toChild(soapVersion.getBodyQName())) {
                SchemaType faultType = soapVersion.getFaultType();
                Node bodyNode = cursor.getDomNode();
                Document dom = XmlUtils.parseXml(generator.createSample(faultType));
                bodyNode.appendChild(bodyNode.getOwnerDocument().importNode(dom.getDocumentElement(), true));
            }

            cursor.dispose();
            emptyResponse = xmlObject.toString();
        } catch (Exception e) {
            SoapUI.logError(e);
        }
        return emptyResponse;
    }

    public String buildEmptyMessage() {
        return buildEmptyMessage(getSoapVersion());
    }

    public static String buildEmptyMessage(SoapVersion soapVersion) {
        SampleXmlUtil generator = new SampleXmlUtil(false);
        generator.setTypeComment(false);
        generator.setIgnoreOptional(true);
        return generator.createSample(soapVersion.getEnvelopeType());
    }

    public String buildSoapMessageFromInput(BindingOperation bindingOperation, boolean buildOptional) throws Exception {
        return buildSoapMessageFromInput(bindingOperation, buildOptional, true);
    }

    public String buildSoapMessageFromInput(BindingOperation bindingOperation, boolean buildOptional,
                                            boolean alwaysBuildHeaders) throws Exception {
        boolean inputSoapEncoded = WsdlUtils.isInputSoapEncoded(bindingOperation);
        SampleXmlUtil xmlGenerator = new SampleXmlUtil(inputSoapEncoded);
        xmlGenerator.setMultiValues(multiValues);
        xmlGenerator.setIgnoreOptional(!buildOptional);

        XmlObject object = XmlObject.Factory.newInstance();
        XmlCursor cursor = object.newCursor();
        cursor.toNextToken();
        cursor.beginElement(wsdlContext.getSoapVersion().getEnvelopeQName());

        if (inputSoapEncoded) {
            cursor.insertNamespace("xsi", Constants.XSI_NS);
            cursor.insertNamespace("xsd", Constants.XSD_NS);
        }

        cursor.toFirstChild();

        cursor.beginElement(wsdlContext.getSoapVersion().getBodyQName());
        cursor.toFirstChild();

        if (WsdlUtils.isRpc(wsdlContext.getDefinition(), bindingOperation)) {
            buildRpcRequest(bindingOperation, cursor, xmlGenerator);
        } else {
            buildDocumentRequest(bindingOperation, cursor, xmlGenerator);
        }

        if (alwaysBuildHeaders) {
            BindingInput bindingInput = bindingOperation.getBindingInput();
            if (bindingInput != null) {
                List<?> extensibilityElements = bindingInput.getExtensibilityElements();
                List<SoapHeader> soapHeaders = WsdlUtils.getSoapHeaders(extensibilityElements);
                addHeaders(soapHeaders, cursor, xmlGenerator);
            }
        }
        cursor.dispose();

        try {
            StringWriter writer = new StringWriter();
            XmlUtils.serializePretty(object, writer);
            return writer.toString();
        } catch (Exception e) {
            SoapUI.logError(e);
            return object.xmlText();
        }
    }

    private void addHeaders(List<SoapHeader> headers, XmlCursor cursor, SampleXmlUtil xmlGenerator) throws Exception {
        // reposition
        cursor.toStartDoc();
        cursor.toChild(wsdlContext.getSoapVersion().getEnvelopeQName());
        cursor.toFirstChild();

        cursor.beginElement(wsdlContext.getSoapVersion().getHeaderQName());
        cursor.toFirstChild();

        for (int i = 0; i < headers.size(); i++) {
            SoapHeader header = headers.get(i);

            Message message = wsdlContext.getDefinition().getMessage(header.getMessage());
            if (message == null) {
                log.error("Missing message for header: " + header.getMessage());
                continue;
            }

            Part part = message.getPart(header.getPart());

            if (part != null) {
                createElementForPart(part, cursor, xmlGenerator);
            } else {
                log.error("Missing part for header; " + header.getPart());
            }
        }
    }

    public void createElementForPart(Part part, XmlCursor cursor, SampleXmlUtil xmlGenerator) throws Exception {
        QName elementName = part.getElementName();
        QName typeName = part.getTypeName();

        if (elementName != null) {
            cursor.beginElement(elementName);

            if (wsdlContext.hasSchemaTypes()) {
                SchemaGlobalElement elm = wsdlContext.getSchemaTypeLoader().findElement(elementName);
                if (elm != null) {
                    cursor.toFirstChild();
                    xmlGenerator.createSampleForType(elm.getType(), cursor);
                } else {
                    log.error("Could not find element [" + elementName + "] specified in part [" + part.getName() + "]");
                }
            }

            cursor.toParent();
        } else {
            // cursor.beginElement( new QName(
            // wsdlContext.getWsdlDefinition().getTargetNamespace(), part.getName()
            // ));
            cursor.beginElement(part.getName());
            if (typeName != null && wsdlContext.hasSchemaTypes()) {
                SchemaType type = wsdlContext.getSchemaTypeLoader().findType(typeName);

                if (type != null) {
                    cursor.toFirstChild();
                    xmlGenerator.createSampleForType(type, cursor);
                } else {
                    log.error("Could not find type [" + typeName + "] specified in part [" + part.getName() + "]");
                }
            }

            cursor.toParent();
        }
    }

    private void buildDocumentRequest(BindingOperation bindingOperation, XmlCursor cursor, SampleXmlUtil xmlGenerator)
            throws Exception {
        Part[] parts = WsdlUtils.getInputParts(bindingOperation);

        for (int i = 0; i < parts.length; i++) {
            Part part = parts[i];
            if (!WsdlUtils.isAttachmentInputPart(part, bindingOperation)
                    && (part.getElementName() != null || part.getTypeName() != null)) {
                XmlCursor c = cursor.newCursor();
                c.toLastChild();
                createElementForPart(part, c, xmlGenerator);
                c.dispose();
            }
        }
    }

    private void buildDocumentResponse(BindingOperation bindingOperation, XmlCursor cursor, SampleXmlUtil xmlGenerator)
            throws Exception {
        Part[] parts = WsdlUtils.getOutputParts(bindingOperation);

        for (int i = 0; i < parts.length; i++) {
            Part part = parts[i];

            if (!WsdlUtils.isAttachmentOutputPart(part, bindingOperation)
                    && (part.getElementName() != null || part.getTypeName() != null)) {
                XmlCursor c = cursor.newCursor();
                c.toLastChild();
                createElementForPart(part, c, xmlGenerator);
                c.dispose();
            }
        }
    }

    private void buildRpcRequest(BindingOperation bindingOperation, XmlCursor cursor, SampleXmlUtil xmlGenerator)
            throws Exception {
        // rpc requests use the operation name as root element
        String ns = WsdlUtils.getSoapBodyNamespace(bindingOperation.getBindingInput().getExtensibilityElements());
        if (ns == null) {
            ns = WsdlUtils.getTargetNamespace(wsdlContext.getDefinition());
            log.warn("missing namespace on soapbind:body for RPC request, using targetNamespace instead (BP violation)");
        }

        cursor.beginElement(new QName(ns, bindingOperation.getName()));
        if (xmlGenerator.isSoapEnc()) {
            cursor.insertAttributeWithValue(new QName(wsdlContext.getSoapVersion().getEnvelopeNamespace(),
                    "encodingStyle"), wsdlContext.getSoapVersion().getEncodingNamespace());
        }

        Part[] inputParts = WsdlUtils.getInputParts(bindingOperation);
        for (int i = 0; i < inputParts.length; i++) {
            Part part = inputParts[i];
            if (WsdlUtils.isAttachmentInputPart(part, bindingOperation)) {
                if (iface.getSettings().getBoolean(WsdlSettings.ATTACHMENT_PARTS)) {
                    XmlCursor c = cursor.newCursor();
                    c.toLastChild();
                    c.beginElement(part.getName());
                    c.insertAttributeWithValue("href", part.getName() + "Attachment");
                    c.dispose();
                }
            } else {
                if (wsdlContext.hasSchemaTypes()) {
                    QName typeName = part.getTypeName();
                    if (typeName != null) {
                        SchemaType type = wsdlContext.getInterfaceDefinition().findType(typeName);

                        if (type != null) {
                            XmlCursor c = cursor.newCursor();
                            c.toLastChild();
                            c.insertElement(part.getName());
                            c.toPrevToken();

                            xmlGenerator.createSampleForType(type, c);
                            c.dispose();
                        } else {
                            log.warn("Failed to find type [" + typeName + "]");
                        }
                    } else {
                        SchemaGlobalElement element = wsdlContext.getSchemaTypeLoader().findElement(part.getElementName());
                        if (element != null) {
                            XmlCursor c = cursor.newCursor();
                            c.toLastChild();
                            c.insertElement(element.getName());
                            c.toPrevToken();

                            xmlGenerator.createSampleForType(element.getType(), c);
                            c.dispose();
                        } else {
                            log.warn("Failed to find element [" + part.getElementName() + "]");
                        }
                    }
                }
            }
        }
    }

    private void buildRpcResponse(BindingOperation bindingOperation, XmlCursor cursor, SampleXmlUtil xmlGenerator)
            throws Exception {
        // rpc requests use the operation name as root element
        BindingOutput bindingOutput = bindingOperation.getBindingOutput();
        String ns = bindingOutput == null ? null : WsdlUtils.getSoapBodyNamespace(bindingOutput
                .getExtensibilityElements());

        if (ns == null) {
            ns = WsdlUtils.getTargetNamespace(wsdlContext.getDefinition());
            log.warn("missing namespace on soapbind:body for RPC response, using targetNamespace instead (BP violation)");
        }

        cursor.beginElement(new QName(ns, bindingOperation.getName() + "Response"));
        if (xmlGenerator.isSoapEnc()) {
            cursor.insertAttributeWithValue(new QName(wsdlContext.getSoapVersion().getEnvelopeNamespace(),
                    "encodingStyle"), wsdlContext.getSoapVersion().getEncodingNamespace());
        }

        Part[] inputParts = WsdlUtils.getOutputParts(bindingOperation);
        for (int i = 0; i < inputParts.length; i++) {
            Part part = inputParts[i];
            if (WsdlUtils.isAttachmentOutputPart(part, bindingOperation)) {
                if (iface.getSettings().getBoolean(WsdlSettings.ATTACHMENT_PARTS)) {
                    XmlCursor c = cursor.newCursor();
                    c.toLastChild();
                    c.beginElement(part.getName());
                    c.insertAttributeWithValue("href", part.getName() + "Attachment");
                    c.dispose();
                }
            } else {
                if (wsdlContext.hasSchemaTypes()) {
                    QName typeName = part.getTypeName();
                    if (typeName != null) {
                        SchemaType type = wsdlContext.getInterfaceDefinition().findType(typeName);

                        if (type != null) {
                            XmlCursor c = cursor.newCursor();
                            c.toLastChild();
                            c.insertElement(part.getName());
                            c.toPrevToken();

                            xmlGenerator.createSampleForType(type, c);
                            c.dispose();
                        } else {
                            log.warn("Failed to find type [" + typeName + "]");
                        }
                    } else {
                        SchemaGlobalElement element = wsdlContext.getSchemaTypeLoader().findElement(part.getElementName());
                        if (element != null) {
                            XmlCursor c = cursor.newCursor();
                            c.toLastChild();
                            c.insertElement(element.getName());
                            c.toPrevToken();

                            xmlGenerator.createSampleForType(element.getType(), c);
                            c.dispose();
                        } else {
                            log.warn("Failed to find element [" + part.getElementName() + "]");
                        }
                    }
                }
            }
        }
    }

    public void setWsdlContext(WsdlContext wsdlContext) {
        this.wsdlContext = wsdlContext;
    }

    public void setInterface(WsdlInterface iface) {
        this.iface = iface;
    }

    public String buildSoapMessageFromOutput(BindingOperation bindingOperation, boolean buildOptional)
            throws Exception {
        return buildSoapMessageFromOutput(bindingOperation, buildOptional, true);
    }

    public String buildSoapMessageFromOutput(BindingOperation bindingOperation, boolean buildOptional,
                                             boolean alwaysBuildHeaders) throws Exception {
        boolean inputSoapEncoded = WsdlUtils.isInputSoapEncoded(bindingOperation);
        SampleXmlUtil xmlGenerator = new SampleXmlUtil(inputSoapEncoded);
        xmlGenerator.setIgnoreOptional(!buildOptional);
        xmlGenerator.setMultiValues(multiValues);

        XmlObject object = XmlObject.Factory.newInstance();
        XmlCursor cursor = object.newCursor();
        cursor.toNextToken();
        cursor.beginElement(wsdlContext.getSoapVersion().getEnvelopeQName());

        if (inputSoapEncoded) {
            cursor.insertNamespace("xsi", Constants.XSI_NS);
            cursor.insertNamespace("xsd", Constants.XSD_NS);
        }

        cursor.toFirstChild();

        cursor.beginElement(wsdlContext.getSoapVersion().getBodyQName());
        cursor.toFirstChild();

        if (WsdlUtils.isRpc(wsdlContext.getDefinition(), bindingOperation)) {
            buildRpcResponse(bindingOperation, cursor, xmlGenerator);
        } else {
            buildDocumentResponse(bindingOperation, cursor, xmlGenerator);
        }

        if (alwaysBuildHeaders) {
            // bindingOutput will be null for one way operations,
            // but then we shouldn't be here in the first place???
            BindingOutput bindingOutput = bindingOperation.getBindingOutput();
            if (bindingOutput != null) {
                List<?> extensibilityElements = bindingOutput.getExtensibilityElements();
                List<SoapHeader> soapHeaders = WsdlUtils.getSoapHeaders(extensibilityElements);
                addHeaders(soapHeaders, cursor, xmlGenerator);
            }
        }
        cursor.dispose();

        try {
            StringWriter writer = new StringWriter();
            XmlUtils.serializePretty(object, writer);
            return writer.toString();
        } catch (Exception e) {
            SoapUI.logError(e);
            return object.xmlText();
        }
    }

    public String buildFault(FaultPart faultPart) {
        SampleXmlUtil generator = new SampleXmlUtil(false);
        generator.setExampleContent(false);
        generator.setTypeComment(false);
        generator.setMultiValues(multiValues);
        String faultResponse = iface.getMessageBuilder().buildEmptyFault();

        XmlCursor cursor = null;
        try {
            // XmlObject xmlObject = XmlObject.Factory.parse( faultResponse );
            XmlObject xmlObject = XmlUtils.createXmlObject(faultResponse);
            XmlObject[] detail = xmlObject.selectPath("//detail");
            if (detail.length > 0) {
                cursor = detail[0].newCursor();

                cursor.toFirstContentToken();

                generator.setTypeComment(true);
                generator.setIgnoreOptional(iface.getSettings().getBoolean(
                        WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS));

                for (Part part : faultPart.getWsdlParts()) {
                    createElementForPart(part, cursor, generator);
                }
            }

            faultResponse = xmlObject.xmlText(new XmlOptions().setSaveAggressiveNamespaces().setSavePrettyPrint());
        } catch (Exception e1) {
            SoapUI.logError(e1);
        } finally {
            if (cursor != null) {
                cursor.dispose();
            }
        }

        return faultResponse;
    }
}
