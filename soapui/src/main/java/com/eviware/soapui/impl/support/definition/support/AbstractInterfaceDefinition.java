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

import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.InterfaceDefinition;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;

import java.util.List;

public abstract class AbstractInterfaceDefinition<T extends AbstractInterface<?>> implements InterfaceDefinition<T> {
    private DefinitionCache definitionCache;
    private T iface;

    protected AbstractInterfaceDefinition(T iface) {
        this.iface = iface;
    }

    public DefinitionCache getDefinitionCache() {
        return definitionCache;
    }

    public void setDefinitionCache(DefinitionCache definitionCache) {
        this.definitionCache = definitionCache;
    }

    public InterfaceDefinitionPart getRootPart() {
        return definitionCache == null ? null : definitionCache.getRootPart();
    }

    public List<InterfaceDefinitionPart> getDefinitionParts() throws Exception {
        return definitionCache == null ? null : definitionCache.getDefinitionParts();
    }

    public T getInterface() {
        return iface;
    }

    public void setIface(T iface) {
        this.iface = iface;
    }
}
