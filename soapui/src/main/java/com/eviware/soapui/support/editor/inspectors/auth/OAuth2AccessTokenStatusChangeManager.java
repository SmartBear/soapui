/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.google.common.base.Preconditions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Helper class used to subscribe to Access Token Status changes
 * <br/>
 * <b>Note!</b> You need to call <i>unregister()</i> when you are done with the manager to remove its internal property listener
 */
final class OAuth2AccessTokenStatusChangeManager implements PropertyChangeListener {
    OAuth2AccessTokenStatusChangeListener listener = null;

    public OAuth2AccessTokenStatusChangeManager(OAuth2AccessTokenStatusChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OAuth2Profile.ACCESS_TOKEN_STATUS_PROPERTY)) {
            OAuth2Profile.AccessTokenStatus status = (OAuth2Profile.AccessTokenStatus) evt.getNewValue();
            listener.onAccessTokenStatusChanged(status);
        }
    }

    /**
     * Start receiving Access Token Status change events
     */
    public void register() {
        Preconditions.checkNotNull(listener.getProfile(), "Could not get OAuth 2 profile from the listener");
        listener.getProfile().addPropertyChangeListener(this);
    }

    /**
     * Stop receiving Access Token Status change events.
     */
    public void unregister() {
        Preconditions.checkNotNull(listener.getProfile(), "Could not get OAuth 2 profile from the listener");
        listener.getProfile().removePropertyChangeListener(this);
    }
}
