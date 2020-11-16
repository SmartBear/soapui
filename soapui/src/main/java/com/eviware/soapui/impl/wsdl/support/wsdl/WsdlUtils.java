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
import com.eviware.soapui.config.AnonymousTypeConfig;
import com.eviware.soapui.config.DefinitionCacheConfig;
import com.eviware.soapui.config.DefinitionCacheTypeConfig;
import com.eviware.soapui.config.DefintionPartConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.support.definition.DefinitionLoader;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.impl.wsdl.support.policy.PolicyUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaConfig;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.settings.WsaSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.xml.XmlUtils;
import com.ibm.wsdl.util.xml.QNameUtils;
import com.ibm.wsdl.xml.WSDLReaderImpl;
import com.ibm.wsdl.xml.WSDLWriterImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.AttributeExtensible;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Fault;
import javax.wsdl.extensions.soap12.SOAP12Header;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Wsdl-related tools
 *
 * @author Ole.Matzura
 */

public class WsdlUtils {
    private final static Logger log = LogManager.getLogger(WsdlUtils.class);
    private static WSDLReader wsdlReader;
    private final static String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";

    public static <T extends ExtensibilityElement> T getExtensiblityElement(List<?> list, Class<T> clazz) {
        List<T> elements = getExtensiblityElements(list, clazz);
        return elements.isEmpty() ? null : elements.get(0);
    }

    public static <T extends ExtensibilityElement> List<T> getExtensiblityElements(List list, Class<T> clazz) {
        List<T> result = new ArrayList<T>();

        for (Iterator<T> i = list.iterator(); i.hasNext(); ) {
            T elm = i.next();
            if (clazz.isAssignableFrom(elm.getClass())) {
                result.add(elm);
            }
        }

        return result;
    }

    public static Element[] getExentsibilityElements(ElementExtensible item, QName qname) {
        if (item == null) {
            return new Element[0];
        }

        List<Element> result = new ArrayList<Element>();

        List<?> list = item.getExtensibilityElements();
        for (Iterator<?> i = list.iterator(); i.hasNext(); ) {
            ExtensibilityElement elm = (ExtensibilityElement) i.next();
            if (elm.getElementType().equals(qname) && elm instanceof UnknownExtensibilityElement) {
                result.add(((UnknownExtensibilityElement) elm).getElement());
            }
        }

        return result.toArray(new Element[result.size()]);
    }

    public static String[] getExentsibilityAttributes(AttributeExtensible item, QName qname) {
        if (item == null) {
            return new String[0];
        }

        StringList result = new StringList();

        Map map = item.getExtensionAttributes();

        for (Iterator<?> i = map.keySet().iterator(); i.hasNext(); ) {
            QName name = (QName) i.next();
            if (name.equals(qname)) {
                result.add(map.get(name).toString());
            }
        }

        return result.toStringArray();
    }

    public static String getSoapAction(BindingOperation operation) {
        List list = operation.getExtensibilityElements();
        SOAPOperation soapOperation = WsdlUtils.getExtensiblityElement(list, SOAPOperation.class);
        if (soapOperation != null) {
            return soapOperation.getSoapActionURI();
        }

        SOAP12Operation soap12Operation = WsdlUtils.getExtensiblityElement(list, SOAP12Operation.class);
        if (soap12Operation != null) {
            return soap12Operation.getSoapActionURI();
        }

        return null;
    }

    public static String[] getEndpointsForBinding(Definition definition, Binding binding) {
        List<String> result = new ArrayList<String>();
        Map map = definition.getAllServices();
        for (Iterator i = map.values().iterator(); i.hasNext(); ) {
            Service service = (Service) i.next();
            Map portMap = service.getPorts();
            for (Iterator i2 = portMap.values().iterator(); i2.hasNext(); ) {
                Port port = (Port) i2.next();
                if (port.getBinding() == binding) {
                    String endpoint = WsdlUtils.getSoapEndpoint(port);
                    if (endpoint != null) {
                        result.add(endpoint);
                    }
                }
            }
        }

        return result.toArray(new String[result.size()]);
    }

    public static Binding findBindingForOperation(Definition definition, BindingOperation bindingOperation) {
        Map services = definition.getAllServices();
        Iterator<Service> s = services.values().iterator();

        while (s.hasNext()) {
            Map ports = s.next().getPorts();
            Iterator<Port> p = ports.values().iterator();
            while (p.hasNext()) {
                Binding binding = p.next().getBinding();
                List bindingOperations = binding.getBindingOperations();
                for (Iterator iter = bindingOperations.iterator(); iter.hasNext(); ) {
                    BindingOperation op = (BindingOperation) iter.next();
                    if (op.getName().equals(bindingOperation.getName())) {
                        return binding;
                    }
                }
            }
        }

        Map bindings = definition.getAllBindings();
        Iterator<QName> names = bindings.keySet().iterator();
        while (names.hasNext()) {
            Binding binding = definition.getBinding(names.next());
            List bindingOperations = binding.getBindingOperations();
            for (Iterator iter = bindingOperations.iterator(); iter.hasNext(); ) {
                BindingOperation op = (BindingOperation) iter.next();
                if (op.getName().equals(bindingOperation.getName())) {
                    return binding;
                }
            }
        }

        return null;
    }

    public static BindingOperation findBindingOperation(Definition definition, String operationName) {
        Map services = definition.getAllServices();
        for (Iterator i = services.keySet().iterator(); i.hasNext(); ) {
            QName qName = (QName) i.next();
            Service service = definition.getService(qName);
            Map ports = service.getPorts();

            for (Iterator iterator = ports.keySet().iterator(); iterator.hasNext(); ) {
                String key = (String) iterator.next();
                Port port = service.getPort(key);
                BindingOperation bindingOperation = port.getBinding().getBindingOperation(operationName, null, null);
                if (bindingOperation != null) {
                    return bindingOperation;
                }
            }
        }

        Map bindings = definition.getAllBindings();
        for (Iterator i = bindings.keySet().iterator(); i.hasNext(); ) {
            Object key = i.next();
            Binding binding = (Binding) bindings.get(key);
            BindingOperation bindingOperation = binding.getBindingOperation(operationName, null, null);
            if (bindingOperation != null) {
                return bindingOperation;
            }
        }

        return null;
    }

    public static boolean isInputSoapEncoded(BindingOperation bindingOperation) {
        if (bindingOperation == null) {
            return false;
        }

        BindingInput bindingInput = bindingOperation.getBindingInput();
        if (bindingInput == null) {
            return false;
        }

        SOAPBody soapBody = WsdlUtils.getExtensiblityElement(bindingInput.getExtensibilityElements(), SOAPBody.class);

        if (soapBody != null) {
            return soapBody.getUse() != null
                    && soapBody.getUse().equalsIgnoreCase("encoded")
                    && (soapBody.getEncodingStyles() == null || soapBody.getEncodingStyles().contains(
                    "http://schemas.xmlsoap.org/soap/encoding/"));
        }

        SOAP12Body soap12Body = WsdlUtils.getExtensiblityElement(bindingInput.getExtensibilityElements(),
                SOAP12Body.class);

        if (soap12Body != null) {
            return soap12Body.getUse() != null
                    && soap12Body.getUse().equalsIgnoreCase("encoded")
                    && (soap12Body.getEncodingStyle() == null || soap12Body.getEncodingStyle().equals(
                    "http://schemas.xmlsoap.org/soap/encoding/"));
        }

        return false;
    }

    public static boolean isOutputSoapEncoded(BindingOperation bindingOperation) {
        if (bindingOperation == null) {
            return false;
        }

        BindingOutput bindingOutput = bindingOperation.getBindingOutput();
        if (bindingOutput == null) {
            return false;
        }

        SOAPBody soapBody = WsdlUtils.getExtensiblityElement(bindingOutput.getExtensibilityElements(), SOAPBody.class);

        if (soapBody != null) {
            return soapBody.getUse() != null
                    && soapBody.getUse().equalsIgnoreCase("encoded")
                    && (soapBody.getEncodingStyles() == null || soapBody.getEncodingStyles().contains(
                    "http://schemas.xmlsoap.org/soap/encoding/"));
        }

        SOAP12Body soap12Body = WsdlUtils.getExtensiblityElement(bindingOutput.getExtensibilityElements(),
                SOAP12Body.class);

        if (soap12Body != null) {
            return soap12Body.getUse() != null
                    && soap12Body.getUse().equalsIgnoreCase("encoded")
                    && (soap12Body.getEncodingStyle() == null || soap12Body.getEncodingStyle().equals(
                    "http://schemas.xmlsoap.org/soap/encoding/"));
        }

        return false;
    }

    public static boolean isRpc(Definition definition, BindingOperation bindingOperation) {
        SOAPOperation soapOperation = WsdlUtils.getExtensiblityElement(bindingOperation.getExtensibilityElements(),
                SOAPOperation.class);

        if (soapOperation != null && soapOperation.getStyle() != null) {
            return soapOperation.getStyle().equalsIgnoreCase("rpc");
        }

        SOAP12Operation soap12Operation = WsdlUtils.getExtensiblityElement(bindingOperation.getExtensibilityElements(),
                SOAP12Operation.class);

        if (soap12Operation != null && soap12Operation.getStyle() != null) {
            return soap12Operation.getStyle().equalsIgnoreCase("rpc");
        }

        Binding binding = findBindingForOperation(definition, bindingOperation);
        if (binding == null) {
            log.error("Failed to find binding for operation [" + bindingOperation.getName() + "] in definition ["
                    + definition.getDocumentBaseURI() + "]");
            return false;
        }

        return isRpc(binding);
    }

    public static boolean isRpc(Binding binding) {
        SOAPBinding soapBinding = WsdlUtils
                .getExtensiblityElement(binding.getExtensibilityElements(), SOAPBinding.class);

        if (soapBinding != null) {
            return "rpc".equalsIgnoreCase(soapBinding.getStyle());
        }

        SOAP12Binding soap12Binding = WsdlUtils.getExtensiblityElement(binding.getExtensibilityElements(),
                SOAP12Binding.class);

        if (soap12Binding != null) {
            return "rpc".equalsIgnoreCase(soap12Binding.getStyle());
        }

        return false;
    }

    /**
     * Returns a list of parts for the specifed operation, either as specified in
     * body or all
     */

    public static Part[] getInputParts(BindingOperation operation) {
        List<Part> result = new ArrayList<Part>();
        Input input = operation.getOperation().getInput();
        if (input == null || operation.getBindingInput() == null) {
            return new Part[0];
        }

        Message msg = input.getMessage();

        if (msg != null) {
            SOAPBody soapBody = WsdlUtils.getExtensiblityElement(operation.getBindingInput().getExtensibilityElements(),
                    SOAPBody.class);

            if (soapBody == null || soapBody.getParts() == null) {
                SOAP12Body soap12Body = WsdlUtils.getExtensiblityElement(operation.getBindingInput()
                        .getExtensibilityElements(), SOAP12Body.class);

                if (soap12Body == null || soap12Body.getParts() == null) {
                    if (msg != null) {
                        result.addAll(msg.getOrderedParts(null));
                    }
                } else {
                    Iterator i = soap12Body.getParts().iterator();
                    while (i.hasNext()) {
                        String partName = (String) i.next();
                        Part part = msg.getPart(partName);

                        result.add(part);
                    }
                }
            } else {
                Iterator i = soapBody.getParts().iterator();
                while (i.hasNext()) {
                    String partName = (String) i.next();
                    Part part = msg.getPart(partName);

                    result.add(part);
                }
            }
        } else {
        }

        return result.toArray(new Part[result.size()]);
    }

    public static boolean isAttachmentInputPart(Part part, BindingOperation operation) {
        return getInputMultipartContent(part, operation).length > 0;
    }

    public static boolean isAttachmentOutputPart(Part part, BindingOperation operation) {
        return getOutputMultipartContent(part, operation).length > 0;
    }

    public static MIMEContent[] getOutputMultipartContent(Part part, BindingOperation operation) {
        BindingOutput output = operation.getBindingOutput();
        if (output == null) {
            return new MIMEContent[0];
        }

        MIMEMultipartRelated multipartOutput = WsdlUtils.getExtensiblityElement(output.getExtensibilityElements(),
                MIMEMultipartRelated.class);

        return getContentParts(part, multipartOutput);
    }

    public static MIMEContent[] getInputMultipartContent(Part part, BindingOperation operation) {
        BindingInput bindingInput = operation.getBindingInput();
        if (bindingInput == null) {
            return new MIMEContent[0];
        }

        MIMEMultipartRelated multipartInput = WsdlUtils.getExtensiblityElement(bindingInput.getExtensibilityElements(),
                MIMEMultipartRelated.class);

        return getContentParts(part, multipartInput);
    }

    public static MIMEContent[] getContentParts(Part part, MIMEMultipartRelated multipart) {
        List<MIMEContent> result = new ArrayList<MIMEContent>();

        if (multipart != null) {
            List<MIMEPart> parts = multipart.getMIMEParts();

            for (int c = 0; c < parts.size(); c++) {
                List<MIMEContent> contentParts = WsdlUtils.getExtensiblityElements(parts.get(c)
                        .getExtensibilityElements(), MIMEContent.class);

                for (MIMEContent content : contentParts) {
                    if (content.getPart().equals(part.getName())) {
                        result.add(content);
                    }
                }
            }
        }

        return result.toArray(new MIMEContent[result.size()]);
    }

    public static Part[] getFaultParts(BindingOperation bindingOperation, String faultName) throws Exception {
        List<Part> result = new ArrayList<Part>();

        BindingFault bindingFault = bindingOperation.getBindingFault(faultName);
        SOAPFault soapFault = WsdlUtils.getExtensiblityElement(bindingFault.getExtensibilityElements(), SOAPFault.class);

        Operation operation = bindingOperation.getOperation();
        if (soapFault != null && soapFault.getName() != null) {
            Fault fault = operation.getFault(soapFault.getName());
            if (fault == null) {
                throw new Exception("Missing Fault [" + soapFault.getName() + "] in operation [" + operation.getName()
                        + "]");
            }
            result.addAll(fault.getMessage().getOrderedParts(null));
        } else {
            SOAP12Fault soap12Fault = WsdlUtils.getExtensiblityElement(bindingFault.getExtensibilityElements(),
                    SOAP12Fault.class);

            if (soap12Fault != null && soap12Fault.getName() != null) {
                Fault fault = operation.getFault(soap12Fault.getName());
                if (fault != null && fault.getMessage() != null) {
                    result.addAll(fault.getMessage().getOrderedParts(null));
                }
            } else {
                Fault fault = operation.getFault(faultName);
                if (fault != null && fault.getMessage() != null) {
                    result.addAll(fault.getMessage().getOrderedParts(null));
                }
            }
        }

        return result.toArray(new Part[result.size()]);
    }

    public static String findSoapFaultPartName(SoapVersion soapVersion, BindingOperation bindingOperation,
                                               String message) throws Exception {
        if (WsdlUtils.isOutputSoapEncoded(bindingOperation)) {
            throw new Exception("SOAP-Encoded messages not supported");
        }

        // XmlObject xml = XmlObject.Factory.parse( message );
        XmlObject xml = XmlUtils.createXmlObject(message);
        XmlObject[] msgPaths = xml.selectPath("declare namespace env='" + soapVersion.getEnvelopeNamespace() + "';"
                + "$this/env:Envelope/env:Body/env:Fault");
        if (msgPaths.length == 0) {
            return null;
        }

        XmlObject msgXml = msgPaths[0];

        Map faults = bindingOperation.getBindingFaults();
        for (Iterator<BindingFault> i = faults.values().iterator(); i.hasNext(); ) {
            BindingFault bindingFault = i.next();
            String faultName = bindingFault.getName();
            Part[] faultParts = WsdlUtils.getFaultParts(bindingOperation, faultName);
            Part part = faultParts[0];

            QName elementName = part.getElementName();
            if (elementName != null) {
                XmlObject[] faultPaths = msgXml.selectPath("declare namespace env='" + soapVersion.getEnvelopeNamespace()
                        + "';" + "declare namespace ns='" + elementName.getNamespaceURI() + "';" + "//env:Fault/detail/ns:"
                        + elementName.getLocalPart());

                if (faultPaths.length == 1) {
                    return faultName;
                }
            }
            // this is not allowed by Basic Profile.. remove?
            else if (part.getTypeName() != null) {
                QName typeName = part.getTypeName();
                XmlObject[] faultPaths = msgXml.selectPath("declare namespace env='" + soapVersion.getEnvelopeNamespace()
                        + "';" + "declare namespace ns='" + typeName.getNamespaceURI() + "';" + "//env:Fault/detail/ns:"
                        + part.getName());

                if (faultPaths.length == 1) {
                    return faultName;
                }
            }
        }

        return null;
    }

    public static Part[] getOutputParts(BindingOperation operation) {
        BindingOutput bindingOutput = operation.getBindingOutput();
        if (bindingOutput == null) {
            return new Part[0];
        }

        List<Part> result = new ArrayList<Part>();
        Output output = operation.getOperation().getOutput();
        if (output == null) {
            return new Part[0];
        }

        Message msg = output.getMessage();
        if (msg != null) {
            SOAPBody soapBody = WsdlUtils
                    .getExtensiblityElement(bindingOutput.getExtensibilityElements(), SOAPBody.class);

            if (soapBody == null || soapBody.getParts() == null) {
                SOAP12Body soap12Body = WsdlUtils.getExtensiblityElement(bindingOutput.getExtensibilityElements(),
                        SOAP12Body.class);

                if (soap12Body == null || soap12Body.getParts() == null) {
                    result.addAll(msg.getOrderedParts(null));
                } else {
                    Iterator i = soap12Body.getParts().iterator();
                    while (i.hasNext()) {
                        String partName = (String) i.next();
                        Part part = msg.getPart(partName);

                        result.add(part);
                    }
                }
            } else {
                Iterator i = soapBody.getParts().iterator();
                while (i.hasNext()) {
                    String partName = (String) i.next();
                    Part part = msg.getPart(partName);

                    result.add(part);
                }
            }
        } else {
            log.warn("Missing output message for binding operation [" + operation.getName() + "]");
        }

        return result.toArray(new Part[result.size()]);
    }

    public static boolean isMultipartRequest(Definition definition, BindingOperation bindingOperation) {
        return WsdlUtils.getExtensiblityElement(bindingOperation.getBindingInput().getExtensibilityElements(),
                MIMEMultipartRelated.class) != null;
    }

    public static String getUsingAddressing(ElementExtensible item) {
        String version = WsaVersionTypeConfig.NONE.toString();

        Element[] usingAddressingElements = WsdlUtils.getExentsibilityElements(item, new QName(
                "http://www.w3.org/2006/05/addressing/wsdl", "UsingAddressing"));
        if (usingAddressingElements.length == 0) {
            usingAddressingElements = WsdlUtils.getExentsibilityElements(item, new QName(
                    "http://www.w3.org/2006/02/addressing/wsdl", "UsingAddressing"));
        }

        if (usingAddressingElements.length != 0) {
            // this should resolve wsdl version, not addressing version??? what is
            // the connection?
            String addressingVersion = usingAddressingElements[0].getAttributeNS(WSDL_NAMESPACE, "required");
            if (addressingVersion != null && addressingVersion.equals("true")) {
                version = WsaVersionTypeConfig.X_200508.toString();
            }

        }
        return version;
    }

    private static String checkIfWsaPolicy(String version, Element policy) {
        // Policy builtPolicy = new
        // WsaPolicy().buildPolicy(policy.getTextContent());
        // policy = WsaPolicy.normalize(policy);

        // check if found reference is addressing policy
        // Element wsAddressing = XmlUtils.getFirstChildElementNS(policy,
        // WsaUtils.WSAM_NAMESPACE, "Addressing");
        // Element addressingPolicy = null;
        // if (wsAddressing != null)
        // {
        // String optional =
        // wsAddressing.getAttributeNS(WsaPolicy.WS_POLICY_NAMESPACE, "Optional");
        // addressingPolicy = XmlUtils.getFirstChildElementNS(wsAddressing,
        // WsaPolicy.WS_POLICY_NAMESPACE, "Policy");
        // if (addressingPolicy != null)
        // {
        // if (StringUtils.isNullOrEmpty(optional) || optional.equals("false") ||
        // (optional.equals("true") &&
        // SoapUI.getSettings().getBoolean(WsaSettings.ENABLE_FOR_OPTIONAL)) )
        // {
        // version = WsaVersionTypeConfig.X_200508.toString();
        // }
        // //check if policy has Anonymous
        // Element anonymousElm =
        // XmlUtils.getFirstChildElementNS(addressingPolicy, new
        // QName(WsaPolicy.WSAM_NAMESPACE,"AnonymousResponses"));
        // if (anonymousElm != null)
        // {
        // //TODO anonymous = required
        // } else {
        // Element nonAnonymousElement =
        // XmlUtils.getFirstChildElementNS(addressingPolicy, new
        // QName(WsaPolicy.WSAM_NAMESPACE,"NonAnonymousResponses"));
        // if (nonAnonymousElement != null)
        // {
        // //TODO anonymous = prohibited
        // }
        // }
        // }
        // }
        return version;
    }

    public static String getWsaPolicyAnonymous(Element policy) {
        String version = WsaVersionTypeConfig.NONE.toString();
        String anonymous = AnonymousTypeConfig.OPTIONAL.toString();
        // check if found reference is addressing policy
        Element wsAddressing = XmlUtils.getFirstChildElementNS(policy, WsaUtils.WS_A_NAMESPACE_200705, "Addressing");
        Element addressingPolicy = null;
        if (wsAddressing != null) {
            String optional = wsAddressing.getAttributeNS(PolicyUtils.WS_W3_POLICY_NAMESPACE, "Optional");
            addressingPolicy = XmlUtils
                    .getFirstChildElementNS(wsAddressing, PolicyUtils.WS_W3_POLICY_NAMESPACE, "Policy");
            if (addressingPolicy != null) {
                if (StringUtils.isNullOrEmpty(optional) || optional.equals("false")
                        || (optional.equals("true") && SoapUI.getSettings().getBoolean(WsaSettings.ENABLE_FOR_OPTIONAL))) {
                    version = WsaVersionTypeConfig.X_200508.toString();
                }
                // check if policy has Anonymous
                Element anonymousElm = XmlUtils.getFirstChildElementNS(addressingPolicy, new QName(
                        WsaUtils.WS_A_NAMESPACE_200705, "AnonymousResponses"));
                if (anonymousElm != null) {
                    anonymous = AnonymousTypeConfig.REQUIRED.toString();
                } else {
                    Element nonAnonymousElement = XmlUtils.getFirstChildElementNS(addressingPolicy, new QName(
                            WsaUtils.WS_A_NAMESPACE_200705, "NonAnonymousResponses"));
                    if (nonAnonymousElement != null) {
                        anonymous = AnonymousTypeConfig.PROHIBITED.toString();
                    }
                }
            }
        }
        return anonymous;
    }

    public static String getSoapEndpoint(Port port) {
        SOAPAddress soapAddress = WsdlUtils.getExtensiblityElement(port.getExtensibilityElements(), SOAPAddress.class);
        if (soapAddress != null && StringUtils.hasContent(soapAddress.getLocationURI())) {
            try {
                return URLDecoder.decode(soapAddress.getLocationURI(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return soapAddress.getLocationURI();
            }
        }

        SOAP12Address soap12Address = WsdlUtils.getExtensiblityElement(port.getExtensibilityElements(),
                SOAP12Address.class);
        if (soap12Address != null && StringUtils.hasContent(soap12Address.getLocationURI())) {
            try {
                return URLDecoder.decode(soap12Address.getLocationURI(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return soap12Address.getLocationURI();
            }
        }

        return null;
    }

    public static boolean replaceSoapEndpoint(Port port, String endpoint) {
        SOAPAddress soapAddress = WsdlUtils.getExtensiblityElement(port.getExtensibilityElements(), SOAPAddress.class);
        if (soapAddress != null) {
            soapAddress.setLocationURI(endpoint);
            return true;
        }

        SOAP12Address soap12Address = WsdlUtils.getExtensiblityElement(port.getExtensibilityElements(),
                SOAP12Address.class);
        if (soap12Address != null) {
            soap12Address.setLocationURI(endpoint);
            return true;
        }

        return false;
    }

    public static String getSoapBodyNamespace(List<?> list) {
        SOAPBody soapBody = WsdlUtils.getExtensiblityElement(list, SOAPBody.class);
        if (soapBody != null) {
            return soapBody.getNamespaceURI();
        }

        SOAP12Body soap12Body = WsdlUtils.getExtensiblityElement(list, SOAP12Body.class);
        if (soap12Body != null) {
            return soap12Body.getNamespaceURI();
        }

        return null;
    }

    public static final class NonSchemaImportingWsdlReaderImpl extends WSDLReaderImpl {
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensibilityElement parseSchema(Class parentType, Element el, Definition def, ExtensionRegistry extReg)
                throws WSDLException {
            QName elementType = QNameUtils.newQName(el);

            ExtensionDeserializer exDS = extReg.queryDeserializer(parentType, elementType);

            // Now unmarshall the DOM element.
            ExtensibilityElement ee = exDS.unmarshall(parentType, elementType, el, def, extReg);

            return ee;
        }

    }

    /**
     * A SOAP-Header wrapper
     *
     * @author ole.matzura
     */

    public interface SoapHeader {
        public QName getMessage();

        public String getPart();
    }

    /**
     * SOAP 1.1 Header implementation
     *
     * @author ole.matzura
     */

    public static class Soap11Header implements SoapHeader {
        private final SOAPHeader soapHeader;

        public Soap11Header(SOAPHeader soapHeader) {
            this.soapHeader = soapHeader;
        }

        public QName getMessage() {
            return soapHeader.getMessage();
        }

        public String getPart() {
            return soapHeader.getPart();
        }
    }

    /**
     * SOAP 1.2 Header implementation
     *
     * @author ole.matzura
     */

    public static class Soap12Header implements SoapHeader {
        private final SOAP12Header soapHeader;

        public Soap12Header(SOAP12Header soapHeader) {
            this.soapHeader = soapHeader;
        }

        public QName getMessage() {
            return soapHeader.getMessage();
        }

        public String getPart() {
            return soapHeader.getPart();
        }
    }

    public static List<SoapHeader> getSoapHeaders(List list) {
        List<SoapHeader> result = new ArrayList<SoapHeader>();

        List<SOAPHeader> soapHeaders = WsdlUtils.getExtensiblityElements(list, SOAPHeader.class);
        if (soapHeaders != null && !soapHeaders.isEmpty()) {
            for (SOAPHeader header : soapHeaders) {
                result.add(new Soap11Header(header));
            }
        } else {
            List<SOAP12Header> soap12Headers = WsdlUtils.getExtensiblityElements(list, SOAP12Header.class);
            if (soap12Headers != null && !soap12Headers.isEmpty()) {
                for (SOAP12Header header : soap12Headers) {
                    result.add(new Soap12Header(header));
                }
            }
        }

        return result;
    }

    public static synchronized Definition readDefinition(String wsdlUrl) throws Exception {
        if (wsdlReader == null) {
            WSDLFactory factory = WSDLFactory.newInstance();
            wsdlReader = factory.newWSDLReader();
            wsdlReader.setFeature("javax.wsdl.verbose", true);
            wsdlReader.setFeature("javax.wsdl.importDocuments", true);
        }

        return wsdlReader.readWSDL(new UrlWsdlLoader(wsdlUrl));
    }

    public static SchemaType getSchemaTypeForPart(WsdlContext wsdlContext, javax.wsdl.Part part) throws Exception {
        SchemaType schemaType = null;
        QName elementName = part.getElementName();

        if (elementName != null) {
            SchemaGlobalElement elm = wsdlContext.getSchemaTypeLoader().findElement(elementName);
            if (elm != null) {
                schemaType = elm.getType();
            } else {
                WsdlRequest.log.error("Could not find element [" + elementName + "] specified in part [" + part.getName()
                        + "]");
            }
        } else {
            QName typeName = part.getTypeName();

            if (typeName != null) {
                schemaType = wsdlContext.getSchemaTypeLoader().findType(typeName);

                if (schemaType == null) {
                    WsdlRequest.log.error("Could not find type [" + typeName + "] specified in part [" + part.getName()
                            + "]");
                }
            }
        }
        return schemaType;
    }

    public static SchemaGlobalElement getSchemaElementForPart(WsdlContext wsdlContext, javax.wsdl.Part part)
            throws Exception {
        QName elementName = part.getElementName();

        if (elementName != null) {
            return wsdlContext.getSchemaTypeLoader().findElement(elementName);
        }

        return null;
    }

    public static String replacePortEndpoint(WsdlInterface iface, InputSource inputSource, String endpoint)
            throws WSDLException {
        WSDLReader wsdlReader = new NonSchemaImportingWsdlReaderImpl();

        wsdlReader.setFeature("javax.wsdl.verbose", true);
        wsdlReader.setFeature("javax.wsdl.importDocuments", false);

        Definition definition = wsdlReader.readWSDL(null, inputSource);

        boolean updated = false;
        Map map = definition.getServices();
        for (Iterator i = map.values().iterator(); i.hasNext(); ) {
            Service service = (Service) i.next();
            Map portMap = service.getPorts();
            for (Iterator i2 = portMap.values().iterator(); i2.hasNext(); ) {
                Port port = (Port) i2.next();
                if (port.getBinding().getQName().equals(iface.getBindingName())) {
                    if (replaceSoapEndpoint(port, endpoint)) {
                        updated = true;
                    }
                }
            }
        }

        if (updated) {
            StringWriter writer = new StringWriter();

            Map nsMap = definition.getNamespaces();
            if (!nsMap.values().contains(Constants.SOAP_HTTP_BINDING_NS)) {
                definition.addNamespace("soaphttp", Constants.SOAP_HTTP_BINDING_NS);
            }

            new WSDLWriterImpl().writeWSDL(definition, writer);
            return writer.toString();
        }

        return null;
    }

    public static BindingOperation findBindingOperation(Binding binding, String bindingOperationName, String inputName,
                                                        String outputName) {
        if (binding == null) {
            return null;
        }

        if (inputName == null) {
            inputName = ":none";
        }

        if (outputName == null) {
            outputName = ":none";
        }

        BindingOperation result = binding.getBindingOperation(bindingOperationName, inputName, outputName);

        if (result == null && (inputName.equals(":none") || outputName.equals(":none"))) {
            // fall back to this behaviour for WSDL4j 1.5.0 compatibility
            result = binding.getBindingOperation(bindingOperationName, inputName.equals(":none") ? null : inputName,
                    outputName.equals(":none") ? null : outputName);
        }
        return result;
    }

    public static boolean isHeaderInputPart(Part part, Message message, BindingOperation bindingOperation) {
        List<SOAPHeader> headers = WsdlUtils.getExtensiblityElements(bindingOperation.getBindingInput()
                .getExtensibilityElements(), SOAPHeader.class);

        if (headers == null || headers.isEmpty()) {
            return false;
        }

        for (SOAPHeader header : headers) {
            if (message.getQName().equals(header.getMessage()) && part.getName().equals(header.getPart())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHeaderOutputPart(Part part, Message message, BindingOperation bindingOperation) {
        BindingOutput bindingOutput = bindingOperation.getBindingOutput();
        List<SOAPHeader> headers = bindingOutput == null ? null : WsdlUtils.getExtensiblityElements(
                bindingOutput.getExtensibilityElements(), SOAPHeader.class);

        if (headers == null || headers.isEmpty()) {
            return false;
        }

        for (SOAPHeader header : headers) {
            if (message.getQName().equals(header.getMessage()) && part.getName().equals(header.getPart())) {
                return true;
            }
        }

        return false;
    }

    public static DefinitionCacheConfig cacheWsdl(DefinitionLoader loader) throws Exception {
        DefinitionCacheConfig definitionCache = DefinitionCacheConfig.Factory.newInstance();
        definitionCache.setRootPart(loader.getBaseURI());
        definitionCache.setType(DefinitionCacheTypeConfig.TEXT);

        Map<String, XmlObject> urls = SchemaUtils.getDefinitionParts(loader);

        for (Map.Entry<String, XmlObject> urlEntry : urls.entrySet()) {
            DefintionPartConfig definitionPart = definitionCache.addNewPart();
            String url = urlEntry.getKey();
            definitionPart.setUrl(url);
            XmlObject xmlObject = urlEntry.getValue();
            Node domNode = xmlObject.getDomNode();

            if (domNode.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
                Node node = domNode.getFirstChild();
                if (node.getNodeType() == Node.TEXT_NODE) {
                    domNode = XmlUtils.parseXml(node.getNodeValue());
                    xmlObject = XmlUtils.createXmlObject(domNode);
                }
            }

            Element contentElement = ((Document) domNode).getDocumentElement();

            Node newDomNode = definitionPart.addNewContent().getDomNode();
            newDomNode.appendChild(newDomNode.getOwnerDocument().createTextNode(xmlObject.toString()));
            definitionPart.setType(contentElement.getNamespaceURI());
        }

        return definitionCache;
    }

    public static void getAnonymous(WsdlOperation wsdlOperation) {
        String anonymous = "";

        Element[] anonymousElements = WsdlUtils.getExentsibilityElements(wsdlOperation.getBindingOperation(), new QName(
                "http://www.w3.org/2006/05/addressing/wsdl", "Anonymous"));
        if (anonymousElements.length == 0) {
            anonymousElements = WsdlUtils.getExentsibilityElements(wsdlOperation.getBindingOperation(), new QName(
                    "http://www.w3.org/2006/02/addressing/wsdl", "Anonymous"));
        }

        if (anonymousElements != null && anonymousElements.length > 0) {
            anonymous = XmlUtils.getElementText(anonymousElements[0]);
        }
        wsdlOperation.setAnonymous(anonymous);
    }

    public static String getDefaultWsaAction(WsdlOperation operation, boolean output) {
        // SOAP 1.1 specific handling
        if (operation.getInterface().getSoapVersion() == SoapVersion.Soap11
                && StringUtils.hasContent(operation.getAction())
                && SoapUI.getSettings().getBoolean(WsaSettings.SOAP_ACTION_OVERRIDES_WSA_ACTION)) {
            return operation.getAction();
        }

        try {
            if (operation.getBindingOperation() == null || operation.getBindingOperation().getOperation() == null) {
                return null;
            }

            AttributeExtensible attributeExtensible = output ? operation.getBindingOperation().getOperation().getOutput()
                    : operation.getBindingOperation().getOperation().getInput();

            if (attributeExtensible == null) {
                return null;
            }

            String[] attrs;
            for (String namespace : WsaUtils.wsaNamespaces) {
                attrs = WsdlUtils.getExentsibilityAttributes(attributeExtensible, new QName(namespace, "Action"));
                if (attrs != null && attrs.length > 0) {
                    return attrs[0];
                }
            }

            WsdlInterface iface = operation.getInterface();

            Definition definition = iface.getWsdlContext().getDefinition();
            String targetNamespace = WsdlUtils.getTargetNamespace(definition);
            String portTypeName = iface.getBinding().getPortType().getQName().getLocalPart();
            String operationName = operation.getName();
            if (!StringUtils.isNullOrEmpty(operationName)) {
                Operation op = iface.getBinding().getPortType().getOperation(operationName, null, null);
                if (op != null) {
                    attributeExtensible = output ? op.getOutput() : op.getInput();
                    attrs = WsdlUtils.getExentsibilityAttributes(attributeExtensible, new QName(
                            WsaUtils.WS_A_NAMESPACE_200705, "Action"));
                    if (attrs != null && attrs.length > 0) {
                        return attrs[0];
                    }
                }
            }
            String operationInOutName = output ? operation.getOutputName() : operation.getInputName();
            if (operationInOutName == null) {
                operationInOutName = operation.getName() + (output ? "Response" : "Request");
            }

            StringBuffer result = new StringBuffer(targetNamespace);
            if (targetNamespace.length() > 0 && targetNamespace.charAt(targetNamespace.length() - 1) != '/'
                    && portTypeName.charAt(0) != '/') {
                result.append('/');
            }
            result.append(portTypeName);
            if (portTypeName.charAt(portTypeName.length() - 1) != '/' && operationInOutName.charAt(0) != '/') {
                result.append('/');
            }
            result.append(operationInOutName);

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.warn(e.toString());
            return null;
        }
    }

    public static void setDefaultWsaAction(WsaConfig wsaConfig, boolean output) {
        String defaultAction = getDefaultWsaAction(wsaConfig.getWsaContainer().getOperation(), output);
        if (StringUtils.hasContent(defaultAction)) {
            wsaConfig.setAction(defaultAction);
        }
    }

    public static String getRequestWsaMessageId(WsdlMessageExchange messageExchange, String wsaVersionNameSpace) {
        String requestMessageId = null;
        try {
            // XmlObject xmlObject = XmlObject.Factory.parse(
            // messageExchange.getRequestContent() );
            XmlObject xmlObject = XmlUtils.createXmlObject(messageExchange.getRequestContent());
            SoapVersion soapVersion = messageExchange.getOperation().getInterface().getSoapVersion();

            Element header = (Element) SoapUtils.getHeaderElement(xmlObject, soapVersion, true).getDomNode();
            Element msgNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "MessageID");
            if (msgNode != null) {
                requestMessageId = XmlUtils.getElementText(msgNode);
            }
        } catch (XmlException e) {
            e.printStackTrace();
            log.warn(e.toString());
            return null;
        }
        return requestMessageId;
    }

    public static NodeList getRequestReplyToRefProps(WsdlMessageExchange messageExchange, String wsaVersionNameSpace) {
        try {
            // XmlObject xmlObject = XmlObject.Factory.parse(
            // messageExchange.getRequestContent() );
            XmlObject xmlObject = XmlUtils.createXmlObject(messageExchange.getRequestContent());
            SoapVersion soapVersion = messageExchange.getOperation().getInterface().getSoapVersion();

            Element header = (Element) SoapUtils.getHeaderElement(xmlObject, soapVersion, true).getDomNode();
            Element replyToNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "ReplyTo");
            Element replyRefParamsNode = XmlUtils.getFirstChildElementNS(replyToNode, wsaVersionNameSpace,
                    "ReferenceParameters");
            if (replyRefParamsNode != null) {
                return XmlUtils.getChildElements(replyRefParamsNode);
            }
        } catch (XmlException e) {
            e.printStackTrace();
            log.warn(e.toString());
        }
        return null;
    }

    public static NodeList getRequestFaultToRefProps(WsdlMessageExchange messageExchange, String wsaVersionNameSpace) {
        try {
            XmlObject xmlObject = XmlUtils.createXmlObject(messageExchange.getRequestContent());
            SoapVersion soapVersion = messageExchange.getOperation().getInterface().getSoapVersion();

            Element header = (Element) SoapUtils.getHeaderElement(xmlObject, soapVersion, true).getDomNode();
            Element faultToNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "FaultTo");
            Element faultRefParamsNode = XmlUtils.getFirstChildElementNS(faultToNode, wsaVersionNameSpace,
                    "ReferenceParameters");
            if (faultRefParamsNode != null) {
                return XmlUtils.getChildElements(faultRefParamsNode);
            }
        } catch (XmlException e) {
            e.printStackTrace();
            log.warn(e.toString());
        }
        return null;
    }

    public static String getFaultCode(WsdlMessageExchange messageExchange) {
        try {
            XmlObject xmlObject = XmlUtils.createXmlObject(messageExchange.getResponseContent());
            SoapVersion soapVersion = messageExchange.getOperation().getInterface().getSoapVersion();

            Element body = (Element) SoapUtils.getBodyElement(xmlObject, soapVersion).getDomNode();
            Element soapenvFault = XmlUtils.getFirstChildElementNS(body, "http://schemas.xmlsoap.org/soap/envelope/",
                    "Fault");
            Element faultCode = XmlUtils.getFirstChildElement(soapenvFault, "faultcode");
            if (faultCode != null) {
                return XmlUtils.getElementText(faultCode);
            }
        } catch (XmlException e) {
            e.printStackTrace();
            log.warn(e.toString());
        }
        return null;
    }

    public static String getTargetNamespace(Definition definition) {
        return definition.getTargetNamespace() == null ? XMLConstants.NULL_NS_URI : definition.getTargetNamespace();
    }

    public static SchemaType generateRpcBodyType(WsdlOperation operation) {
        return XmlString.type;
    }
}
