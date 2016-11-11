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

import com.eviware.soapui.config.DefinitionCacheTypeConfig;
import com.eviware.soapui.config.DefintionPartConfig;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.support.xml.XmlUtils;
import org.w3c.dom.Node;

public class ConfigInterfaceDefinitionPart implements InterfaceDefinitionPart {
    private DefintionPartConfig config;
    private boolean isRoot;
    private DefinitionCacheTypeConfig.Enum type;

    public ConfigInterfaceDefinitionPart(DefintionPartConfig config, boolean isRoot, DefinitionCacheTypeConfig.Enum type) {
        this.config = config;
        this.isRoot = isRoot;
        this.type = type;
    }

    public String getUrl() {
        return config.getUrl();
    }

    public String getType() {
        return config.getType();
    }

    public String getContent() {
        if (type == DefinitionCacheTypeConfig.TEXT) {
            Node domNode = config.getContent().getDomNode();
            return XmlUtils.getNodeValue(domNode);
        } else {
            return config.getContent().toString();
        }
    }

    public boolean isRootPart() {
        return isRoot;
    }
}
