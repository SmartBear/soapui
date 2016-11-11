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

package com.eviware.soapui.model.propertyexpansion;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToObjectMap;

public class DefaultPropertyExpansionContext extends StringToObjectMap implements PropertyExpansionContext {
    private ModelItem modelItem;

    public DefaultPropertyExpansionContext(ModelItem modelItem) {
        this.modelItem = modelItem;
    }

    public String expand(String content) {
        return PropertyExpander.expandProperties(this, content);
    }

    public ModelItem getModelItem() {
        return modelItem;
    }

    public Object getProperty(String name) {
        return super.get(name);
    }

    public String[] getPropertyNames() {
        return keySet().toArray(new String[size()]);
    }

    @Override
    public Object get(Object key) {
        Object result = super.get(key);

        if (result == null) {
            result = expand((String) key);
            if (key.equals(result)) {
                result = expand("${" + key + "}");
                if (StringUtils.isNullOrEmpty((String) result)) {
                    result = null;
                }
            }
        }

        return result;
    }

    public boolean hasProperty(String name) {
        return containsKey(name);
    }

    public Object removeProperty(String name) {
        return remove(name);
    }

    public void setProperty(String name, Object value) {
        put(name, value);
    }

    public void setProperties(PropertyExpansionContext context) {
        for (String name : context.getPropertyNames()) {
            setProperty(name, context.getProperty(name));
        }
    }

    public StringToObjectMap getProperties() {
        return this;
    }
}
