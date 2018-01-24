package com.eviware.soapui.analytics;

import com.smartbear.analytics.AnalyticsManager;
import com.smartbear.analytics.OSUserDescription;

import java.util.Map;

public class Analytics {
    private static final String SOURCE_MODULE_PARAM_NAME = "SourceModule";
    private static final String PRODUCT_AREA_PARAM_NAME = "ProductArea";

    public static void trackAction(SoapUIActions action) {
        trackAction(action, null);
    }

    public static void trackAction(SoapUIActions action, String... args) {
        String[] updatedArgs = new String[((args != null) ? args.length : 0) + 4]; // 4 is an amount of "SourceModule" + value and ProductArea + Value
        updatedArgs[0] = SOURCE_MODULE_PARAM_NAME;
        updatedArgs[1] = (action.getModuleType() != null) ? action.getModuleType().toString() : "Any";
        updatedArgs[2] = PRODUCT_AREA_PARAM_NAME;
        updatedArgs[3] = action.getProductArea().getProductArea();
        for (int i = 0; i < ((args != null) ? args.length : 0); i++) {
            updatedArgs[i + 4] = args[i];
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

    public static void trackAction(AnalyticsManager.Category category, String actionName, Map<String, String> params) {
        com.smartbear.analytics.Analytics.getAnalyticsManager().trackAction(category, actionName, params);
    }

    public static void trackUserInfo(OSUserDescription osUserDescription) {
        com.smartbear.analytics.Analytics.getAnalyticsManager().trackUserInfo(osUserDescription);
    }

    public static void trackAction(String action) {
        com.smartbear.analytics.Analytics.trackAction(action);
    }
}
