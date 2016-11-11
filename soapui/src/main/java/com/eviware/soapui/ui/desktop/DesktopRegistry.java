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

package com.eviware.soapui.ui.desktop;

import com.eviware.soapui.model.workspace.Workspace;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of available desktops
 *
 * @author ole.matzura
 */

public class DesktopRegistry {
    private static DesktopRegistry instance;
    private Map<String, DesktopFactory> factories = new HashMap<String, DesktopFactory>();

    public static DesktopRegistry getInstance() {
        if (instance == null) {
            instance = new DesktopRegistry();
        }

        return instance;
    }

    public void addDesktop(String name, DesktopFactory factory) {
        factories.put(name, factory);
    }

    public String[] getNames() {
        return factories.keySet().toArray(new String[factories.size()]);
    }

    public SoapUIDesktop createDesktop(String desktopType, Workspace workspace) {
        if (factories.containsKey(desktopType)) {
            return factories.get(desktopType).createDesktop(workspace);
        }

        return null;
    }
}
