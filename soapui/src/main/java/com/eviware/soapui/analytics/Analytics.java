package com.eviware.soapui.analytics;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for lazy developers
 *
 * Created by ole on 19/05/14.
 */

public final class Analytics {

    final public static void trackAction( String action )
    {
        SoapUI.getSoapUICore().getAnalyticsManager().trackAction( action );
    }

    final public static void  tackActiveScreen(String screenName){
        SoapUI.getSoapUICore().getAnalyticsManager().trackActiveScreen( screenName );
    }

    final public static void  tackError(String errorText){
        SoapUI.getSoapUICore().getAnalyticsManager().trackError( errorText );
    }

    final public static void trackAction( String action, String... args )
    {
        Map<String,String> params = new HashMap<String, String>();

        for( int c = 0; c < args.length; c+=2 ) {
            if(StringUtils.hasContent( args[c]) && StringUtils.hasContent(args[c+1]))
                params.put(args[c], args[c + 1]);
        }

        SoapUI.getSoapUICore().getAnalyticsManager().trackAction( action, params );
    }

    final public static AnalyticsManager get()
    {
        return SoapUI.getSoapUICore().getAnalyticsManager();
    }
}
