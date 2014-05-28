package com.eviware.soapui.analytics;

/**
 * Created by aleshin on 5/15/2014.
 */

public interface AnalyticsProviderFactory {
    public String getName();

    public String getDescription(); // Just in case we'll need any UI for provider selection

    public AnalyticsProvider allocateProvider();
}
