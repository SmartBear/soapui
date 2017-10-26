package com.eviware.soapui.analytics;

import com.eviware.soapui.support.preferences.UserPreferences;
import com.smartbear.analytics.api.UserIdentificationInformation;

import java.util.UUID;

public class UniqueUserIdentifier implements UserIdentificationInformation {
    private String userId = "";
    private static UniqueUserIdentifier instance;

    private UniqueUserIdentifier(UserPreferences prefs) {
        userId = prefs.getAnalyticsUserId();
        if (userId == null || userId.length() == 0) {
            userId = UUID.randomUUID().toString();
            prefs.setAnalyticsUserId(userId);
        }
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public static UniqueUserIdentifier initialize(UserPreferences prefs) {
        if (instance == null) {
            instance = new UniqueUserIdentifier(prefs);
        }
        return instance;
    }

    public static UniqueUserIdentifier getInstance() {
        return instance;
    }
}