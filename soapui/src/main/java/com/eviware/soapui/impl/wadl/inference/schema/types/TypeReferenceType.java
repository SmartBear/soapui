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

package com.eviware.soapui.impl.wadl.inference.schema.types;

import com.eviware.soapui.impl.wadl.inference.schema.Context;
import com.eviware.soapui.impl.wadl.inference.schema.Schema;
import com.eviware.soapui.impl.wadl.inference.schema.SchemaSystem;
import com.eviware.soapui.impl.wadl.inference.schema.Type;
import com.eviware.soapui.inferredSchema.TypeConfig;
import com.eviware.soapui.inferredSchema.TypeReferenceConfig;
import org.apache.xmlbeans.XmlException;

/**
 * This Type is simply a reference to another, actual Type. It is used when
 * loading previously saved data, since the Type may not yet be loaded.
 *
 * @author Dain Nilsson
 */
public class TypeReferenceType implements Type {
    String name;
    String namespace;
    SchemaSystem schemaSystem;

    /**
     * Constructs a new TypeReferenceType from previously saved data. Should be
     * called in the Type.Factory.
     */
    public TypeReferenceType(TypeReferenceConfig xml, Schema schema) {
        schemaSystem = schema.getSystem();
        name = xml.getReference().getLocalPart();
        namespace = xml.getReference().getNamespaceURI();
    }

    public TypeConfig save() {
        return schemaSystem.getSchemaForNamespace(namespace).getType(name).save();
    }

    public String getName() {
        return name;
    }

    public Type validate(Context context) throws XmlException {
        return schemaSystem.getSchemaForNamespace(namespace).getType(name);
    }

    public Schema getSchema() {
        return schemaSystem.getSchemaForNamespace(namespace);
    }

    public void setSchema(Schema schema) {
        namespace = schema.getNamespace();
    }

}
