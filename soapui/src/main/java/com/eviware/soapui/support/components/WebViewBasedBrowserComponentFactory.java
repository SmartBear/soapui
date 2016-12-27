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

import com.eviware.soapui.SoapUI;

/**
 * @author joel.jonsson
 */
public class WebViewBasedBrowserComponentFactory {
    /*
    This factory  resides in a separate class to avoid ClassNotFoundException when jfxrt.jar in unavailable
    and browser component is disabled.
     */
    public static WebViewBasedBrowserComponent createBrowserComponent(boolean addNavigationBar) {
        return createBrowserComponent(addNavigationBar, WebViewBasedBrowserComponent.PopupStrategy.INTERNAL_BROWSER_NEW_WINDOW);
    }

    public static WebViewBasedBrowserComponent createBrowserComponent(boolean addNavigationBar, WebViewBasedBrowserComponent.PopupStrategy popupStrategy) {
        if (SoapUI.isBrowserDisabled()) {
            return new DisabledWebViewBasedBrowserComponent();
        } else {
            return new EnabledWebViewBasedBrowserComponent(addNavigationBar, popupStrategy);
        }
    }
}
