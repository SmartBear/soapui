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
