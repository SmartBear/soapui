package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AccessTokenStatusConfig;
import com.eviware.soapui.impl.rest.OAuth1Profile;
import com.eviware.soapui.impl.rest.actions.oauth.GetOAuth1TokenAction;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.components.PropertyComponent;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.jgoodies.binding.PresentationModel;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;

public class OAuth1GetTokenForm implements OAuth1TokenStatusChangeListener {
    protected static final String GET_ACCESS_TOKEN_FORM_LAYOUT = "7dlu:none,left:pref,10dlu,left:pref,10dlu,left:MAX(112dlu;pref),7dlu";
    protected static final Color CARD_BORDER_COLOR = new Color(121, 121, 121);
    static final ImageIcon DEFAULT_ICON = null;
    private static final MessageSupport messages = MessageSupport.getMessages(OAuth1GetTokenForm.class);
    public static final String CONSUMER_KEY_TITLE = messages.get("OAuth1GetTokenForm.ConsumerKey.Title");
    public static final String CONSUMER_SECRET_TITLE = messages.get("OAuth1GetTokenForm.ConsumerSecret.Title");
    public static final String TEMPORARY_TOKEN_URI_TITLE = messages.get("OAuth1GetTokenForm.TemporaryTokenURI.Title");
    public static final String ACCESS_TOKEN_URI_TITLE = messages.get("OAuth1GetTokenForm.AccessTokenURI.Title");
    public static final String AUTHORIZATION_URI_TITLE = messages.get("OAuth1GetTokenForm.AuthorizationURI.Title");
    public static final String REDIRECT_URI_TITLE = messages.get("OAuth1GetTokenForm.RedirectURI.Title");
    private static final String DIALOG_DESCRIPTION = messages.get("OAuth1GetTokenForm.RedirectURI.Description");
    private static final int GROUP_SPACING = 20;
    private static final int BOARDER_SPACING = 15;
    private static final int NORMAL_SPACING = 10;
    private static final String ACCESS_TOKEN_FORM_DIALOG_NAME = "getAccessTokenFormDialog";
    private static final String GET_ACCESS_TOKEN_BUTTON_NAME = "getAccessTokenButtonName";
    private static final String GET_ACCESS_BUTTON_LABEL = messages.get("OAuth1GetTokenForm.GetButton.Title");
    protected JLabel accessTokenStatusText;
    protected JDialog accessTokenDialog;
    protected OAuth1Profile profile;
    protected OAuth1TokenStatusChangeManager statusChangeManager;

    public OAuth1GetTokenForm(OAuth1Profile profile) {
        this.profile = profile;
    }

    @Override
    public void onAccessTokenStatusChanged(@Nonnull AccessTokenStatusConfig.Enum status) {
        setOAuth1StatusFeedback(status);
    }

    @Override
    public void onTokenSecretStatusChanged(@Nonnull AccessTokenStatusConfig.Enum status) {
        setOAuth1StatusFeedback(status);
    }

    @Nonnull
    @Override
    public OAuth1Profile getProfile() {
        return profile;
    }

    protected void closeGetAccessTokenDialog() {
        if (accessTokenDialog != null) {
            accessTokenDialog.setVisible(false);
            accessTokenDialog.dispose();
        }
    }

    protected void initStatusChangeManager() {
        statusChangeManager = new OAuth1TokenStatusChangeManager(this);
        statusChangeManager.register();
    }

    protected void initTokenStatus() {
        if (profile.getAccessTokenStatus() != AccessTokenStatusConfig.RETRIEVAL_CANCELED) {
            profile.resetAccessTokenStatusToStartingStatus();
        }
        if (profile.getTokenSecretStatus() != AccessTokenStatusConfig.RETRIEVAL_CANCELED) {
            profile.resetTokenSecretStatusToStartingStatus();
        }
    }

    void release() {
        statusChangeManager.unregister();
    }

    public JDialog getComponent() {
        initStatusChangeManager();
        SimpleBindingForm accessTokenForm = createSimpleBindingForm(getProfile());

        populateGetAccessTokenForm(accessTokenForm);

        initTokenStatus();
        setOAuth1StatusFeedback(getTokenStatus());

        accessTokenDialog = createGetAccessTokenDialog(accessTokenForm.getPanel());
        return accessTokenDialog;
    }

    protected AccessTokenStatusConfig.Enum getTokenStatus() {
        return profile.getAccessTokenStatus();
    }

    protected void populateGetAccessTokenForm(SimpleBindingForm accessTokenForm) {
        accessTokenForm.addSpace(BOARDER_SPACING);

        accessTokenForm.appendHeading(DIALOG_DESCRIPTION);
        accessTokenForm.addSpace(NORMAL_SPACING);

        accessTokenForm.appendTextField(OAuth1Profile.CONSUMER_KEY_PROPERTY, CONSUMER_KEY_TITLE, "");

        appendClientSecretField(accessTokenForm);

        accessTokenForm.addSpace(GROUP_SPACING);

        accessTokenForm.appendTextField(OAuth1Profile.TEMPORARY_TOKEN_URI_PROPERTY, TEMPORARY_TOKEN_URI_TITLE, "");
        accessTokenForm.appendTextField(OAuth1Profile.AUTHORIZATION_URI_PROPERTY, AUTHORIZATION_URI_TITLE, "");
        appendAccessTokenUriField(accessTokenForm);
        accessTokenForm.appendTextField(OAuth1Profile.REDIRECT_URI_PROPERTY, REDIRECT_URI_TITLE, "");

        accessTokenForm.addSpace(GROUP_SPACING);

        accessTokenForm.appendComponentsInOneRow(createGetAccessTokenButton(), createAccessTokenStatusText());

        accessTokenForm.addSpace(GROUP_SPACING);
        //TODO: add help link
        //accessTokenForm.appendLabelAsLink(HelpUrls.OAUTH1_ACCESS_TOKEN_FROM_SERVER, messages.get("OAuth2GetTokenForm.GetAccessTokenLink.Title"));

        accessTokenForm.addSpace(BOARDER_SPACING);
    }

    private JTextField appendClientSecretField(SimpleBindingForm accessTokenForm) {
        final JTextField clientSecretField = accessTokenForm.appendTextField(OAuth1Profile.CONSUMER_SECRET_PROPERTY,
                CONSUMER_SECRET_TITLE, "");
        return clientSecretField;
    }

    private JTextField appendAccessTokenUriField(SimpleBindingForm accessTokenForm) {
        final JTextField accessTokenUriField = accessTokenForm.appendTextField(OAuth1Profile.ACCESS_TOKEN_URI_PROPERTY,
                ACCESS_TOKEN_URI_TITLE, "");

        return accessTokenUriField;
    }

    protected PropertyComponent createGetAccessTokenButton() {
        JButton getAccessTokenButton = new JButton(new GetOAuth1TokenAction(profile,
                GET_ACCESS_BUTTON_LABEL));
        getAccessTokenButton.setName(GET_ACCESS_TOKEN_BUTTON_NAME);
        return new PropertyComponent(getAccessTokenButton);
    }

    private PropertyComponent createAccessTokenStatusText() {
        accessTokenStatusText = new JLabel();
        return new PropertyComponent(accessTokenStatusText);
    }

    private JDialog createGetAccessTokenDialog(JPanel accessTokenFormPanel) {
        final JDialog accessTokenFormDialog = new JDialog();
        accessTokenFormDialog.setName(getFormDialogName());
        accessTokenFormDialog.setTitle(getFormDialogTitle());
        accessTokenFormDialog.setIconImages(SoapUI.getFrameIcons());
        accessTokenFormDialog.setUndecorated(true);
        accessTokenFormDialog.getContentPane().add(accessTokenFormPanel);
        return accessTokenFormDialog;
    }

    private SimpleBindingForm createSimpleBindingForm(OAuth1Profile profile) {
        PresentationModel presentationModel = new PresentationModel<OAuth1Profile>(profile);
        String columnsSpecs = GET_ACCESS_TOKEN_FORM_LAYOUT;
        Border border = BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1);
        return new SimpleBindingForm(presentationModel, columnsSpecs, border);
    }

    private String getFormDialogName() {
        return ACCESS_TOKEN_FORM_DIALOG_NAME;
    }

    private String getFormDialogTitle() {
        return messages.get("OAuth1GetTokenForm.Dialog.Title");
    }

    private void setOAuth1StatusFeedback(AccessTokenStatusConfig.Enum status) {
        // There are no auth profile selected
        if (status == null) {
            setDefaultFeedback();
        } else {
            if (status == AccessTokenStatusConfig.WAITING_FOR_AUTHORIZATION
                    || status == AccessTokenStatusConfig.RECEIVED_AUTHORIZATION_CODE) {
                setWaitingFeedback();
            } else if (status == AccessTokenStatusConfig.RETRIEVAL_CANCELED) {
                setCanceledFeedback();
            } else if (status == AccessTokenStatusConfig.RETRIEVED_FROM_SERVER) {
                setSuccessfulFeedback();
            } else if (status == AccessTokenStatusConfig.ENTERED_MANUALLY) {
                setEnteredManuallyFeedback();
            } else {
                setDefaultFeedback();
            }
        }
    }

    private void setCanceledFeedback() {
        accessTokenStatusText.setText(messages.get("OAuth1GetTokenForm.CancelledFeedback"));
        accessTokenStatusText.setIcon(OAuth2Form.FAIL_ICON);
    }

    private void setWaitingFeedback() {
        accessTokenStatusText.setText(messages.get("OAuth1GetTokenForm.WaitingFeedback"));
        accessTokenStatusText.setIcon(OAuth2Form.WAIT_ICON);
    }

    private void setDefaultFeedback() {
        accessTokenStatusText.setText("");
        accessTokenStatusText.setIcon(DEFAULT_ICON);
    }

    private void setEnteredManuallyFeedback() {
        accessTokenStatusText.setText(messages.get("OAuth1GetTokenForm.EnteredManuallyFeedback"));
        accessTokenStatusText.setIcon(DEFAULT_ICON);
    }

    private void setSuccessfulFeedback() {
        accessTokenStatusText.setText(messages.get("OAuth1GetTokenForm.RetrievedFeedback"));
        accessTokenStatusText.setIcon(OAuth2Form.SUCCESS_ICON);
    }
}
