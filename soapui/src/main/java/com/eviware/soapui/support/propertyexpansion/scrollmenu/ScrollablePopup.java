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

package com.eviware.soapui.support.propertyexpansion.scrollmenu;

import com.eviware.soapui.support.UISupport;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

/**
 * JMenu with the scrolling feature.
 */
public class ScrollablePopup extends JPopupMenu implements ScrollableMenuContainer {
    /**
     * How fast the scrolling will happen.
     */
    private int scrollSpeed = 20;
    /**
     * Handles the scrolling upwards.
     */
    private Timer timerUp;
    /**
     * Handles the scrolling downwards.
     */
    private Timer timerDown;
    /**
     * How many items are visible.
     */
    private int visibleItems;
    /**
     * Menuitem's index which is used to control if up and downbutton are visible
     * or not.
     */
    private int indexVisible = 0;
    /**
     * Button to scroll menu upwards.
     */
    private JButton upButton;
    /**
     * Button to scroll menu downwards.
     */
    private JButton downButton;
    /**
     * Container to hold submenus.
     */
    private Vector<JMenuItem> subMenus = new Vector<JMenuItem>();
    /**
     * Height of the screen.
     */
    private double screenHeight;
    /**
     * Height of the menu.
     */
    private double menuHeight;
    private int headerCount;
    private int footerCount;

    /**
     * Creates a new ScrollableMenu object with a given name.
     * <p/>
     * This also instantiates the timers and buttons. After the buttons are
     * created they are set invisible.
     *
     * @param name name to be displayed on the JMenu
     */
    public ScrollablePopup(String name) {
        super(name);

        timerUp = new Timer(scrollSpeed, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                scrollUp();
            }
        });
        timerDown = new Timer(scrollSpeed, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                scrollDown();
            }
        });

        screenHeight = 400;
        createButtons();
        hideButtons();
    }

    /**
     * JMenu's add-method is override to keep track of the added items. If there
     * are more items that JMenu can display, then the added menuitems will be
     * invisible. After that downscrolling button will be visible.
     *
     * @param menuItem to be added
     * @return added menuitem
     */
    public JMenuItem add(JMenuItem menuItem) {
        add(menuItem, subMenus.size() + headerCount + 1 + (headerCount == 0 ? 0 : 1));
        subMenus.add(menuItem);

        menuHeight += menuItem.getPreferredSize().getHeight();

        if (menuHeight > screenHeight) {
            menuItem.setVisible(false);
            downButton.setVisible(true);
        } else {
            visibleItems++;
        }

        return menuItem;
    }

    public Component add(Component comp) {
        if (comp instanceof JMenuItem) {
            return add((JMenuItem) comp);
        } else {
            return super.add(comp);
        }
    }

    public void removeAll() {
        super.removeAll();

        headerCount = 0;
        footerCount = 0;
        menuHeight = 0;
        indexVisible = 0;
        visibleItems = 0;

        subMenus.clear();

        add(upButton);
        add(downButton);
    }

    /**
     * When timerUp is started it calls constantly this method to make the JMenu
     * scroll upwards. When the top of menu is reached then upButton is set
     * invisible. When scrollUp starts downButton is setVisible.
     */
    private void scrollUp() {
        // closeOpenedSubMenus();

        if (indexVisible == 0) {
            upButton.setVisible(false);

            return;
        } else {
            indexVisible--;
            ((JComponent) subMenus.get(indexVisible + visibleItems)).setVisible(false);
            ((JComponent) subMenus.get(indexVisible)).setVisible(true);
            downButton.setVisible(true);
            if (indexVisible == 0) {
                upButton.setVisible(false);
            }
        }
    }

    /**
     * When timerDown is started it calls constantly this method to make the
     * JMenu scroll downwards. When the bottom of menu is reached then downButton
     * is set invisible. When scrolldown starts upButton is setVisible.
     */
    private void scrollDown() {
        // closeOpenedSubMenus();

        if ((indexVisible + visibleItems) == subMenus.size()) {
            downButton.setVisible(false);

            return;
        } else if ((indexVisible + visibleItems) > subMenus.size()) {
            return;
        } else {
            try {
                ((JComponent) subMenus.get(indexVisible)).setVisible(false);
                ((JComponent) subMenus.get(indexVisible + visibleItems)).setVisible(true);
                upButton.setVisible(true);
                indexVisible++;
                if ((indexVisible + visibleItems) == subMenus.size()) {
                    downButton.setVisible(false);
                }
            } catch (Exception eks) {
                eks.printStackTrace();
            }
        }
    }

    /**
     * Creates two button: upButton and downButton.
     */
    private void createButtons() {
        upButton = new JButton(UISupport.createImageIcon("/up_arrow.gif"));

        Dimension d = new Dimension(100, 20);
        upButton.setPreferredSize(d);
        upButton.setBorderPainted(false);
        upButton.setFocusPainted(false);
        upButton.setRolloverEnabled(true);

        class Up extends MouseAdapter {
            /**
             * When mouse enters over the upbutton, timerUp starts the scrolling
             * upwards.
             *
             * @param e
             *           MouseEvent
             */
            public void mouseEntered(MouseEvent e) {
                try {
                    timerUp.start();
                } catch (Exception ekas) {
                }
            }

            /**
             * When mouse exites the upbutton, timerUp stops.
             *
             * @param e
             *           MouseEvent
             */
            public void mouseExited(MouseEvent e) {
                try {
                    timerUp.stop();
                } catch (Exception ekas) {
                }
            }
        }

        MouseListener scrollUpListener = new Up();
        upButton.addMouseListener(scrollUpListener);

        add(upButton);
        downButton = new JButton(UISupport.createImageIcon("/down_arrow.gif"));
        downButton.setPreferredSize(d);
        downButton.setBorderPainted(false);
        downButton.setFocusPainted(false);

        class Down extends MouseAdapter {
            /**
             * When mouse enters over the downbutton, timerDown starts the
             * scrolling downwards.
             *
             * @param e
             *           MouseEvent
             */
            public void mouseEntered(MouseEvent e) {
                try {
                    timerDown.start();
                } catch (Exception ekas) {
                }
            }

            /**
             * When mouse exites the downbutton, timerDown stops.
             *
             * @param e
             *           MouseEvent
             */
            public void mouseExited(MouseEvent e) {
                try {
                    timerDown.stop();
                } catch (Exception ekas) {
                }
            }
        }

        MouseListener scrollDownListener = new Down();
        downButton.addMouseListener(scrollDownListener);
        add(downButton, 1 + subMenus.size());
    }

    /**
     * Hides the scrollButtons.
     */
    public void hideButtons() {
        upButton.setVisible(false);
        downButton.setVisible(false);
    }

    public JMenuItem addHeader(JMenuItem header) {
        add(header, headerCount);

        if (++headerCount == 1) {
            add(new JSeparator(), 1);
        }

        return header;
    }

    public JMenuItem addHeader(Action action) {
        return addHeader(new JMenuItem(action));
    }

    public JMenuItem addFooter(JMenuItem footer) {
        if (footerCount == 0) {
            add(new JSeparator(), subMenus.size() + headerCount + 2 + (headerCount == 0 ? 0 : 1));
        }

        add(footer, subMenus.size() + headerCount + footerCount + 3 + (headerCount == 0 ? 0 : 1));
        footerCount++;

        return footer;
    }

    public JMenuItem addFooter(Action action) {
        return addFooter(new JMenuItem(action));
    }
}
