package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.settings.ProxySettings;
import com.smartbear.analytics.api.ProductInfo;
import com.smartbear.analytics.impl.SoapUIOSUserProvider;

import java.net.*;

public class OSUserProvider extends SoapUIOSUserProvider {

    public OSUserProvider(ProductInfo productInfo) {
        super(productInfo);
    }

    @Override
    protected HttpURLConnection initializeConnection(String connectionURL) {
        HttpURLConnection connection;
        try {
            URL url = new URL(connectionURL);
            String host = SoapUI.getSettings().getString(ProxySettings.HOST, "");
            int port = 0;

            try {
                port = Integer.parseInt(SoapUI.getSettings().getString(ProxySettings.PORT, "0"));
            } catch (NumberFormatException ex) {
            }

            if (SoapUI.getSettings().getBoolean(ProxySettings.ENABLE_PROXY, false) && host.compareTo("") != 0 && port != 0) {
                SocketAddress sa = new InetSocketAddress(host, port);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, sa);
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

            return connection;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
