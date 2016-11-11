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

package com.eviware.soapui.ui;

import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponentFactory;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;

public class URLDesktopPanel extends DefaultDesktopPanel {
    private WebViewBasedBrowserComponent browser;
    private boolean closed;

    public URLDesktopPanel(String title, String description, String url) throws InterruptedException,
            InvocationTargetException {
        super(title, description, new JPanel(new BorderLayout()));

        JPanel panel = (JPanel) getComponent();

        browser = WebViewBasedBrowserComponentFactory.createBrowserComponent(false, WebViewBasedBrowserComponent.PopupStrategy.EXTERNAL_BROWSER);

        //browser.addJavaScriptEventHandler("templateProjectCreator", new TemplateProjectCreator());

        panel.add(browser.getComponent(), BorderLayout.CENTER);

        if (StringUtils.hasContent(url)) {
            navigate(url, null, true);
        }
    }

    public void navigate(String url, String errorUrl, boolean async) {
        if (async) {
            SwingUtilities.invokeLater(new Navigator(url, errorUrl));
        } else {
            browser.navigate(url);
        }
    }

    public boolean onClose(boolean canCancel) {
        browser.close(true);
        closed = true;
        return super.onClose(canCancel);
    }

    public boolean isClosed() {
        return closed;
    }

    protected WebViewBasedBrowserComponent getBrowser() {
        return browser;
    }

    private class Navigator implements Runnable {
        private final String url;
        private final String errorUrl;

        public Navigator(String url, String errorUrl) {
            this.url = url;
            this.errorUrl = errorUrl;
        }

        public void run() {

            browser.navigate(url);

        }
    }

}
