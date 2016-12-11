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

package com.eviware.soapui.support.registry;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RegistryEntryConfig;
import com.eviware.soapui.support.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractRegistry<T1 extends RegistryEntry<T2, T3>, T2 extends RegistryEntryConfig, T3 extends Object> {
    private Map<String, Class<? extends T1>> registry = new HashMap<String, Class<? extends T1>>();

    public void mapType(String type, Class<? extends T1> clazz) {
        registry.put(type, clazz);
    }

    public T1 create(String type, T3 parent) {
        if (registry.containsKey(type)) {
            T2 config = addNewConfig(parent);
            config.setType(type);
            return build(config, parent);
        } else {
            throw new RuntimeException("Invalid type [" + type + "]");
        }
    }

    protected abstract T2 addNewConfig(T3 parent);

    public T1 build(T2 config, T3 parent) {
        try {
            Class<? extends T1> clazz = registry.get(config.getType());
            if (clazz == null) {
                return null;
            }

            T1 entry = clazz.newInstance();
            entry.init(config, parent);
            return entry;
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return null;
    }

    public String[] getTypes() {
        return StringUtils.sortNames(registry.keySet().toArray(new String[registry.size()]));
    }

    public String[] getTypesWithInterface(Class<?> clazz) {
        List<String> result = new ArrayList<String>();

        for (String type : registry.keySet()) {
            if (Arrays.asList(registry.get(type).getInterfaces()).contains(clazz)) {
                result.add(type);
            }
        }

        return result.toArray(new String[result.size()]);
    }
}
