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
 * Support class for reading XmlObject based configurations..
 *
 * @author Ole.Matzura
 */

public class XmlObjectConfigurationReader {
    private final XmlObject config;

    public XmlObjectConfigurationReader(XmlObject config) {
        this.config = config;
    }

    public int readInt(String name, int def) {
        if (config == null) {
            return def;
        }

        try {
            String str = readString(name, null);
            return str == null ? def : Integer.parseInt(str);
        } catch (NumberFormatException e) {
        }

        return def;
    }

    public long readLong(String name, int def) {
        if (config == null) {
            return def;
        }

        try {
            String str = readString(name, null);
            return str == null ? def : Long.parseLong(str);
        } catch (NumberFormatException e) {
        }

        return def;
    }

    public float readFloat(String name, float def) {
        if (config == null) {
            return def;
        }

        try {
            String str = readString(name, null);
            return str == null ? def : Float.parseFloat(str);
        } catch (NumberFormatException e) {
        }

        return def;
    }

    public String readString(String name, String def) {
        if (config == null) {
            return def;
        }

        XmlObject[] paths = config.selectPath("$this/" + name);
        if (paths.length == 1) {
            XmlCursor cursor = paths[0].newCursor();
            String textValue = cursor.getTextValue();
            cursor.dispose();
            return textValue;
        }

        return def;
    }

    public String[] readStrings(String name) {
        if (config == null) {
            return null;
        }

        XmlObject[] paths = config.selectPath("$this/" + name);
        String[] result = new String[paths.length];

        for (int c = 0; c < paths.length; c++) {
            XmlCursor cursor = paths[c].newCursor();
            result[c] = cursor.getTextValue();
            cursor.dispose();
        }

        return result;
    }

    public boolean readBoolean(String name, boolean def) {
        try {
            return Boolean.valueOf(readString(name, String.valueOf(def)));
        } catch (Exception e) {
            return def;
        }
    }
}
