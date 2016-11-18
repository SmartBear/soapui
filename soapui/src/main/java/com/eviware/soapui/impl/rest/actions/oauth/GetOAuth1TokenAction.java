package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.OAuth1Profile;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

public class GetOAuth1TokenAction extends AbstractAction {
    private static final MessageSupport messages = MessageSupport.getMessages(GetOAuth1TokenAction.class);
    private final OAuth1Profile target;

    public GetOAuth1TokenAction(OAuth1Profile target, String name) {
        this.target = target;
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, messages.get("GetOAuth1TokenAction.Description"));
    }

    public void actionPerformed(ActionEvent event) {
        try {
            getOAuthClientFacade().requestAccessToken(target);
        } catch (InvalidOAuthParametersException e) {
            UISupport.showErrorMessage(messages.get("GetOAuth1TokenAction.Error.InvalidParameters") + e.getMessage());
        } catch (Exception e) {
            SoapUI.logError(e, messages.get("GetOAuth1TokenAction.Error.CommonError"));
            UISupport.showErrorMessage(messages.get("GetOAuth1TokenAction.Error.GUIMessage"));
        }
    }

    protected OAuth1ClientFacade getOAuthClientFacade() {
        return new GoogleOAuth1ClientFacade();
    }

}
