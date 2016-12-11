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

import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.swing.JXButtonPanel;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.Action;
import javax.swing.JButton;

public class JButtonBar extends JXButtonPanel {
    private ButtonBarBuilder builder;
    private JButton defaultButton;

    public JButtonBar() {
        builder = new ButtonBarBuilder(this);
    }

    public void addActions(ActionList actions) {
        for (int c = 0; c < actions.getActionCount(); c++) {
            Action action = actions.getActionAt(c);

            if (!(action instanceof HelpActionMarker) && c == 0) {
                if (getComponentCount() == 0) {
                    builder.addGlue();
                } else {
                    builder.addUnrelatedGap();
                }
            }

            if (action == ActionSupport.SEPARATOR_ACTION) {
                builder.addUnrelatedGap();
            } else {
                if (c > 0) {
                    builder.addRelatedGap();
                }

                JButton button = new JButton(action);
                button.setName((String) action.getValue(Action.NAME));
                if (c == 0 || actions.getDefaultAction() == action) {
                    defaultButton = button;
                }

                if (action.getValue(Action.SMALL_ICON) != null) {
                    button.setText(null);
                }

                builder.addFixed(button);
            }

            if (action instanceof HelpActionMarker && c == 0) {
                builder.addGlue();
            }
        }
    }

    public JButton getDefaultButton() {
        return defaultButton;
    }
}
