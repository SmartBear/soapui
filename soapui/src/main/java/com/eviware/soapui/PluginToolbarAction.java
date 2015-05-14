/*
 * Copyright 2004-2015 SmartBear Software
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

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

public class PluginToolbarAction extends AbstractAction {


    private final SoapUIAction action;

    public PluginToolbarAction(SoapUIAction action, String iconPath, String description) {
        this.action = action;
        putValue(Action.SMALL_ICON, UISupport.createImageIcon(iconPath));
        putValue(Action.SHORT_DESCRIPTION, description);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        action.perform(SoapUI.getWorkspace(), null);
    }

    public SoapUIAction getAction() {
        return action;
    }
}
