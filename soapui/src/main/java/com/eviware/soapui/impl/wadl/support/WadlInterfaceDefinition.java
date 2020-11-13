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

package com.eviware.soapui.impl.wadl.support;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.support.definition.DefinitionLoader;
import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.support.definition.support.XmlSchemaBasedInterfaceDefinition;
import net.java.dev.wadl.x2009.x02.ApplicationDocument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlObject;

public class WadlInterfaceDefinition extends XmlSchemaBasedInterfaceDefinition<RestService> {
    private ApplicationDocument applicationDocument;
    private Logger log = LogManager.getLogger(WadlInterfaceDefinition.class);

    public WadlInterfaceDefinition(RestService iface) {
        super(iface);
    }

    public WadlInterfaceDefinition load(DefinitionLoader loader) throws Exception {
        try {
            XmlObject obj = loader.loadXmlObject(loader.getBaseURI(), null);
            applicationDocument = (ApplicationDocument) obj.changeType(ApplicationDocument.type);
        } catch (Exception e) {
            throw new InvalidDefinitionException(e);
        }

        if (!loader.isAborted()) {
            super.loadSchemaTypes(loader);
        } else {
            throw new Exception("Loading of WADL from [" + loader.getBaseURI() + "] was aborted");
        }

        return this;
    }

    public String getTargetNamespace() {
        return null;
    }

    public ApplicationDocument.Application getApplication() {
        return applicationDocument.getApplication();
    }
}
