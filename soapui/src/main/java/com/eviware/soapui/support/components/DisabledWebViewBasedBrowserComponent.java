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

import com.eviware.soapui.impl.rest.actions.oauth.BrowserListener;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

/**
 * @author joel.jonsson
 */
class DisabledWebViewBasedBrowserComponent implements WebViewBasedBrowserComponent {
    private JPanel panel = new JPanel(new BorderLayout());

    DisabledWebViewBasedBrowserComponent() {
        JEditorPane browserDisabledPanel = new JEditorPane();
        browserDisabledPanel.setText("Browser component is disabled.");
        browserDisabledPanel.setEditable(false);
        panel.add(browserDisabledPanel);
        panel.setPreferredSize(new Dimension(300, 200));
    }

    @Override
    public Component getComponent() {
        return panel;
    }

    @Override
    public void navigate(String url) {
    }

    @Override
    public void setContent(String contentAsString) {
    }

    @Override
    public void setContent(String contentAsString, String contentType) {
    }

    @Override
    public void close(boolean cascade) {
    }

    @Override
    public void addBrowserStateListener(BrowserListener listener) {
    }

    @Override
    public void removeBrowserStateListener(BrowserListener listener) {
    }

    @Override
    public void executeJavaScript(String script) {
    }

    @Override
    public void addJavaScriptEventHandler(String memberName, Object eventHandler) {
    }
}
