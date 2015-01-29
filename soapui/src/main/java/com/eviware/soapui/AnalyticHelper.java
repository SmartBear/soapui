package com.eviware.soapui;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.AnalyticsManager;
import com.eviware.soapui.analytics.providers.GoogleAnalyticsProviderFactory;
import com.eviware.soapui.analytics.providers.KeenIOProviderFactory;
import com.eviware.soapui.analytics.providers.LogTabAnalyticsProvider;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.UISettings;

import javax.swing.JOptionPane;

/**
 * Created by SmartBear company.
 */
public class AnalyticHelper {
    private static boolean isInitialize = false;

    private static boolean analyticsDisabled() {
        Settings settings = SoapUI.getSettings();
        boolean disableAnalytics = settings.getBoolean(UISettings.DISABLE_ANALYTICS, SoapUI.usingGraphicalEnvironment());
        if (!disableAnalytics) {
            return false;
        }
        Version optOutVersion = new Version(settings.getString(UISettings.ANALYTICS_OPT_OUT_VERSION, "0.0"));
        Version currentSoapUIVersion = new Version(SoapUI.SOAPUI_VERSION);
        if (!optOutVersion.getMajorVersion().equals(currentSoapUIVersion.getMajorVersion()) && SoapUI.usingGraphicalEnvironment()) {
            disableAnalytics = JOptionPane.showConfirmDialog(null, "Do you want to help us improve SoapUI by sending anonymous usage statistics?",
                    "Usage statistics", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION;
            settings.setBoolean(UISettings.DISABLE_ANALYTICS, disableAnalytics);
        }
        if (disableAnalytics) {
            settings.setString(UISettings.ANALYTICS_OPT_OUT_VERSION, currentSoapUIVersion.getMajorVersion());
        }
        return disableAnalytics;
    }

    public static void InitializeAnalytics() {
        if(isInitialize)
            return;
        isInitialize = true;
        AnalyticsManager manager = Analytics.getAnalyticsManager();
        manager.setExecutorService(SoapUI.getThreadPool());
        //We send non-anonymous (license) data to Keen IO anyway, even if user has opted out
        manager.registerAnalyticsProviderFactory(new KeenIOProviderFactory());
        if (analyticsDisabled()) {
            return;
        }

        manager.registerAnalyticsProviderFactory(new GoogleAnalyticsProviderFactory());
        if (System.getProperty("soapui.analytics.logtab", "false").equals("true")) {
            manager.registerAnalyticsProviderFactory(new LogTabAnalyticsProvider.LogTabAnalyticsProviderFactory());
        }
    }
}
