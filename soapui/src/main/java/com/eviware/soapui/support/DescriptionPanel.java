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

package com.eviware.soapui.support;

import com.eviware.soapui.support.swing.GradientPanel;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;

public class DescriptionPanel extends GradientPanel {
    private JLabel titleLabel;
    private JLabel descriptionLabel;

    public DescriptionPanel(String title, String description, ImageIcon icon) {
        super(new BorderLayout());
        setBackground(UIManager.getColor("control"));
        setForeground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        descriptionLabel = new JLabel();
        setDescription(description);

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.add(descriptionLabel, BorderLayout.CENTER);
        innerPanel.setOpaque(false);

        if (title != null) {
            descriptionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            titleLabel = new JLabel("<html><div style=\"font-size: 9px\"><b>" + title + "</b></div></html>");
            innerPanel.add(titleLabel, BorderLayout.NORTH);
        }
        add(innerPanel, BorderLayout.CENTER);

        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            add(iconLabel, BorderLayout.EAST);
        }
    }

    public void setTitle(String title) {
        titleLabel.setText("<html><div style=\"font-size: 9px\"><b>" + title + "</b></div></html>");
    }

    public void setDescription(String description) {
        descriptionLabel.setText("<html><div style=\"font-size: 9px\">" + description + "</div></html>");
    }
}
