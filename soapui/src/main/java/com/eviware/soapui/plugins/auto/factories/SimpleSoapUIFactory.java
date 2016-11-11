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

package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.plugins.PluginProxies;

public class SimpleSoapUIFactory<T extends Object> extends AbstractSoapUIFactory<T> {
    private Class<T> objectClass;

    protected SimpleSoapUIFactory(Class<?> factoryType, Class<T> objectClass) {
        super(factoryType);
        this.objectClass = objectClass;
    }

    public T create() {
        try {
            T returnValue = createByReflection(objectClass);
            return PluginProxies.proxyIfApplicable(returnValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }
}
