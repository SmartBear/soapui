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

import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Separates a path component into its parts.
 *
 * @author lars
 */
public class XPathComponent {
    private String namespace;
    private String prefix;
    private String localNameWithoutBraces;

    // index and conditions, for example "[1]" or "[x > 3]"
    private String braces;

    public XPathComponent(String c, StringToStringMap prefixMap) {
        String localName;
        int ix = c.indexOf(':');
        if (ix >= 0) {
            prefix = c.substring(0, ix);
            localName = c.substring(ix + 1);
            namespace = prefixMap.get(prefix);
        } else {
            prefix = null;
            localName = c;
            namespace = null;
        }
        ix = localName.indexOf('[');
        if (ix >= 0) {
            localNameWithoutBraces = localName.substring(0, ix);
            braces = localName.substring(ix);
        } else {
            localNameWithoutBraces = localName;
            braces = "";
        }
        assert localName.equals(localNameWithoutBraces + braces) : localName + " != " + localNameWithoutBraces + " + "
                + braces;
    }

    @Override
    public String toString() {
        if (prefix != null) {
            return prefix + ":" + localNameWithoutBraces + braces;
        } else {
            return localNameWithoutBraces + braces;
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean hasPrefix() {
        return prefix != null;
    }

    public String getPrefix() {
        if (prefix == null) {
            return "";
        } else {
            return prefix;
        }
    }

    public String getLocalName() {
        return localNameWithoutBraces;
    }

    public String getBraces() {
        return braces;
    }

    public String getFullNameWithPrefix() {
        return getFullNameWithPrefix(localNameWithoutBraces);
    }

    public String getFullNameWithPrefix(String aLocalName) {
        return (hasPrefix() ? getPrefix() + ":" : "") + aLocalName + getBraces();
    }
}
