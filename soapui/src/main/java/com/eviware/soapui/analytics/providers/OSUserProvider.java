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
import com.eviware.soapui.analytics.OSUserDescription;
import com.eviware.soapui.analytics.UserInfoProvider;
import com.eviware.soapui.impl.support.HttpUtils;

import java.util.Date;
import java.util.UUID;

import static com.eviware.soapui.analytics.AnalyticsManager.Category.SESSION_START;
import static com.eviware.soapui.analytics.AnalyticsManager.Category.SESSION_STOP;

/**
 * Created by avdeev on 02.02.2015.
 */
public class OSUserProvider extends BaseAnalyticsProvider implements UserInfoProvider {
    final private static String ANALYTICS_SERVER_URL = "https://analytics01.smartbear.com/open-source-analytics-server";
    final private static String USER_INFO_URL = ANALYTICS_SERVER_URL + "/analytics";
    final private static String SESSION_URL = ANALYTICS_SERVER_URL + "/session";
    private Date sessionStartTime;
    private String sessionId;

    @Override
    public void trackUserInfo(OSUserDescription osUserDescription) {
        String requestParams = prepareRequestParams(osUserDescription);
        sendRecord(USER_INFO_URL, requestParams);
    }

    @Override
    public void trackAction(ActionDescription actionDescription) {
        if (SESSION_START != actionDescription.getCategory() &&
                SESSION_STOP != actionDescription.getCategory()) {
            return;
        }
        StringBuilder jsonBuilder = new StringBuilder("{")
                .append("\"action\":\"").append(actionDescription.getCategory()).append("\",")
                .append("\"productVersion\":\"").append(getSoapUIVersion()).append("\",")
                .append("\"installId\":\"").append(getInstallId()).append("\",")
                .append("\n")
                .append("\"environment\": {")
                .append("\"osName\":\"").append(getOsName()).append("\",")
                .append("\"osVersion\":\"").append(getOsVersion()).append("\",")
                .append("\"userCountry\":\"").append(getUserCountry()).append("\",")
                .append("\"javaVersion\":\"").append(getJavaVersion()).append("\",")
                .append("\"screenSize\":\"").append(getStrScreenSize()).append("\",")
                .append("\"language\":\"").append(getUserLanguage()).append("\"")
                .append("},");
        switch (actionDescription.getCategory()) {
            case SESSION_START:
                sessionStartTime = new Date();
                sessionId = UUID.randomUUID().toString();
                break;

            case SESSION_STOP:
                jsonBuilder.append("\"endTime\": ").append(new Date());
                break;
        }

        jsonBuilder.append("\"sessionId\":\"").append(sessionId).append("\",")
                .append("\"startTime\":\"").append(sessionStartTime).append("\"")
                .append("}");

        String jsonString = jsonBuilder.toString();
        System.out.println(jsonString);
        sendRecord(SESSION_URL, jsonString);
    }

    private String prepareRequestParams(OSUserDescription osUserDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append("name=");
        sb.append(HttpUtils.urlEncodeWithUtf8(osUserDescription.getName()));
        sb.append("&email=");
        sb.append(HttpUtils.urlEncodeWithUtf8(osUserDescription.getEmail()));
        return sb.toString();
    }
}
