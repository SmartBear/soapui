/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassUtils {
    public static List<Class<?>> getImplementedAndExtendedClasses(Object obj) {
        ArrayList<Class<?>> result = new ArrayList<Class<?>>();
        addImplementedAndExtendedClasses(obj.getClass(), result);
        return result;
    }

    private static void addImplementedAndExtendedClasses(Class<?> clazz, ArrayList<Class<?>> result) {
        result.add(clazz);
        // result.addAll( Arrays.asList( clazz.getInterfaces() ));
        addImplementedInterfaces(clazz, result);
        if (clazz.getSuperclass() != null) {
            addImplementedAndExtendedClasses(clazz.getSuperclass(), result);
        }
    }

    private static void addImplementedInterfaces(Class<?> intrfc, ArrayList<Class<?>> result) {
        // result.add( intrfc.getClass() );
        Class<?>[] interfacesArray = intrfc.getInterfaces();
        if (interfacesArray.length > 0) {
            result.addAll(Arrays.asList(interfacesArray));
            for (int i = 0; i < interfacesArray.length; i++) {
                Class<?> class1 = interfacesArray[i];
                addImplementedInterfaces(class1, result);
            }
        }
    }

}
