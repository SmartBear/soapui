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

package com.eviware.soapui.impl.support.actions;

import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;

/**
 * Shows an online help page
 *
 * @author Ole.Matzura
 */

public class ShowOnlineHelpAction extends AbstractAction implements HelpActionMarker {
    private final String url;
    private String helpurl;

    public ShowOnlineHelpAction(String url) {
        this("Online Help", url, UISupport.getKeyStroke("F1"));
    }

    public ShowOnlineHelpAction(String title, String url) {
        this(title, url, null, null, null);
    }

    public ShowOnlineHelpAction(String title, String url, String description) {
        this(title, url, null, description, null);
    }

    public ShowOnlineHelpAction(String title, String url, String description, String iconPath) {
        this(title, url, null, description, iconPath);
    }

    public ShowOnlineHelpAction(String title, String url, KeyStroke accelerator) {
        this(title, url, accelerator, null);
    }

    public ShowOnlineHelpAction(String title, String url, KeyStroke accelerator, String description) {
        this(title, url, accelerator, description, null);
    }

    public ShowOnlineHelpAction(String title, String url, KeyStroke accelerator, String description, String iconPath) {
        super(title);
        this.url = url;
        putValue(Action.SHORT_DESCRIPTION, description == null ? "Show online help" : description);
        if (accelerator != null) {
            putValue(Action.ACCELERATOR_KEY, accelerator);
        }

        putValue(Action.SMALL_ICON, iconPath == null ? UISupport.HELP_ICON : UISupport.createImageIcon(iconPath));
    }

    public void actionPerformed(ActionEvent e) {

        Integer mods = e.getModifiers();
        String helpUrl = Tools.modifyUrl (url, mods);
        Tools.openURL(helpUrl);
    }
}
