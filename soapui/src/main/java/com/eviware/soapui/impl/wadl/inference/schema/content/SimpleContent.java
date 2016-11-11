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

package com.eviware.soapui.impl.wadl.inference.schema.content;

import com.eviware.soapui.impl.wadl.inference.ConflictHandler;
import com.eviware.soapui.impl.wadl.inference.schema.Content;
import com.eviware.soapui.impl.wadl.inference.schema.Context;
import com.eviware.soapui.impl.wadl.inference.schema.Schema;
import com.eviware.soapui.impl.wadl.inference.schema.Settings;
import com.eviware.soapui.impl.wadl.inference.support.TypeInferrer;
import com.eviware.soapui.inferredSchema.SimpleContentConfig;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;

import javax.xml.namespace.QName;

/**
 * SimpleContent may contain xs:simpleContent.
 *
 * @author Dain Nilsson
 */
public class SimpleContent implements Content {
    private Schema schema;
    private XmlAnySimpleType simpleType;

    public SimpleContent(Schema schema, String initialValue) {
        this.schema = schema;
        simpleType = TypeInferrer.inferSimpleType(initialValue);
    }

    public SimpleContent(Schema schema, XmlAnySimpleType initialType) {
        this.schema = schema;
        simpleType = initialType;
    }

    public SimpleContent(SimpleContentConfig xml, Schema schema) {
        this.schema = schema;
        simpleType = TypeInferrer.getType(xml.getTypeName());
    }

    public SimpleContentConfig save() {
        SimpleContentConfig xml = SimpleContentConfig.Factory.newInstance();
        xml.setTypeName(simpleType.schemaType().getName().getLocalPart());
        return xml;
    }

    public Content validate(Context context) throws XmlException {
        XmlCursor cursor = context.getCursor();
        String value = "";
        if (cursor.isStart()) {
            throw new XmlException("Unsupported!");
        }
        if (!cursor.isEnd()) {
            value = cursor.getTextValue();
        }
        if (!TypeInferrer.validateSimpleType(value, simpleType)) {
            XmlAnySimpleType newSimpleType = TypeInferrer.expandTypeForValue(value, simpleType);
            if (context.getHandler().callback(ConflictHandler.Event.MODIFICATION, ConflictHandler.Type.TYPE,
                    new QName(schema.getNamespace(), context.getAttribute("typeName")), context.getPath(),
                    "Illegal value.")) {
                simpleType = newSimpleType;
            } else {
                throw new XmlException("Illegal content!");
            }
        }
        return this;
    }

    public String toString(String attrs) {
        if (simpleType == null) {
            return attrs;
        }
        String xsdns = schema.getPrefixForNamespace(Settings.xsdns);
        StringBuilder s = new StringBuilder("<" + xsdns + ":simpleContent><" + xsdns + ":extension base=\"" + xsdns
                + ":" + simpleType.schemaType().getName().getLocalPart() + "\">");
        s.append(attrs);
        s.append("</" + xsdns + ":extension></" + xsdns + ":simpleContent>");
        return s.toString();
    }

}
