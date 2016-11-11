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

package com.eviware.soapui.impl.rest.panels.request;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;

public class TextPanelWithTopLabel extends JPanel {

    JLabel textLabel;
    JTextField textField;


    public TextPanelWithTopLabel(String label, String text, JTextField textField) {
        textLabel = new JLabel(label);
        this.textField = textField;
        textField.setText(text);
        setToolTipText(text);
        super.setLayout(new BorderLayout());
        super.add(textLabel, BorderLayout.NORTH);
        super.add(textField, BorderLayout.SOUTH);

    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        textField.setText(text);
        setToolTipText(text);
    }

    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
        textLabel.setToolTipText(text);
        textField.setToolTipText(text);
    }
}
