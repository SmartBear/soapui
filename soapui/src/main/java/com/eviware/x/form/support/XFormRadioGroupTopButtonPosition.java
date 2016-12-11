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

package com.eviware.x.form.support;

import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class XFormRadioGroupTopButtonPosition extends XFormRadioGroup {

    public XFormRadioGroupTopButtonPosition(String[] values) {
        super(values);
    }

    public void addItem(Object value) {
        JRadioButton button = new JRadioButton(String.valueOf(value));
        button.setVerticalTextPosition(SwingConstants.TOP);

        button.setActionCommand(String.valueOf(value));
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fireValueChanged(e.getActionCommand(), null);
            }
        });

        getComponent().add(button);
        buttonGroup.add(button);
        models.put(String.valueOf(value), button.getModel());
        items.add(String.valueOf(value));
    }

}
