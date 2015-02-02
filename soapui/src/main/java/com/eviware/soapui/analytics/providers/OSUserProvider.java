package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.analytics.ActionDescription;
import com.eviware.soapui.analytics.OSUserDescription;
import com.eviware.soapui.analytics.UserInfoProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by avdeev on 02.02.2015.
 */
public class OSUserProvider extends BaseAnalyticsProvider implements UserInfoProvider{

    @Override
    public void trackUserInfo(OSUserDescription osUserDescription) {
        Map<String, Object> event = new HashMap<String, Object>();

        try {
            //KeenClient.client().addEvent(OPEN_SOURCE_USER_COLLECTION_NAME, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void trackAction(ActionDescription actionDescription) {
    }
}
