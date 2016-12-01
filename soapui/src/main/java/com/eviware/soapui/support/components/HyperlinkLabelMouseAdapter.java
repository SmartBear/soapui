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

package com.eviware.soapui.support.components;

import com.eviware.soapui.support.Tools;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class HyperlinkLabelMouseAdapter extends MouseAdapter {
    private final JComponent label;

    public HyperlinkLabelMouseAdapter(JTextComponent label) {
        this.label = label;
    }

    public HyperlinkLabelMouseAdapter(JLabel label) {
        this.label = label;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        String text = label instanceof JLabel ? ((JLabel) label).getText() : ((JTextComponent) label).getText();
        Tools.openURL(text);
    }
}
