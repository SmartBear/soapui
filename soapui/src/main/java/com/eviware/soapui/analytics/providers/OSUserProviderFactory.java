package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.analytics.AnalyticsProvider;
import com.eviware.soapui.analytics.AnalyticsProviderFactory;

/**
 * Created by avdeev on 02.02.2015.
 */
public class OSUserProviderFactory implements AnalyticsProviderFactory {
    @Override
    public String getName() {
        return "OS User Contact Info Provider";
    }

    @Override
    public String getDescription() {
        return "Allow to track OS user contact information";
    }

    @Override
    public AnalyticsProvider allocateProvider() {
        return new OSUserProvider();
    }

}
