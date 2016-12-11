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

package com.eviware.soapui.support.swing;

import javax.swing.JComponent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility for working with collections of components
 *
 * @author Ole.Matzura
 */

public class ComponentBag {
    private Map<String, JComponent> components = new HashMap<String, JComponent>();

    public ComponentBag() {
    }

    public void add(JComponent component) {
        components.put(String.valueOf(component.hashCode()), component);
    }

    public void add(String name, JComponent component) {
        components.put(name, component);
    }

    public JComponent get(String name) {
        return components.get(name);
    }

    public void setEnabled(boolean enabled) {
        Iterator<JComponent> iterator = components.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().setEnabled(enabled);
        }
    }

    public void setEnabled(boolean enabled, String name) {
        if (components.containsKey(name)) {
            components.get(name).setEnabled(enabled);
        }
    }

    public void setEnabled(boolean enabled, String[] names) {
        for (int c = 0; c < names.length; c++) {
            setEnabled(enabled, names[c]);
        }
    }
}
