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

import com.eviware.soapui.config.DefinitionCacheConfig;
import com.eviware.soapui.config.DefinitionCacheTypeConfig;
import com.eviware.soapui.config.DefintionPartConfig;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.DefinitionLoader;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractDefinitionCache<T extends AbstractInterface<?>> implements DefinitionCache {
    protected DefinitionCacheConfig definitionCache;
    private T container;
    private InterfaceDefinitionPart rootPart;
    private List<InterfaceDefinitionPart> parts;

    public AbstractDefinitionCache(DefinitionCacheConfig definitionCache, T container) {
        this.definitionCache = definitionCache;
        this.container = container;

        if (this.definitionCache == null) {
            this.definitionCache = reinit(container);
        }
    }

    protected abstract DefinitionCacheConfig reinit(T owner);

    public T getContainer() {
        return container;
    }

    public boolean validate() {
        if (definitionCache.getRootPart() == null) {
            return false;
        }

        if (definitionCache.sizeOfPartArray() == 0) {
            return false;
        }

        return true;
    }

    public void importCache(DefinitionCache cache) throws Exception {
        if (cache instanceof AbstractDefinitionCache<?>) {
            definitionCache = reinit(container);
            definitionCache.set(((AbstractDefinitionCache<?>) cache).getConfig());
            initParts();
        } else {
            update(new InterfaceCacheDefinitionLoader(cache));
        }
    }

    protected DefinitionCacheConfig getConfig() {
        return definitionCache;
    }

    public void update(DefinitionLoader loader) throws Exception {
        definitionCache = reinit(container);

        String baseUri = loader.getBaseURI();
        definitionCache.setType(DefinitionCacheTypeConfig.TEXT);
        Map<String, XmlObject> urls = SchemaUtils.getDefinitionParts(loader);
        definitionCache.setRootPart(baseUri);

        for (Map.Entry<String, XmlObject> entry : urls.entrySet()) {
            DefintionPartConfig definitionPart = definitionCache.addNewPart();
            String url = entry.getKey();
            definitionPart.setUrl(url);
            XmlObject xmlObject = entry.getValue();
            Node domNode = xmlObject.getDomNode();

            if (domNode.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
                Node node = ((DocumentFragment) domNode).getFirstChild();
                if (node.getNodeType() == Node.TEXT_NODE) {
                    domNode = XmlUtils.parseXml(node.getNodeValue());
                    // xmlObject = XmlObject.Factory.parse( domNode );
                    xmlObject = XmlUtils.createXmlObject(domNode);
                }
            }

            Element contentElement = ((Document) domNode).getDocumentElement();

            Node newDomNode = definitionPart.addNewContent().getDomNode();
            newDomNode.appendChild(newDomNode.getOwnerDocument().createTextNode(xmlObject.toString()));
            definitionPart.setType(contentElement.getNamespaceURI());
        }

        initParts();
    }

    public List<InterfaceDefinitionPart> getDefinitionParts() throws Exception {
        if (parts == null) {
            initParts();
        }

        return parts;
    }

    private void initParts() {
        parts = new ArrayList<InterfaceDefinitionPart>();

        List<DefintionPartConfig> partList = definitionCache.getPartList();
        for (DefintionPartConfig part : partList) {
            try {
                boolean rootElement = URLDecoder.decode(part.getUrl(), "UTF-8").equals(
                        URLDecoder.decode(definitionCache.getRootPart(), "UTF-8"));
                ConfigInterfaceDefinitionPart configInterfaceDefinitionPart = new ConfigInterfaceDefinitionPart(part,
                        rootElement, definitionCache.getType());
                parts.add(configInterfaceDefinitionPart);

                if (configInterfaceDefinitionPart.isRootPart()) {
                    rootPart = configInterfaceDefinitionPart;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            // ConfigInterfaceDefinitionPart configInterfaceDefinitionPart = new
            // ConfigInterfaceDefinitionPart( part, part
            // .getUrl().equals( definitionCache.getRootPart() ),
            // definitionCache.getType() );

        }
    }

    public InterfaceDefinitionPart getRootPart() {
        if (parts == null) {
            initParts();
        }

        return rootPart;
    }

    public void clear() {
        definitionCache.setRootPart(null);

        while (definitionCache.sizeOfPartArray() > 0) {
            definitionCache.removePart(0);
        }
    }
}
