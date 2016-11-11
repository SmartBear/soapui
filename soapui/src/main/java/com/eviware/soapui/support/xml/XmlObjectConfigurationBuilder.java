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

package com.eviware.soapui.support.xml;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * Support class for building XmlObject based configurations
 *
 * @author Ole.Matzura
 */

public class XmlObjectConfigurationBuilder {
    private XmlObject config;
    private XmlCursor cursor;

    public XmlObjectConfigurationBuilder(XmlObject config) {
        this.config = config;
        cursor = config.newCursor();
        cursor.toNextToken();
    }

    public XmlObjectConfigurationBuilder() {
        this(XmlObject.Factory.newInstance());
        cursor = config.newCursor();
        cursor.toNextToken();
    }

    public XmlObjectConfigurationBuilder add(String name, String value) {
        cursor.insertElementWithText(name, value);
        return this;
    }

    public XmlObjectConfigurationBuilder add(String name, int value) {
        cursor.insertElementWithText(name, String.valueOf(value));
        return this;
    }

    public XmlObjectConfigurationBuilder add(String name, long value) {
        cursor.insertElementWithText(name, String.valueOf(value));
        return this;
    }

    public XmlObjectConfigurationBuilder add(String name, float value) {
        cursor.insertElementWithText(name, String.valueOf(value));
        return this;
    }

    public XmlObject finish() {
        cursor.dispose();
        return config;
    }

    public XmlObjectConfigurationBuilder add(String name, boolean value) {
        cursor.insertElementWithText(name, String.valueOf(value));
        return this;
    }

    public void add(String name, String[] values) {
        for (String value : values) {
            add(name, value);
        }
    }
}
