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
