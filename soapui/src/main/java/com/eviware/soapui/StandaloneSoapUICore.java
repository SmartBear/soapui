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
import com.formdev.flatlaf.FlatDarkLaf;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyBluer;

import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import java.awt.Color;
import java.awt.Insets;

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
                boolean isDarkmode = getSettings().getBoolean("UISettings.DARK_MODE", false);
                
                SoapUITheme theme = new SoapUITheme();

                // Dark mode customization
                if (isDarkmode) {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    UIManager.put("TabbedPane.tabAreaInsets", new Insets(3, 2, 0, 0));
                    UIManager.put("TabbedPane.unselectedBackground", Color.DARK_GRAY);
                    UIManager.put("TabbedPane.selected", Color.BLACK);
                    UIManager.put("Button.background", Color.DARK_GRAY);
                    UIManager.put("Panel.background", Color.BLACK);
                    UIManager.put("Label.foreground", Color.WHITE);
                    UIManager.put("CheckBox.background", Color.DARK_GRAY);
                } else {
                    PlasticXPLookAndFeel.setCurrentTheme(theme);
                    PlasticXPLookAndFeel.setTabStyle("Metal");
                    UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
                    UIManager.put("TabbedPane.tabAreaInsets", new Insets(3, 2, 0, 0));
                    UIManager.put("TabbedPane.unselectedBackground", new Color(220, 220, 220));
                    UIManager.put("TabbedPane.selected", new Color(240, 240, 240));
                    PlasticXPLookAndFeel.setPlasticTheme(theme);
                }

            }
        } catch (Exception e) {
            SoapUI.logError(e, "Error initializing Look and Feel");
        }
    }

    /**
     * Adapted theme for SoapUI Look and Feel
     *
     * @author ole.matzura
     */

    public static class SoapUITheme extends SkyBluer {
        private static boolean isDarkmode = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);

        public static final Color BACKGROUND_COLOR = isDarkmode ? new Color(45, 45, 45) :  new Color(240, 240, 240);
        public static final Color MENU_BACKGROUND_COLOR = new Color(35, 35, 35);
        public static final Color MENU_ITEM_BACKGROUND_COLOR = new Color(50, 50, 50);
        public static final Color TEXT_COLOR = new Color(230, 230, 230);


        @Override
        public ColorUIResource getControl() {
            return new ColorUIResource(BACKGROUND_COLOR);
        }

        @Override
        public ColorUIResource getMenuBackground() {
            return isDarkmode ? new ColorUIResource(MENU_BACKGROUND_COLOR) : getControl();
        }

        @Override
        public ColorUIResource getMenuItemBackground() {
            return new ColorUIResource(isDarkmode ? MENU_ITEM_BACKGROUND_COLOR : new Color(248, 248, 248));
        }

        @Override
        public ColorUIResource getWindowBackground() {
            return isDarkmode ? new ColorUIResource(BACKGROUND_COLOR) : super.getWindowBackground();
        }

        // Override the correct methods for text color
        @Override
        public ColorUIResource getSystemTextColor() {
            return isDarkmode ? new ColorUIResource(TEXT_COLOR) : super.getSystemTextColor();
        }

        @Override
        public ColorUIResource getControlTextColor() {
            return isDarkmode ? new ColorUIResource(TEXT_COLOR) : super.getControlTextColor();
        }

        @Override
        public ColorUIResource getWindowTitleForeground() {
            return isDarkmode ? new ColorUIResource(TEXT_COLOR) : super.getWindowTitleForeground();
        }
    }
}
