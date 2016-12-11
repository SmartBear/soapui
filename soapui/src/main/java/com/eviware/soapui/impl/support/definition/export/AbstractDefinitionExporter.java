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

package com.eviware.soapui.impl.support.definition.export;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.support.definition.InterfaceDefinition;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDefinitionExporter<T extends Interface> implements DefinitionExporter {
    private InterfaceDefinition<T> definition;

    public AbstractDefinitionExporter(InterfaceDefinition<T> definition) {
        this.definition = definition;
    }

    public InterfaceDefinition<T> getDefinition() {
        return definition;
    }

    public void setDefinition(InterfaceDefinition<T> definition) {
        this.definition = definition;
    }

    public String export(String folderName) throws Exception {
        if (definition.getDefinitionCache() == null || !definition.getDefinitionCache().validate()) {
            throw new Exception("Definition is not cached for export");
        }

        File outFolder = new File(folderName);
        if (!outFolder.exists() && !outFolder.mkdirs()) {
            throw new Exception("Failed to create directory [" + folderName + "]");
        }

        Map<String, String> urlToFileMap = new HashMap<String, String>();

        setFilenameForPart(definition.getDefinitionCache().getRootPart(), urlToFileMap, null);

        List<InterfaceDefinitionPart> partList = definition.getDefinitionCache().getDefinitionParts();
        for (InterfaceDefinitionPart part : partList) {
            setFilenameForPart(part, urlToFileMap, null);
        }

        for (InterfaceDefinitionPart part : partList) {
            // XmlObject obj = XmlObject.Factory.parse( part.getContent() );
            XmlObject obj = XmlUtils.createXmlObject(part.getContent());
            replaceImportsAndIncludes(obj, urlToFileMap, part.getUrl());
            postProcessing(obj, part);
            obj.save(new File(outFolder, urlToFileMap.get(part.getUrl())));
        }

        return folderName + File.separatorChar
                + urlToFileMap.get(definition.getDefinitionCache().getRootPart().getUrl());
    }

    public StringToStringMap createFilesForExport(String urlPrefix) throws Exception {
        StringToStringMap result = new StringToStringMap();
        Map<String, String> urlToFileMap = new HashMap<String, String>();

        if (urlPrefix == null) {
            urlPrefix = "";
        }

        setFilenameForPart(definition.getDefinitionCache().getRootPart(), urlToFileMap, urlPrefix);

        List<InterfaceDefinitionPart> partList = definition.getDefinitionCache().getDefinitionParts();
        for (InterfaceDefinitionPart part : partList) {
            if (!part.isRootPart()) {
                setFilenameForPart(part, urlToFileMap, urlPrefix);
            }
        }

        for (InterfaceDefinitionPart part : partList) {
            // XmlObject obj = XmlObject.Factory.parse( part.getContent() );
            XmlObject obj = XmlUtils.createXmlObject(part.getContent());
            replaceImportsAndIncludes(obj, urlToFileMap, part.getUrl());
            String urlString = urlToFileMap.get(part.getUrl());
            if (urlString.startsWith(urlPrefix)) {
                urlString = urlString.substring(urlPrefix.length());
            }

            result.put(urlString, obj.xmlText());

            if (part.isRootPart()) {
                result.put("#root#", urlString);
            }
        }

        return result;
    }

    protected void postProcessing(XmlObject obj, InterfaceDefinitionPart part) {
    }

    private void setFilenameForPart(InterfaceDefinitionPart part, Map<String, String> urlToFileMap, String urlPrefix)
            throws MalformedURLException {

        String path = part.getUrl();

        try {
            URL url = new URL(path);
            path = url.getPath();
        } catch (MalformedURLException ignored) {
        }

        int ix = path.lastIndexOf('/');
        if (ix == -1) {
            ix = path.lastIndexOf('\\');
        }
        String fileName = ix == -1 ? path : path.substring(ix + 1);

        ix = fileName.lastIndexOf('.');
        if (ix != -1) {
            fileName = fileName.substring(0, ix);
        }

        String type = part.getType();

        if (type.equals(Constants.WSDL11_NS)) {
            fileName += ".wsdl";
        } else if (part.getType().equals(Constants.XSD_NS)) {
            fileName += ".xsd";
        } else if (getDefinition().getInterface() instanceof RestService
                && part.getType().equals(((RestService) getDefinition().getInterface()).getWadlVersion())) {
            fileName += ".wadl";
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

        urlToFileMap.put(part.getUrl(), fileName);
    }

    private void replaceImportsAndIncludes(XmlObject xmlObject, Map<String, String> urlToFileMap, String baseUrl)
            throws Exception {
        String[] paths = getLocationXPathsToReplace();

        for (String path : paths) {
            XmlObject[] locations = xmlObject.selectPath(path);

            for (XmlObject location : locations) {
                SimpleValue wsdlImport = ((SimpleValue) location);
                replaceLocation(urlToFileMap, baseUrl, wsdlImport);
            }
        }
    }

    protected abstract String[] getLocationXPathsToReplace();

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
                if (newLocation == null) {
                    newLocation = urlToFileMap.get(loc.replaceAll("/", "\\\\"));
                }
                if (newLocation == null) {
                    newLocation = urlToFileMap.get(loc.replaceAll("\\\\", "/"));
                }
                if (newLocation != null) {
                    wsdlImport.setStringValue(newLocation);
                } else {
                    throw new Exception("Missing local file for [" + loc + "]");
                }
            }
        }
    }

}
