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

package com.eviware.soapui.impl.wsdl.actions.support;

import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;

/**
 * Opens a URL in the external browser
 *
 * @author Ole.Matzura
 */

public class OpenUrlAction extends AbstractAction implements HelpActionMarker {
    private final String url;

    public OpenUrlAction(String title, String url) {
        this(title, url, null);
    }

    public OpenUrlAction(String title, String url, KeyStroke accelerator) {
        super(title);
        this.url = url;

        putValue(Action.SHORT_DESCRIPTION, title);
        if (accelerator != null) {
            putValue(Action.ACCELERATOR_KEY, accelerator);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (url == null) {
            UISupport.showErrorMessage("Missing url");
        } else {
            Tools.openURL(url);
        }
    }
}
