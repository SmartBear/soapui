package com.eviware.soapui.analytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by Dmitry N. Aleshin on 5/15/2014.
 */

public class AnalyticsManager {

    private static AnalyticsManager instance = null;
    
    List<AnalyticsProvider> providers = new ArrayList<AnalyticsProvider>();
    private String sessionId;
    private List<AnalyticsProviderFactory> factories = new ArrayList<AnalyticsProviderFactory>();
    private Executor executorService;
    private static boolean disabled;

    AnalyticsManager() {

        Date date = new Date();
        String newString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        try {
            sessionId = String.format("AutoGeneratedSessionTimeStamp:%s:UserId:%s",
                    newString, ActionDescription.getUserId());
        }
        catch (Exception e){
            sessionId = String.format("AutoGeneratedSessionTimeStamp:%s", newString);
        }
    }

    public static AnalyticsManager getAnalytics() {
        if (instance == null) {
            instance = new AnalyticsManager();
        }
        return instance;
    }

    public static void disable() {
        disabled = true;
    }

    public void setExecutorService(Executor executorService) {
        this.executorService = executorService;
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


    private boolean trackAction(ActionId actionId, String actionName, Map<String, String> params) {

        if (disabled) {
            return false;
        }

        if (providers.isEmpty()) {
            return false;
        }

        final ActionDescription description = new ActionDescription(sessionId, actionId, actionName, params);

        runInBackground(new Runnable() {
            public void run() {
                for (AnalyticsProvider provider : providers) {
                    provider.trackAction(description);
                }
            }
        });

        return providers.size() > 0;
    }

    private void runInBackground(Runnable runnable) {
        if (executorService != null) {
            executorService.execute(runnable);
        } else {
            new Thread(runnable).start();
        }
    }

    public enum ActionId {SESSION_START, SESSION_STOP, ACTION}

}