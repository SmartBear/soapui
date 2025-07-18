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

package com.eviware.soapui.support.components;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.SoapUI;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

@SuppressWarnings("serial")
public class JCollapsibleTablePanel extends JCollapsiblePanel {

    private JTable table;
    private String title;

    public JCollapsibleTablePanel(JTable table, String title) {
        super(new JPanel(), title);
        setTable(table);
        setMinusIcon(createAdaptiveIcon("/minus.gif"));
        setPlusIcon(createAdaptiveIcon("/plus.gif"));
        this.title = title;
    }

    /**
     * Creates an adaptive icon that changes color based on dark mode for better
     * visibility
     */
    private ImageIcon createAdaptiveIcon(String iconPath) {
        boolean isDarkMode = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);

        if (isDarkMode) {
            // In dark mode, create a light colored version of the icon
            ImageIcon originalIcon = UISupport.createImageIcon(iconPath);
            if (originalIcon != null) {
                return createLightColoredIcon(originalIcon);
            }
        }

        // Fallback to original icon for light mode
        return UISupport.createImageIcon(iconPath);
    }

    /**
     * Creates a light-colored version of an icon for better visibility in dark mode
     */
    private ImageIcon createLightColoredIcon(ImageIcon originalIcon) {
        Image img = originalIcon.getImage();
        int width = img.getWidth(null);
        int height = img.getHeight(null);

        if (width <= 0 || height <= 0) {
            return originalIcon; // Return original if dimensions are invalid
        }

        BufferedImage bufferedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Draw the original image first
        java.awt.Graphics2D g2d = bufferedImg.createGraphics();
        g2d.drawImage(img, 0, 0, null);

        // Apply a light overlay for dark mode visibility
        g2d.setComposite(java.awt.AlphaComposite.SrcAtop);
        g2d.setColor(new Color(220, 220, 220)); // Light gray color for dark mode
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();

        return new ImageIcon(bufferedImg);
    }

    private void setTable(JTable table) {
        this.table = table;
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel content = new JPanel(new BorderLayout());
        content.add(table, BorderLayout.CENTER);
        setContentPanel(content);
    }

    public JTable getTable() {
        return table;
    }

    public String getTitle() {
        return title;
    }
}
