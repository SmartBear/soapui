/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

package com.eviware.soapui;

import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.ui.desktop.DesktopRegistry;
import com.eviware.soapui.ui.desktop.standalone.StandaloneDesktopFactory;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.ToolTipManager;
import javax.swing.UIManager;

public class StandaloneSoapUICore extends SwingSoapUICore {

    public StandaloneSoapUICore(boolean init) {
        super();

        if (init) {
            init(DEFAULT_SETTINGS_FILE);
        }
    }

    public StandaloneSoapUICore(String settingsFile) {
        super(null, settingsFile);

    }

    public StandaloneSoapUICore(boolean init, String soapUISettingsPassword) {
        super(true, soapUISettingsPassword);

        if (init) {
            init(DEFAULT_SETTINGS_FILE);
        }
    }

    @Override
    public void prepareUI() {
        super.prepareUI();

        initSoapUILookAndFeel();
        DesktopRegistry.getInstance().addDesktop(SoapUI.DEFAULT_DESKTOP, new StandaloneDesktopFactory());

        ToolTipManager.sharedInstance().setEnabled(!getSettings().getBoolean(UISettings.DISABLE_TOOLTIPS));
    }

    public void initSoapUILookAndFeel() {
        try {
            // Enabling native look & feel by default on Mac OS X
            if (UISupport.isMac()) {
                javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                getSettings().setBoolean(UISettings.NATIVE_LAF, true);
                log.info("Defaulting to native L&F for Mac OS X");
            } else if (getSettings().getBoolean(UISettings.NATIVE_LAF)) {
                javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
        } catch (Exception e) {
            SoapUI.logError(e, "Error initializing Look and Feel");
        }
    }
}
