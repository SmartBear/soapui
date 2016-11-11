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

package com.eviware.soapui.support.editor.xml;

import com.eviware.soapui.support.editor.EditorLocation;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;

/**
 * Location in a XmlDocument
 *
 * @author ole.matzura
 */

public class XmlLocation implements EditorLocation<XmlDocument> {
    private final int line;
    private final int column;
    private XmlObject xmlObject;
    private final SchemaType schemaType;
    private String documentation;

    public XmlLocation(int line, int column) {
        this(line, column, null, null, null);
    }

    public XmlLocation(int line, int column, XmlObject xmlObject, SchemaType schemaType, String documentation) {
        this.line = line;
        this.column = column;
        this.xmlObject = xmlObject;
        this.schemaType = schemaType;
        this.documentation = documentation;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.impl.wsdl.panels.request.components.editor.EditorLocation
     * #getColumn()
     */
    public int getColumn() {
        return column;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.impl.wsdl.panels.request.components.editor.EditorLocation
     * #getLine()
     */
    public int getLine() {
        return line;
    }

    public SchemaType getSchemaType() {
        return schemaType;
    }

    public XmlObject getXmlObject() {
        return xmlObject;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

}
