package com.eviware.soapui.analytics;

import com.smartbear.analytics.AnalyticsManager;

public class Analytics {
    private static final String PRODUCT_AREA_PARAM_NAME = "ProductArea";

    public static void trackAction(SoapUIActions action) {
        trackAction(action, null);
    }

    public static void trackAction(SoapUIActions action, String... args) {
        String[] updatedArgs = new String[((args != null) ? args.length : 0) + 2]; // 2 is an amount of ProductArea + Value
        updatedArgs[0] = PRODUCT_AREA_PARAM_NAME;
        updatedArgs[1] = action.getProductArea().getProductArea();
        for (int i = 0; i < ((args != null) ? args.length : 0); i++) {
            updatedArgs[i + 2] = args[i];
        }
        com.smartbear.analytics.Analytics.trackAction(action.getCategory(), action.getActionName(), updatedArgs);
    }

    public static void trackAction(AnalyticsManager.Category analyticsCategory, SoapUIActions action, String... args) {
        com.smartbear.analytics.Analytics.trackAction(analyticsCategory, action.getActionName(), args);
    }

    public static void trackError(Throwable error) {
        com.smartbear.analytics.Analytics.trackError(error);
    }

    public static boolean trackSessionStart() {
        return com.smartbear.analytics.Analytics.trackSessionStart();
    }

    public static boolean trackSessionStop() {
        return com.smartbear.analytics.Analytics.trackSessionStop();
    }
}
