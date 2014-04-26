/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.wsdl;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.model.iface.MessagePart;

/**
 * Descriptor for a Message/SOAP Header
 *
 * @author ole.matzura
 */

public class WsdlHeaderPart extends MessagePart.HeaderPart {
    private String name;
    private SchemaType schemaType;
    private QName partElementName;
    private final SchemaGlobalElement partElement;

    public WsdlHeaderPart(String name, SchemaType schemaType, QName partElementName, SchemaGlobalElement partElement) {
        super();

        this.name = name;
        this.schemaType = schemaType;
        this.partElementName = partElementName;
        this.partElement = partElement;
    }

    public SchemaType getSchemaType() {
        return schemaType;
    }

    public String getDescription() {
        return name + " of type [" + schemaType.getName() + "]";
    }

    public String getName() {
        return name;
    }

    public QName getPartElementName() {
        return partElementName;
    }

    @Override
    public SchemaGlobalElement getPartElement() {
        return partElement;
    }
}
