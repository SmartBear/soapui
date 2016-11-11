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
import com.eviware.soapui.impl.wadl.inference.schema.Settings;
import com.eviware.soapui.impl.wadl.inference.schema.Type;
import com.eviware.soapui.impl.wadl.inference.schema.content.EmptyContent;
import com.eviware.soapui.impl.wadl.inference.support.TypeInferrer;
import com.eviware.soapui.inferredSchema.EmptyTypeConfig;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;

/**
 * EmptyRtpe corresponds to an instance of a type with no attributes, nor any
 * content.
 *
 * @author Dain Nilsson
 */
public class EmptyType implements Type {
    private Schema schema;
    private EmptyContent empty;
    private boolean completed = false;

    public EmptyType(Schema schema) {
        this.schema = schema;
        empty = new EmptyContent(schema, false);
    }

    public EmptyType(EmptyTypeConfig xml, Schema schema) {
        this.schema = schema;
        empty = new EmptyContent(schema, xml.getCompleted());
        completed = xml.getCompleted();
    }

    public EmptyTypeConfig save() {
        EmptyTypeConfig xml = EmptyTypeConfig.Factory.newInstance();
        xml.setCompleted(completed);
        return xml;
    }

    public String getName() {
        return "empty_element";
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public Type validate(Context context) throws XmlException {
        XmlCursor cursor = context.getCursor();
        if (!cursor.isAttr() && (cursor.toFirstAttribute() || cursor.toFirstChild())) {
            // Element has attributes or children, must be complexType
            ComplexType newType = new ComplexType(schema, context.getName(), completed);
            newType.setContent(empty);
            return newType;
        }
        cursor.toFirstContentToken();
        if (empty.validate(context) != empty) {
            // Element has simple content, must be simpleType
            String value = cursor.getTextValue();
            XmlAnySimpleType simpleType;
            if (completed) {
                simpleType = TypeInferrer.getBlankType();
            } else {
                simpleType = TypeInferrer.inferSimpleType(value);
            }
            // return
            // context.getSchemaSystem().getType(simpleType.schemaType().getName());
            return new SimpleType(schema, simpleType, completed);
        }
        completed = true;
        return this;
    }

    public String toString() {
        String xsdns = schema.getPrefixForNamespace(Settings.xsdns);
        StringBuilder s = new StringBuilder("<" + xsdns + ":complexType name=\"" + getName() + "\"/>");
        return s.toString();
    }

}
