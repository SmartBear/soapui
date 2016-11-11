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

import com.eviware.soapui.impl.support.definition.InterfaceDefinition;
import com.eviware.soapui.impl.wsdl.WsdlInterface;

public class WsdlDefinitionExporter extends AbstractDefinitionExporter {
    public WsdlDefinitionExporter(WsdlInterface iface) throws Exception {
        this(iface.getWsdlContext().getInterfaceDefinition());
    }

    public WsdlDefinitionExporter(InterfaceDefinition<WsdlInterface> definition) {
        super(definition);
    }

    protected String[] getLocationXPathsToReplace() {
        return new String[]{"declare namespace s='http://schemas.xmlsoap.org/wsdl/' .//s:import/@location",
                "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:import/@schemaLocation",
                "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:include/@schemaLocation"};
    }
}
