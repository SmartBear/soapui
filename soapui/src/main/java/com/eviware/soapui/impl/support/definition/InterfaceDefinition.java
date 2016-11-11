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

package com.eviware.soapui.impl.support.definition;

import com.eviware.soapui.model.iface.Interface;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;

import javax.xml.namespace.QName;
import java.util.Collection;

public interface InterfaceDefinition<T extends Interface> {
    public String getTargetNamespace();

    public SchemaTypeLoader getSchemaTypeLoader();

    public SchemaTypeSystem getSchemaTypeSystem();

    public boolean hasSchemaTypes();

    public Collection<String> getDefinedNamespaces() throws Exception;

    public SchemaType findType(QName name);

    public DefinitionCache getDefinitionCache();

    public T getInterface();
}
