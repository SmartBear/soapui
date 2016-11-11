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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.OAuth2FlowConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.actions.oauth.GetOAuthAccessTokenAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.PropertyComponent;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.value.AbstractValueModel;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class OAuth2GetAccessTokenForm implements OAuth2AccessTokenStatusChangeListener {
    public static final String CLIENT_ID_TITLE = "Client Identification";
    public static final String CLIENT_SECRET_TITLE = "Client Secret";
    public static final String RESOURCE_OWNER_LOGIN = "Resource Owner Name";
    public static final String RESOURCE_OWNER_PASSWORD = "Resource Owner Password";
    public static final String AUTHORIZATION_URI_TITLE = "Authorization URI";
    public static final String ACCESS_TOKEN_URI_TITLE = "Access Token URI";
    public static final String REDIRECT_URI_TITLE = "Redirect URI";
    public static final String SCOPE_TITLE = "Scope";
    public static final String OAUTH_2_FLOW_COMBO_BOX_NAME = "OAuth2Flow";
    public static final String ACCESS_TOKEN_FORM_DIALOG_NAME = "getAccessTokenFormDialog";

    private static final String GET_ACCESS_TOKEN_BUTTON_NAME = "getAccessTokenButtonName";
    private static final String ACCESS_TOKEN_FORM_DIALOG_TITLE = "Get Access Token";
    private static final String AUTOMATION_BUTTON_TITLE = "Automation...";

    private static final String GET_ACCESS_TOKEN_FORM_LAYOUT = "7dlu:none,left:pref,10dlu,left:pref,10dlu,left:MAX(112dlu;pref),7dlu";

    private static final int BOARDER_SPACING = 15;
    private static final int NORMAL_SPACING = 10;
    private static final int GROUP_SPACING = 20;

    private static final Color CARD_BORDER_COLOR = new Color(121, 121, 121);

    static final ImageIcon DEFAULT_ICON = null;

    private OAuth2Profile profile;
    private JLabel accessTokenStatusText;
    private OAuth2AccessTokenStatusChangeManager statusChangeManager;
    private JDialog accessTokenDialog;
    private OAuth2ScriptsDesktopPanel scriptEditorPanel;

    public OAuth2GetAccessTokenForm(OAuth2Profile profile) {
        this.profile = profile;
    }

    public JDialog getComponent() {
        SimpleBindingForm accessTokenForm = createSimpleBindingForm(profile);
        statusChangeManager = new OAuth2AccessTokenStatusChangeManager(this);
        populateGetAccessTokenForm(accessTokenForm);
        statusChangeManager.register();

        if (profile.getAccessTokenStatus() != OAuth2Profile.AccessTokenStatus.RETRIEVAL_CANCELED) {
            profile.resetAccessTokenStatusToStartingStatus();
        }
        setOAuth2StatusFeedback(profile.getAccessTokenStatus());

        accessTokenDialog = createGetAccessTokenDialog(accessTokenForm.getPanel());
        return accessTokenDialog;
    }

    @Override
    public void onAccessTokenStatusChanged(@Nonnull OAuth2Profile.AccessTokenStatus status) {
        setOAuth2StatusFeedback(status);
    }

    @Nonnull
    @Override
    public OAuth2Profile getProfile() {
        return profile;
    }

    void release() {
        statusChangeManager.unregister();
    }

    private SimpleBindingForm createSimpleBindingForm(OAuth2Profile profile) {
        PresentationModel presentationModel = new PresentationModel<OAuth2Profile>(profile);
        String columnsSpecs = GET_ACCESS_TOKEN_FORM_LAYOUT;
        Border border = BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1);
        return new SimpleBindingForm(presentationModel, columnsSpecs, border);
    }

    private void populateGetAccessTokenForm(SimpleBindingForm accessTokenForm) {
        accessTokenForm.addSpace(BOARDER_SPACING);

        accessTokenForm.appendHeadingAndHelpButton("Get Access Token from the authorization server", HelpUrls.OAUTH_ACCESS_TOKEN_RETRIEVAL);

        accessTokenForm.addSpace(NORMAL_SPACING);

        JComboBox oauth2FlowComboBox = appendOAuth2ComboBox(accessTokenForm);

        accessTokenForm.addSpace(GROUP_SPACING);

        final JTextField resOwnerPassTextField = accessTokenForm.appendTextField(OAuth2Profile.RESOURCE_OWNER_LOGIN_PROPERTY, RESOURCE_OWNER_LOGIN, "");
        resOwnerPassTextField.setVisible(oauth2FlowComboBox.getSelectedItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.RESOURCE_OWNER_PASSWORD_CREDENTIALS.toString())));

        final JTextField resOwnerNameTextField = accessTokenForm.appendTextField(OAuth2Profile.RESOURCE_OWNER_PASSWORD_PROPERTY, RESOURCE_OWNER_PASSWORD, "");
        resOwnerNameTextField.setVisible(oauth2FlowComboBox.getSelectedItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.RESOURCE_OWNER_PASSWORD_CREDENTIALS.toString())));

        accessTokenForm.appendTextField(OAuth2Profile.CLIENT_ID_PROPERTY, CLIENT_ID_TITLE, "");
        final JTextField clientSecretField = appendClientSecretField(accessTokenForm, getOAuth2FlowValueModel(accessTokenForm));

        accessTokenForm.addSpace(GROUP_SPACING);

        final JTextField authUriTextField = accessTokenForm.appendTextField(OAuth2Profile.AUTHORIZATION_URI_PROPERTY, AUTHORIZATION_URI_TITLE, "");
        authUriTextField.setVisible(!oauth2FlowComboBox.getSelectedItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.RESOURCE_OWNER_PASSWORD_CREDENTIALS.toString())) &&
                !oauth2FlowComboBox.getSelectedItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.CLIENT_CREDENTIALS_GRANT.toString())));
        final JTextField accessTokenUriField = appendAccessTokenUriField(accessTokenForm, getOAuth2FlowValueModel(accessTokenForm));

        final JTextField redirectUriTextField = accessTokenForm.appendTextField(OAuth2Profile.REDIRECT_URI_PROPERTY, REDIRECT_URI_TITLE, "");
        redirectUriTextField.setVisible(!oauth2FlowComboBox.getSelectedItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.RESOURCE_OWNER_PASSWORD_CREDENTIALS.toString())) &&
                !oauth2FlowComboBox.getSelectedItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.CLIENT_CREDENTIALS_GRANT.toString())));

        accessTokenForm.addSpace(GROUP_SPACING);

        accessTokenForm.appendTextField(OAuth2Profile.SCOPE_PROPERTY, SCOPE_TITLE, "");

        accessTokenForm.addSpace(NORMAL_SPACING);

        accessTokenForm.appendComponentsInOneRow(createGetAccessTokenButton(), createAccessTokenStatusText());
        accessTokenForm.appendButtonWithoutLabel(AUTOMATION_BUTTON_TITLE, new EditAutomationScriptsAction(profile));

        accessTokenForm.addSpace(GROUP_SPACING);

        accessTokenForm.appendLabelAsLink(HelpUrls.OAUTH_ACCESS_TOKEN_FROM_SERVER, "How to get an access token from an authorization server");

        accessTokenForm.addSpace(BOARDER_SPACING);

        oauth2FlowComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    clientSecretField.setVisible(!e.getItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.IMPLICIT_GRANT.toString())));
                    accessTokenUriField.setVisible(!e.getItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.IMPLICIT_GRANT.toString())));
                    authUriTextField.setVisible(!e.getItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.RESOURCE_OWNER_PASSWORD_CREDENTIALS.toString())) &&
                            !e.getItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.CLIENT_CREDENTIALS_GRANT.toString())));
                    redirectUriTextField.setVisible(!e.getItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.RESOURCE_OWNER_PASSWORD_CREDENTIALS.toString())) &&
                            !e.getItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.CLIENT_CREDENTIALS_GRANT.toString())));
                    resOwnerNameTextField.setVisible(e.getItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.RESOURCE_OWNER_PASSWORD_CREDENTIALS.toString())));
                    resOwnerPassTextField.setVisible(e.getItem().equals(OAuth2Profile.OAuth2Flow.valueOf(OAuth2FlowConfig.RESOURCE_OWNER_PASSWORD_CREDENTIALS.toString())));

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            accessTokenDialog.pack();
                        }
                    });
                }
            }
        });
    }

    private AbstractValueModel getOAuth2FlowValueModel(SimpleBindingForm accessTokenForm) {
        return accessTokenForm.getPresentationModel().getModel(OAuth2Profile.OAUTH2_FLOW_PROPERTY, "getOAuth2Flow", "setOAuth2Flow");
    }

    private JComboBox appendOAuth2ComboBox(SimpleBindingForm accessTokenForm) {
        AbstractValueModel valueModel = getOAuth2FlowValueModel(accessTokenForm);
        ComboBoxModel oauth2FlowsModel = new DefaultComboBoxModel(OAuth2Profile.OAuth2Flow.values());
        JComboBox oauth2FlowComboBox = accessTokenForm.appendComboBox("OAuth 2 Flow", oauth2FlowsModel, "OAuth 2 Authorization Flow", valueModel);
        oauth2FlowComboBox.setName(OAUTH_2_FLOW_COMBO_BOX_NAME);
        return oauth2FlowComboBox;
    }

    private JTextField appendClientSecretField(SimpleBindingForm accessTokenForm, AbstractValueModel valueModel) {
        final JTextField clientSecretField = accessTokenForm.appendTextField(OAuth2Profile.CLIENT_SECRET_PROPERTY, CLIENT_SECRET_TITLE, "");
        if (valueModel.getValue() == OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT) {
            clientSecretField.setVisible(false);
        }
        return clientSecretField;
    }

    private JTextField appendAccessTokenUriField(SimpleBindingForm accessTokenForm, AbstractValueModel valueModel) {
        final JTextField accessTokenUriField = accessTokenForm.appendTextField(OAuth2Profile.ACCESS_TOKEN_URI_PROPERTY, ACCESS_TOKEN_URI_TITLE, "");
        if (valueModel.getValue() == OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT) {
            accessTokenUriField.setVisible(false);
        }
        return accessTokenUriField;
    }

    private PropertyComponent createGetAccessTokenButton() {
        JButton getAccessTokenButton = new JButton(new GetOAuthAccessTokenAction(profile));
        getAccessTokenButton.setName(GET_ACCESS_TOKEN_BUTTON_NAME);
        return new PropertyComponent(getAccessTokenButton);
    }

    private PropertyComponent createAccessTokenStatusText() {
        accessTokenStatusText = new JLabel();
        return new PropertyComponent(accessTokenStatusText);
    }

    private JDialog createGetAccessTokenDialog(JPanel accessTokenFormPanel) {
        final JDialog accessTokenFormDialog = new JDialog();
        accessTokenFormDialog.setName(ACCESS_TOKEN_FORM_DIALOG_NAME);
        accessTokenFormDialog.setTitle(ACCESS_TOKEN_FORM_DIALOG_TITLE);
        accessTokenFormDialog.setIconImages(SoapUI.getFrameIcons());
        accessTokenFormDialog.setUndecorated(true);
        accessTokenFormDialog.getContentPane().add(accessTokenFormPanel);

        return accessTokenFormDialog;
    }

    private void setOAuth2StatusFeedback(OAuth2Profile.AccessTokenStatus status) {
        // There are no auth profile selected
        if (status == null) {
            setDefaultFeedback();
        } else {
            switch (status) {
                case WAITING_FOR_AUTHORIZATION:
                case RECEIVED_AUTHORIZATION_CODE:
                    setWaitingFeedback(status);
                    break;
                case RETRIEVAL_CANCELED:
                    setCanceledFeedback(status);
                    break;
                case ENTERED_MANUALLY:
                case RETRIEVED_FROM_SERVER:
                default:
                    setDefaultFeedback();
                    break;
            }
        }
    }

    private void setCanceledFeedback(OAuth2Profile.AccessTokenStatus status) {
        accessTokenStatusText.setText(status.toString());
        accessTokenStatusText.setIcon(OAuth2Form.FAIL_ICON);
    }

    private void setWaitingFeedback(OAuth2Profile.AccessTokenStatus status) {
        accessTokenStatusText.setText(status.toString());
        accessTokenStatusText.setIcon(OAuth2Form.WAIT_ICON);
    }

    private void setDefaultFeedback() {
        accessTokenStatusText.setText("");
        accessTokenStatusText.setIcon(DEFAULT_ICON);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                closeGetAccessTokenDialog();
            }
        });
    }

    private void closeGetAccessTokenDialog() {
        if (accessTokenDialog != null) {
            accessTokenDialog.setVisible(false);
            accessTokenDialog.dispose();
        }
    }


    private class EditAutomationScriptsAction extends AbstractAction {
        private final OAuth2Profile profile;

        public EditAutomationScriptsAction(OAuth2Profile profile) {
            putValue(Action.NAME, AUTOMATION_BUTTON_TITLE);
            this.profile = profile;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            closeGetAccessTokenDialog();
            if (scriptEditorPanel == null) {
                scriptEditorPanel = new OAuth2ScriptsDesktopPanel(profile);
            }
            UISupport.showDesktopPanel(scriptEditorPanel);
        }
    }
}
