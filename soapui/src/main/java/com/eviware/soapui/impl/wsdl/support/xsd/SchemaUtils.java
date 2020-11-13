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

package com.eviware.soapui.impl.wsdl.support.xsd;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlBase64Binary;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlHexBinary;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * XML-Schema related tools
 *
 * @author Ole.Matzura
 */

public class SchemaUtils {
    private final static Logger log = LogManager.getLogger(SchemaUtils.class);
    private static Map<String, XmlObject> defaultSchemas = new HashMap<String, XmlObject>();

    static {
        initDefaultSchemas();

        SoapUI.getSettings().addSettingsListener(new SettingsListener() {

            public void settingChanged(String name, String newValue, String oldValue) {
                if (name.equals(WsdlSettings.SCHEMA_DIRECTORY)) {
                    log.info("Reloading default schemas..");
                    initDefaultSchemas();
                }
            }

            @Override
            public void settingsReloaded() {
                // TODO Auto-generated method stub

            }
        });
    }

    public static void initDefaultSchemas() {
        SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();

        try {
            defaultSchemas.clear();

            String root = "/com/eviware/soapui/resources/xsds";

            loadDefaultSchema(SoapUI.class.getResource(root + "/xop.xsd"));
            loadDefaultSchema(SoapUI.class.getResource(root + "/XMLSchema.xsd"));
            loadDefaultSchema(SoapUI.class.getResource(root + "/xml.xsd"));
            loadDefaultSchema(SoapUI.class.getResource(root + "/swaref.xsd"));
            loadDefaultSchema(SoapUI.class.getResource(root + "/xmime200505.xsd"));
            loadDefaultSchema(SoapUI.class.getResource(root + "/xmime200411.xsd"));
            loadDefaultSchema(SoapUI.class.getResource(root + "/soapEnvelope.xsd"));
            loadDefaultSchema(SoapUI.class.getResource(root + "/soapEncoding.xsd"));
            loadDefaultSchema(SoapUI.class.getResource(root + "/soapEnvelope12.xsd"));
            loadDefaultSchema(SoapUI.class.getResource(root + "/soapEncoding12.xsd"));

            String schemaDirectory = SoapUI.getSettings().getString(WsdlSettings.SCHEMA_DIRECTORY, null);
            if (StringUtils.hasContent(schemaDirectory)) {
                loadSchemaDirectory(schemaDirectory);
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        } finally {
            state.restore();
        }
    }

    private static void loadSchemaDirectory(String schemaDirectory) throws IOException, MalformedURLException {
        File dir = new File(schemaDirectory);
        if (dir.exists() && dir.isDirectory()) {
            String[] xsdFiles = dir.list();
            int cnt = 0;

            if (xsdFiles != null && xsdFiles.length > 0) {
                for (int c = 0; c < xsdFiles.length; c++) {
                    try {
                        String xsdFile = xsdFiles[c];
                        if (xsdFile.endsWith(".xsd")) {
                            String filename = schemaDirectory + File.separator + xsdFile;
                            loadDefaultSchema(new URL("file:" + filename));
                            cnt++;
                        }
                    } catch (Throwable e) {
                        SoapUI.logError(e);
                    }
                }
            }

            if (cnt == 0) {
                log.warn("Missing schema files in  schemaDirectory [" + schemaDirectory + "]");
            }
        } else {
            log.warn("Failed to open schemaDirectory [" + schemaDirectory + "]");
        }
    }

    private static void loadDefaultSchema(URL url) throws Exception {
        // XmlObject xmlObject = XmlObject.Factory.parse( url );
        XmlObject xmlObject = XmlUtils.createXmlObject(url);
        if (!((Document) xmlObject.getDomNode()).getDocumentElement().getNamespaceURI().equals(Constants.XSD_NS)) {
            return;
        }

        String targetNamespace = getTargetNamespace(xmlObject);

        if (defaultSchemas.containsKey(targetNamespace)) {
            log.warn("Overriding schema for targetNamespace " + targetNamespace);
        }

        defaultSchemas.put(targetNamespace, xmlObject);

        log.info("Added default schema from " + url.getPath() + " with targetNamespace " + targetNamespace);
    }

    public static SchemaTypeSystem loadSchemaTypes(String wsdlUrl, SchemaLoader loader) throws SchemaException {
        SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();

        try {
            log.info("Loading schema types from [" + wsdlUrl + "]");
            ArrayList<XmlObject> schemas = new ArrayList<XmlObject>(getSchemas(wsdlUrl, loader).values());

            return buildSchemaTypes(schemas);
        } catch (Exception e) {
            SoapUI.logError(e);
            if (e instanceof SchemaException) {
                throw (SchemaException) e;
            } else {
                throw new SchemaException("Error loading schema types", e);
            }
        } finally {
            state.restore();
        }
    }

    public static SchemaTypeSystem buildSchemaTypes(List<XmlObject> schemas) throws SchemaException {
        XmlOptions options = new XmlOptions();
        options.setCompileNoValidation();
        options.setCompileNoPvrRule();
        options.setCompileDownloadUrls();
        options.setCompileNoUpaRule();
        options.setValidateTreatLaxAsSkip();

        for (int c = 0; c < schemas.size(); c++) {
            XmlObject xmlObject = schemas.get(c);
            if (xmlObject == null
                    || !((Document) xmlObject.getDomNode()).getDocumentElement().getNamespaceURI()
                    .equals(Constants.XSD_NS)) {
                schemas.remove(c);
                c--;
            }
        }

        boolean strictSchemaTypes = SoapUI.getSettings().getBoolean(WsdlSettings.STRICT_SCHEMA_TYPES);
        if (!strictSchemaTypes) {
            Set<String> mdefNamespaces = new HashSet<String>();

            for (XmlObject xObj : schemas) {
                mdefNamespaces.add(getTargetNamespace(xObj));
            }

            options.setCompileMdefNamespaces(mdefNamespaces);
        }

        ArrayList<?> errorList = new ArrayList<Object>();
        options.setErrorListener(errorList);

        XmlCursor cursor = null;

        try {
            // remove imports
            for (int c = 0; c < schemas.size(); c++) {
                XmlObject s = schemas.get(c);

                Map<?, ?> map = new HashMap<String, String>();
                cursor = s.newCursor();
                cursor.toStartDoc();
                if (toNextContainer(cursor)) {
                    cursor.getAllNamespaces(map);
                } else {
                    log.warn("Can not get namespaces for " + s);
                }

                String tns = getTargetNamespace(s);

                // log.info( "schema for [" + tns + "] contained [" + map.toString()
                // + "] namespaces" );

                if (strictSchemaTypes && defaultSchemas.containsKey(tns)) {
                    schemas.remove(c);
                    c--;
                } else {
                    removeImports(s);
                }

                cursor.dispose();
                cursor = null;
            }

            // schemas.add( soapVersion.getSoapEncodingSchema());
            // schemas.add( soapVersion.getSoapEnvelopeSchema());
            schemas.addAll(defaultSchemas.values());

            SchemaTypeSystem sts = XmlBeans.compileXsd(schemas.toArray(new XmlObject[schemas.size()]),
                    XmlBeans.getBuiltinTypeSystem(), options);

            return sts;
            // return XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] { sts,
            // XmlBeans.getBuiltinTypeSystem() });
        } catch (Exception e) {
            SoapUI.logError(e);
            throw new SchemaException(e, errorList);
        } finally {
            for (int c = 0; c < errorList.size(); c++) {
                log.warn("Error: " + errorList.get(c));
            }

            if (cursor != null) {
                cursor.dispose();
            }
        }
    }

    public static boolean toNextContainer(XmlCursor cursor) {
        while (!cursor.isContainer() && !cursor.isEnddoc()) {
            cursor.toNextToken();
        }

        return cursor.isContainer();
    }

    public static String getTargetNamespace(XmlObject s) {
        return ((Document) s.getDomNode()).getDocumentElement().getAttribute("targetNamespace");
    }

    public static Map<String, XmlObject> getSchemas(String wsdlUrl, SchemaLoader loader) throws SchemaException {
        Map<String, XmlObject> result = new HashMap<String, XmlObject>();
        getSchemas(wsdlUrl, result, loader, null /* , false */);
        return result;
    }

    /**
     * Returns a map mapping urls to corresponding XmlSchema XmlObjects for the
     * specified wsdlUrl
     */

    public static void getSchemas(String wsdlUrl, Map<String, XmlObject> existing, SchemaLoader loader, String tns)
            throws SchemaException {

        if (existing.containsKey(wsdlUrl)) {
            return;
        }

        // if( add )
        // existing.put( wsdlUrl, null );

        log.info("Getting schema " + wsdlUrl);

        ArrayList<?> errorList = new ArrayList<Object>();

        Map<String, XmlObject> result = new HashMap<String, XmlObject>();

        boolean common = false;

        try {
            XmlOptions options = new XmlOptions();
            options.setCompileNoValidation();
            options.setSaveUseOpenFrag();
            options.setErrorListener(errorList);
            options.setSaveSyntheticDocumentElement(new QName(Constants.XSD_NS, "schema"));

            XmlObject xmlObject = loader.loadXmlObject(wsdlUrl, options);
            if (xmlObject == null) {
                throw new Exception("Failed to load schema from [" + wsdlUrl + "]");
            }

            Document dom = (Document) xmlObject.getDomNode();
            Node domNode = dom.getDocumentElement();

            // is this an xml schema?
            if (domNode.getLocalName().equals("schema") && Constants.XSD_NS.equals(domNode.getNamespaceURI())) {
                // set targetNamespace (this happens if we are following an include
                // statement)
                if (tns != null) {
                    Element elm = ((Element) domNode);
                    if (!elm.hasAttribute("targetNamespace")) {
                        common = true;
                        elm.setAttribute("targetNamespace", tns);
                    }

                    // check for namespace prefix for targetNamespace
                    NamedNodeMap attributes = elm.getAttributes();
                    int c = 0;
                    for (; c < attributes.getLength(); c++) {
                        Node item = attributes.item(c);
                        if (item.getNodeName().equals("xmlns")) {
                            break;
                        }

                        if (item.getNodeValue().equals(tns) && item.getNodeName().startsWith("xmlns")) {
                            break;
                        }
                    }

                    if (c == attributes.getLength()) {
                        elm.setAttribute("xmlns", tns);
                    }
                }

                if (common && !existing.containsKey(wsdlUrl + "@" + tns)) {
                    result.put(wsdlUrl + "@" + tns, xmlObject);
                } else {
                    result.put(wsdlUrl, xmlObject);
                }
            } else {
                existing.put(wsdlUrl, null);

                XmlObject[] paths = xmlObject.selectPath("declare namespace s='" + Constants.XSD_NS + "' .//s:schema");

                for (int i = 0; i < paths.length; i++) {
                    XmlCursor xmlCursor = paths[i].newCursor();
                    String xmlText = xmlCursor.getObject().xmlText(options);

                    XmlObject obj = XmlUtils.createXmlObject(xmlText, new XmlOptions());
                    obj.documentProperties().setSourceName(wsdlUrl);

                    result.put(wsdlUrl + "@" + (i + 1), obj);
                }

                XmlObject[] wsdlImports = xmlObject.selectPath("declare namespace s='" + Constants.WSDL11_NS
                        + "' .//s:import/@location");
                for (int i = 0; i < wsdlImports.length; i++) {
                    String location = ((SimpleValue) wsdlImports[i]).getStringValue();
                    if (location != null) {
                        if (!location.startsWith("file:") && location.indexOf("://") == -1) {
                            location = Tools.joinRelativeUrl(wsdlUrl, location);
                        }

                        getSchemas(location, existing, loader, null);
                    }
                }

                XmlObject[] wadl10Imports = xmlObject.selectPath("declare namespace s='" + Constants.WADL10_NS
                        + "' .//s:grammars/s:include/@href");
                for (int i = 0; i < wadl10Imports.length; i++) {
                    String location = ((SimpleValue) wadl10Imports[i]).getStringValue();
                    if (location != null) {
                        if (!location.startsWith("file:") && location.indexOf("://") == -1) {
                            location = Tools.joinRelativeUrl(wsdlUrl, location);
                        }

                        getSchemas(location, existing, loader, null);
                    }
                }

                XmlObject[] wadlImports = xmlObject.selectPath("declare namespace s='" + Constants.WADL11_NS
                        + "' .//s:grammars/s:include/@href");
                for (int i = 0; i < wadlImports.length; i++) {
                    String location = ((SimpleValue) wadlImports[i]).getStringValue();
                    if (location != null) {
                        if (!location.startsWith("file:") && location.indexOf("://") == -1) {
                            location = Tools.joinRelativeUrl(wsdlUrl, location);
                        }

                        getSchemas(location, existing, loader, null);
                    }
                }

            }

            existing.putAll(result);

            XmlObject[] schemas = result.values().toArray(new XmlObject[result.size()]);

            for (int c = 0; c < schemas.length; c++) {
                xmlObject = schemas[c];

                XmlObject[] schemaImports = xmlObject.selectPath("declare namespace s='" + Constants.XSD_NS
                        + "' .//s:import/@schemaLocation");
                for (int i = 0; i < schemaImports.length; i++) {
                    String location = ((SimpleValue) schemaImports[i]).getStringValue();
                    Element elm = ((Attr) schemaImports[i].getDomNode()).getOwnerElement();

                    if (location != null && !defaultSchemas.containsKey(elm.getAttribute("namespace"))) {
                        if (!location.startsWith("file:") && location.indexOf("://") == -1) {
                            location = Tools.joinRelativeUrl(wsdlUrl, location);
                        }

                        getSchemas(location, existing, loader, null);
                    }
                }

                XmlObject[] schemaIncludes = xmlObject.selectPath("declare namespace s='" + Constants.XSD_NS
                        + "' .//s:include/@schemaLocation");
                for (int i = 0; i < schemaIncludes.length; i++) {
                    String location = ((SimpleValue) schemaIncludes[i]).getStringValue();
                    if (location != null) {
                        String targetNS = getTargetNamespace(xmlObject);

                        if (!location.startsWith("file:") && location.indexOf("://") == -1) {
                            location = Tools.joinRelativeUrl(wsdlUrl, location);
                        }

                        getSchemas(location, existing, loader, targetNS);
                    }
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
            throw new SchemaException(e, errorList);
        }
    }

    /**
     * Returns a map mapping urls to corresponding XmlObjects for the specified
     * wsdlUrl
     */

    public static Map<String, XmlObject> getDefinitionParts(SchemaLoader loader) throws Exception {
        Map<String, XmlObject> result = new LinkedHashMap<String, XmlObject>();
        getDefinitionParts(loader.getBaseURI(), result, loader);
        return result;
    }

    public static void getDefinitionParts(String origWsdlUrl, Map<String, XmlObject> existing, SchemaLoader loader)
            throws Exception {
        String wsdlUrl = origWsdlUrl;
        if (existing.containsKey(wsdlUrl)) {
            return;
        }

        XmlObject xmlObject = loader.loadXmlObject(wsdlUrl, null);
        existing.put(wsdlUrl, xmlObject);
        // wsdlUrl = loader.getBaseURI();

        selectDefinitionParts(wsdlUrl, existing, loader, xmlObject, "declare namespace s='" + Constants.WSDL11_NS
                + "' .//s:import/@location");
        selectDefinitionParts(wsdlUrl, existing, loader, xmlObject, "declare namespace s='" + Constants.WADL10_NS
                + "' .//s:grammars/s:include/@href");
        selectDefinitionParts(wsdlUrl, existing, loader, xmlObject, "declare namespace s='" + Constants.WADL11_NS
                + "' .//s:grammars/s:include/@href");
        selectDefinitionParts(wsdlUrl, existing, loader, xmlObject, "declare namespace s='" + Constants.XSD_NS
                + "' .//s:import/@schemaLocation");
        selectDefinitionParts(wsdlUrl, existing, loader, xmlObject, "declare namespace s='" + Constants.XSD_NS
                + "' .//s:include/@schemaLocation");
    }

    private static void selectDefinitionParts(String wsdlUrl, Map<String, XmlObject> existing, SchemaLoader loader,
                                              XmlObject xmlObject, String path) throws Exception {
        XmlObject[] wsdlImports = xmlObject.selectPath(path);
        for (int i = 0; i < wsdlImports.length; i++) {
            String location = ((SimpleValue) wsdlImports[i]).getStringValue();
            if (location != null) {
                if (StringUtils.hasContent(location)) {
                    if (!location.startsWith("file:") && location.indexOf("://") == -1) {
                        location = Tools.joinRelativeUrl(wsdlUrl, location);
                    }

                    getDefinitionParts(location, existing, loader);
                } else {
                    Node domNode = ((Attr) wsdlImports[i].getDomNode()).getOwnerElement();
                    domNode.getParentNode().removeChild(domNode);
                }
            }
        }
    }

    /**
     * Extracts namespaces - used in tool integrations for mapping..
     */

    public static Collection<String> extractNamespaces(SchemaTypeSystem schemaTypes, boolean removeDefault) {
        Set<String> namespaces = new HashSet<String>();
        SchemaType[] globalTypes = schemaTypes.globalTypes();
        for (int c = 0; c < globalTypes.length; c++) {
            namespaces.add(globalTypes[c].getName().getNamespaceURI());
        }

        if (removeDefault) {
            namespaces.removeAll(defaultSchemas.keySet());
            namespaces.remove(Constants.SOAP11_ENVELOPE_NS);
            namespaces.remove(Constants.SOAP_ENCODING_NS);
        }

        return namespaces;
    }

    /**
     * Used when creating a TypeSystem from a complete collection of
     * SchemaDocuments so that referenced types are not downloaded (again)
     */

    public static void removeImports(XmlObject xmlObject) throws XmlException {
        XmlObject[] imports = xmlObject.selectPath("declare namespace s='" + Constants.XSD_NS + "' .//s:import");

        for (int c = 0; c < imports.length; c++) {
            XmlCursor cursor = imports[c].newCursor();
            cursor.removeXml();
            cursor.dispose();
        }

        XmlObject[] includes = xmlObject.selectPath("declare namespace s='" + Constants.XSD_NS + "' .//s:include");

        for (int c = 0; c < includes.length; c++) {
            XmlCursor cursor = includes[c].newCursor();
            cursor.removeXml();
            cursor.dispose();
        }
    }

    public static boolean isInstanceOf(SchemaType schemaType, SchemaType baseType) {
        if (schemaType == null) {
            return false;
        }
        return schemaType.equals(baseType) ? true : isInstanceOf(schemaType.getBaseType(), baseType);
    }

    public static boolean isBinaryType(SchemaType schemaType) {
        return isInstanceOf(schemaType, XmlHexBinary.type) || isInstanceOf(schemaType, XmlBase64Binary.type);
    }

    public static String getDocumentation(SchemaType schemaType) {
        String result = null;
        String xsPrefix = null;

        SchemaField containerField = schemaType.getContainerField();

        if (containerField instanceof SchemaLocalElement) {
            SchemaAnnotation annotation = ((SchemaLocalElement) containerField).getAnnotation();
            if (annotation != null) {
                XmlObject[] userInformation = annotation.getUserInformation();
                if (userInformation != null && userInformation.length > 0) {
                    XmlObject xmlObject = userInformation[0];
                    XmlCursor cursor = xmlObject.newCursor();
                    xsPrefix = cursor.prefixForNamespace("http://www.w3.org/2001/XMLSchema");
                    cursor.dispose();

                    result = xmlObject.xmlText(); // XmlUtils.getElementText( (
                    // Element )
                    // userInformation[0].getDomNode());
                }
            }
        }

        if (result == null && schemaType != null && schemaType.getAnnotation() != null) {
            XmlObject[] userInformation = schemaType.getAnnotation().getUserInformation();
            if (userInformation != null && userInformation.length > 0 && userInformation[0] != null) {
                XmlObject xmlObject = userInformation[0];
                XmlCursor cursor = xmlObject.newCursor();
                xsPrefix = cursor.prefixForNamespace("http://www.w3.org/2001/XMLSchema");
                cursor.dispose();
                result = xmlObject.xmlText(); // = XmlUtils.getElementText( (
                // Element )
                // userInformation[0].getDomNode());
            }
        }

        if (result != null) {
            result = result.trim();
            if (result.startsWith("<") && result.endsWith(">")) {
                int ix = result.indexOf('>');
                if (ix > 0) {
                    result = result.substring(ix + 1);
                }

                ix = result.lastIndexOf('<');
                if (ix >= 0) {
                    result = result.substring(0, ix);
                }
            }

            if (xsPrefix == null || xsPrefix.length() == 0) {
                xsPrefix = "xs:";
            } else {
                xsPrefix += ":";
            }

            // result = result.trim().replaceAll( "<" + xsPrefix + "br/>", "\n"
            // ).trim();
            result = result.trim().replaceAll(xsPrefix, "").trim();

            result = StringUtils.toHtml(result);
        }

        return result;
    }

    public static String[] getEnumerationValues(SchemaType schemaType, boolean addNull) {
        if (schemaType != null) {
            XmlAnySimpleType[] enumerationValues = schemaType.getEnumerationValues();
            if (enumerationValues != null && enumerationValues.length > 0) {
                if (addNull) {
                    String[] values = new String[enumerationValues.length + 1];
                    values[0] = null;

                    for (int c = 1; c < values.length; c++) {
                        values[c] = enumerationValues[c - 1].getStringValue();
                    }

                    return values;
                } else {
                    String[] values = new String[enumerationValues.length];

                    for (int c = 0; c < values.length; c++) {
                        values[c] = enumerationValues[c].getStringValue();
                    }

                    return values;
                }
            }
        }

        return new String[0];
    }

    public static Collection<? extends QName> getExcludedTypes() {
        String excluded = SoapUI.getSettings().getString(WsdlSettings.EXCLUDED_TYPES, null);
        return SettingUtils.string2QNames(excluded);
    }

    public static boolean isAnyType(SchemaType schemaType) {
        return schemaType != null
                && (schemaType.getBuiltinTypeCode() == SchemaType.BTC_ANY_TYPE || (schemaType.getBaseType() != null && schemaType
                .getBaseType().getBuiltinTypeCode() == SchemaType.BTC_ANY_TYPE));
    }
}
