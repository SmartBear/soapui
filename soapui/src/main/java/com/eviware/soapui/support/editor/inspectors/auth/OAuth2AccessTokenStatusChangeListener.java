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

import javax.annotation.Nonnull;

public interface OAuth2AccessTokenStatusChangeListener {
    /**
     * @param status The new Access Token status
     */
    void onAccessTokenStatusChanged(@Nonnull OAuth2Profile.AccessTokenStatus status);

    /**
     * @return The OAuth 2 profile associated with the listener
     */
    @Nonnull
    OAuth2Profile getProfile();
}
