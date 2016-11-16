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

import java.awt.Component;

/**
 * @author joel.jonsson
 */
public interface WebViewBasedBrowserComponent {
    public enum PopupStrategy {
        INTERNAL_BROWSER_NEW_WINDOW, INTERNAL_BROWSER_REUSE_WINDOW, EXTERNAL_BROWSER, DISABLED
    }

    Component getComponent();

    void navigate(String url);

    void setContent(String contentAsString);

    void setContent(String contentAsString, String contentType);

    void close(boolean cascade);

    void addBrowserStateListener(BrowserListener listener);

    void removeBrowserStateListener(BrowserListener listener);

    void executeJavaScript(String script);

    /**
     * Provides a JavaScript object <i>memberName</i> when the current page is successfully loaded which can be used to call
     * the <i>eventHandler</i>.
     *
     * @see netscape.javascript.JSObject#setMember()
     */
    void addJavaScriptEventHandler(String memberName, Object eventHandler);
}
