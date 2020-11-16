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

package com.eviware.soapui.support.xml;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringMap;
import net.sf.saxon.expr.Token;
import net.sf.saxon.expr.Tokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.util.SecurityManager;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * General XML-related utilities
 */

@SuppressWarnings("deprecation")
public final class XmlUtils {
    private static DocumentBuilder documentBuilder;
    private final static Logger log = LogManager.getLogger(XmlUtils.class);

    static synchronized public Document parse(InputStream in) {
        try {
            return ensureDocumentBuilder().parse(in);
        } catch (Exception e) {
            log.error("Error parsing InputStream; " + e.getMessage(), e);
        }

        return null;
    }

    static synchronized public Document parse(String fileName) throws IOException {
        try {
            return ensureDocumentBuilder().parse(fileName);
        } catch (SAXException e) {
            log.error("Error parsing fileName [" + fileName + "]; " + e.getMessage(), e);
        }

        return null;
    }

    public static String entitize(String xml) {
        return xml.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
    }

    public static String entitizeContent(String xml) {
        return xml.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
    }

    static synchronized public Document parse(InputSource inputSource) throws IOException {
        try {
            return ensureDocumentBuilder().parse(inputSource);
        } catch (SAXException e) {
            throw new IOException(e.toString());
        }
    }

    private static DocumentBuilder ensureDocumentBuilder() {
        if (documentBuilder == null) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                documentBuilder = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                log.error("Error creating DocumentBuilder; " + e.getMessage());
            }
        }

        return documentBuilder;
    }

    public static String serializePretty(Document document) {
        try {
            Writer out = new StringWriter();
            serializePretty(document, out);
            return out.toString();
        } catch (IOException e) {
            log.error("Failed to serialize: " + e);
        }
        return null;
    }

    public static void serializePretty(Document dom, Writer writer) throws IOException {
        try {
            XmlObject xmlObject = XmlObject.Factory.parse(dom.getDocumentElement());
            serializePretty(xmlObject, writer);
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
    }

    public static void serializePretty(XmlObject xmlObject, Writer writer) throws IOException {
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(3);
        options.setSaveNoXmlDecl();
        options.setSaveAggressiveNamespaces();
        // StringToStringMap map = new StringToStringMap();
        // map.put( SoapVersion.Soap11.getEnvelopeNamespace(), "SOAPENV" );
        // map.put( SoapVersion.Soap12.getEnvelopeNamespace(), "SOAPENV" );
        //
        // options.setSaveSuggestedPrefixes( map );

        xmlObject.save(writer, options);
    }

    public static XmlObject createXmlObject(InputStream input, XmlOptions xmlOptions) throws XmlException {
        try {
            return XmlObject.Factory.parse(input, xmlOptions);
        } catch (Exception e) {
            throw new XmlException(e.toString());
        }
    }

    public static XmlObject createXmlObject(String input, XmlOptions xmlOptions) throws XmlException {
        return XmlObject.Factory.parse(input, xmlOptions);
    }

    public static XmlObject createXmlObject(URL input, XmlOptions xmlOptions) throws XmlException {
        try {
            return XmlObject.Factory.parse(input, xmlOptions);
        } catch (Exception e) {
            throw new XmlException(e.toString());
        }
    }

    public static XmlObject createXmlObject(Node input, XmlOptions xmlOptions) throws XmlException {
        return XmlObject.Factory.parse(input, xmlOptions);
    }

    public static XmlObject createXmlObject(File input, XmlOptions xmlOptions) throws XmlException {
        try {
            return XmlObject.Factory.parse(input, xmlOptions);
        } catch (Exception e) {
            throw new XmlException(e.toString());
        }
    }

    public static XmlObject createXmlObject(InputStream input) throws XmlException {
        try {
            return XmlObject.Factory.parse(input);
        } catch (Exception e) {
            throw new XmlException(e.toString());
        }
    }

    public static XmlObject createXmlObject(String input) throws XmlException {
        return XmlObject.Factory.parse(input);
    }

    public static XmlObject createXmlObject(URL input) throws XmlException {
        try {
            return XmlObject.Factory.parse(input);
        } catch (Exception e) {
            throw new XmlException(e.toString());
        }
    }

    public static XmlObject createXmlObject(Node input) throws XmlException {
        return XmlObject.Factory.parse(input);
    }

    public static XmlObject createXmlObject(File input) throws XmlException {
        try {
            return XmlObject.Factory.parse(input);
        } catch (Exception e) {
            throw new XmlException(e.toString());
        }
    }

    /**
     * XmlOptions configuration used in preventing XML Bomb
     *
     * @return XmlOptions
     */
    public static XmlOptions createDefaultXmlOptions() {
        XmlOptions xmlOptions;

        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            SecurityManager securityManager = new SecurityManager();
            // Default seems to be 64000!
            securityManager.setEntityExpansionLimit(16);

            saxParser.setProperty("http://apache.org/xml/properties/security-manager", securityManager);
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlOptions = new XmlOptions().setLoadUseXMLReader(xmlReader);
        } catch (Exception e) {
            xmlOptions = new XmlOptions();
            log.error("Error creating XmlOptions; " + e.getMessage(), e);
        }
        return xmlOptions;
    }

    public static void serialize(Document dom, Writer writer) throws IOException {
        serialize(dom.getDocumentElement(), writer);
    }

    public static void serialize(Element elm, Writer writer) throws IOException {
        try {
            XmlObject xmlObject = XmlObject.Factory.parse(elm);
            xmlObject.save(writer);
        } catch (XmlException e) {
            throw new IOException(e.toString());
        }
    }

    static public String serialize(Node node, boolean prettyPrint) {
        try {
            XmlObject xmlObject = XmlObject.Factory.parse(node);
            return prettyPrint ? xmlObject.xmlText(new XmlOptions().setSavePrettyPrint()) : xmlObject.xmlText();
        } catch (XmlException e) {
            return e.toString();
        }
    }

    static public void setElementText(Element elm, String text) {
        Node node = elm.getFirstChild();
        if (node == null) {
            if (text != null) {
                elm.appendChild(elm.getOwnerDocument().createTextNode(text));
            }
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            if (text == null) {
                node.getParentNode().removeChild(node);
            } else {
                node.setNodeValue(text);
            }
        } else if (text != null) {
            Text textNode = node.getOwnerDocument().createTextNode(text);
            elm.insertBefore(textNode, elm.getFirstChild());
        }
    }

    public static String getChildElementText(Element elm, String name) {
        Element child = getFirstChildElement(elm, name);
        return child == null ? null : getElementText(child);
    }

    public static Element getFirstChildElement(Element elm) {
        return getFirstChildElement(elm, null);
    }

    public static Element getFirstChildElement(Element elm, String name) {
        if (elm == null) {
            return null;
        }

        NodeList nl = elm.getChildNodes();
        for (int c = 0; c < nl.getLength(); c++) {
            Node node = nl.item(c);
            if (node.getNodeType() == Node.ELEMENT_NODE && (name == null || node.getNodeName().equals(name))) {
                return (Element) node;
            }
        }

        return null;
    }

    public static Element getFirstChildElementIgnoreCase(Element elm, String name) {
        if (elm == null) {
            return null;
        }

        NodeList nl = elm.getChildNodes();
        for (int c = 0; c < nl.getLength(); c++) {
            Node node = nl.item(c);
            if (node.getNodeType() == Node.ELEMENT_NODE && (name == null || node.getNodeName().equalsIgnoreCase(name))) {
                return (Element) node;
            }
        }

        return null;
    }

    public static Element getFirstChildElementNS(Element elm, String tns, String localName) {
        if (tns == null && localName == null) {
            return getFirstChildElement(elm);
        }

        if (tns == null || tns.length() == 0) {
            return getFirstChildElement(elm, localName);
        }

        NodeList nl = elm.getChildNodes();
        for (int c = 0; c < nl.getLength(); c++) {
            Node node = nl.item(c);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (localName == null && tns.equals(node.getNamespaceURI())) {
                return (Element) node;
            }

            if (localName != null && tns.equals(node.getNamespaceURI()) && localName.equals(node.getLocalName())) {
                return (Element) node;
            }
        }

        return null;
    }

    static public String getElementText(Element elm) {
        Node node = elm.getFirstChild();
        if (node != null && node.getNodeType() == Node.TEXT_NODE) {
            return node.getNodeValue();
        }

        return null;
    }

    static public String getFragmentText(DocumentFragment elm) {
        Node node = elm.getFirstChild();
        if (node != null && node.getNodeType() == Node.TEXT_NODE) {
            return node.getNodeValue();
        }

        return null;
    }

    public static String getChildElementText(Element elm, String name, String defaultValue) {
        String result = getChildElementText(elm, name);
        return result == null ? defaultValue : result;
    }

    static public String getNodeValue(Node node) {
        if (node == null) {
            return null;
        }

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            return getElementText((Element) node);
        } else if (node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
            return getFragmentText((DocumentFragment) node);
        } else {
            return node.getNodeValue();
        }
    }

    public static Node createNodeFromPath(Element modelElement, String path) {
        Document document = modelElement.getOwnerDocument();
        StringTokenizer st = new StringTokenizer(path, "/");
        while (st.hasMoreTokens()) {
            String t = st.nextToken();

            if (st.hasMoreTokens()) {
                if (t.equals("..")) {
                    modelElement = (Element) modelElement.getParentNode();
                } else {
                    Element elm = getFirstChildElement(modelElement, t);
                    if (elm == null) {
                        modelElement = (Element) modelElement.insertBefore(document.createElement(t),
                                getFirstChildElement(modelElement, t));
                    } else {
                        modelElement = elm;
                    }
                }
            } else {
                modelElement = (Element) modelElement.insertBefore(document.createElement(t),
                        getFirstChildElement(modelElement, t));
            }
        }

        return modelElement;
    }

    public static Element addChildElement(Element element, String name, String text) {
        Document document = element.getOwnerDocument();
        Element result = (Element) element.appendChild(document.createElement(name));
        if (text != null) {
            result.appendChild(document.createTextNode(text));
        }

        return result;
    }

    public static void setChildElementText(Element element, String name, String text) {
        Element elm = getFirstChildElement(element, name);
        if (elm == null) {
            elm = element.getOwnerDocument().createElement(name);
            element.appendChild(elm);
        }

        setElementText(elm, text);
    }

    public static Document parseXml(String xmlString) throws IOException {
        return parse(new InputSource(new StringReader(xmlString)));
    }

    public static String transferValues(String source, String dest) {
        if (StringUtils.isNullOrEmpty(source) || StringUtils.isNullOrEmpty(dest)) {
            return dest;
        }

        XmlCursor cursor = null;
        try {
            XmlObject sourceXml = XmlObject.Factory.parse(source);
            XmlObject destXml = XmlObject.Factory.parse(dest);

            cursor = sourceXml.newCursor();
            cursor.toNextToken();
            while (!cursor.isEnddoc()) {
                while (!cursor.isContainer() && !cursor.isEnddoc()) {
                    cursor.toNextToken();
                }

                if (cursor.isContainer()) {
                    Element elm = (Element) cursor.getDomNode();
                    String path = createXPath(elm);
                    XmlObject[] paths = destXml.selectPath(path);
                    if (paths != null && paths.length > 0) {
                        Element elm2 = (Element) paths[0].getDomNode();

                        // transfer attributes
                        transferAttributes(elm, elm2);

                        // transfer text
                        setElementText(elm2, getElementText(elm));

                        while (elm.getNextSibling() != null && elm2.getNextSibling() != null
                                && elm.getNextSibling().getNodeName().equals(elm.getNodeName())
                                && !elm2.getNextSibling().getNodeName().equals(elm2.getNodeName())) {
                            elm2 = (Element) elm2.getParentNode().insertBefore(
                                    elm2.getOwnerDocument().createElementNS(elm2.getNamespaceURI(), elm2.getLocalName()),
                                    elm2.getNextSibling());

                            elm = (Element) elm.getNextSibling();

                            // transfer attributes
                            transferAttributes(elm, elm2);

                            // transfer text
                            setElementText(elm2, getElementText(elm));
                        }

                    }

                    cursor.toNextToken();
                }
            }

            return destXml.xmlText();
        } catch (Exception e) {
            SoapUI.logError(e);
        } finally {
            if (cursor != null) {
                cursor.dispose();
            }
        }

        return dest;
    }

    private static void transferAttributes(Element elm, Element elm2) {
        NamedNodeMap attributes = elm.getAttributes();
        for (int c = 0; c < attributes.getLength(); c++) {
            Attr attr = (Attr) attributes.item(c);
            elm2.setAttributeNodeNS((Attr) elm2.getOwnerDocument().importNode(attr, true));
        }
    }

    /**
     * Returns absolute xpath for specified element, ignores namespaces
     *
     * @param element the element to create for
     * @return the elements path in its containing document
     */

    public static String getElementPath(Element element) {
        Node elm = element;

        String result = elm.getNodeName() + "[" + getElementIndex(elm) + "]";
        while (elm.getParentNode() != null && elm.getParentNode().getNodeType() != Node.DOCUMENT_NODE) {
            elm = elm.getParentNode();
            result = elm.getNodeName() + "[" + getElementIndex(elm) + "]/" + result;
        }

        return "/" + result;
    }

    /**
     * Gets the index of the specified element amongst elements with the same
     * name
     *
     * @param element the element to get for
     * @return the index of the element, will be >= 1
     */

    public static int getElementIndex(Node element) {
        int result = 1;

        Node elm = element.getPreviousSibling();
        while (elm != null) {
            if (elm.getNodeType() == Node.ELEMENT_NODE && elm.getNodeName().equals(element.getNodeName())) {
                result++;
            }
            elm = elm.getPreviousSibling();
        }

        return result;
    }

    public static String declareXPathNamespaces(String xmlString) throws XmlException {
        return declareXPathNamespaces(XmlObject.Factory.parse(xmlString));
    }

    public static synchronized String prettyPrintXml(String xml) {
        try {
            if (!XmlUtils.seemsToBeXml(xml)) {
                return xml;
            }

            StringWriter writer = new StringWriter();
            XmlUtils.serializePretty(XmlObject.Factory.parse(xml), writer);
            return writer.toString();
        } catch (Exception e) {
            log.warn("Failed to prettyPrint xml [" + xml + "]: " + e);
            return xml;
        }
    }

    public static synchronized String prettyPrintXml(XmlObject xml) {
        if (xml == null) {
            return null;
        }
        try {

            StringWriter writer = new StringWriter();
            XmlUtils.serializePretty(xml, writer);
            return writer.toString();
        } catch (Exception e) {
            log.warn("Failed to prettyPrint xml [" + xml + "]: " + e);
            return xml.xmlText();
        }
    }

    public static String declareXPathNamespaces(WsdlInterface iface) {
        StringBuilder buf = new StringBuilder();
        buf.append("declare namespace soap='");
        buf.append(iface.getSoapVersion().getEnvelopeNamespace());
        buf.append("';\n");

        try {
            Collection<String> namespaces = iface.getWsdlContext().getInterfaceDefinition().getDefinedNamespaces();
            int c = 1;
            for (String namespace : namespaces) {
                buf.append("declare namespace ns");
                buf.append(c++);
                buf.append("='");
                buf.append(namespace);
                buf.append("';\n");
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return buf.toString();
    }

    public static String createXPath(Node domNode, boolean normalize) {
        return createXPath(domNode, false, false, false, null, normalize);
    }

    public static String createXPath(Node node) {
        return createXPath(node, false);
    }

    public static String createAbsoluteXPath(Node node) {
        return createXPath(node, false, false, true, null);
    }

    public static String createXPath(Node node, boolean anonymous, boolean selectText, XPathModifier modifier) {
        return createXPath(node, anonymous, selectText, false, modifier);
    }

    public static String createXPath(Node node, boolean anonymous, boolean selectText, boolean absolute,
                                     XPathModifier modifier) {
        return createXPath(node, anonymous, selectText, absolute, modifier, false);
    }

    public static String createXPath(Node node, boolean anonymous, boolean selectText, boolean absolute,
                                     XPathModifier modifier, boolean normalize) {
        XPathData xpathData = createXPathData(node, anonymous, selectText, absolute, normalize);
        if (xpathData == null) {
            return null;
        }
        return xpathData.buildXPath(modifier);
    }

    public static XPathData createXPathData(Node node, boolean anonymous, boolean selectText, boolean absolute) {
        return createXPathData(node, anonymous, selectText, absolute, false);
    }

    public static XPathData createXPathData(Node node, boolean anonymous, boolean selectText, boolean absolute,
                                            boolean normalize) {
        StringToStringMap nsMap = new StringToStringMap();
        List<String> pathComponents = new ArrayList<String>();

        int nsCnt = 1;

        String namespaceURI = node.getNamespaceURI();
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            if (namespaceURI != null && namespaceURI.length() > 0) {
                String prefix = node.getPrefix();
                if (normalize || prefix == null || prefix.length() == 0) {
                    prefix = "ns" + nsCnt++;
                }

                nsMap.put(namespaceURI, prefix);
                pathComponents.add("@" + prefix + ":" + node.getLocalName());
            } else {
                pathComponents.add("@" + node.getLocalName());
            }
            node = ((Attr) node).getOwnerElement();
        } else if (node.getNodeType() == Node.DOCUMENT_NODE) {
            node = ((Document) node).getDocumentElement();
        }

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            int index = anonymous ? 0 : findNodeIndex(node);

            String pc;

            namespaceURI = node.getNamespaceURI();
            if (namespaceURI != null && namespaceURI.length() > 0) {
                String prefix = node.getPrefix();
                if (nsMap.containsKey(namespaceURI)) {
                    prefix = nsMap.get(namespaceURI);
                } else {
                    if (normalize || prefix == null || prefix.length() == 0) {
                        prefix = "ns" + nsCnt++;
                    }

                    while (!nsMap.containsKey(namespaceURI) && nsMap.containsValue(prefix)) {
                        prefix = "ns" + nsCnt++;
                    }

                    nsMap.put(namespaceURI, prefix);
                }
                pc = prefix + ":" + node.getLocalName();
            } else {
                pc = node.getLocalName();
            }

            String elementText = XmlUtils.getElementText((Element) node);

            // not an attribute?
            if (selectText && pathComponents.isEmpty() && elementText != null && elementText.trim().length() > 0) {
                pathComponents.add("text()");
            }

            pathComponents.add(pc + ((index == 0) ? "" : "[" + index + "]"));
        } else {
            return null;
        }

        node = node.getParentNode();
        namespaceURI = node.getNamespaceURI();
        while (node != null
                && node.getNodeType() == Node.ELEMENT_NODE
                && (absolute || (!"Body".equals(node.getNodeName())
                && !SoapVersion.Soap11.getEnvelopeNamespace().equals(namespaceURI) && !SoapVersion.Soap12
                .getEnvelopeNamespace().equals(namespaceURI)))) {
            int index = anonymous ? 0 : findNodeIndex(node);

            String ns = nsMap.get(namespaceURI);
            String pc;

            if (ns == null && namespaceURI != null && namespaceURI.length() > 0) {
                String prefix = node.getPrefix();
                if (normalize || prefix == null || prefix.length() == 0) {
                    prefix = "ns" + nsCnt++;
                }

                while (!nsMap.containsKey(namespaceURI) && nsMap.containsValue(prefix)) {
                    prefix = "ns" + nsCnt++;
                }

                nsMap.put(namespaceURI, prefix);
                pc = prefix + ":" + node.getLocalName();
            } else if (ns != null) {
                pc = ns + ":" + node.getLocalName();
            } else {
                pc = node.getLocalName();
            }

            pathComponents.add(pc + ((index == 0) ? "" : "[" + index + "]"));
            node = node.getParentNode();
            namespaceURI = node.getNamespaceURI();
        }

        return new XPathData(nsMap, pathComponents, absolute);
    }

    private static int findNodeIndex(Node node) {
        String nm = node.getLocalName();
        String ns = node.getNamespaceURI();
        short nt = node.getNodeType();

        Node parentNode = node.getParentNode();
        if (parentNode.getNodeType() != Node.ELEMENT_NODE) {
            return 1;
        }

        Node child = parentNode.getFirstChild();

        int ix = 0;
        while (child != null) {
            if (child == node) {
                return ix + 1;
            }

            if (child.getNodeType() == nt
                    && nm.equals(child.getLocalName())
                    && ((ns == null && child.getNamespaceURI() == null) || (ns != null && ns.equals(child
                    .getNamespaceURI())))) {
                ix++;
            }

            child = child.getNextSibling();
        }

        throw new RuntimeException("Child node not found in parent!?");
    }

    public static boolean setNodeValue(Node domNode, String string) {
        if (domNode == null) {
            return false;
        }

        short nodeType = domNode.getNodeType();

        switch (nodeType) {
            case Node.ELEMENT_NODE: {
                setElementText((Element) domNode, string);
                break;
            }
            case Node.ATTRIBUTE_NODE:
            case Node.TEXT_NODE: {
                domNode.setNodeValue(string);
                break;
            }
            case Node.PROCESSING_INSTRUCTION_NODE: {
                ((ProcessingInstruction) domNode).setData(string);
                break;
            }
            case Node.CDATA_SECTION_NODE: {
                ((CDATASection) domNode).setData(string);
                break;
            }
            default: {
                return false;
            }
        }

        return true;
    }

    public static String declareXPathNamespaces(XmlObject xmlObject) {
        Map<QName, String> map = new HashMap<QName, String>();
        XmlCursor cursor = xmlObject.newCursor();

        while (cursor.hasNextToken()) {
            if (cursor.toNextToken().isNamespace()) {
                map.put(cursor.getName(), cursor.getTextValue());
            }
        }

        cursor.dispose();

        int nsCnt = 0;

        StringBuilder buf = new StringBuilder();
        Set<String> prefixes = new HashSet<String>();
        Set<String> usedPrefixes = new HashSet<String>();

        for (Map.Entry<QName, String> entry : map.entrySet()) {
            QName name = entry.getKey();
            String prefix = name.getLocalPart();
            if (prefix.length() == 0) {
                prefix = "ns" + Integer.toString(++nsCnt);
            } else if (prefix.equals("xsd") || prefix.equals("xsi")) {
                continue;
            }

            if (usedPrefixes.contains(prefix)) {
                int c = 1;
                while (usedPrefixes.contains(prefix + c)) {
                    c++;
                }

                prefix = prefix + Integer.toString(c);
            } else {
                prefixes.add(prefix);
            }

            buf.append("declare namespace ");
            buf.append(prefix);
            buf.append("='");
            buf.append(entry.getValue());
            buf.append("';\n");

            usedPrefixes.add(prefix);
        }

        return buf.toString();
    }

    public static String setXPathContent(String xmlText, String xpath, String value) {
        try {
            XmlObject xmlObject = XmlObject.Factory.parse(xmlText);

            String namespaces = declareXPathNamespaces(xmlObject);
            if (namespaces != null && namespaces.trim().length() > 0) {
                xpath = namespaces + xpath;
            }

            XmlObject[] path = xmlObject.selectPath(xpath);
            for (XmlObject xml : path) {
                setNodeValue(xml.getDomNode(), value);
            }

            return xmlObject.toString();
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return xmlText;
    }

    public static QName getQName(Node node) {
        if (node == null) {
            return null;
        } else if (node.getNamespaceURI() == null) {
            return new QName(node.getNodeName());
        } else {
            return new QName(node.getNamespaceURI(), node.getLocalName());
        }
    }

    public static String removeXPathNamespaceDeclarations(String xpath) {
        while (xpath.startsWith("declare namespace")) {
            int ix = xpath.indexOf(';');
            if (ix == -1) {
                break;
            }

            xpath = xpath.substring(ix + 1).trim();
        }
        return xpath;
    }

    public static String stripWhitespaces(String content) {
        try {
            XmlObject xml = XmlObject.Factory.parse(content, new XmlOptions().setLoadStripWhitespace()
                    .setLoadStripComments());
            content = xml.xmlText();
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return content;
    }

    public static NodeList getChildElements(Element elm) {
        List<Element> list = new ArrayList<Element>();

        NodeList nl = elm.getChildNodes();
        for (int c = 0; c < nl.getLength(); c++) {
            Node item = nl.item(c);
            if (item.getParentNode() == elm && item.getNodeType() == Node.ELEMENT_NODE) {
                list.add((Element) item);
            }
        }

        return new ElementNodeList(list);
    }

    public static NodeList getChildElementsByTagName(Element elm, String name) {
        List<Element> list = new ArrayList<Element>();

        NodeList nl = elm.getChildNodes();
        for (int c = 0; c < nl.getLength(); c++) {
            Node item = nl.item(c);
            if (item.getParentNode() == elm && item.getNodeType() == Node.ELEMENT_NODE && name.equals(item.getNodeName())) {
                list.add((Element) item);
            }
        }

        return new ElementNodeList(list);
    }

    public static NodeList getChildElementsOfType(Element elm, SchemaType schemaType) {
        List<Element> list = new ArrayList<Element>();

        NodeList nl = elm.getChildNodes();
        for (int c = 0; c < nl.getLength(); c++) {
            Node item = nl.item(c);
            if (item.getParentNode() == elm
                    && item.getNodeType() == Node.ELEMENT_NODE
                    && ((Element) item).getAttributeNS(Constants.XSI_NS, "type").endsWith(
                    ":" + schemaType.getName().getLocalPart())) {
                list.add((Element) item);
            }
        }

        return new ElementNodeList(list);
    }

    public static NodeList getChildElementsNS(Element elm, QName name) {
        return getChildElementsByTagNameNS(elm, name.getNamespaceURI(), name.getLocalPart());
    }

    public static NodeList getChildElementsByTagNameNS(Element elm, String namespaceUri, String localName) {
        List<Element> list = new ArrayList<Element>();

        NodeList nl = elm.getChildNodes();
        for (int c = 0; c < nl.getLength(); c++) {
            Node item = nl.item(c);
            if (item.getParentNode() == elm && item.getNodeType() == Node.ELEMENT_NODE
                    && localName.equals(item.getLocalName()) && namespaceUri.equals(item.getNamespaceURI())) {
                list.add((Element) item);
            }
        }

        return new ElementNodeList(list);
    }

    public static String serialize(Document document) {
        StringWriter writer = new StringWriter();
        try {
            serialize(document, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static Element getFirstChildElementNS(Element domNode, QName name) {
        return getFirstChildElementNS(domNode, name.getNamespaceURI(), name.getLocalPart());
    }

    public static QName findTypeNameForXsiType(String typeName, Element elm) {
        int ix = typeName.indexOf(':');
        if (ix == -1) {
            return null;
        }

        String prefix = typeName.substring(0, ix);
        String localName = typeName.substring(ix + 1);
        String namespaceUri = elm.getAttribute("xmlns:" + prefix);

        if (!StringUtils.hasContent(namespaceUri)) {
            namespaceUri = findNamespaceForPrefix(elm, prefix);
        }

        if (StringUtils.hasContent(namespaceUri)) {
            return new QName(namespaceUri, localName);
        }

        return null;
    }

    private static String findNamespaceForPrefix(Element elm, String prefix) {
        String namespaceUri = null;
        while (StringUtils.isNullOrEmpty(namespaceUri) && elm != null) {
            if (elm.getParentNode().getNodeType() != Node.ELEMENT_NODE) {
                break;
            }

            elm = (Element) elm.getParentNode();
            namespaceUri = elm.getAttribute("xmlns:" + prefix);
        }

        return StringUtils.isNullOrEmpty(namespaceUri) ? null : namespaceUri;
    }

    public static String findPrefixForNamespace(Element elm, String namespace) {
        while (elm != null) {
            NamedNodeMap attributes = elm.getAttributes();
            for (int c = 0; c < attributes.getLength(); c++) {
                if (attributes.item(c).getNodeValue().equals(namespace)
                        && attributes.item(c).getNodeName().startsWith("xmlns:")) {
                    return attributes.item(c).getNodeName().substring(6);
                }
            }

            if (elm.getParentNode().getNodeType() != Node.ELEMENT_NODE) {
                break;
            }

            elm = (Element) elm.getParentNode();
        }

        return null;
    }

    public static void setXsiType(Element elm, QName name) {
        String prefix = findPrefixForNamespace(elm, name.getNamespaceURI());
        if (prefix == null) {
            prefix = generatePrefixForNamespace(name.getNamespaceURI());
            while (findNamespaceForPrefix(elm, prefix) != null) {
                prefix = generatePrefixForNamespace(name.getNamespaceURI());
            }

            elm.setAttribute("xmlns:" + prefix, name.getNamespaceURI());
        }

        elm.setAttributeNS(Constants.XSI_NS, "type", prefix + ":" + name.getLocalPart());
    }

    private static String generatePrefixForNamespace(String namespaceURI) {
        return "ns" + (int) (Math.random() * 1000);
    }

    public static QName createQName(Node node) {
        return new QName(node.getNamespaceURI(), node.getLocalName());
    }

    public static Node getNextElementSibling(Node node) {
        node = node.getNextSibling();
        while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
            node = node.getNextSibling();
        }

        return node;
    }

    public static Document createDocument(QName element) {
        ensureDocumentBuilder();

        Document document = documentBuilder.newDocument();
        document.appendChild(document.createElementNS(element.getNamespaceURI(), element.getLocalPart()));
        return document;
    }

    public static String getValueForMatch(XmlCursor cursor) {
        Node domNode = cursor.getDomNode();
        String stringValue;

        if (domNode.getNodeType() == Node.ATTRIBUTE_NODE || domNode.getNodeType() == Node.TEXT_NODE) {
            stringValue = domNode.getNodeValue();
        } else if (cursor.getObject() instanceof XmlAnySimpleType) {
            stringValue = ((XmlAnySimpleType) cursor.getObject()).getStringValue();
        } else {
            if (domNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elm = (Element) domNode;
                if (elm.getChildNodes().getLength() == 1 && !hasContentAttributes(elm)) {
                    stringValue = getElementText(elm);
                } else {
                    stringValue = cursor.getObject().xmlText(
                            new XmlOptions().setSavePrettyPrint().setSaveOuter().setSaveAggressiveNamespaces());
                }
            } else {
                stringValue = domNode.getNodeValue();
            }
        }
        return stringValue;
    }

    public static boolean hasContentAttributes(Element elm) {
        NamedNodeMap attributes = elm.getAttributes();
        for (int c = 0; c < attributes.getLength(); c++) {
            Node item = attributes.item(c);
            String ns = item.getNamespaceURI();
            if (!ns.equals(Constants.XML_NS)
                // && !ns.equals( Constants.XSI_NS ) && !ns.equals(
                // Constants.XSI_NS_2000 )
                // && !ns.equals( Constants.XSD_NS )
                    ) {
                return true;
            }
        }

        return false;
    }

    public static String getValueForMatch(Node domNode, boolean prettyPrintXml) {
        String stringValue;

        if (domNode.getNodeType() == Node.ATTRIBUTE_NODE || domNode.getNodeType() == Node.TEXT_NODE) {
            stringValue = domNode.getNodeValue();
        } else {
            if (domNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elm = (Element) domNode;
                if (elm.getChildNodes().getLength() == 1 && !hasContentAttributes(elm)) {
                    stringValue = getElementText(elm);
                } else {
                    stringValue = XmlUtils.serialize(domNode, prettyPrintXml);
                }
            } else {
                stringValue = domNode.getNodeValue();
            }
        }

        return stringValue;
    }

    public static String selectFirstNodeValue(XmlObject xmlObject, String xpath) throws XmlException {
        Node domNode = selectFirstDomNode(xmlObject, xpath);
        return domNode == null ? null : getNodeValue(domNode);
    }

    public static String[] selectNodeValues(XmlObject xmlObject, String xpath) {
        Node[] nodes = selectDomNodes(xmlObject, xpath);

        String[] result = new String[nodes.length];
        for (int c = 0; c < nodes.length; c++) {
            result[c] = getNodeValue(nodes[c]);
        }

        return result;
    }

    public static Node selectFirstDomNode(XmlObject xmlObject, String xpath) {
        XmlCursor cursor = xmlObject.newCursor();
        try {
            cursor.selectPath(xpath);

            if (cursor.toNextSelection()) {
                return cursor.getDomNode();
            } else {
                return null;
            }
        } finally {
            cursor.dispose();
        }
    }

    public static Node[] selectDomNodes(XmlObject xmlObject, String xpath) {
        List<Node> result = new ArrayList<Node>();

        XmlCursor cursor = xmlObject.newCursor();
        try {
            cursor.selectPath(xpath);

            while (cursor.toNextSelection()) {
                result.add(cursor.getDomNode());
            }
        } finally {
            cursor.dispose();
        }

        return result.toArray(new Node[result.size()]);
    }

    private final static class ElementNodeList implements NodeList {
        private final List<Element> list;

        public ElementNodeList(List<Element> list) {
            this.list = list;
        }

        public int getLength() {
            return list.size();
        }

        public Node item(int index) {
            return list.get(index);
        }
    }

    public static boolean seemsToBeXml(String str) {
        try {
            if (StringUtils.isNullOrEmpty(str)) {
                return false;
            }

            XmlObject.Factory.parse(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractNamespaces(String xpath) {
        String result = xpath;
        int ix = xpath.lastIndexOf("declare namespace");
        if (ix != -1) {
            ix = xpath.indexOf('\'', ix + 1);
            if (ix != -1) {
                ix = xpath.indexOf('\'', ix + 1);
                if (ix != -1) {
                    ix = xpath.indexOf(';');
                    if (ix != -1) {
                        result = xpath.substring(0, ix + 1);
                    }
                }
            }
        } else {
            result = "";
        }

        return result;
    }

    public static String removeUnneccessaryNamespaces(String xml) {
        if (StringUtils.isNullOrEmpty(xml)) {
            return xml;
        }

        XmlObject xmlObject;
        XmlCursor cursor = null;
        try {
            xmlObject = XmlObject.Factory.parse(xml);

            cursor = xmlObject.newCursor();
            while (cursor.currentTokenType() != TokenType.START && cursor.currentTokenType() != TokenType.ENDDOC) {
                cursor.toNextToken();
            }

            if (cursor.currentTokenType() == TokenType.START) {
                Map<?, ?> nsMap = new HashMap<Object, Object>();

                cursor.getAllNamespaces(nsMap);
                nsMap.remove(cursor.getDomNode().getPrefix());

                NamedNodeMap attributes = cursor.getDomNode().getAttributes();
                for (int c = 0; attributes != null && c < attributes.getLength(); c++) {
                    nsMap.remove(attributes.item(c).getPrefix());
                }

                if (cursor.toFirstChild()) {
                    while (cursor.getDomNode() != xmlObject.getDomNode()) {
                        attributes = cursor.getDomNode().getAttributes();
                        for (int c = 0; attributes != null && c < attributes.getLength(); c++) {
                            nsMap.remove(attributes.item(c).getPrefix());
                        }

                        nsMap.remove(cursor.getDomNode().getPrefix());
                        cursor.toNextToken();
                    }
                }

                xml = xmlObject.xmlText(new XmlOptions().setSaveOuter().setSavePrettyPrint()
                        .setSaveImplicitNamespaces(nsMap));
            }
        } catch (XmlException e) {

        } finally {
            if (cursor != null) {
                cursor.dispose();
            }
        }

        return xml;
    }

    public static String replaceNameInPathOrQuery(String pathOrQuery, String oldName, String newName) throws Exception {
        Tokenizer t = new Tokenizer();
        t.tokenize(pathOrQuery, 0, -1, 1);
        StringBuffer result = new StringBuffer();
        int lastIx = 0;

        while (t.currentToken != Token.EOF) {
            if (t.currentToken == Token.NAME && t.currentTokenValue.equals(oldName)) {
                result.append(pathOrQuery.substring(lastIx, t.currentTokenStartOffset));
                result.append(newName);
                lastIx = t.currentTokenStartOffset + t.currentTokenValue.length();
            }

            t.next();
        }

        if (lastIx < pathOrQuery.length()) {
            result.append(pathOrQuery.substring(lastIx));
        }
        //
        System.out.println("returning " + result.toString());
        return result.toString();
    }

    public static QName getQName(XmlObject contentElement) {
        return contentElement == null ? null : getQName(contentElement.getDomNode());
    }

    public static String getXPathValue(String value, String xpath) {
        try {
            XmlObject xmlObject = XmlObject.Factory.parse(value);
            XmlObject[] nodes = xmlObject.selectPath(xpath);
            if (nodes.length > 0) {
                return getNodeValue(nodes[0].getDomNode());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String createJdbcXmlResult(Statement statement) throws SQLException, ParserConfigurationException {
        return createJdbcXmlResultEx(statement, true);
    }

    public static String createJdbcXmlResultEx(Statement statement, boolean makeUpperCased) throws SQLException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document xmlDocumentResult = builder.newDocument();
        Element resultsElement = xmlDocumentResult.createElement("Results");
        xmlDocumentResult.appendChild(resultsElement);

        if (statement != null) {
            try (ResultSet resultSet = statement.getResultSet()) {
                if (resultSet != null) {
                    resultSet.setFetchSize(statement.getFetchSize());
                    xmlDocumentResult = addResultSetXmlPart(resultsElement, resultSet, xmlDocumentResult, makeUpperCased);
                    while (statement.getMoreResults()) {
                        try (ResultSet moreResults = statement.getResultSet()) {
                            xmlDocumentResult = addResultSetXmlPart(resultsElement, moreResults, xmlDocumentResult, makeUpperCased);
                        }
                    }
                } else {
                    Element errorElement = xmlDocumentResult.createElement("UpdateCount");
                    errorElement.appendChild(xmlDocumentResult.createTextNode(String.valueOf(statement.getUpdateCount())));
                    resultsElement.appendChild(errorElement);
                }
            }
        }

        StringWriter out = new StringWriter();

        OutputFormat outputFormat = new OutputFormat(xmlDocumentResult);
        outputFormat.setOmitComments(true);
        outputFormat.setOmitDocumentType(true);
        outputFormat.setOmitXMLDeclaration(true);
        // outputFormat.setLineSeparator( "\n" );
        // add this line //
        // outputFormat.setPreserveSpace( true );
        outputFormat.setIndent(3);
        outputFormat.setIndenting(true);

        try {
            XMLSerializer serializer = new XMLSerializer(new PrintWriter(out), outputFormat);
            serializer.asDOMSerializer();
            serializer.serialize(xmlDocumentResult);
        } catch (IOException e) {
            SoapUI.logError(e);
        }

        return out.toString();
    }

    public static Document addResultSetXmlPart(Element resultsElement, ResultSet rs, Document xmlDocumentResult, boolean uppercase)
            throws SQLException {
        final String TABLE_COLUMN_DELIMITER = ".";
        ResultSetMetaData rsmd = rs.getMetaData();
        Element resultSetElement = xmlDocumentResult.createElement("ResultSet");

        resultSetElement.setAttribute("fetchSize", String.valueOf(rs.getFetchSize()));
        resultsElement.appendChild(resultSetElement);

        int colCount = rsmd.getColumnCount();
        while (rs.next()) {
            Element rowElement = xmlDocumentResult.createElement("Row");
            rowElement.setAttribute("rowNumber", String.valueOf(rs.getRow()));

            resultsElement.appendChild(rowElement);
            for (int i = 1; i <= colCount; i++) {
                StringBuffer resultColumnName = new StringBuffer();
                String tableName = rsmd.getTableName(i);
                String columnName = rsmd.getColumnName(i);
                if (uppercase) {
                    tableName = tableName.toUpperCase();
                    columnName = columnName.toUpperCase();
                }
                if (StringUtils.hasContent(tableName)) {
                    resultColumnName.append(tableName);
                    resultColumnName.append(TABLE_COLUMN_DELIMITER);
                }
                resultColumnName.append(columnName);
                String xmlName = StringUtils.createXmlName(resultColumnName.toString());
                Element node = xmlDocumentResult.createElement(xmlName);
                String value = rs.getString(i);
                if (StringUtils.hasContent(value)) {
                    Text textNode = xmlDocumentResult.createTextNode(value);
                    node.appendChild(textNode);
                }
                rowElement.appendChild(node);
            }
            resultSetElement.appendChild(rowElement);
        }
        return xmlDocumentResult;
    }

}
