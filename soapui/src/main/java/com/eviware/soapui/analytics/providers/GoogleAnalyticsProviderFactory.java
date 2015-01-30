package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.analytics.AnalyticsProvider;
import com.eviware.soapui.analytics.AnalyticsProviderFactory;

/**
 * Created by aleshin on 5/16/2014.
 */
public class GoogleAnalyticsProviderFactory implements AnalyticsProviderFactory {

    @Override
    public String getName() {
        return "Google Analytics Provider";
    }

    @Override
    public String getDescription() {
        return "Allow to track action with Google Analytics";
    }

    @Override
    public AnalyticsProvider allocateProvider() {
        return new GoogleAnalyticsProvider();
    }
}
