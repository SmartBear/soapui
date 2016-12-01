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

package com.eviware.soapui.actions;

import com.eviware.soapui.autoupdate.SoapUIAutoUpdaterUtils;
import com.eviware.soapui.autoupdate.SoapUIUpdateProvider;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

public class VersionUpdateAction extends AbstractAction {

    SoapUIUpdateProvider updateProvider = SoapUIAutoUpdaterUtils.getProvider();

    public VersionUpdateAction() {
        super("Check for updates");
        putValue(Action.SHORT_DESCRIPTION, "Checks if newer version is available");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateProvider.showUpdateStatus();
    }

}
