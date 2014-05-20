package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.analytics.AnalyticsProvider;
import com.eviware.soapui.analytics.AnalyticsProviderFactory;

/**
 * Created by ole on 19/05/14.
 */
public class NullAnalyticsProvider implements AnalyticsProvider {
    @Override
    public final void trackAction(ActionDescription actionDescription) {
    }

    public static class NullAnalyticsProviderFactory implements AnalyticsProviderFactory {
        @Override
        public String getName() {
            return "Null Provider";
        }

        @Override
        public String getDescription() {
            return "Discards all tracking data";
        }

        @Override
        public AnalyticsProvider allocateProvider() {
            return new NullAnalyticsProvider();
        }
    }
}
