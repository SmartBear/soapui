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

import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.support.definition.support.XmlSchemaBasedInterfaceDefinition;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

public class WsdlInterfaceDefinition extends XmlSchemaBasedInterfaceDefinition<WsdlInterface> {
    private Definition definition;

    private static WSDLFactory factory;
    private static WSDLReader wsdlReader;
    private Logger log = LogManager.getLogger(WsdlInterfaceDefinition.class);

    public WsdlInterfaceDefinition(WsdlInterface iface) {
        super(iface);
    }

    public WsdlInterfaceDefinition load(WsdlDefinitionLoader loader) throws Exception {
        if (factory == null) {
            factory = WSDLFactory.newInstance();
            wsdlReader = factory.newWSDLReader();
            wsdlReader.setFeature("javax.wsdl.verbose", true);
            wsdlReader.setFeature("javax.wsdl.importDocuments", true);
        }

        log.debug("Loading WSDL: " + loader.getBaseURI());
        try {
            definition = wsdlReader.readWSDL(loader);
        } catch (WSDLException e) {
            throw new InvalidDefinitionException(e);
        }

        if (!loader.isAborted()) {
            super.loadSchemaTypes(loader);
        } else {
            throw new Exception("Loading of WSDL from [" + loader.getBaseURI() + "] was aborted");
        }

        return this;
    }

    public String getTargetNamespace() {
        return WsdlUtils.getTargetNamespace(definition);
    }

    public Definition getWsdlDefinition() {
        return definition;
    }
}
