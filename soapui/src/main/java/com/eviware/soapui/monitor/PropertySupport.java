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

package com.eviware.soapui.monitor;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.util.Properties;

public class PropertySupport {
    public static void applySystemProperties(Object target, String scope, ModelItem modelItem) {
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(target);
        DefaultPropertyExpansionContext context = new DefaultPropertyExpansionContext(modelItem);
        Properties properties = System.getProperties();

        for (PropertyDescriptor descriptor : descriptors) {
            String name = descriptor.getName();
            String key = scope + "." + name;
            if (PropertyUtils.isWriteable(target, name) && properties.containsKey(key)) {
                try {
                    String value = context.expand(String.valueOf(properties.get(key)));
                    BeanUtils.setProperty(target, name, value);
                    SoapUI.log.info("Set property [" + name + "] to [" + value + "] in scope [" + scope + "]");
                } catch (Throwable e) {
                    SoapUI.logError(e);
                }
            }
        }
    }

}
