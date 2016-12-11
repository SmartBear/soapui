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

package com.eviware.soapui.model.propertyexpansion.resolvers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.JsonPathFacade;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

public class ResolverUtils {
    public static String checkForExplicitReference(String propertyName, String prefix, TestPropertyHolder holder,
                                                   PropertyExpansionContext context, boolean globalOverride) {
        if (holder == null) {
            return null;
        }

        if (propertyName.startsWith(prefix)) {
            propertyName = propertyName.substring(prefix.length());
        } else {
            return null;
        }

        return ResolverUtils.parseProperty(propertyName, holder, context, globalOverride);
    }

    public static String parseProperty(String name, TestPropertyHolder holder, PropertyExpansionContext context,
                                       boolean globalOverride) {
        int sepIx = name.indexOf(PropertyExpansion.PROPERTY_SEPARATOR);
        if (sepIx != -1) {
            String xpath = name.substring(sepIx + 1);
            name = name.substring(0, sepIx);

            if (globalOverride) {
                String value = PropertyExpansionUtils.getGlobalProperty(name);
                if (value != null) {
                    return value;
                }
            }

            TestProperty property = holder.getProperty(name);

            if (property != null) {
                return context == null ? ResolverUtils.extractXPathPropertyValue(property, xpath) : ResolverUtils
                        .extractXPathPropertyValue(property, PropertyExpander.expandProperties(context, xpath));
            }
        } else {
            if (globalOverride) {
                String value = PropertyExpansionUtils.getGlobalProperty(name);
                if (value != null) {
                    return value;
                }
            }

            TestProperty property = holder.getProperty(name);
            if (property != null) {
                return property.getValue();
            }
        }

        return null;
    }

    public static String extractXPathPropertyValue(Object property, String pathExpression) {
        try {
            String value = property instanceof TestProperty ? ((TestProperty) property).getValue() : property
                    .toString();
            if (pathExpression.startsWith("$")) {
                return new JsonPathFacade(value).readStringValue(pathExpression);
            } else {
                XmlObject xmlObject = XmlUtils.createXmlObject(value);
                String ns = pathExpression.trim().startsWith("declare namespace") ? "" : XmlUtils.declareXPathNamespaces(xmlObject);
                Node domNode = XmlUtils.selectFirstDomNode(xmlObject, ns + pathExpression);
                return domNode == null ? null : XmlUtils.getValueForMatch(domNode, false);
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return null;
    }

}
