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

import com.eviware.soapui.config.TimeUnitConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormRadioGroup;

import javax.swing.JButton;

import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenPosition;

public class OAuth2AdvancedOptionsDialog {
    public static final MessageSupport messages = MessageSupport.getMessages(OAuth2AdvancedOptionsDialog.class);
    private ExpirationTimeChooser expirationTimeComponent;
    private JButton refreshAccessTokenButton;

    public OAuth2AdvancedOptionsDialog(OAuth2Profile profile, JButton refreshAccessTokenButton) {
        this.refreshAccessTokenButton = refreshAccessTokenButton;
        expirationTimeComponent = new ExpirationTimeChooser(profile);
        XFormDialog dialog = ADialogBuilder.buildDialog(Form.class);

        dialog.getFormField(Form.ACCESS_TOKEN_EXPIRATION_TIME).setProperty("component", expirationTimeComponent);

        setAccessTokenOptions(profile, dialog);

        setRefreshAccessTokenOptions(profile, dialog);

        if (dialog.show()) {
            String accessTokenPosition = dialog.getValue(Form.ACCESS_TOKEN_POSITION);
            profile.setAccessTokenPosition(AccessTokenPosition.valueOf(accessTokenPosition));

            String refreshAccessTokenMethod = dialog.getValue(Form.AUTOMATIC_ACCESS_TOKEN_REFRESH);
            profile.setRefreshAccessTokenMethod(OAuth2Profile.RefreshAccessTokenMethods.valueOf(refreshAccessTokenMethod.toUpperCase()));

            String manualExpirationTime = expirationTimeComponent.getAccessTokenExpirationTime();
            TimeUnitConfig.Enum expirationTimeUnit = expirationTimeComponent.getAccessTokenExpirationTimeUnit();
            profile.setManualAccessTokenExpirationTime(manualExpirationTime);
            profile.setManualAccessTokenExpirationTimeUnit(expirationTimeUnit);

            if (expirationTimeComponent.manualExpirationTimeIsSelected()) {
                profile.setUseManualAccessTokenExpirationTime(true);
            } else {
                profile.setUseManualAccessTokenExpirationTime(false);
            }

            enableRefreshAccessTokenButton(profile);
        }
    }

    private void enableRefreshAccessTokenButton(OAuth2Profile profile) {
        boolean enabled = profile.getRefreshAccessTokenMethod() == OAuth2Profile.RefreshAccessTokenMethods.MANUAL
                && (!org.apache.commons.lang.StringUtils.isEmpty(profile.getRefreshToken()));
        refreshAccessTokenButton.setEnabled(enabled);
        refreshAccessTokenButton.setVisible(enabled);
    }

    private void setRefreshAccessTokenOptions(OAuth2Profile profile, XFormDialog dialog) {
        XFormRadioGroup refreshOptions = (XFormRadioGroup) dialog.getFormField(Form.AUTOMATIC_ACCESS_TOKEN_REFRESH);
        refreshOptions.setOptions(OAuth2Profile.RefreshAccessTokenMethods.values());
        refreshOptions.setValue(profile.getRefreshAccessTokenMethod().name());
    }

    private void setAccessTokenOptions(OAuth2Profile profile, XFormDialog dialog) {
        XFormRadioGroup accessTokenPositionField = (XFormRadioGroup) dialog.getFormField(Form.ACCESS_TOKEN_POSITION);

        // TODO We're explicity removing the BODY option. Why?
        AccessTokenPosition[] accessTokenPositions = new AccessTokenPosition[]{AccessTokenPosition.HEADER, AccessTokenPosition.QUERY};
        accessTokenPositionField.setOptions(accessTokenPositions);

        dialog.setValue(Form.ACCESS_TOKEN_POSITION, profile.getAccessTokenPosition().name());
    }

    @AForm(name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.OAUTH_ADVANCED_OPTIONS)
    public interface Form {
        @AField(description = "Form.AccessTokenPosition.Description", type = AField.AFieldType.RADIOGROUP)
        public final static String ACCESS_TOKEN_POSITION = messages.get("Form.AccessTokenPosition.Label");

        @AField(description = "Form.AutomaticRefreshAccessToken.Description", type = AField.AFieldType.RADIOGROUP)
        public final static String AUTOMATIC_ACCESS_TOKEN_REFRESH = messages.get("Form.AutomaticRefreshAccessToken.Label");

        @AField(description = "Form.AccessTokenExpirationTime.Description", type = AField.AFieldType.COMPONENT)
        public final static String ACCESS_TOKEN_EXPIRATION_TIME = messages.get("Form.AccessTokenExpirationTime.Label");
    }
}
