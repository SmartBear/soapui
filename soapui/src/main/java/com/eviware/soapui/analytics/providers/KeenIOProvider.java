package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.analytics.ActionDescription;
import com.eviware.soapui.analytics.OSUserDescription;
import com.eviware.soapui.analytics.UserInfoProvider;
import com.eviware.soapui.analytics.SoapUIActions;
import io.keen.client.java.KeenClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ole on 15/05/14.
 */
public class KeenIOProvider extends BaseAnalyticsProvider implements UserInfoProvider {

    private static final String KEEN_PROJECT_ID = "5374a299ce5e43174b000010";
    private static final String KEEN_WRITE_KEY = "f74cc3e7e02d0c0509d067077e14f63122114bf33c605c2b577d98b3655f5eb287f5d6077563b071e59dd0558e242ee554ed4ab775cbb2dbd21763c5e4fc7bbbc09360a14e0d2f15aa83c04772ca3fcad8ac8f170073db17c76d54e2adaaa62bcf2fd5444d1751c2009ac7786d61b56b";
    private static final String KEEN_READ_KEY = "4384c44dabf852a1498e4759699dbd6840fba4f71465ed8c684a4dddefe8906dd9a1d2bb5281831ef65e81fee75b6f0dbb324f48b3ba8777ca6e83e0310934cdad9fefefe3b03fa6126ca5a28ccf7aa763b11826f1514e73082b369e9c229ed5702498d894c745f31406cd08f28d898e";
    private static final String OPEN_SOURCE_USER_COLLECTION_NAME = "OSUsers";

    private long sessionStartTime;
    private Map<String, Integer> featureUsageCount;

    public KeenIOProvider() {
        KeenClient.initialize(KEEN_PROJECT_ID, KEEN_WRITE_KEY, KEEN_READ_KEY);
    }

    @Override
    public void trackUserInfo(OSUserDescription osUserDescription) {
        Map<String, Object> event = new HashMap<String, Object>();

        populateUserInfoWithData(osUserDescription, event);

        try {
            KeenClient.client().addEvent(OPEN_SOURCE_USER_COLLECTION_NAME, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void trackAction(ActionDescription actionDescription) {
        Map<String, Object> event = new HashMap<String, Object>();

        switch (actionDescription.getCategory()) {
            case SESSION_START:
                sessionStartTime = System.currentTimeMillis();
                featureUsageCount = new HashMap<String, Integer>();
                return;

            case SESSION_STOP:
                populateEventMapWithSessionData(actionDescription, event);
                sendEvent(KeenIOCollections.SESSIONS.toString().toLowerCase(), event);
                break;

            case ACTION:
                populateFeatureUsageCount(actionDescription);
                break;

            case LICENSE_UPDATE:
                populateEventMapWithActionData(actionDescription, event);
                sendEvent(KeenIOCollections.LicenseUpdate.toString(), event);
                break;

            default:
                break;

        }
    }

    private void populateFeatureUsageCount(ActionDescription actionDescription) {
        try {
            SoapUIActions action = SoapUIActions.getByActionName(actionDescription.getActionName());
            if (action != null) {
                String featureName = action.getFeature().getFeatureName();
                Integer usageCount = featureUsageCount.get(featureName);
                if (usageCount == null) {
                    usageCount = new Integer(0);
                }
                featureUsageCount.put(featureName, usageCount + 1);
            }
        } catch (Exception e) {
        }
    }

    private void populateEventMapWithActionData(ActionDescription actionDescription, Map<String, Object> event) {
        event.put("action", actionDescription.getActionName());
        event.put("session_id", actionDescription.getSessionId());

        Map<String, String> params = actionDescription.getParams();
        if (params != null) {
            for (String key : params.keySet()) {
                event.put(key, params.get(key));
            }
        }
    }

    private void sendEvent(String collectionName, Map<String, Object> event) {
        if (collectionName != null) {
            try {
                KeenClient.client().addEvent(collectionName, event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void populateEventMapWithSessionData(ActionDescription actionDescription, Map<String, Object> event) {
        event.put("license_type", getLicenseType());
        event.put("license_id", "");
        event.put("soapui_version", getSoapUIVersion());

        Map<String, Object> session = new HashMap<String, Object>();
        session.put("id", actionDescription.getSessionId());
        session.put("duration", String.valueOf((System.currentTimeMillis() - sessionStartTime) / 1000));
        session.put("start_time", new Date(sessionStartTime).toString());
        event.put("session", session);

        if (featureUsageCount != null) {
            event.put("features", featureUsageCount);
        }

        Map<String, Object> environment = new HashMap<String, Object>();
        environment.put("os_name", getOsName());
        environment.put("os_version", getOsVersion());
        environment.put("java_version", getJavaVersion());
        environment.put("screensize", getStrScreenSize());
        environment.put("user_country", getUserCountry());
        environment.put("user_language", getUserLanguage());

        event.put("environment", environment);
    }

    private void populateUserInfoWithData(OSUserDescription description, Map<String, Object> event) {
        event.put("FirstName", description.getFirstName());
        event.put("LastName", description.getLastName());
        event.put("EMail", description.getEmail());
        event.put("soapui_version", getSoapUIVersion());
    }

    enum KeenIOCollections {SESSIONS, ACTIONS, LicenseUpdate, FEATURES}
}