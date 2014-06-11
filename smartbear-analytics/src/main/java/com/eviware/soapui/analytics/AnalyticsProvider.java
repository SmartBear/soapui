package com.eviware.soapui.analytics;

/**
 * Created by aleshin on 5/15/2014.
 */
public interface AnalyticsProvider {

    public void trackAction(ActionDescription actionDescription);

    public void trackError(Throwable error);

}
