package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AccessTokenStatusConfig;
import com.eviware.soapui.impl.rest.OAuth1Profile;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.Bindings;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class OAuth1Form extends AbstractAuthenticationForm implements OAuth1TokenStatusChangeListener {
    public static final String ADVANCED_OPTIONS_BUTTON_NAME = "Advanced...";
    static final ImageIcon SUCCESS_ICON = UISupport.createImageIcon("/check.png");
    static final ImageIcon FAIL_ICON = UISupport.createImageIcon("/alert.png");
    private static final MessageSupport messages = MessageSupport.getMessages(OAuth1Form.class);
    private static final int ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET = 120;
    private static final Dimension HORIZONTAL_SPACING_IN_ACCESS_TOKEN_ROW = new Dimension(5, 0);
    private static final String ACCESS_TOKEN_LABEL = "Access Token";
    private static final String TOKEN_SECRET_LABEL = "Token Secret";
    private static final Insets TOKEN_FIELD_INSETS = new Insets(5, 5, 5, 5);
    private static final float TOKEN_STATUS_TEXT_FONT_SCALE = 0.95f;
    private static final String GET_ACCESS_TOKEN_BUTTON_DEFAULT_LABEL = "Get Token";
    private final Color DEFAULT_COLOR = Color.WHITE;
    private final Color SUCCESS_COLOR = new Color(0xccffcb);
    private final Color FAIL_COLOR = new Color(0xffcccc);
    private final AbstractXmlInspector inspector;
    private final OAuth1TokenStatusChangeManager statusChangeManager;
    private OAuth1Profile profile;
    private JPanel formPanel;
    private boolean disclosureButtonDisabled;
    private boolean isMouseOnDisclosureLabel;

    private SimpleBindingForm oAuth1Form;

    private JTextField accessTokenField;
    private JLabel accessTokenStatusIcon;
    private JLabel accessTokenStatusText;

    private JTextField secretTokenField;
    private JLabel secretTokenStatusIcon;
    private JLabel secretTokenStatusText;

    private JLabel disclosureButton;
    private OAuth1GetTokenForm accessTokenForm;
    private SoapUIMainWindowFocusListener mainWindowFocusListener;

    public OAuth1Form(OAuth1Profile profile, AbstractXmlInspector inspector) {
        super();
        this.profile = profile;
        this.inspector = inspector;
        statusChangeManager = new OAuth1TokenStatusChangeManager(this);
        statusChangeManager.register();
    }

    void release() {
        SoapUI.getFrame().removeWindowFocusListener(mainWindowFocusListener);
        accessTokenForm.release();
        oAuth1Form.getPresentationModel().release();
        statusChangeManager.unregister();
    }

    @Override
    public void onAccessTokenStatusChanged(@Nonnull AccessTokenStatusConfig.Enum status) {
        setAccessTokenStatusFeedback(status);
    }

    @Override
    public void onTokenSecretStatusChanged(@Nonnull AccessTokenStatusConfig.Enum status) {
        setSecretTokenStatusFeedback(status);
    }

    @Nonnull
    @Override
    public OAuth1Profile getProfile() {
        return profile;
    }

    @Override
    protected JPanel buildUI() {
        oAuth1Form = new SimpleBindingForm(new PresentationModel<OAuth1Profile>(profile));
        addOAuth2Panel(oAuth1Form);

        if (profile.getAccessTokenStatus() != AccessTokenStatusConfig.RETRIEVAL_CANCELED) {
            profile.resetAccessTokenStatusToStartingStatus();
        }
        setAccessTokenStatusFeedback(profile.getAccessTokenStatus());
        setSecretTokenStatusFeedback(profile.getTokenSecretStatus());
        return formPanel;
    }

    private void addOAuth2Panel(SimpleBindingForm oAuth2Form) {
        populateOAuth1Form(oAuth2Form);

        formPanel = new JPanel(new BorderLayout());

        JPanel centerPanel = oAuth2Form.getPanel();
        setBackgroundColorOnPanel(centerPanel);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //TODO: add help link
        //JLabel oAuthDocumentationLink = UISupport.createLabelLink(HelpUrls.OAUTH_OVERVIEW, "Learn about OAuth 2");
        //southPanel.add(oAuthDocumentationLink);
        //southPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER_COLOR));

        setBackgroundColorOnPanel(southPanel);

        formPanel.add(centerPanel, BorderLayout.CENTER);
        formPanel.add(southPanel, BorderLayout.SOUTH);

        setBorderOnPanel(formPanel);
    }

    private void populateOAuth1Form(SimpleBindingForm oAuth1Form) {
        initForm(oAuth1Form);
        oAuth1Form.addSpace(TOP_SPACING);

        accessTokenField = createTokenField("accessToken");
        accessTokenStatusIcon = createTokenStatusIcon();
        accessTokenStatusText = createAccessTokenStatusText();
        JPanel accessTokenRowPanel =
                createAccessTokenRowPanel(accessTokenField, accessTokenStatusIcon, accessTokenStatusText);
        oAuth1Form.append(ACCESS_TOKEN_LABEL, accessTokenRowPanel);
        oAuth1Form.addInputFieldHintText(messages.get("OAuth1Form.AccessTokenField.Hint"));

        disclosureButton = new JLabel(GET_ACCESS_TOKEN_BUTTON_DEFAULT_LABEL);
        disclosureButton.setIcon(UISupport.createImageIcon("/pop-down-open.png"));
        disclosureButton.setName("oAuth2DisclosureButton");
        oAuth1Form.addComponentWithoutLabel(disclosureButton);

        secretTokenField = createTokenField("tokenSecret");
        secretTokenStatusIcon = createTokenStatusIcon();
        secretTokenStatusText = createAccessTokenStatusText();
        JPanel secretTokenRowPanel =
                createAccessTokenRowPanel(secretTokenField, secretTokenStatusIcon, secretTokenStatusText);
        oAuth1Form.append(TOKEN_SECRET_LABEL, secretTokenRowPanel);
        oAuth1Form.addInputFieldHintText(messages.get("OAuth1Form.SecretTokenField.Hint"));

        JButton advancedOptionsButton = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new OAuth1AdvancedOptionsDialog(profile);
            }
        });
        advancedOptionsButton.setText(ADVANCED_OPTIONS_BUTTON_NAME);
        oAuth1Form.addLeftComponent(advancedOptionsButton);

        accessTokenForm = new OAuth1GetTokenForm(profile);
        final JDialog accessTokenFormDialog = accessTokenForm.getComponent();

        disclosureButton.addMouseListener(new DisclosureButtonMouseListener(accessTokenFormDialog, disclosureButton));

        accessTokenFormDialog.addWindowFocusListener(new AccessTokenFormDialogWindowListener(accessTokenFormDialog));

        mainWindowFocusListener = new SoapUIMainWindowFocusListener(accessTokenFormDialog);
        SoapUI.getFrame().addWindowFocusListener(mainWindowFocusListener);
    }

    private JTextField createTokenField(String fieldName) {
        JTextField tokenField = new JTextField();
        tokenField.setName(fieldName);
        tokenField.setColumns(SimpleForm.MEDIUM_TEXT_FIELD_COLUMNS);
        tokenField.setMargin(TOKEN_FIELD_INSETS);
        Bindings.bind(tokenField, oAuth1Form.getPresentationModel().getModel(fieldName));
        return tokenField;
    }

    private JLabel createTokenStatusIcon() {
        JLabel tokenStatusIcon = new JLabel();
        tokenStatusIcon.setVisible(false);
        return tokenStatusIcon;
    }

    private JLabel createAccessTokenStatusText() {
        JLabel tokenStatusText = new JLabel();
        tokenStatusText.setFont(scaledFont(tokenStatusText, TOKEN_STATUS_TEXT_FONT_SCALE));
        tokenStatusText.setVisible(false);
        tokenStatusText.setAlignmentX(Component.CENTER_ALIGNMENT);

        return tokenStatusText;
    }

    private JPanel createAccessTokenRowPanel(JTextField accessTokenField, JLabel accessTokenStatusIcon, JLabel accessTokenStatusText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(CARD_BACKGROUND_COLOR);
        panel.add(accessTokenField);
        panel.add(Box.createRigidArea(HORIZONTAL_SPACING_IN_ACCESS_TOKEN_ROW));
        panel.add(accessTokenStatusIcon);
        panel.add(Box.createRigidArea(HORIZONTAL_SPACING_IN_ACCESS_TOKEN_ROW));
        panel.add(accessTokenStatusText);
        panel.add(Box.createRigidArea(HORIZONTAL_SPACING_IN_ACCESS_TOKEN_ROW));
        return panel;
    }

    private Font scaledFont(JComponent component, float scale) {
        Font currentFont = component.getFont();
        return currentFont.deriveFont((float) currentFont.getSize() * scale);
    }

    private void setAccessTokenFormDialogBoundsBelowTheButton(Point disclosureButtonLocation, JDialog accessTokenFormDialog, int disclosureButtonHeight) {
        accessTokenFormDialog.setLocation((int) disclosureButtonLocation.getX() - ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET,
                (int) disclosureButtonLocation.getY() + disclosureButtonHeight);
    }

    private void setAccessTokenFormDialogBoundsAboveTheButton(Point disclosureButtonLocation, JDialog accessTokenFormDialog) {
        accessTokenFormDialog.setLocation((int) disclosureButtonLocation.getX() - ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET,
                (int) disclosureButtonLocation.getY() - accessTokenFormDialog.getHeight());
    }

    private void setAccessTokenStatusFeedback(AccessTokenStatusConfig.Enum status) {
        if (status == AccessTokenStatusConfig.UNKNOWN) {
            setAccessTokenDefaultFeedback();
        } else if (status == AccessTokenStatusConfig.ENTERED_MANUALLY) {
            setAccessTokenEnteredManuallyFeedback();
        } else if (status == AccessTokenStatusConfig.RETRIEVED_FROM_SERVER) {
            setAccessTokenSuccessfulFeedback();
        } else if (status == AccessTokenStatusConfig.EXPIRED
                || status == AccessTokenStatusConfig.RETRIEVAL_CANCELED) {
            setAccessTokenFailedFeedback();
        } else {
            setAccessTokenDefaultFeedback();
        }
    }

    private void setSecretTokenStatusFeedback(AccessTokenStatusConfig.Enum status) {
        if (status == AccessTokenStatusConfig.UNKNOWN) {
            setSecretTokenDefaultFeedback();
        } else if (status == AccessTokenStatusConfig.ENTERED_MANUALLY) {
            setSecretTokenEnteredManuallyFeedback();
        } else if (status == AccessTokenStatusConfig.RETRIEVED_FROM_SERVER) {
            setSecretTokenSuccessfulFeedback();
        } else if (status == AccessTokenStatusConfig.EXPIRED
                || status == AccessTokenStatusConfig.RETRIEVAL_CANCELED) {
            setSecretTokenFailedFeedback();
        } else {
            setSecretTokenDefaultFeedback();
        }
    }

    private void setSecretTokenEnteredManuallyFeedback() {
        secretTokenField.setBackground(DEFAULT_COLOR);
        secretTokenStatusIcon.setIcon(null);
        secretTokenStatusIcon.setVisible(false);
        secretTokenStatusText.setText(messages.get("OAuth1Form.EnteredManually"));
        secretTokenStatusText.setVisible(true);
        inspector.setIcon(ProfileSelectionForm.AUTH_ENABLED_ICON);
    }

    private void setSecretTokenSuccessfulFeedback() {
        secretTokenField.setBackground(SUCCESS_COLOR);
        secretTokenStatusIcon.setIcon(SUCCESS_ICON);
        secretTokenStatusIcon.setVisible(true);
        secretTokenStatusText.setText(messages.get("OAuth1Form.Retrieved"));
        secretTokenStatusText.setVisible(true);
        inspector.setIcon(ProfileSelectionForm.AUTH_ENABLED_ICON);
    }

    private void setSecretTokenFailedFeedback() {
        secretTokenField.setBackground(FAIL_COLOR);
        secretTokenStatusIcon.setIcon(FAIL_ICON);
        secretTokenStatusIcon.setVisible(true);
        secretTokenStatusText.setText(messages.get("OAuth1Form.FailedFeedback"));
        secretTokenStatusText.setVisible(true);
        inspector.setIcon(FAIL_ICON);
    }

    private void setSecretTokenDefaultFeedback() {
        secretTokenField.setBackground(DEFAULT_COLOR);
        secretTokenStatusIcon.setIcon(null);
        secretTokenStatusIcon.setVisible(false);
        secretTokenStatusText.setText("");
        secretTokenStatusText.setVisible(false);
        inspector.setIcon(ProfileSelectionForm.AUTH_ENABLED_ICON);
    }

    private void setAccessTokenEnteredManuallyFeedback() {
        accessTokenField.setBackground(DEFAULT_COLOR);
        accessTokenStatusIcon.setIcon(null);
        accessTokenStatusIcon.setVisible(false);
        accessTokenStatusText.setText(messages.get("OAuth1Form.EnteredManually"));
        accessTokenStatusText.setVisible(true);
        inspector.setIcon(ProfileSelectionForm.AUTH_ENABLED_ICON);
    }

    private void setAccessTokenSuccessfulFeedback() {
        accessTokenField.setBackground(SUCCESS_COLOR);
        accessTokenStatusIcon.setIcon(SUCCESS_ICON);
        accessTokenStatusIcon.setVisible(true);
        accessTokenStatusText.setText(messages.get("OAuth1Form.Retrieved"));
        accessTokenStatusText.setVisible(true);
        inspector.setIcon(ProfileSelectionForm.AUTH_ENABLED_ICON);
    }

    private void setAccessTokenFailedFeedback() {
        accessTokenField.setBackground(FAIL_COLOR);
        accessTokenStatusIcon.setIcon(FAIL_ICON);
        accessTokenStatusIcon.setVisible(true);
        accessTokenStatusText.setText(messages.get("OAuth1Form.FailedFeedback"));
        accessTokenStatusText.setVisible(true);
        inspector.setIcon(FAIL_ICON);
    }

    private void setAccessTokenDefaultFeedback() {
        accessTokenField.setBackground(DEFAULT_COLOR);
        accessTokenStatusIcon.setIcon(null);
        accessTokenStatusIcon.setVisible(false);
        accessTokenStatusText.setText("");
        accessTokenStatusText.setVisible(false);
        inspector.setIcon(ProfileSelectionForm.AUTH_ENABLED_ICON);
    }

    private void hideAccessTokenFormDialogAndEnableDisclosureButton(JDialog accessTokenFormDialog) {
        accessTokenFormDialog.setVisible(false);
        disclosureButton.setIcon(UISupport.createImageIcon("/pop-down-open.png"));
        // If the focus is lost due to click on the disclosure button then don't enable it yet, since it
        // will then show the dialog directly again.
        if (!isMouseOnDisclosureLabel) {
            disclosureButtonDisabled = false;
        }
    }

    private class DisclosureButtonMouseListener extends MouseAdapter {
        private final JDialog accessTokenFormDialog;
        private final JLabel disclosureButton;

        public DisclosureButtonMouseListener(JDialog accessTokenFormDialog, JLabel disclosureButton) {
            this.accessTokenFormDialog = accessTokenFormDialog;
            this.disclosureButton = disclosureButton;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // Check if this click is to hide the access token form dialog
            if (disclosureButtonDisabled) {
                disclosureButtonDisabled = false;
                return;
            }

            JLabel source = (JLabel) e.getSource();
            Point disclosureButtonLocation = source.getLocationOnScreen();
            accessTokenFormDialog.pack();
            accessTokenFormDialog.setVisible(true);
            disclosureButton.setIcon(UISupport.createImageIcon("/pop-down-close.png"));
            if (UISupport.isEnoughSpaceAvailableBelowComponent(disclosureButtonLocation, accessTokenFormDialog.getHeight(), source.getHeight())) {
                setAccessTokenFormDialogBoundsBelowTheButton(disclosureButtonLocation, accessTokenFormDialog, source.getHeight());
            } else {
                setAccessTokenFormDialogBoundsAboveTheButton(disclosureButtonLocation, accessTokenFormDialog);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            isMouseOnDisclosureLabel = true;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            isMouseOnDisclosureLabel = false;
        }
    }

    private class AccessTokenFormDialogWindowListener implements WindowFocusListener {
        private final JDialog accessTokenFormDialog;

        public AccessTokenFormDialogWindowListener(JDialog accessTokenFormDialog) {
            this.accessTokenFormDialog = accessTokenFormDialog;
        }

        @Override
        public void windowGainedFocus(WindowEvent e) {
            disclosureButtonDisabled = true;
        }

        @Override
        public void windowLostFocus(WindowEvent e) {
            if (isMouseOnComponent(SoapUI.getFrame()) && !isMouseOnComponent(accessTokenFormDialog)) {
                hideAccessTokenFormDialogAndEnableDisclosureButton(accessTokenFormDialog);
            }
        }

        private boolean isMouseOnComponent(Component component) {
            if (!component.isShowing()) {
                return false;
            }
            Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
            Point componentLocationOnScreen = component.getLocationOnScreen();
            return component.contains(mouseLocation.x - componentLocationOnScreen.x, mouseLocation.y - componentLocationOnScreen.y);
        }
    }

    private class SoapUIMainWindowFocusListener extends WindowAdapter {
        private final JDialog accessTokenFormDialog;

        public SoapUIMainWindowFocusListener(JDialog accessTokenFormDialog) {
            this.accessTokenFormDialog = accessTokenFormDialog;
        }

        @Override
        public void windowGainedFocus(WindowEvent e) {
            if (accessTokenFormDialog.isVisible()) {
                hideAccessTokenFormDialogAndEnableDisclosureButton(accessTokenFormDialog);
            }
        }
    }
}