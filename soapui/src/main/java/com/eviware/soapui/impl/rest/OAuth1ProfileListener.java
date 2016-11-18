package com.eviware.soapui.impl.rest;

public interface OAuth1ProfileListener {
    void profileAdded(OAuth1Profile profile);

    void profileRemoved(String profileName);

    void profileRenamed(String profileOldName, String newName);
}
