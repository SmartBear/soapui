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

package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Action for retrieving an OAuth2 access token using the values in the OAuth2Profile object.
 */
public class GetOAuthAccessTokenAction extends AbstractAction {
    private static final MessageSupport messages = MessageSupport.getMessages(GetOAuthAccessTokenAction.class);
    private OAuth2Profile target;

    public GetOAuthAccessTokenAction(OAuth2Profile target) {
        this.target = target;
        putValue(Action.NAME, messages.get("GetOAuthAccessTokenAction.Action.Name"));
        putValue(Action.SHORT_DESCRIPTION, messages.get("GetOAuthAccessTokenAction.Action.Description"));
    }

    public void actionPerformed(ActionEvent event) {
        try {
            getOAuthClientFacade().requestAccessToken(target);
        } catch (InvalidOAuthParametersException e) {
            UISupport.showErrorMessage(messages.get("GetOAuthAccessTokenAction.Error.InvalidParameters", e.getMessage()));
        } catch (Exception e) {
            SoapUI.logError(e, messages.get("GetOAuthAccessTokenAction.Error.RetrievingFailLog"));
            UISupport.showErrorMessage(messages.get("GetOAuthAccessTokenAction.Error.RetrievingFailMessage"));
        }
    }

    protected OAuth2ClientFacade getOAuthClientFacade() {
        return new OltuOAuth2ClientFacade();
    }
}
