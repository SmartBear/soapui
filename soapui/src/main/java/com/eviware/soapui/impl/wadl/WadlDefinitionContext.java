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

package com.eviware.soapui.impl.wadl;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.panels.request.inspectors.schema.InferredSchemaManager;
import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.DefinitionLoader;
import com.eviware.soapui.impl.support.definition.export.WadlDefinitionExporter;
import com.eviware.soapui.impl.support.definition.support.AbstractDefinitionContext;
import com.eviware.soapui.impl.support.definition.support.InterfaceCacheDefinitionLoader;
import com.eviware.soapui.impl.wadl.support.GeneratedWadlDefinitionLoader;
import com.eviware.soapui.impl.wadl.support.WadlInterfaceDefinition;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.support.StringUtils;
import org.apache.xmlbeans.SchemaTypeSystem;

public class WadlDefinitionContext extends
        AbstractDefinitionContext<RestService, DefinitionLoader, WadlInterfaceDefinition> {

    public WadlDefinitionContext(String url, RestService iface) {
        super(url, iface);
    }

    public WadlDefinitionContext(String wadlUrl) {
        super(wadlUrl);
    }

    protected DefinitionLoader createDefinitionLoader(DefinitionCache restServiceDefinitionCache) {
        if (getInterface() != null
                && (getInterface().isGenerated() || StringUtils.isNullOrEmpty(getInterface().getWadlUrl())
                || getInterface().exportChanges())) {
            return new GeneratedWadlDefinitionLoader(getInterface());
        } else {
            return new InterfaceCacheDefinitionLoader(restServiceDefinitionCache);
        }
    }

    protected DefinitionLoader createDefinitionLoader(String url) {
        if ((getInterface() != null && getInterface().isGenerated()) || StringUtils.isNullOrEmpty(url)
                || (getInterface() != null && getInterface().exportChanges())) {
            return new GeneratedWadlDefinitionLoader(getInterface());
        } else {
            return new UrlWsdlLoader(url, getInterface());
        }
    }

    protected WadlInterfaceDefinition loadDefinition(DefinitionLoader loader) throws Exception {
        return new WadlInterfaceDefinition(getInterface()).load(loader);
    }

    public String export(String path) throws Exception {
        return new WadlDefinitionExporter(getInterface()).export(path);
    }

    public WadlInterfaceDefinition regenerateWadl() {
        try {
            reload();

            return getInterfaceDefinition();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean hasSchemaTypes() {
        return (super.hasSchemaTypes() || InferredSchemaManager.getInferredSchema(getInterface()).getNamespaces().length > 0);
    }

    public SchemaTypeSystem getSchemaTypeSystem() throws Exception {
        if (super.hasSchemaTypes()) {
            return InferredSchemaManager.getInferredSchema(getInterface()).getSchemaTypeSystem(
                    super.getSchemaTypeSystem());
        }
        return InferredSchemaManager.getInferredSchema(getInterface()).getSchemaTypeSystem();
    }
}
