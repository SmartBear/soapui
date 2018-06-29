package com.eviware.soapui.analytics;

import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.preferences.UserPreferences;
import com.smartbear.analytics.api.UserIdentificationInformation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UniqueUserIdentifier implements UserIdentificationInformation {
    private String userId;
    private static UniqueUserIdentifier instance;
    private String email;
    private String name;

    private UniqueUserIdentifier(UserPreferences prefs) {
        userId = prefs.getAnalyticsUserId();
        if (StringUtils.isNullOrEmpty(userId)) {
            userId = UUID.randomUUID().toString();
            prefs.setAnalyticsUserId(userId);
        }
    }

    public static UniqueUserIdentifier initialize(UserPreferences prefs) {
       if (instance == null) {
            instance = new UniqueUserIdentifier(prefs);
        }
        return instance;
    }

    public static UniqueUserIdentifier getInstance() {
        if (instance == null) {
            return initialize(new UserPreferences());
        }
        return instance;
    }

    public Map<String, String> prepareUserProfile() {
        Map<String, String> props = new HashMap<>();

        props.put("userId", userId);
        if (StringUtils.hasContent(name)) {
            props.put("$name", name);
        }
        if (StringUtils.hasContent(email)) {
            props.put("$email", email);
        }

        return props;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }
}