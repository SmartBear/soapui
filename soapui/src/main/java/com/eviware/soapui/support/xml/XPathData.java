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

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author lars
 */
public class XPathData {
    private StringToStringMap nsMap = new StringToStringMap();
    private List<String> pathComponents = new ArrayList<String>();
    private String function;
    private boolean absolute = false;

    public XPathData(StringToStringMap nsMap, List<String> pathComponents, boolean absolute) {
        this.nsMap = nsMap;
        this.pathComponents = pathComponents;
        this.absolute = absolute;
    }

    public XPathData(String xpath, boolean skipFirst) {
        try {
            LineNumberReader reader = new LineNumberReader(new StringReader(xpath.trim()));
            String s;
            while ((s = reader.readLine()) != null) {
                if (s.startsWith("declare namespace ")) {
                    String[] words = s.substring("declare namespace ".length()).split("=");
                    String prefix = words[0];
                    int ix1 = words[1].indexOf('\'');
                    int ix2 = words[1].lastIndexOf('\'');
                    String ns = words[1].substring(ix1 + 1, ix2 - ix1);
                    nsMap.put(ns, prefix);
                } else {
                    if (s.startsWith("count(") && s.endsWith(")")) {
                        function = "count";
                        s = s.substring("count(".length(), s.length() - ")".length());
                    } else if (s.startsWith("exists(") && s.endsWith(")")) {
                        function = "exists";
                        s = s.substring("exists(".length(), s.length() - ")".length());
                    }

                    String[] words = s.split("/");
                    int firstWord = 1 + (skipFirst ? 1 : 0);
                    for (int i = firstWord; i < words.length; i++) {
                        pathComponents.add(0, words[i]);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void strip() {
        if (pathComponents.size() > 0 && "text()".equals(pathComponents.get(0))) {
            pathComponents.remove(0);
        }

        for (int i1 = 0; i1 < pathComponents.size(); i1++) {
            String s = pathComponents.get(i1);
            if (s.indexOf('[') >= 0) {
                StringBuffer buf = new StringBuffer();
                boolean skip = false;
                for (int i2 = 0; i2 < s.length(); i2++) {
                    char ch = s.charAt(i2);
                    if (ch == '[') {
                        skip = true;
                    }
                    if (!skip) {
                        buf.append(ch);
                    }

                    if (ch == ']') {
                        skip = false;
                    }
                }

                s = buf.toString();
                pathComponents.set(i1, s);
            }
        }

        function = null;
    }

    public XPathData createParent() {
        if (pathComponents.isEmpty()) {
            return null;
        }

        StringToStringMap nsMap2 = new StringToStringMap(nsMap);
        ArrayList<String> pathComponents2 = new ArrayList<String>(pathComponents);
        pathComponents2.remove(0);
        return new XPathData(nsMap2, pathComponents2, absolute);
    }

    @Override
    public String toString() {
        return getShortPath();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != XPathData.class) {
            return false;
        }

        XPathData other = (XPathData) obj;
        return this.getHashKey().equals(other.getHashKey());
    }

    @Override
    public int hashCode() {
        return getHashKey().hashCode();
    }

    public String getHashKey() {
        return getCanonicalPath();
    }

    public StringToStringMap getNamespaceMap() {
        return nsMap;
    }

    public List<String> getPathComponents() {
        return pathComponents;
    }

    public boolean isAttribute() {
        if (pathComponents.isEmpty()) {
            return false;
        }

        String c = pathComponents.get(0);
        return c.startsWith("@");
    }

    public String getAttributeName() {
        String c = pathComponents.get(0);
        return c.substring(1);
    }

    public String getFunction() {
        return function;
    }

    public String getXPath() {
        return buildXPath(null);
    }

    public String getFullPath() {
        return buildXPath(null);
    }

    public String buildXPath(XPathModifier modifier) {
        StringBuffer xpath = new StringBuffer();

        for (Iterator<String> i = nsMap.keySet().iterator(); i.hasNext(); ) {
            String ns = i.next();
            xpath.append("declare namespace " + nsMap.get(ns) + "='" + ns + "';\n");
        }

        if (function != null) {
            xpath.append(function).append("(");
        }

        if (modifier != null) {
            modifier.beforeSelector(xpath);
        }

        String firstComponent = "";
        if (pathComponents.size() > 0) {
            firstComponent = pathComponents.get(pathComponents.size() - 1);
        }
        if (!absolute && !"".equals(firstComponent)) {
            xpath.append("/");
        }

        for (int c = pathComponents.size() - 1; c >= 0; c--) {
            xpath.append("/").append(pathComponents.get(c));
        }

        if (modifier != null) {
            modifier.afterSelector(xpath);
        }

        if (function != null) {
            xpath.append(")");
        }

        return xpath.toString();
    }

    public String getPath() {
        StringBuffer buf = new StringBuffer();
        buf.append("/");

        for (int c = pathComponents.size() - 1; c >= 0; c--) {
            buf.append("/").append(pathComponents.get(c));
        }

        return buf.toString();
    }

    /**
     * Get a path with all namespaces replaced.
     */
    public String getCanonicalPath() {
        HashMap<String, String> inverseNsMap = new HashMap<String, String>();
        for (String key : nsMap.keySet()) {
            String value = nsMap.get(key);
            inverseNsMap.put(value, key);
        }

        StringBuffer buf = new StringBuffer();
        buf.append("/");

        for (int c = pathComponents.size() - 1; c >= 0; c--) {
            buf.append("/");
            String s = pathComponents.get(c);
            String[] words = s.split(":");
            if (words.length == 2) {
                String ns = inverseNsMap.get(words[0]);
                if (ns != null) {
                    buf.append(ns).append(":").append(words[1]);
                } else {
                    buf.append(s);
                }
            } else {
                buf.append(s);
            }
        }

        return buf.toString();
    }

    /**
     * Get a path with no namespaces or namespace prefixes.
     */
    public String getShortPath() {
        StringBuffer buf = new StringBuffer();
        buf.append("/");

        for (int c = pathComponents.size() - 1; c >= 0; c--) {
            buf.append("/");
            String s = pathComponents.get(c);
            String[] words = s.split(":");
            if (words.length == 2) {
                buf.append(words[1]);
            } else {
                buf.append(s);
            }
        }

        return buf.toString();
    }

    public Collection<String> getNamespaces() {
        return nsMap.keySet();
    }

    public boolean hasNamespaces() {
        return nsMap != null && !nsMap.isEmpty();
    }
}
