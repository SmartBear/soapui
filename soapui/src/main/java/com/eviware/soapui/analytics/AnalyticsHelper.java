/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.providers.OSUserProviderFactory;
import com.eviware.soapui.settings.UISettings;
import com.smartbear.analytics.AnalyticsManager;
import com.smartbear.analytics.api.AnalyticsProviderFactory;
import com.smartbear.analytics.impl.SoapUIOSMixpanelProviderFactory;

public class AnalyticsHelper {
    private static boolean initialized = false;

    public static void initializeAnalytics() {
        if (initialized) {
            return;
        }
        initialized = true;
        UniqueUserIdentifier userIdentifier = UniqueUserIdentifier.getInstance();
        AnalyticsManager manager = com.smartbear.analytics.Analytics.getAnalyticsManager();
        manager.setExecutorService(SoapUI.getThreadPool());
        SoapUIProductInfo productInfo = SoapUIProductInfo.getInstance();
        manager.registerAnalyticsProviderFactory(new OSUserProviderFactory(productInfo));
        if (SoapUI.getSettings().getBoolean(UISettings.DISABLE_ANALYTICS, false)) {
            return;
        }
        manager.registerAnalyticsProviderFactory(new SoapUIOSMixpanelProviderFactory(productInfo, userIdentifier, AnalyticsProviderFactory.HandleType.MANDATORY));
        manager.registerAnalyticsProviderFactory(new SoapUIOSMixpanelProviderFactory(productInfo, userIdentifier, AnalyticsProviderFactory.HandleType.USER_ALLOWED));
    }
}
