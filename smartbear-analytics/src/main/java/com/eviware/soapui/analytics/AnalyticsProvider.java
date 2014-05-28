package com.eviware.soapui.analytics;

import com.eviware.soapui.SoapUI;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Map;

/**
 * Created by aleshin on 5/15/2014.
 */
public interface AnalyticsProvider {

    public void trackAction(ActionDescription actionDescription);

    public void trackError(Throwable error);

    public final static class ActionDescription {
        private final String sessionId;
        private final AnalyticsManager.ActionId actionId;
        private final String additionalData;
        private Map<String, String> params;

        public ActionDescription(String sessionId, AnalyticsManager.ActionId actionId, String additionalData, Map<String, String> params) {
            this.sessionId = sessionId;
            this.actionId = actionId;
            this.additionalData = additionalData;
            this.params = params;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public String getSessionId() {
            return sessionId;
        }

        public AnalyticsManager.ActionId getActionId() {
            return this.actionId;
        }

        public String getActionIdAsString() {
            return actionId.toString();
        }

        public String getAdditionalData() {
            return additionalData;
        }

        public String getParamsAsString() {
            if (params != null) {
                return params.toString();
            } else {
                return "";
            }
        }

        public String toString() {
            return String.format("Acton: %s, Additional data: %s", getActionIdAsString(), getAdditionalData());
        }

        public static final String getUserId() {
            try {
                NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                byte[] mac = network.getHardwareAddress();
                StringBuilder sb = new StringBuilder();
                for (byte aMac : mac) {
                    sb.append(String.format("%d", aMac));
                }
                return sb.toString();
            } catch (IOException e) {
                SoapUI.log.warn("Couldn't determine MAC address - returning empty String");
                return "";
            }
        }


    }
}
