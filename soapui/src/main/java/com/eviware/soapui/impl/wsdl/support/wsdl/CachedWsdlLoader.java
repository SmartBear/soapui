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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import com.eviware.soapui.config.DefinitionCacheConfig;
import com.eviware.soapui.config.DefinitionCacheTypeConfig;
import com.eviware.soapui.config.DefintionPartConfig;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Node;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WsdlLoader for cached definitions
 *
 * @author ole.matzura
 */

public class CachedWsdlLoader extends WsdlLoader {
    private final DefinitionCacheConfig config;
    private String rootInConfig = "";

    public CachedWsdlLoader(DefinitionCacheConfig config) {
        super(config.getRootPart());
        this.config = config;
    }

    public CachedWsdlLoader(AbstractInterface<?> iface) throws Exception {
        this(WsdlUtils.cacheWsdl(new UrlWsdlLoader(PathUtils.expandPath(iface.getDefinition(), iface), iface)));
    }

    public InputStream load(String url) throws Exception {
        XmlObject xmlObject = loadXmlObject(url, null);
        return xmlObject == null ? null : xmlObject.newInputStream();
    }

    public XmlObject loadXmlObject(String url, XmlOptions options) throws Exception {
        // required for backwards compatibility when the entire path was stored
        if (url.endsWith(config.getRootPart())) {
            rootInConfig = url.substring(0, url.length() - config.getRootPart().length());
        }

        List<DefintionPartConfig> partList = config.getPartList();
        for (DefintionPartConfig part : partList) {
            if ((rootInConfig + part.getUrl()).equalsIgnoreCase(url)) {
                return getPartContent(config, part);
            }
        }

        // hack: this could be due to windows -> unix, try again with replaced '/'
        if (File.separatorChar == '/') {
            url = url.replace('/', '\\');

            for (DefintionPartConfig part : partList) {
                if ((rootInConfig + part.getUrl()).equalsIgnoreCase(url)) {
                    return getPartContent(config, part);
                }
            }
        }
        // or the other way around..
        else if (File.separatorChar == '\\') {
            url = url.replace('\\', '/');

            for (DefintionPartConfig part : partList) {
                if ((rootInConfig + part.getUrl()).equalsIgnoreCase(url)) {
                    return getPartContent(config, part);
                }
            }
        }

        return null;
    }

    public static XmlObject getPartContent(DefinitionCacheConfig config, DefintionPartConfig part) throws XmlException {
        if (config.getType() == DefinitionCacheTypeConfig.TEXT) {
            Node domNode = part.getContent().getDomNode();
            String nodeValue = XmlUtils.getNodeValue(domNode);
            // return XmlObject.Factory.parse( nodeValue, new
            // XmlOptions().setLoadLineNumbers() );
            return XmlUtils.createXmlObject(nodeValue, new XmlOptions().setLoadLineNumbers());
        }

        // return XmlObject.Factory.parse( part.getContent().toString(), new
        // XmlOptions().setLoadLineNumbers() );
        return XmlUtils.createXmlObject(part.getContent().toString(), new XmlOptions().setLoadLineNumbers());
    }

    /**
     * Saves the complete definition to the specified folder, returns path to
     * root part
     *
     * @param folderName
     * @return
     * @throws Exception
     */

    public String saveDefinition(String folderName) throws Exception {
        File outFolder = new File(folderName);
        if (!outFolder.exists() && !outFolder.mkdirs()) {
            throw new Exception("Failed to create directory [" + folderName + "]");
        }

        Map<String, String> urlToFileMap = new HashMap<String, String>();

        setFilenameForUrl(config.getRootPart(), Constants.WSDL11_NS, urlToFileMap, null);

        List<DefintionPartConfig> partList = config.getPartList();
        for (DefintionPartConfig part : partList) {
            setFilenameForUrl(part.getUrl(), part.getType(), urlToFileMap, null);
        }

        for (DefintionPartConfig part : partList) {
            XmlObject obj = null;
            if (config.getType() == DefinitionCacheTypeConfig.TEXT) {
                // obj = XmlObject.Factory.parse( XmlUtils.getNodeValue(
                // part.getContent().getDomNode() ) );
                obj = XmlUtils.createXmlObject(XmlUtils.getNodeValue(part.getContent().getDomNode()));
            } else {
                // obj = XmlObject.Factory.parse( part.getContent().toString() );
                obj = XmlUtils.createXmlObject(part.getContent().toString());
            }

            replaceImportsAndIncludes(obj, urlToFileMap, part.getUrl());
            obj.save(new File(outFolder, urlToFileMap.get(part.getUrl())));
        }

        return folderName + File.separatorChar + urlToFileMap.get(config.getRootPart());
    }

    public StringToStringMap createFilesForExport(String urlPrefix) throws Exception {
        StringToStringMap result = new StringToStringMap();
        Map<String, String> urlToFileMap = new HashMap<String, String>();

        if (urlPrefix == null) {
            urlPrefix = "";
        }

        setFilenameForUrl(config.getRootPart(), Constants.WSDL11_NS, urlToFileMap, urlPrefix);

        List<DefintionPartConfig> partList = config.getPartList();
        for (DefintionPartConfig part : partList) {
            if (!part.getUrl().equals(config.getRootPart())) {
                setFilenameForUrl(part.getUrl(), part.getType(), urlToFileMap, urlPrefix);
            }
        }

        for (DefintionPartConfig part : partList) {
            XmlObject obj = CachedWsdlLoader.getPartContent(config, part);
            replaceImportsAndIncludes(obj, urlToFileMap, part.getUrl());
            String urlString = urlToFileMap.get(part.getUrl());
            if (urlString.startsWith(urlPrefix)) {
                urlString = urlString.substring(urlPrefix.length());
            }

            result.put(urlString, obj.xmlText());

            if (part.getUrl().equals(config.getRootPart())) {
                result.put("#root#", obj.xmlText());
            }
        }

        return result;
    }

    private void setFilenameForUrl(String fileUrl, String type, Map<String, String> urlToFileMap, String urlPrefix)
            throws MalformedURLException {

        String path = fileUrl;

        try {
            URL url = new URL(fileUrl);
            path = url.getPath();
        } catch (MalformedURLException e) {
        }

        int ix = path.lastIndexOf('/');
        String fileName = ix == -1 ? path : path.substring(ix + 1);

        ix = fileName.lastIndexOf('.');
        if (ix != -1) {
            fileName = fileName.substring(0, ix);
        }

        if (type.equals(Constants.WSDL11_NS)) {
            fileName += ".wsdl";
        } else if (type.equals(Constants.XSD_NS)) {
            fileName += ".xsd";
        } else {
            fileName += ".xml";
        }

        if (urlPrefix != null) {
            fileName = urlPrefix + fileName;
        }

        int cnt = 1;
        while (urlToFileMap.containsValue(fileName)) {
            ix = fileName.lastIndexOf('.');
            fileName = fileName.substring(0, ix) + "_" + cnt + fileName.substring(ix);
            cnt++;
        }

        urlToFileMap.put(fileUrl, fileName);
    }

    private void replaceImportsAndIncludes(XmlObject xmlObject, Map<String, String> urlToFileMap, String baseUrl)
            throws Exception {
        XmlObject[] wsdlImports = xmlObject
                .selectPath("declare namespace s='http://schemas.xmlsoap.org/wsdl/' .//s:import/@location");

        for (int i = 0; i < wsdlImports.length; i++) {
            SimpleValue wsdlImport = ((SimpleValue) wsdlImports[i]);
            replaceLocation(urlToFileMap, baseUrl, wsdlImport);
        }

        XmlObject[] schemaImports = xmlObject
                .selectPath("declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:import/@schemaLocation");

        for (int i = 0; i < schemaImports.length; i++) {
            SimpleValue schemaImport = ((SimpleValue) schemaImports[i]);
            replaceLocation(urlToFileMap, baseUrl, schemaImport);
        }

        XmlObject[] schemaIncludes = xmlObject
                .selectPath("declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:include/@schemaLocation");
        for (int i = 0; i < schemaIncludes.length; i++) {
            SimpleValue schemaInclude = ((SimpleValue) schemaIncludes[i]);
            replaceLocation(urlToFileMap, baseUrl, schemaInclude);
        }

        XmlObject[] wadlImports = xmlObject.selectPath("declare namespace s='" + Constants.WADL10_NS
                + "' .//s:grammars/s:include/@href");

        for (int i = 0; i < wadlImports.length; i++) {
            SimpleValue wadlImport = ((SimpleValue) wadlImports[i]);
            replaceLocation(urlToFileMap, baseUrl, wadlImport);
        }

        wadlImports = xmlObject.selectPath("declare namespace s='" + Constants.WADL11_NS
                + "' .//s:grammars/s:include/@href");

        for (int i = 0; i < wadlImports.length; i++) {
            SimpleValue wadlImport = ((SimpleValue) wadlImports[i]);
            replaceLocation(urlToFileMap, baseUrl, wadlImport);
        }
    }

    private void replaceLocation(Map<String, String> urlToFileMap, String baseUrl, SimpleValue wsdlImport)
            throws Exception {
        String location = wsdlImport.getStringValue();
        if (location != null) {
            if (location.startsWith("file:") || location.indexOf("://") > 0) {
                String newLocation = urlToFileMap.get(location);
                if (newLocation != null) {
                    wsdlImport.setStringValue(newLocation);
                } else {
                    throw new Exception("Missing local file for [" + newLocation + "]");
                }
            } else {
                String loc = Tools.joinRelativeUrl(baseUrl, location);
                String newLocation = urlToFileMap.get(loc);
                if (newLocation != null) {
                    wsdlImport.setStringValue(newLocation);
                } else {
                    throw new Exception("Missing local file for [" + loc + "]");
                }
            }
        }
    }

    public void close() {
    }
}
