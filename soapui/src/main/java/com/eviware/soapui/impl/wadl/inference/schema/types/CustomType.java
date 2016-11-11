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
import com.eviware.soapui.impl.wadl.inference.schema.Type;
import com.eviware.soapui.inferredSchema.CustomTypeConfig;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * CustomType corresponds to any custom type given as a user-defined xsd type
 * definition.
 *
 * @author Dain Nilsson
 */
public class CustomType implements Type {
    private String xsd;
    private String name;
    private Schema schema;

    public CustomType(String name, String xsd) {
        this.name = name;
        this.xsd = xsd;
    }

    public CustomType(CustomTypeConfig xml, Schema schema) {
        this.schema = schema;
        name = xml.getName();
        xsd = xml.getXsd();
    }

    public CustomTypeConfig save() {
        CustomTypeConfig xml = CustomTypeConfig.Factory.newInstance();
        xml.setName(name);
        xml.setXsd(xsd);
        return xml;
    }

    public Type validate(Context context) throws XmlException {
        String name = context.getCursor().getName().getLocalPart();
        SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]{XmlObject.Factory
                .parse("<schema xmlns=\"http://www.w3.org/2001/XMLSchema\"><element name=\"" + name + "\">" + xsd
                        + "</element></schema>")}, XmlBeans.getBuiltinTypeSystem(), null);
        SchemaTypeLoader stl = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]{sts, XmlBeans.getBuiltinTypeSystem()});
        if (!stl.parse(context.getCursor().xmlText(), null, null).validate()) {
            throw new XmlException("Element '" + name + "' does not validate for custom type!");
        }
        return this;
    }

    @Override
    public String toString() {
        return xsd;
    }

    public String getName() {
        return name;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

}
