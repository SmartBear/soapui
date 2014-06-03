/*
 * Copyright 2004-2014 SmartBear Software
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
package com.eviware.soapui.analytics;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;

public final class ActionDescription {

    private static final Logger log = Logger.getLogger(ActionDescription.class);

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
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append(": ").append(entry.getValue());
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    public String toString() {
        return String.format("Action: %s, Additional data: %s", getActionIdAsString(), getAdditionalData());
    }

    public static final String getUserId() throws UnknownHostException, SocketException {
        NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        byte[] mac = network.getHardwareAddress();
        StringBuilder sb = new StringBuilder();
        for (byte aMac : mac) {
            sb.append(String.format("%d", aMac));
        }
        return sb.toString();
    }


}
