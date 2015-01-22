package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.ActionDescription;
import com.eviware.soapui.analytics.AnalyticsProvider;
import com.eviware.soapui.analytics.AnalyticsProviderFactory;
import com.eviware.soapui.support.log.JLogList;

/**
 * Created by ole on 04/06/14.
 */
public class LogTabAnalyticsProvider extends BaseAnalyticsProvider
{
    private final JLogList logList;

    public LogTabAnalyticsProvider()
    {
        logList = SoapUI.getLogMonitor().addLogArea( "Analytics", "com.smartbear.analytics", false );
    }

    @Override
    public void trackAction(ActionDescription actionDescription) {

        logList.addLine(actionDescription.toString() );
    }

    public static class LogTabAnalyticsProviderFactory implements AnalyticsProviderFactory
    {

        @Override
        public String getName() {
            return "Log Tab Analytics";
        }

        @Override
        public String getDescription() {
            return "Logs analytics events to a dedicated tab in SoapUI - for debugging - enable with -Dsoapui.analytics.logtab=true";
        }

        @Override
        public AnalyticsProvider allocateProvider() {
            return new LogTabAnalyticsProvider();
        }
    }
}
