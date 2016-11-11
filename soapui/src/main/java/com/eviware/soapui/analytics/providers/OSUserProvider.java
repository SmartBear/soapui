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
        sb.append("name=");
        sb.append(HttpUtils.urlEncodeWithUtf8(osUserDescription.getName()));
        sb.append("&email=");
        sb.append(HttpUtils.urlEncodeWithUtf8(osUserDescription.getEmail()));
        return sb.toString();
    }
}
