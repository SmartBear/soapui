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

package com.eviware.soapui.support;

import com.eviware.soapui.SoapUI;
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
    private boolean isDarkmode;

    public DescriptionPanel(String title, String description, ImageIcon icon) {
        super(new BorderLayout());
        this.isDarkmode = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);
        descriptionLabel = new JLabel();
        JPanel innerPanel = new JPanel(new BorderLayout());
        Color darkBackground = new Color(43, 43, 43);

        if (this.isDarkmode) {
            setBackground(darkBackground);
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            descriptionLabel.setForeground(Color.WHITE);
            descriptionLabel.setBackground(darkBackground);
            innerPanel.setBackground(darkBackground);
            innerPanel.setOpaque(true);
        } else {
            setBackground(UIManager.getColor("control"));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            innerPanel.setOpaque(false);
        }

        setDescription(description);
        innerPanel.add(descriptionLabel, BorderLayout.CENTER);

        if (title != null) {
            descriptionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            if (this.isDarkmode) {
                titleLabel = new JLabel("<html><div style=\"font-size: 9px; color: white;\"><b>" + title + "</b></div></html>");
                titleLabel.setForeground(Color.WHITE);
                titleLabel.setBackground(darkBackground);
                titleLabel.setOpaque(true);
            } else {
                titleLabel = new JLabel("<html><div style=\"font-size: 9px\"><b>" + title + "</b></div></html>");
            }
            innerPanel.add(titleLabel, BorderLayout.NORTH);
        }
        add(innerPanel, BorderLayout.CENTER);

        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            if (this.isDarkmode) {
                iconLabel.setOpaque(true);
                iconLabel.setBackground(darkBackground);
            }
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
