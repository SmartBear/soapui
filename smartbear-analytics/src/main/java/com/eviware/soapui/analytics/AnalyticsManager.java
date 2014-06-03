package com.eviware.soapui.analytics;

import org.apache.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dmitry N. Aleshin on 5/15/2014.
 */

public class AnalyticsManager {
    private static final Logger log = Logger.getLogger(AnalyticsManager.class);


    private static AnalyticsManager instance = null;
    List<AnalyticsProvider> providers = new ArrayList<AnalyticsProvider>();

    private String sessionId;
    private List<AnalyticsProviderFactory> factories = new ArrayList<AnalyticsProviderFactory>();
    private static boolean disabled;

    AnalyticsManager() {
        String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        sessionId = makeUserId() + ":" + startTime;
    }

    public static AnalyticsManager getAnalytics() {
        if (instance == null) {
            instance = new AnalyticsManager();
        }
        return instance;
    }

    private static String makeUserId() {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();
            MessageDigest hasher = MessageDigest.getInstance("SHA-1");
            return DatatypeConverter.printHexBinary(hasher.digest(mac));
        } catch (Exception e) {
            log.warn("Error generating Analytics session ID - returning empty String", e);
            return "";
        }
    }


    public static void disable() {
        disabled = true;
    }

    public void trackAction(String action, Map<String, String> params) {
        trackAction(ActionId.ACTION, action, params);
    }

    public void trackError(Throwable error) {
        if (disabled) {
            return;
        }
        for (AnalyticsProvider provider : providers) {
            provider.trackError(error);
        }

    }

    public boolean trackAction(String actionName) {
        if (disabled) {
            return false;
        }
        return this.trackAction(ActionId.ACTION, actionName, null);
    }

    // Single param action
    public boolean trackAction(String actionName, String paramName, String value) {
        if (disabled) {
            return false;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put(paramName, value);
        return trackAction(ActionId.ACTION, actionName, params);
    }

    public boolean trackSessionStart() {
        return trackAction(ActionId.SESSION_START, "", null);
    }

    public boolean trackSessionStop() {
        return trackAction(ActionId.SESSION_STOP, "", null);
    }

    protected void registerActiveProvider(AnalyticsProvider provider, boolean keepTheOnlyOne) {
        if (keepTheOnlyOne) {
            providers.clear();
        }
        providers.add(provider);
    }

    public void registerAnalyticsProviderFactory(AnalyticsProviderFactory factory) {
        factories.add(factory);
        // if (factories.size() == 1) {
        registerActiveProvider(factory.allocateProvider(), false);
        // }
    }

    public boolean selectAnalyticsProvider(String name, boolean keepTheOnlyOne) {
        for (AnalyticsProviderFactory factory : factories) {
            if (factory.getName().compareToIgnoreCase(name) == 0) {
                registerActiveProvider(factory.allocateProvider(), keepTheOnlyOne);
                return true;
            }
        }
        if (keepTheOnlyOne) {
            // A way to stop logging
            providers.clear();
        }
        return false;
    }


    private boolean trackAction(ActionId category, String actionName, Map<String, String> params) {

        if (disabled) {
            return false;
        }

        if (providers.isEmpty()) {
            return false;
        }

        final ActionDescription description = new ActionDescription(sessionId, category, actionName, params);

        new Thread(new Runnable() {
            public void run() {
                for (AnalyticsProvider provider : providers) {
                    provider.trackAction(description);
                }
            }
        }).start();

        return providers.size() > 0;
    }

    public enum ActionId {SESSION_START, SESSION_STOP, ACTION}

}
