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

package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.support.UISupport;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class StatisticsCollectionConfirmationDialog {
    public static int showDialog() {
        return JOptionPane.showConfirmDialog(null,
                getPanel(),
                "Usage statistics",
                JOptionPane.YES_NO_OPTION);
    }

    private static JPanel getPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Do you want to help us improve SoapUI by sending anonymous usage statistics?");
        JLabel labelEx = new JLabel("This can be turned off any time in UI settings.");
        panel.add(label);
        panel.add(labelEx);
        panel.add(UISupport.createLabelLink("http://www.soapui.org/Store-Info/privacy-policy.html", "Privacy policy"));

        return panel;
    }
}
