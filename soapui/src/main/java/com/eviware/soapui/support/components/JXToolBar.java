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

import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

public class JXToolBar extends JToolBar {

    public static final int STANDARD_COMPONENT_HEIGHT = 18;
    public static final int MAIN_COMPONENT_HEIGHT = 50;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        paintChildren(g);
    }

    public <T extends JComponent> T addFixed(T component) {
        if (!(component instanceof JButton)) {
            UISupport.setPreferredHeight(component, STANDARD_COMPONENT_HEIGHT);
        }

        Dimension preferredSize = component.getPreferredSize();
        component.setMinimumSize(preferredSize);
        component.setMaximumSize(preferredSize);

        add(component);

        return component;
    }

    @Override
    public JButton add(Action a) {
        JButton b = super.add(a);
        b.setText((String) a.getValue(Action.NAME));
        addSpace(20);
        return b;
    }

    public Component add(Component component) {
        if (!(component instanceof AbstractButton)) {
            UISupport.setPreferredHeight(component, STANDARD_COMPONENT_HEIGHT);
        }

        return super.add(component);
    }

    public <T extends JComponent> T addWithOnlyMinimumHeight(T component) {
        if (!(component instanceof JButton)) {
            Dimension minimumSize = component.getMinimumSize();
            component.setMinimumSize(new Dimension(minimumSize.width, STANDARD_COMPONENT_HEIGHT));
        }
        super.add(component);
        return component;
    }

    public void addGlue() {
        add(Box.createHorizontalGlue());
    }

    public void addRelatedGap() {
        addSpace(3);
    }

    public void addUnrelatedGap() {
        addSeparator();
    }

    public void addLabeledFixed(String string, JComponent component) {
        addFixed(new JLabel(string));
        addSeparator(new Dimension(3, 3));
        addFixed(component);
    }

    public void addSpace(int i) {
        addSeparator(new Dimension(i, 1));
    }
}
