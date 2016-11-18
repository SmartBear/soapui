package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.impl.rest.OAuth1Profile;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth1Exception;
import org.apache.http.client.methods.HttpRequestBase;

public interface OAuth1ClientFacade {
    void requestAccessToken(OAuth1Profile profile) throws OAuth1Exception;

    void applyAccessToken(OAuth1Profile profile, HttpRequestBase request, String requestContent);
}
