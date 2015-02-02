package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.analytics.AnalyticsProvider;
import com.eviware.soapui.analytics.AnalyticsProviderFactory;

/**
 * Created by aleshin on 5/16/2014.
 */
public class KeenIOProviderFactory implements AnalyticsProviderFactory {

    public KeenIOProviderFactory() {
    }

    @Override
    public String getName() {
        return "KeenIO Provider";
    }

    @Override
    public String getDescription() {
        return "Allow to track actions with KeenIO";
    }

    @Override
    public AnalyticsProvider allocateProvider() {
        return new KeenIOProvider();
    }
}
