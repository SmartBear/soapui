package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.ActionDescription;
import com.eviware.soapui.analytics.AnalyticsManager;
import com.eviware.soapui.settings.ProxySettings;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import static com.eviware.soapui.impl.support.HttpUtils.urlEncodeWithUtf8;

/**
 * Created by Dmitry N. Aleshin on 5/16/2014.
 */
public class GoogleAnalyticsProvider extends BaseAnalyticsProvider {
    private static final Logger log = Logger.getLogger(GoogleAnalyticsProvider.class);

    private static final String GA_ID = "UA-92447-17";
    private static final String CATEGORY_SESSION = "Session";
    private static final String CATEGORY_ACTION = "Action";
    private static final String CATEGORY_PGU_IN = "Plug-in";
    private static final String CATEGORY_INVALID = "[Unknown]";
    private static final String EVENT_START = "Start";
    private static final String EVENT_STOP = "Stop";
    private static final String EVENT_CUSTOM = "Custom";
    private static final String EVENT_INVALID = "[Unknown]";

    @Override
    public void trackAction(ActionDescription actionDescription) {
        try {
            if (AnalyticsManager.Category.LICENSE_UPDATE == actionDescription.getCategory()) {
                return;
            }
            sendRecord(buildParametersString(actionDescription));
            trackActiveScreen(actionDescription.getActionName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void trackError(Throwable error) {

        try {
            String errorParametersString = String.format("v=1&an=%s&av=%s&cd=%s&tid=%s&cid=%s&t=exception&exd=%s&exf=%s&aip=1",
                    urlEncodeWithUtf8("SoapUI-pro"), urlEncodeWithUtf8(getSoapUIVersion()), "undefined",
                    GA_ID, getMacAddressString(),
                    urlEncodeWithUtf8(error.getLocalizedMessage()),
                    "0" // Unable to determine if an exception was handled correctly or not
            );
            sendRecord(errorParametersString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    String buildParametersString(ActionDescription actionDescription) throws SocketException, UnknownHostException {
        String gaParametersString = String.format("v=1&an=%s&av=%s&cd=%s&tid=%s&cid=%s&t=event&ec=%s&ea=%s&el=%s&ev=1&sr=%s&cm1=%s&aip=1",
                urlEncodeWithUtf8("SoapUI-pro"), urlEncodeWithUtf8(getSoapUIVersion()), "undefined",
                GA_ID, getMacAddressString(),
                urlEncodeWithUtf8(getEventCategory(actionDescription)),
                urlEncodeWithUtf8(getEventAction(actionDescription)),
                urlEncodeWithUtf8(getEventLabel(actionDescription)),
                getStrScreenSize(),
                urlEncodeWithUtf8(actionDescription.getSessionId())
        );

        switch (actionDescription.getCategory()) {
            case SESSION_START:
                gaParametersString += "&sc=start&ua=" +
                        urlEncodeWithUtf8("SoapUI-pro/" + getSoapUIVersion() + " (" + getOsName() + " " + getOsVersion() + ")");
                break;

            case SESSION_STOP:
                gaParametersString += "&sc=end";
                break;
        }
        return gaParametersString;
    }

    private static String getMacAddressString() {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (byte aMac : mac) {
                sb.append(String.format("%d", aMac));
            }
            return sb.toString();
        } catch (IOException e) {
            log.warn("Couldn't determine MAC address - returning empty String");
            return "";
        }
    }

    private HttpURLConnection initializeConnection() {

        HttpURLConnection connection;

        try {
            URL url = new URL("http://www.google-analytics.com/collect");
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

    public void trackActiveScreen(String screenName) {
        String gaParametersString;
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%d", mac[i]));
            }
            gaParametersString = String.format("v=1&an=%s&av=%s&cd=%s&tid=%s&cid=%s&t=screenview&cd=%s&sr=%s",
                    urlEncodeWithUtf8("SoapUI-pro"), urlEncodeWithUtf8(getSoapUIVersion()), "undefined",
                    GA_ID, sb.toString(), urlEncodeWithUtf8(screenName),
                    getStrScreenSize()
            );

            sendRecord(gaParametersString);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private boolean sendRecord(String parameters) {

        if (parameters == null) {
            return false;
        }

        HttpURLConnection connection = initializeConnection();
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

    private String getEventCategory(ActionDescription actionDescription) {
        switch (actionDescription.getCategory()) {
            case SESSION_START:
            case SESSION_STOP:
                return CATEGORY_SESSION;
            case ACTION:
                return CATEGORY_ACTION;
            case CUSTOM_PLUGIN_ACTION:
                return CATEGORY_PGU_IN;
            default:
                return CATEGORY_INVALID;
        }
    }

    private String getEventAction(ActionDescription actionDescription) {
        switch (actionDescription.getCategory()) {
            case SESSION_START:
                return EVENT_START;
            case SESSION_STOP:
                return EVENT_STOP;
            case ACTION:
                return actionDescription.getActionName();
            case CUSTOM_PLUGIN_ACTION:
                return EVENT_CUSTOM;
            default:
                return EVENT_INVALID;
        }
    }

    private String getEventLabel(ActionDescription actionDescription) {
        switch (actionDescription.getCategory()) {
            case SESSION_START: {
                return String.format("%s %s, %s; License: %s", getOsName(), getOsVersion(), getStrScreenSize(), getLicenseDescription());
            }

            default: {
                return actionDescription.getParamsAsString();
            }
        }
    }

}
