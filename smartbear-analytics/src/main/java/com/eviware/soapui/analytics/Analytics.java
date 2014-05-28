package com.eviware.soapui.analytics;


import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for lazy developers
 * <p/>
 * Created by ole on 19/05/14.
 */

public final class Analytics {

    public static AnalyticsManager getAnalyticsManager() {
        return AnalyticsManager.getAnalytics();
    }

    public static void trackAction(String action) {
        getAnalyticsManager().trackAction(action);
    }

    public static void trackError(Throwable error) {
        getAnalyticsManager().trackError(error);
    }

    public static void trackAction(String action, String... args) {
        Map<String, String> params = new HashMap<String, String>();

        for (int c = 0; c < args.length; c += 2) {
            if (hasContent(args[c]) && hasContent(args[c + 1])) {
                params.put(args[c], args[c + 1]);
            }
        }

        getAnalyticsManager().trackAction(action, params);
    }

    private static boolean hasContent(String string) {
        return string != null && string.trim().length() > 0;
    }

}
