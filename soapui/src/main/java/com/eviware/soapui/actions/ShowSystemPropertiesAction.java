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

import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ShowSystemPropertiesAction extends AbstractAction {
    public ShowSystemPropertiesAction() {
        super("System Properties");
        putValue(Action.SHORT_DESCRIPTION, "Shows the current systems properties");
    }

    public void actionPerformed(ActionEvent e) {
        StringBuffer buffer = new StringBuffer();
        Properties properties = System.getProperties();

        List<String> keys = new ArrayList<String>();
        for (Object key : properties.keySet()) {
            keys.add(key.toString());
        }

        Collections.sort(keys);

        String lastKey = null;

        for (String key : keys) {
            if (lastKey != null) {
                if (!key.startsWith(lastKey)) {
                    buffer.append("\r\n");
                }
            }

            int ix = key.indexOf('.');
            lastKey = ix == -1 ? key : key.substring(0, ix);

            buffer.append(key).append('=').append(properties.get(key)).append("\r\n");
        }

        UISupport.showExtendedInfo("System Properties", "Current system properties", "<html><body><pre><font size=-1>"
                + buffer.toString() + "</font></pre></body></html>", new Dimension(600, 400));
    }
}
