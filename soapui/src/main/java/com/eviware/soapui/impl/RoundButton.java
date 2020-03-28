package com.eviware.soapui.impl;

import com.eviware.soapui.support.UISupport;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Graphics;

public class RoundButton extends JButton {

    protected int arc;
    private static boolean isMac = UISupport.isMac();

    public RoundButton(int arc) {
        this.arc = arc;
        if (isMac) {
            setUI(new BasicButtonUI());
            setBorder(BorderFactory.createEmptyBorder(2, 5, 3, 5));
        }
        setContentAreaFilled(false);
    }

    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getSize().width - 1, this.getSize().height - 1, arc, arc);
        super.paintComponent(g);
    }

    protected void paintBorder(Graphics g) {
        g.setColor(isMac ? getForeground() : new Color(39, 104, 158));
        g.drawRoundRect(0, 0, getSize().width - 1, this.getSize().height - 1, arc, arc);
    }
}