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

package com.eviware.soapui.impl.support.definition.support;

import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.impl.wsdl.support.wsdl.AbstractWsdlDefinitionLoader;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * WsdlLoader for cached definitions
 *
 * @author ole.matzura
 */

public class InterfaceCacheDefinitionLoader extends AbstractWsdlDefinitionLoader {
    private String rootInConfig = "";
    private DefinitionCache config;

    public InterfaceCacheDefinitionLoader(DefinitionCache config) {
        super(config.getRootPart().getUrl());
        this.config = config;
    }

    public InputStream load(String url) throws Exception {
        XmlObject xmlObject = loadXmlObject(url, null);
        return xmlObject == null ? null : xmlObject.newInputStream();
    }

    public XmlObject loadXmlObject(String url, XmlOptions options) throws Exception {
        // required for backwards compatibility when the entire path was stored
        if (url.endsWith(config.getRootPart().getUrl())) {
            rootInConfig = url.substring(0, url.length() - config.getRootPart().getUrl().length());
        }

        List<InterfaceDefinitionPart> partList = config.getDefinitionParts();
        for (InterfaceDefinitionPart part : partList) {
            if ((rootInConfig + part.getUrl()).equalsIgnoreCase(url)) {
                return getPartContent(part);
            }
        }

        // hack: this could be due to windows -> unix, try again with replaced '/'
        if (File.separatorChar == '/') {
            url = url.replace('/', '\\');

            for (InterfaceDefinitionPart part : partList) {
                if ((rootInConfig + part.getUrl()).equalsIgnoreCase(url)) {
                    return getPartContent(part);
                }
            }
        }
        // or the other way around..
        else if (File.separatorChar == '\\') {
            url = url.replace('\\', '/');

            for (InterfaceDefinitionPart part : partList) {
                if ((rootInConfig + part.getUrl()).equalsIgnoreCase(url)) {
                    return getPartContent(part);
                }
            }
        }

        log.error("Failed to find [" + url + "] in InterfaceCache");

        return null;
    }

    public static XmlObject getPartContent(InterfaceDefinitionPart part) throws XmlException {
        // return XmlObject.Factory.parse( part.getContent(), new
        // XmlOptions().setLoadLineNumbers() );
        return XmlUtils.createXmlObject(part.getContent(), new XmlOptions().setLoadLineNumbers());
    }

    public void close() {
    }

    public void setNewBaseURI(String uri) {
        // not implemented
    }

    public String getFirstNewURI() {
        return getBaseURI();
    }
}
