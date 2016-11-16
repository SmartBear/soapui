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

import com.eviware.soapui.support.DocumentListenerAdapter;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class JFriendlyTextField extends JTextField {
    private String helpString;
    private Color originalColor;
    private Color greyColor = new Color(170, 170, 170);
    private Boolean updateInProgress = false;

    private boolean isItOnlyHelpStr() {
        return helpString.equals(this.getText());
    }

    private void setHelpString() {
        setText(helpString);
        setFont(getFont().deriveFont(Font.ITALIC));
        setForeground(greyColor);
        select(0, 0);
        updateInProgress = false;
    }

    private void setCustomString(String customString) {
        setText(customString);
        setFont(getFont().deriveFont(Font.ROMAN_BASELINE));
        setForeground(originalColor);
        updateInProgress = false;
    }

    public JFriendlyTextField(final String helpString) {
        this.helpString = helpString;
        originalColor = getForeground();
        setToolTipText(helpString);
        setHelpString();
        setBorder(new LineBorder(greyColor, 1));

        this.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isItOnlyHelpStr()) {
                    select(0, 0);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (isItOnlyHelpStr()) {
                    select(0, 0);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isItOnlyHelpStr()) {
                    select(0, 0);
                }
            }
        });

        addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (e.getSource() instanceof JTextField) {
                    ((JTextField) e.getSource()).select(0, 0);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });

        this.addKeyListener(new KeyListener() {

            private void CommonKeyPress(KeyEvent e) {
                if (isItOnlyHelpStr() && (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_HOME || e.getKeyCode() == KeyEvent.VK_END || e.getKeyCode() == KeyEvent.VK_DELETE)) {
                    select(0, 0);
                    e.consume();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                CommonKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                CommonKeyPress(e);
            }
        });

        getDocument().addDocumentListener(new DocumentListenerAdapter() {
            @Override
            public void update(Document document) {
                try {
                    final String fullText = document.getText(0, document.getLength());
                    if (!updateInProgress) {
                        if ("".equals(fullText)) {
                            updateInProgress = true;
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    setHelpString();
                                }
                            });
                        } else if (fullText.contains(helpString)) {
                            final String fullTextTemp = fullText.replace(helpString, "");
                            if (!"".equals(fullTextTemp)) {
                                updateInProgress = true;
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        setCustomString(fullTextTemp);
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception Ex) {
                }
            }
        });
    }

}
