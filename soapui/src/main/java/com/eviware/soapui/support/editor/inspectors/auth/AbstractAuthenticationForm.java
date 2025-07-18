/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;

/**
 *
 */
public abstract class AbstractAuthenticationForm {
    protected static final ColumnSpec LABEL_COLUMN = new ColumnSpec("left:72dlu");
    protected static final ColumnSpec RIGHTMOST_COLUMN = new ColumnSpec("5px");
    protected static final Color CARD_BORDER_COLOR = new Color(121, 121, 121);
    protected static final Color CARD_BACKGROUND_COLOR = new Color(228, 228, 228);
    protected static final int TOP_SPACING = 10;
    protected static final int GROUP_SPACING = 20;

    // Dark mode colors
    protected static final Color DARK_CARD_BORDER_COLOR = new Color(100, 100, 100);
    protected static final Color DARK_CARD_BACKGROUND_COLOR = new Color(60, 63, 65);

    public JPanel getComponent() {
        return buildUI();
    }

    protected abstract JPanel buildUI();

    protected void setBorderOnPanel(JPanel card) {
        boolean isDarkMode = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);
        Color borderColor = isDarkMode ? DARK_CARD_BORDER_COLOR : CARD_BORDER_COLOR;
        Color backgroundColor = isDarkMode ? DARK_CARD_BACKGROUND_COLOR : CARD_BACKGROUND_COLOR;

        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, borderColor),
                BorderFactory.createMatteBorder(10, 10, 10, 10, backgroundColor)));
    }

    protected void setBackgroundColorOnPanel(JPanel panel) {
        boolean isDarkMode = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);
        Color backgroundColor = isDarkMode ? DARK_CARD_BACKGROUND_COLOR : CARD_BACKGROUND_COLOR;
        panel.setBackground(backgroundColor);
    }

    protected void setBorderAndBackgroundColorOnPanel(JPanel panel) {
        boolean isDarkMode = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);
        Color borderColor = isDarkMode ? DARK_CARD_BORDER_COLOR : CARD_BORDER_COLOR;

        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        setBackgroundColorOnPanel(panel);
    }

    void initForm(SimpleBindingForm form) {
        // TODO We should pass the encodedCellConstrains string instead
        FormLayout formLayout = (FormLayout) form.getPanel().getLayout();
        formLayout.setColumnSpec(2, LABEL_COLUMN);
        formLayout.setColumnSpec(5, RIGHTMOST_COLUMN);
    }
}
