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

import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author joel.jonsson
 */
class WebViewNavigationBar {
    private final JComponent toolbar;

    private WebEngine webEngine;
    private EnabledWebViewBasedBrowserComponent webViewBasedBrowserComponent;

    private JTextField urlField;
    private ForwardAction forwardAction;
    private BackAction backAction;
    private Color originalFontColor;
    private String hintText = "Enter URL here";

    WebViewNavigationBar() {
        this.toolbar = createNavigationBar();
    }

    void initialize(final WebEngine webEngine, EnabledWebViewBasedBrowserComponent webViewBasedBrowserComponent) {
        this.webEngine = webEngine;
        this.webViewBasedBrowserComponent = webViewBasedBrowserComponent;

        webEngine.getHistory().currentIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldHistoryIndex, Number newHistoryIndex) {
                if (backAction != null) {
                    backAction.setEnabled(observableValue.getValue().intValue() > 0);
                }
                if (forwardAction != null) {
                    forwardAction.setEnabled(observableValue.getValue().intValue() < webEngine.getHistory().getEntries().size() - 1);
                }
            }
        });


        webEngine.locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldLocation,
                                final String newLocation) {
                if (urlField != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (StringUtils.hasContent(newLocation)) {
                                urlField.setText(newLocation);
                            }
                            resetTextFieldDefaults();
                            urlField.setFocusable(false);
                            urlField.setFocusable(true);
                        }
                    });
                }
            }
        });
    }

    public void focusUrlField() {
        urlField.requestFocus();
    }

    private JComponent createNavigationBar() {
        JXToolBar toolbar = UISupport.createToolbar();
        urlField = new JTextField();
        backAction = new BackAction();
        backAction.setEnabled(false);
        toolbar.add(backAction);
        forwardAction = new ForwardAction();
        forwardAction.setEnabled(false);
        toolbar.add(forwardAction);
        toolbar.add(new ReloadAction());
        toolbar.add(urlField);
        urlField.addActionListener(new UrlEnteredActionListener());
        urlField.setText(hintText);
        urlField.setFont(urlField.getFont().deriveFont(Font.ITALIC));
        originalFontColor = urlField.getForeground();
        urlField.setForeground(new Color(170, 170, 170));
        urlField.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            @Override
            public void update(Document document) {
                removeHintText();
                urlField.getDocument().removeDocumentListener(this);
            }
        });
        urlField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                removeHintText();
                urlField.removeMouseListener(this);
            }
        });
        return toolbar;
    }

    private void removeHintText() {

        String urlFieldText = urlField.getText();

        if (urlFieldText.contains(hintText)) {
            final String textWithOutHint = urlFieldText.replaceFirst(hintText, "");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    urlField.setText(textWithOutHint);
                }
            });
        }

        resetTextFieldDefaults();
    }

    private void resetTextFieldDefaults() {
        urlField.setFont(urlField.getFont().deriveFont(Font.PLAIN));
        urlField.setForeground(originalFontColor);
    }

    Component getComponent() {
        return toolbar;
    }

    private class BackAction extends AbstractAction {
        public BackAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/arrow_left.png"));
            putValue(Action.SHORT_DESCRIPTION, "Go back");
        }

        public void actionPerformed(ActionEvent e) {
            if (webEngine == null) {
                return;
            }
            final WebHistory history = webEngine.getHistory();
            if (history.getCurrentIndex() == 0) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        history.go(-1);
                    }
                });
            }
        }
    }

    private class ForwardAction extends AbstractAction {
        public ForwardAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/arrow_right.png"));
            putValue(Action.SHORT_DESCRIPTION, "Go forward");
        }

        public void actionPerformed(ActionEvent e) {
            if (webEngine == null) {
                return;
            }
            final WebHistory history = webEngine.getHistory();
            if (history.getCurrentIndex() >= history.getEntries().size() - 1) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        history.go(1);
                    }
                });
            }
        }
    }

    private class ReloadAction extends AbstractAction {
        public ReloadAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/reload_properties.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Reload page");
        }

        public void actionPerformed(ActionEvent e) {
            if (webEngine == null) {
                return;
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    webEngine.reload();
                }
            });
        }
    }

    private class UrlEnteredActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String url = urlField.getText();
            url = HttpUtils.completeUrlWithHttpIfProtocolIsMissing(url);
            webViewBasedBrowserComponent.navigate(url);
        }
    }
}
