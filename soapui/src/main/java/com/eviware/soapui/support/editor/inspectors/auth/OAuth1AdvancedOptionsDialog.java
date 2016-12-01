package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.config.AccessTokenPositionConfig;
import com.eviware.soapui.impl.rest.OAuth1Profile;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormRadioGroup;

public class OAuth1AdvancedOptionsDialog {
    private static final MessageSupport messages = MessageSupport.getMessages(OAuth1AdvancedOptionsDialog.class);

    public OAuth1AdvancedOptionsDialog(OAuth1Profile profile) {
        XFormDialog dialog = ADialogBuilder.buildDialog(Form.class);

        setAccessTokenOptions(profile, dialog);

        if (dialog.show()) {
            String accessTokenPosition = dialog.getValue(Form.ACCESS_TOKEN_POSITION);
            profile.setAccessTokenPosition(AccessTokenPositionConfig.Enum.forString(accessTokenPosition));
        }
    }

    private void setAccessTokenOptions(OAuth1Profile profile, XFormDialog dialog) {
        XFormRadioGroup accessTokenPositionField = (XFormRadioGroup) dialog.getFormField(Form.ACCESS_TOKEN_POSITION);

        AccessTokenPositionConfig.Enum[] accessTokenPositions = new AccessTokenPositionConfig.Enum[]{
                AccessTokenPositionConfig.HEADER,
                AccessTokenPositionConfig.QUERY};
        accessTokenPositionField.setOptions(accessTokenPositions);

        dialog.setValue(Form.ACCESS_TOKEN_POSITION, profile.getAccessTokenPosition().toString());
    }

    @AForm(name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.OAUTH_ADVANCED_OPTIONS)
    public interface Form {
        @AField(description = "Form.AccessTokenPosition.Description", type = AField.AFieldType.RADIOGROUP)
        public final static String ACCESS_TOKEN_POSITION = messages.get("Form.SendAuthParameters.Label");

    }
}
