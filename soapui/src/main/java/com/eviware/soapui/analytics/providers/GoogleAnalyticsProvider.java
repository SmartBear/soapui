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

import com.eviware.soapui.analytics.ActionDescription;
import com.eviware.soapui.analytics.AnalyticsManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import static com.eviware.soapui.impl.support.HttpUtils.urlEncodeWithUtf8;

/**
 * Created by Dmitry N. Aleshin on 5/16/2014.
 */
public class GoogleAnalyticsProvider extends BaseAnalyticsProvider {
    private static final Logger log = Logger.getLogger(GoogleAnalyticsProvider.class);

    private static final String GA_ID = "UA-92447-22";
    private static final String CATEGORY_SESSION = "Session";
    private static final String CATEGORY_ACTION = "Action";
    private static final String CATEGORY_PGU_IN = "Plug-in";
    private static final String CATEGORY_INVALID = "[Unknown]";
    private static final String EVENT_START = "Start";
    private static final String EVENT_STOP = "Stop";
    private static final String EVENT_CUSTOM = "Custom";
    private static final String EVENT_INVALID = "[Unknown]";

    private static final String GA_URL = "http://www.google-analytics.com/collect";
    private static final String SoapUI_REVISION = "SoapUI-OS";

    @Override
    public void trackAction(ActionDescription actionDescription) {
        try {
            if (AnalyticsManager.Category.LICENSE_UPDATE == actionDescription.getCategory()) {
                return;
            }
            sendRecord(GA_URL, buildParametersString(actionDescription));
            trackActiveScreen(actionDescription.getActionName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void trackError(Throwable error) {

        try {
            String errorParametersString = String.format("v=1&an=%s&av=%s&cd=%s&tid=%s&cid=%s&t=exception&exd=%s&exf=%s&aip=1",
                    urlEncodeWithUtf8(SoapUI_REVISION), urlEncodeWithUtf8(getSoapUIVersion()), "undefined",
                    GA_ID, getMacAddressString(),
                    urlEncodeWithUtf8(error.getLocalizedMessage()),
                    "0" // Unable to determine if an exception was handled correctly or not
            );
            sendRecord(GA_URL, errorParametersString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    String buildParametersString(ActionDescription actionDescription) throws SocketException, UnknownHostException {
        String gaParametersString = String.format("v=1&an=%s&av=%s&cd=%s&tid=%s&cid=%s&t=event&ec=%s&ea=%s&el=%s&ev=1&sr=%s&cm1=%s&aip=1",
                urlEncodeWithUtf8(SoapUI_REVISION), urlEncodeWithUtf8(getSoapUIVersion()), "undefined",
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
                        urlEncodeWithUtf8(SoapUI_REVISION + "/" + getSoapUIVersion() + " (" + getOsName() + " " + getOsVersion() + ")");
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
                    urlEncodeWithUtf8(SoapUI_REVISION), urlEncodeWithUtf8(getSoapUIVersion()), "undefined",
                    GA_ID, sb.toString(), urlEncodeWithUtf8(screenName),
                    getStrScreenSize()
            );

            sendRecord(GA_URL, gaParametersString);
        } catch (Exception e) {
            e.printStackTrace();
            return;
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
