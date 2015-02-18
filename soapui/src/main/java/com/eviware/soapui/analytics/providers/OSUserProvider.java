package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.ActionDescription;
import com.eviware.soapui.analytics.OSUserDescription;
import com.eviware.soapui.analytics.UserInfoProvider;
import com.eviware.soapui.settings.ProxySettings;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

/**
 * Created by avdeev on 02.02.2015.
 */
public class OSUserProvider extends BaseAnalyticsProvider implements UserInfoProvider{
    final private static String ANALYTICS_SERVER_URL = "https://analytics01.smartbear.com/open-source-analytics-server/analytics";

    @Override
    public void trackUserInfo(OSUserDescription osUserDescription) {
        String requestParams = prepareRequestParams(osUserDescription);
        sendRecord(ANALYTICS_SERVER_URL, requestParams);
    }

    @Override
    public void trackAction(ActionDescription actionDescription) {
    }

    private String prepareRequestParams (OSUserDescription osUserDescription){
        StringBuilder sb = new StringBuilder();
        sb.append("firstName=");
        sb.append(osUserDescription.getFirstName());
        sb.append("&lastName=");
        sb.append(osUserDescription.getLastName());
        sb.append("&email=");
        sb.append(osUserDescription.getEmail());
        return sb.toString();
    }
}
