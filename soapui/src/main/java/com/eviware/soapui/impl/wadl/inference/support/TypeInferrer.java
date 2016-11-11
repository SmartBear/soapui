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

package com.eviware.soapui.impl.wadl.inference.support;

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlBase64Binary;
import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlDecimal;
import org.apache.xmlbeans.XmlGDay;
import org.apache.xmlbeans.XmlGMonth;
import org.apache.xmlbeans.XmlGYear;
import org.apache.xmlbeans.XmlGYearMonth;
import org.apache.xmlbeans.XmlHexBinary;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlNegativeInteger;
import org.apache.xmlbeans.XmlNonNegativeInteger;
import org.apache.xmlbeans.XmlNonPositiveInteger;
import org.apache.xmlbeans.XmlPositiveInteger;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML Schema inferred from gathered XML data.
 *
 * @author Dain Nilsson
 */
public class TypeInferrer {
    private static TypeInferrer ref;
    private TypeTree types;
    private Map<XmlAnySimpleType, TypeTree> typeTable;

    /**
     * Get the instance of the XmlAnySimpleType with the type xs:<typeName>.
     *
     * @param typeName
     * @return Returns the XmlAnySimpleType, if available. Otherwise returns
     *         null.
     */
    public static XmlAnySimpleType getType(String typeName) {
        for (XmlAnySimpleType item : getRef().typeTable.keySet()) {
            if (item.schemaType().getName().getLocalPart().equals(typeName)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Get the XmlAnySimpleType that describes a simple value that is empty.
     *
     * @return Returns the xs:string XmlAnySimpleType.
     */
    public static XmlAnySimpleType getBlankType() {
        return getRef().types.type;
    }

    /**
     * Given a value and a type, a new type will be returned that validates
     * values for both the given type, and the new value.
     *
     * @param value    The new value to expand the type for.
     * @param baseType The type to be expanded.
     * @return The new expanded type.
     */
    public static XmlAnySimpleType expandTypeForValue(String value, XmlAnySimpleType baseType) {
        return getRef().expandTypeForValueInternal(baseType, value);
    }

    /**
     * Given a simple value, infers the type of the value.
     *
     * @param value The value to assign a type to.
     * @return Returns the inferred type for the given value.
     */
    public static XmlAnySimpleType inferSimpleType(String value) {
        return getRef().inferSimpleTypeRec(value, getRef().types);
    }

    /**
     * Validates a string against an XmlAnySimpleType.
     *
     * @param value The value to validate.
     * @param type  The XmlAnySimpleType to validate against.
     * @return True if the value validates, false if not.
     */
    public static boolean validateSimpleType(String value, XmlAnySimpleType type) {
        try {
            type.setStringValue(value);
            return type.validate();
        } catch (Exception e) {
            return false;
        }
    }

    private static TypeInferrer getRef() {
        if (ref == null) {
            ref = new TypeInferrer();
        }
        return ref;
    }

    private TypeInferrer() {
        typeTable = new HashMap<XmlAnySimpleType, TypeTree>();
        TypeTree xmlbool = new TypeTree(XmlBoolean.Factory.newInstance());
        typeTable.put(xmlbool.type, xmlbool);
        TypeTree xmlbool2 = new TypeTree(XmlBoolean.Factory.newInstance());
        typeTable.put(xmlbool2.type, xmlbool2);
        TypeTree xmlnegint = new TypeTree(XmlNegativeInteger.Factory.newInstance());
        typeTable.put(xmlnegint.type, xmlnegint);
        TypeTree xmlposint = new TypeTree(XmlPositiveInteger.Factory.newInstance());
        typeTable.put(xmlposint.type, xmlposint);
        TypeTree xmlnonnegint = new TypeTree(XmlNonNegativeInteger.Factory.newInstance());
        typeTable.put(xmlnonnegint.type, xmlnonnegint);
        xmlnonnegint.addChild(xmlposint);
        xmlnonnegint.addChild(xmlbool);
        TypeTree xmlnonposint = new TypeTree(XmlNonPositiveInteger.Factory.newInstance());
        typeTable.put(xmlnonposint.type, xmlnonposint);
        xmlnonposint.addChild(xmlnegint);
        TypeTree xmlint = new TypeTree(XmlInteger.Factory.newInstance());
        typeTable.put(xmlint.type, xmlint);
        xmlint.addChild(xmlnonnegint);
        xmlint.addChild(xmlnonposint);
        TypeTree xmldec = new TypeTree(XmlDecimal.Factory.newInstance());
        typeTable.put(xmldec.type, xmldec);
        xmldec.addChild(xmlint);
        TypeTree xmldate = new TypeTree(XmlDate.Factory.newInstance());
        typeTable.put(xmldate.type, xmldate);
        TypeTree xmltime = new TypeTree(XmlTime.Factory.newInstance());
        typeTable.put(xmltime.type, xmltime);
        TypeTree xmldatetime = new TypeTree(XmlDateTime.Factory.newInstance());
        typeTable.put(xmldatetime.type, xmldatetime);
        TypeTree xmlhexbin = new TypeTree(XmlHexBinary.Factory.newInstance());
        typeTable.put(xmlhexbin.type, xmlhexbin);
        TypeTree xmlb64bin = new TypeTree(XmlBase64Binary.Factory.newInstance());
        typeTable.put(xmlb64bin.type, xmlb64bin);
        TypeTree xmlgyearmonth = new TypeTree(XmlGYearMonth.Factory.newInstance());
        typeTable.put(xmlgyearmonth.type, xmlgyearmonth);
        TypeTree xmlgyear = new TypeTree(XmlGYear.Factory.newInstance());
        typeTable.put(xmlgyear.type, xmlgyear);
        TypeTree xmlgmonth = new TypeTree(XmlGMonth.Factory.newInstance());
        typeTable.put(xmlgmonth.type, xmlgmonth);
        TypeTree xmlgday = new TypeTree(XmlGDay.Factory.newInstance());
        typeTable.put(xmlgday.type, xmlgday);
        TypeTree xmlstring = new TypeTree(XmlString.Factory.newInstance());
        typeTable.put(xmlstring.type, xmlstring);
        xmlstring.addChild(xmldec);
        xmlstring.addChild(xmldate);
        xmlstring.addChild(xmltime);
        xmlstring.addChild(xmldatetime);
        xmlstring.addChild(xmlbool2);
        xmlstring.addChild(xmlgyearmonth);
        xmlstring.addChild(xmlgyear);
        xmlstring.addChild(xmlgmonth);
        xmlstring.addChild(xmlgday);
        xmlstring.addChild(xmlhexbin);
        // xmlstring.addChild(xmlb64bin);
        types = xmlstring;
    }

    private XmlAnySimpleType expandTypeForValueInternal(XmlAnySimpleType type, String value) {
        TypeTree p = typeTable.get(type);
        while (!validateSimpleType(value, p.type)) {
            p = p.parent;
        }
        return p.type;
    }

    private XmlAnySimpleType inferSimpleTypeRec(String value, TypeTree p) {
        for (TypeTree item : p.children) {
            if (validateSimpleType(value, item.type)) {
                return inferSimpleTypeRec(value, item);
            }
        }
        return p.type;
    }

    private class TypeTree {
        public XmlAnySimpleType type;
        public TypeTree parent;
        public List<TypeTree> children;

        public TypeTree(XmlAnySimpleType type) {
            this.type = type;
            children = new ArrayList<TypeTree>();
        }

        public void addChild(TypeTree type) {
            children.add(type);
            type.parent = this;
        }
    }

}
