package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.config.AccessTokenStatusConfig;
import com.eviware.soapui.impl.rest.OAuth1Profile;
import com.eviware.soapui.support.MessageSupport;
import com.google.common.base.Preconditions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class OAuth1TokenStatusChangeManager implements PropertyChangeListener {
    private static final MessageSupport messages = MessageSupport.getMessages(com.eviware.soapui.support.editor.inspectors.auth.OAuth1TokenStatusChangeManager.class);
    OAuth1TokenStatusChangeListener listener = null;

    public OAuth1TokenStatusChangeManager(OAuth1TokenStatusChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OAuth1Profile.ACCESS_TOKEN_STATUS_PROPERTY)) {
            AccessTokenStatusConfig.Enum status = (AccessTokenStatusConfig.Enum) evt.getNewValue();
            listener.onAccessTokenStatusChanged(status);
        } else if (evt.getPropertyName().equals(OAuth1Profile.TOKEN_SECRET_STATUS_PROPERTY)) {
            AccessTokenStatusConfig.Enum status = (AccessTokenStatusConfig.Enum) evt.getNewValue();
            listener.onTokenSecretStatusChanged(status);
        }
    }

    /**
     * Start receiving Access Token Status change events
     */
    public void register() {
        Preconditions.checkNotNull(listener.getProfile(), messages.get("OAuth1TokenStatusChangeManager.Error.MissingProfile"));
        listener.getProfile().addPropertyChangeListener(this);
    }

    /**
     * Stop receiving Access Token Status change events.
     */
    public void unregister() {
        Preconditions.checkNotNull(listener.getProfile(), messages.get("OAuth1TokenStatusChangeManager.Error.MissingProfile"));
        listener.getProfile().removePropertyChangeListener(this);
    }
}
