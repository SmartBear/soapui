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

package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.AnalyticsProvider;
import com.eviware.soapui.settings.ProxySettings;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

/**
 * Created by aleshin on 5/15/2014.
 */
public abstract class BaseAnalyticsProvider implements AnalyticsProvider {

    @Override
    public void trackError(Throwable error) {
    }

    public final String getOsName() {
        return System.getProperty("os.name", "n/a");
    }

    public final String getOsVersion() {
        return System.getProperty("os.version", "n/a");
    }

    public final String getJavaVersion() {
        return System.getProperty("java.version", "n/a");
    }

    public final String getUserLanguage() {
        return System.getProperty("user.language", "n/a");
    }

    public final String getUserCountry() {
        return System.getProperty("user.country", "n/a");
    }

    public final String getStrScreenSize() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        return String.format("%dx%d", (int) size.getWidth(), (int) size.getHeight());
    }

    public final Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public String getSoapUIVersion() {
        return SoapUI.SOAPUI_VERSION;
    }

    public String getLicenseType() {
        return "Open Source";
    }

    public String getLicenseDescription() {
        return "No License";
    }

    public String getInstanceId() {
        return "";
    }

    protected static String throwableToString(Throwable e) {
        StringWriter output = new StringWriter();
        e.printStackTrace(new PrintWriter(output));
        String stackTraceWithoutLineBreaks = output.toString().replaceAll("(\r|\n)+", " / ");
        return stackTraceWithoutLineBreaks.replaceAll("\\s+/\\s+", " / ");
    }

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

            return connection;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    protected boolean sendRecord(String connectionURL, String parameters) {

        if (parameters == null) {
            return false;
        }

        HttpURLConnection connection = initializeConnection(connectionURL);
        if (connection == null) {
            return false;
        }

        try {
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();
            return responseCode == 200;
            /* This code usefull for debugging. Do not delete
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return true;
            //*/
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
