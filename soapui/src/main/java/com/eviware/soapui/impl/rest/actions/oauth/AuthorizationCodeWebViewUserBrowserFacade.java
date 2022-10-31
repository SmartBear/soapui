package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponentFactory;

public class AuthorizationCodeWebViewUserBrowserFacade extends WebViewUserBrowserFacade {

    private final WebViewBasedBrowserComponent browserComponent;

    public AuthorizationCodeWebViewUserBrowserFacade() {
        this(false);
    }

    public AuthorizationCodeWebViewUserBrowserFacade(boolean addNavigationBar) {
        browserComponent = WebViewBasedBrowserComponentFactory.createAuthorizationBrowserComponent(addNavigationBar);
    }

    @Override
    public WebViewBasedBrowserComponent getBrowserComponent() {
        return browserComponent;
    }
}
