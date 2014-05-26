package com.eviware.soapui.analytics;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUICore;
import com.eviware.soapui.support.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for lazy developers
 *
 * Created by ole on 19/05/14.
 */

public final class Analytics {

    final private static AnalyticsManager getAnalyticsManager(){
        SoapUICore soapUICore =  SoapUI.getSoapUICore();
        if (soapUICore != null)
            return soapUICore.getAnalyticsManager();
        else
            return AnalyticsManager.getAnalytics();
    }

    final public static void trackAction( String action )
    {
        getAnalyticsManager().trackAction( action );
    }

    final public static void  tackActiveScreen(String screenName){
        getAnalyticsManager().trackActiveScreen( screenName );
    }

    final public static void  tackError(String errorText){
        getAnalyticsManager().trackError( errorText );
    }

    final public static void trackAction( String action, String... args )
    {
        Map<String,String> params = new HashMap<String, String>();

        for( int c = 0; c < args.length; c+=2 ) {
            if(StringUtils.hasContent( args[c]) && StringUtils.hasContent(args[c+1]))
                params.put(args[c], args[c + 1]);
        }

        getAnalyticsManager().trackAction( action, params );
    }

    final public static AnalyticsManager get()
    {
        return getAnalyticsManager();
    }
}
