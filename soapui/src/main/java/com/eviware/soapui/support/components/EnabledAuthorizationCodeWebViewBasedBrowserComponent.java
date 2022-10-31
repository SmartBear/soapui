package com.eviware.soapui.support.components;

import com.eviware.soapui.SoapUI;
import javafx.application.Platform;


class EnabledAuthorizationCodeWebViewBasedBrowserComponent extends EnabledWebViewBasedBrowserComponent {
    private static final String DEFAULT_ERROR_PAGE = "<html><body><h1>AuthorizationCode request failed</h1></body></html>";

    public String url;

    @Override
    public String getDefaultErrorPage()
    {
        return DEFAULT_ERROR_PAGE;
    }
    EnabledAuthorizationCodeWebViewBasedBrowserComponent(boolean addNavigationBar, PopupStrategy popupStrategy)
     {
        super(addNavigationBar,popupStrategy);
    }

    @Override
    public void navigate(final String url) {
        navigate(url, DEFAULT_ERROR_PAGE);
    }
    @Override
    public void navigate(final String url, String backupUrl) {
        if (SoapUI.isBrowserDisabled()) {
            return;
        }

        loadUrl(url);

        Platform.runLater(() -> getWebEngine().load(url));
    }

    private void loadUrl(final String url) {
        Platform.runLater(() -> getWebEngine().load(url));
        this.url = url;
    }

}
