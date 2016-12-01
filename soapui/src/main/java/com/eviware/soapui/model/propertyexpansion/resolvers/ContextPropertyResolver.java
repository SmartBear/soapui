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

import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;

public class ContextPropertyResolver implements PropertyResolver {
    public String resolveProperty(PropertyExpansionContext context, String propertyName, boolean globalOverride) {
        Object property = null;
        String xpath = null;

        int sepIx = propertyName.indexOf(PropertyExpansion.PROPERTY_SEPARATOR);
        if (sepIx == 0) {
            propertyName = propertyName.substring(1);
            sepIx = propertyName.indexOf(PropertyExpansion.PROPERTY_SEPARATOR);
        }

        if (sepIx > 0) {
            xpath = propertyName.substring(sepIx + 1);
            propertyName = propertyName.substring(0, sepIx);
        }

        if (globalOverride) {
            property = PropertyExpansionUtils.getGlobalProperty(propertyName);
        }

        if (property == null) {
            property = context.getProperty(propertyName);
        }

        if (property != null && xpath != null) {
            property = ResolverUtils.extractXPathPropertyValue(property,
                    PropertyExpander.expandProperties(context, xpath));
        }

        return property == null ? null : property.toString();
    }

}
