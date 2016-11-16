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

package com.eviware.soapui.settings.impl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.settings.ToolLocator;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.UISupport;

/**
 * Uses the SoapUI Settings to locate the specified tools
 *
 * @author ole.matzura
 */

public class SettingsToolLocatorImpl implements ToolLocator {
    public String getAntDir(boolean mandatory) {
        String antDir = SoapUI.getSettings().getString(ToolsSettings.ANT_LOCATION, null);
        if (mandatory && antDir == null) {
            UISupport.showErrorMessage("ANT 1.6.5 (or later) directory must be set in global preferences");
        }
        return antDir;
    }

    public String getJavacLocation(boolean mandatory) {
        String javac = SoapUI.getSettings().getString(ToolsSettings.JAVAC_LOCATION, null);
        if (mandatory && javac == null) {
            UISupport.showErrorMessage("javac location must be set in global preferences");
        }
        return javac;
    }
}
