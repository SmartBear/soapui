/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.teststeps;

import static com.eviware.soapui.support.JsonUtil.seemsToBeJson;
import static com.eviware.soapui.support.xml.XmlUtils.seemsToBeXml;

public enum PathLanguage {

    XPATH("XPath"),
    XQUERY("XQuery"),
    JSONPATH("JSONPath");

    public static PathLanguage forContent(String content) {
        if (content != null) {
            if (seemsToBeJson(content)) {
                return PathLanguage.JSONPATH;
            } else if (seemsToBeXml(content)) {
                return PathLanguage.XPATH;
            }
        }

        return null;
    }

    private String displayName;

    PathLanguage(String displayName) {
        this.displayName = displayName;
    }


    @Override
    public String toString() {
        return displayName;
    }


}
