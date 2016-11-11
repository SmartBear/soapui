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

package com.eviware.soapui.security.boundary;

import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import org.apache.xmlbeans.XmlAnySimpleType;

import java.util.ArrayList;
import java.util.List;

public class BoundaryRestrictionUtill {
    public static String getRestrictionInfo(String label, String name, String xpath) {
        return BoundaryUtils.createCharacterArray("ABCDEFG ", 100);
    }

    public static List<String> getRestrictions(XmlTreeNode node, List<String> restrictionsList) {
        String baseType = null;

        for (int i = 0; i < node.getChildCount(); i++) {
            XmlTreeNode mynode = node.getChild(i);

            if ("xsd:restriction".equals(mynode.getParent().getNodeName())) {
                if (mynode.getNodeName().equals("@base")) {
                    baseType = mynode.getNodeText();
                    restrictionsList.add("type = " + baseType);
                } else {
                    String nodeName = mynode.getNodeName();
                    String nodeValue = mynode.getChild(0).getNodeText();
                    restrictionsList.add(nodeName + " = " + nodeValue);
                }
            }
            getRestrictions(mynode, restrictionsList);
        }
        return restrictionsList;
    }

    public static List<String> getType(XmlTreeNode node, List<String> restrictionsList) {
        String baseType = null;

        for (int i = 0; i < node.getChildCount(); i++) {
            XmlTreeNode mynode = node.getChild(i);

            if (mynode.getNodeName().equals("@base")) {
                baseType = mynode.getNodeText();
                if (baseType.contains(":")) {
                    baseType = baseType.substring(baseType.indexOf(":") + 1);
                }
                restrictionsList.add("type = " + baseType);
            }
            getType(mynode, restrictionsList);
        }
        return restrictionsList;
    }

    public static List<String> extractEnums(XmlTreeNode node) {
        List<String> restrictionsList = new ArrayList<String>();
        for (XmlAnySimpleType s : node.getSchemaType().getEnumerationValues()) {
            if (restrictionsList.isEmpty()) {
                restrictionsList.add("For type enumeration values are: ");
            }
            restrictionsList.add(s.getStringValue());
        }
        return restrictionsList;
    }

}
