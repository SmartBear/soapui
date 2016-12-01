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

import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.export.WsdlDefinitionExporter;
import com.eviware.soapui.impl.support.definition.support.AbstractDefinitionContext;
import com.eviware.soapui.impl.support.definition.support.InterfaceCacheDefinitionLoader;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;

import javax.wsdl.Definition;

/**
 * Holder for WSDL4J Definitions and related SchemaTypeLoader types
 *
 * @author Ole.Matzura
 */

public class WsdlContext extends
        AbstractDefinitionContext<WsdlInterface, WsdlDefinitionLoader, WsdlInterfaceDefinition> {
    private SoapVersion soapVersion = SoapVersion.Soap11;

    public WsdlContext(String url, WsdlInterface iface) {
        super(url, iface);
    }

    public WsdlContext(String wsdlUrl) {
        this(wsdlUrl, (WsdlInterface) null);
    }

    public WsdlContext(String wsdlUrl, SoapVersion soapVersion) {
        this(wsdlUrl);
        if (soapVersion != null) {
            this.soapVersion = soapVersion;
        }
    }

    protected WsdlDefinitionLoader createDefinitionLoader(DefinitionCache wsdlInterfaceDefinitionCache) {
        return new InterfaceCacheDefinitionLoader(wsdlInterfaceDefinitionCache);
    }

    protected WsdlDefinitionLoader createDefinitionLoader(String url) {
        return new UrlWsdlLoader(url, getInterface());
    }

    protected WsdlInterfaceDefinition loadDefinition(WsdlDefinitionLoader loader) throws Exception {
        return new WsdlInterfaceDefinition(getInterface()).load(loader);
    }

    public Definition getDefinition() throws Exception {
        return getInterfaceDefinition().getWsdlDefinition();
    }

    public SoapVersion getSoapVersion() {
        return getInterface() == null ? soapVersion : getInterface().getSoapVersion();
    }

    public String export(String path) throws Exception {
        return new WsdlDefinitionExporter(getInterface()).export(path);
    }
}
