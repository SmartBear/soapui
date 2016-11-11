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

package com.eviware.soapui.support.swing;

import com.eviware.soapui.support.UISupport;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;

/**
 * SplitPaneUI that draws nicer buttons and enables/disables them appropriately
 *
 * @author Ole.Matzura
 */

public class SoapUISplitPaneUI extends BasicSplitPaneUI {
    private boolean hasBeenDragged;
    private final static ImageIcon upArrow = UISupport.createImageIcon("/up_arrow.gif");
    private final static ImageIcon leftArrow = UISupport.createImageIcon("/left_arrow.gif");
    private final static ImageIcon rightArrow = UISupport.createImageIcon("/right_arrow.gif");
    private final static ImageIcon downArrow = UISupport.createImageIcon("/down_arrow.gif");

    public SoapUISplitPaneUI() {
        super();
    }

    protected void finishDraggingTo(int location) {
        super.finishDraggingTo(location);

        hasBeenDragged = true;
    }

    public void resetToPreferredSizes(JSplitPane jc) {
        super.resetToPreferredSizes(jc);
        hasBeenDragged = false;
    }

    public boolean hasBeenDragged() {
        return hasBeenDragged;
    }

    public void setHasBeenDragged(boolean hasBeenDragged) {
        this.hasBeenDragged = hasBeenDragged;
    }

    public BasicSplitPaneDivider createDefaultDivider() {
        return new SoapUIDivider(this);
    }

    public class SoapUIDivider extends BasicSplitPaneDivider {
        public SoapUIDivider(BasicSplitPaneUI ui) {
            super(ui);

            setLayout(new SoapUIDividerLayout());
        }

        protected JButton createLeftOneTouchButton() {
            if (getSplitPane().getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                JButton b = new JButton(upArrow);

                b.setMinimumSize(new Dimension(8, 6));
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setRequestFocusEnabled(false);
                b.setBorder(null);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                return b;
            } else {
                JButton b = new JButton(leftArrow);

                b.setMinimumSize(new Dimension(6, 8));
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setRequestFocusEnabled(false);
                b.setBorder(null);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                return b;
            }
        }

        protected JButton createRightOneTouchButton() {
            if (getSplitPane().getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                JButton b = new JButton(downArrow);

                b.setMinimumSize(new Dimension(8, 6));
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setRequestFocusEnabled(false);
                b.setBorder(null);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                return b;
            } else {
                JButton b = new JButton(rightArrow);

                b.setMinimumSize(new Dimension(6, 8));
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setRequestFocusEnabled(false);
                b.setBorder(null);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                return b;
            }
        }

        protected class SoapUIDividerLayout implements LayoutManager {
            private int lastOrientation;

            public SoapUIDividerLayout() {
                lastOrientation = getOrientation();
            }

            public void layoutContainer(Container c) {
                if (lastOrientation != getOrientation()) {
                    if (leftButton != null) {
                        leftButton.setIcon(getOrientation() == JSplitPane.VERTICAL_SPLIT ? upArrow : leftArrow);
                        leftButton.setMinimumSize(getOrientation() == JSplitPane.VERTICAL_SPLIT ? new Dimension(8, 6)
                                : new Dimension(6, 8));
                    }

                    if (rightButton != null) {
                        rightButton.setIcon(getOrientation() == JSplitPane.VERTICAL_SPLIT ? downArrow : rightArrow);
                        rightButton.setMinimumSize(getOrientation() == JSplitPane.VERTICAL_SPLIT ? new Dimension(8, 6)
                                : new Dimension(6, 8));
                    }

                    lastOrientation = getOrientation();
                }

                if (getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                    if (leftButton != null) {
                        leftButton.setBounds(2, 2, 8, 6);
                    }

                    if (rightButton != null) {
                        rightButton.setBounds(12, 2, 8, 6);
                    }
                } else {
                    if (leftButton != null) {
                        leftButton.setBounds(2, 2, 6, 8);
                    }

                    if (rightButton != null) {
                        rightButton.setBounds(2, 12, 6, 8);
                    }
                }
            }

            public Dimension preferredLayoutSize(Container c) {
                return minimumLayoutSize(c);
            }

            public void removeLayoutComponent(Component c) {
            }

            public void addLayoutComponent(String string, Component c) {
            }

            public Dimension minimumLayoutSize(Container parent) {
                return new Dimension(10, 10);
            }
        }

        public JButton getLeftButton() {
            return leftButton;
        }

        public JButton getRightButton() {
            return rightButton;
        }
    }

    public void setDividerLocation(JSplitPane jc, int location) {
        super.setDividerLocation(jc, location);
        enableOneTouchButtons(jc, location);
    }

    public void update(Graphics g, JComponent c) {
        super.update(g, c);
        enableOneTouchButtons(getSplitPane(), getSplitPane().getDividerLocation());
    }

    private void enableOneTouchButtons(JSplitPane jc, int location) {
        JButton leftButton = ((SoapUIDivider) getDivider()).getLeftButton();
        JButton rightButton = ((SoapUIDivider) getDivider()).getRightButton();

        if (leftButton != null) {
            leftButton.setEnabled(location > jc.getMinimumDividerLocation() && jc.getRightComponent() != null
                    && jc.getRightComponent().isVisible());
        }

        if (rightButton != null) {
            rightButton.setEnabled(location < jc.getMaximumDividerLocation() && jc.getLeftComponent() != null
                    && jc.getLeftComponent().isVisible());
        }
    }
}
