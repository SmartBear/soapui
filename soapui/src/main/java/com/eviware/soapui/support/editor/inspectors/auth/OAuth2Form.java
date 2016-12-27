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
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.actions.oauth.RefreshOAuthAccessTokenAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.Bindings;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
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
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class OAuth2Form extends AbstractAuthenticationForm implements OAuth2AccessTokenStatusChangeListener {
    public static final String ADVANCED_OPTIONS_BUTTON_NAME = "Advanced...";
    public static final String REFRESH_ACCESS_TOKEN_BUTTON_NAME = "refreshAccessTokenButton";

    private static final int ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET = 120;

    private static final Dimension HORIZONTAL_SPACING_IN_ACCESS_TOKEN_ROW = new Dimension(5, 0);
    private static final String ACCESS_TOKEN_LABEL = "Access Token";
    private static final Insets ACCESS_TOKEN_FIELD_INSETS = new Insets(5, 5, 5, 5);
    private static final float ACCESS_TOKEN_STATUS_TEXT_FONT_SCALE = 0.95f;
    private static final int ACCESS_TOKEN_STATUS_TEXT_WIDTH = 100;

    private static final String GET_ACCESS_TOKEN_BUTTON_DEFAULT_LABEL = "Get Token";
    private static final String GET_ACCESS_TOKEN_BUTTON_RESUME_LABEL = GET_ACCESS_TOKEN_BUTTON_DEFAULT_LABEL + " (Resume)";

    private final Color DEFAULT_COLOR = Color.WHITE;
    private final Color SUCCESS_COLOR = new Color(0xccffcb);
    private final Color FAIL_COLOR = new Color(0xffcccc);

    static final ImageIcon SUCCESS_ICON = UISupport.createImageIcon("/check.png");
    static final ImageIcon WAIT_ICON = UISupport.createImageIcon("/waiting-spinner.gif");
    static final ImageIcon FAIL_ICON = UISupport.createImageIcon("/alert.png");

    private final AbstractXmlInspector inspector;
    private final OAuth2AccessTokenStatusChangeManager statusChangeManager;
    private OAuth2Profile profile;
    private JPanel formPanel;
    private boolean disclosureButtonDisabled;
    private boolean isMouseOnDisclosureLabel;

    private SimpleBindingForm oAuth2Form;

    private JTextField accessTokenField;
    private JLabel accessTokenStatusIcon;
    private JLabel accessTokenStatusText;
    private JLabel disclosureButton;
    private OAuth2GetAccessTokenForm accessTokenForm;
    private SoapUIMainWindowFocusListener mainWindowFocusListener;

    public OAuth2Form(OAuth2Profile profile, AbstractXmlInspector inspector) {
        super();
        this.profile = profile;
        this.inspector = inspector;
        statusChangeManager = new OAuth2AccessTokenStatusChangeManager(this);
    }

    void release() {
        SoapUI.getFrame().removeWindowFocusListener(mainWindowFocusListener);
        accessTokenForm.release();
        oAuth2Form.getPresentationModel().release();
        statusChangeManager.unregister();
    }

    @Override
    public void onAccessTokenStatusChanged(@Nonnull OAuth2Profile.AccessTokenStatus status) {
        setAccessTokenStatusFeedback(status);
    }

    @Nonnull
    @Override
    public OAuth2Profile getProfile() {
        return profile;
    }

    @Override
    protected JPanel buildUI() {
        oAuth2Form = new SimpleBindingForm(new PresentationModel<OAuth2Profile>(profile));
        addOAuth2Panel(oAuth2Form);
        statusChangeManager.register();

        if (profile.getAccessTokenStatus() != OAuth2Profile.AccessTokenStatus.RETRIEVAL_CANCELED) {
            profile.resetAccessTokenStatusToStartingStatus();
        }
        setAccessTokenStatusFeedback(profile.getAccessTokenStatus());
        return formPanel;
    }

    // TODO Use the refactored SimpleBidningsForm instead
    private void addOAuth2Panel(SimpleBindingForm oAuth2Form) {
        populateOAuth2Form(oAuth2Form);

        formPanel = new JPanel(new BorderLayout());

        JPanel centerPanel = oAuth2Form.getPanel();
        setBackgroundColorOnPanel(centerPanel);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel oAuthDocumentationLink = UISupport.createLabelLink(HelpUrls.OAUTH_OVERVIEW, "Learn about OAuth 2");
        southPanel.add(oAuthDocumentationLink);

        southPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER_COLOR));
        setBackgroundColorOnPanel(southPanel);

        formPanel.add(centerPanel, BorderLayout.CENTER);
        formPanel.add(southPanel, BorderLayout.SOUTH);

        setBorderOnPanel(formPanel);
    }

    private void populateOAuth2Form(SimpleBindingForm oAuth2Form) {
        initForm(oAuth2Form);

        oAuth2Form.addSpace(TOP_SPACING);

        JTextField accessTokenField = createAccessTokenField();
        JLabel accessTokenStatusIcon = createAccessTokenStatusIcon();
        JLabel accessTokenStatusText = createAccessTokenStatusText();

        final JButton refreshAccessTokenButton = createRefreshButton();

        JPanel accessTokenRowPanel = createAccessTokenRowPanel(accessTokenField, accessTokenStatusIcon, accessTokenStatusText, refreshAccessTokenButton);
        oAuth2Form.append(ACCESS_TOKEN_LABEL, accessTokenRowPanel);

        oAuth2Form.addInputFieldHintText("Enter existing access token, or use \"Get Token\" below.");

        disclosureButton = new JLabel(GET_ACCESS_TOKEN_BUTTON_DEFAULT_LABEL);
        disclosureButton.setIcon(UISupport.createImageIcon("/pop-down-open.png"));
        disclosureButton.setName("oAuth2DisclosureButton");
        oAuth2Form.addComponentWithoutLabel(disclosureButton);

        accessTokenForm = new OAuth2GetAccessTokenForm(profile);
        final JDialog accessTokenFormDialog = accessTokenForm.getComponent();

        disclosureButton.addMouseListener(new DisclosureButtonMouseListener(accessTokenFormDialog, disclosureButton));

        accessTokenFormDialog.addWindowFocusListener(new AccessTokenFormDialogWindowListener(accessTokenFormDialog));

        JButton advancedOptionsButton = oAuth2Form.addButtonWithoutLabelToTheRight(ADVANCED_OPTIONS_BUTTON_NAME, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new OAuth2AdvancedOptionsDialog(profile, refreshAccessTokenButton);
            }
        });
        advancedOptionsButton.setName(ADVANCED_OPTIONS_BUTTON_NAME);

        mainWindowFocusListener = new SoapUIMainWindowFocusListener(accessTokenFormDialog);
        SoapUI.getFrame().addWindowFocusListener(mainWindowFocusListener);
    }

    private JTextField createAccessTokenField() {
        accessTokenField = new JTextField();
        accessTokenField.setName(OAuth2Profile.ACCESS_TOKEN_PROPERTY);
        accessTokenField.setColumns(SimpleForm.MEDIUM_TEXT_FIELD_COLUMNS);
        accessTokenField.setMargin(ACCESS_TOKEN_FIELD_INSETS);
        Bindings.bind(accessTokenField, oAuth2Form.getPresentationModel().getModel(OAuth2Profile.ACCESS_TOKEN_PROPERTY));
        return accessTokenField;
    }

    private JLabel createAccessTokenStatusIcon() {
        accessTokenStatusIcon = new JLabel();
        accessTokenStatusIcon.setVisible(false);
        return accessTokenStatusIcon;
    }

    private JLabel createAccessTokenStatusText() {
        accessTokenStatusText = new JLabel();
        accessTokenStatusText.setFont(scaledFont(accessTokenStatusText, ACCESS_TOKEN_STATUS_TEXT_FONT_SCALE));
        accessTokenStatusText.setVisible(false);
        accessTokenStatusText.setAlignmentX(Component.CENTER_ALIGNMENT);

        return accessTokenStatusText;
    }

    private JButton createRefreshButton() {
        final JButton refreshAccessTokenButton = new JButton("Refresh");
        refreshAccessTokenButton.setName(REFRESH_ACCESS_TOKEN_BUTTON_NAME);
        refreshAccessTokenButton.addActionListener(new RefreshOAuthAccessTokenAction(profile));
        boolean enabled = profile.getRefreshAccessTokenMethod().equals(OAuth2Profile.RefreshAccessTokenMethods.MANUAL)
                && (!StringUtils.isNullOrEmpty(profile.getRefreshToken()));
        refreshAccessTokenButton.setVisible(enabled);
        return refreshAccessTokenButton;
    }

    private JPanel createAccessTokenRowPanel(JTextField accessTokenField, JLabel accessTokenStatusIcon, JLabel accessTokenStatusText, JButton refreshAccessTokenButton) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(CARD_BACKGROUND_COLOR);
        panel.add(accessTokenField);
        panel.add(Box.createRigidArea(HORIZONTAL_SPACING_IN_ACCESS_TOKEN_ROW));
        panel.add(accessTokenStatusIcon);
        panel.add(Box.createRigidArea(HORIZONTAL_SPACING_IN_ACCESS_TOKEN_ROW));
        panel.add(accessTokenStatusText);
        panel.add(Box.createRigidArea(HORIZONTAL_SPACING_IN_ACCESS_TOKEN_ROW));
        panel.add(refreshAccessTokenButton);
        return panel;
    }

    private Font scaledFont(JComponent component, float scale) {
        Font currentFont = component.getFont();
        return currentFont.deriveFont((float) currentFont.getSize() * scale);
    }

    private String setWrappedText(String text) {
        return String.format("<html><div WIDTH=%d>%s</div><html>", OAuth2Form.ACCESS_TOKEN_STATUS_TEXT_WIDTH, text);
    }

    private void setAccessTokenFormDialogBoundsBelowTheButton(Point disclosureButtonLocation, JDialog accessTokenFormDialog, int disclosureButtonHeight) {
        accessTokenFormDialog.setLocation((int) disclosureButtonLocation.getX() - ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET,
                (int) disclosureButtonLocation.getY() + disclosureButtonHeight);
    }

    private void setAccessTokenFormDialogBoundsAboveTheButton(Point disclosureButtonLocation, JDialog accessTokenFormDialog) {
        accessTokenFormDialog.setLocation((int) disclosureButtonLocation.getX() - ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET,
                (int) disclosureButtonLocation.getY() - accessTokenFormDialog.getHeight());
    }

    private void setAccessTokenStatusFeedback(OAuth2Profile.AccessTokenStatus status) {
        switch (status) {
            case UNKNOWN:
                setDefaultFeedback();
                break;
            case ENTERED_MANUALLY:
                setEnteredManuallyFeedback(status);
                break;
            case RETRIEVED_FROM_SERVER:
                setSucessfulFeedback(status);
                break;
            case RETRIEVAL_CANCELED:
                setCanceledFeedback();
                break;
            case EXPIRED:
                setFailedFeedback(status);
                break;
        }
    }

    private void setEnteredManuallyFeedback(OAuth2Profile.AccessTokenStatus status) {
        accessTokenField.setBackground(DEFAULT_COLOR);

        accessTokenStatusIcon.setIcon(null);
        accessTokenStatusIcon.setVisible(false);

        accessTokenStatusText.setText("");
        accessTokenStatusText.setVisible(false);

        disclosureButton.setText(GET_ACCESS_TOKEN_BUTTON_DEFAULT_LABEL);

        inspector.setIcon(ProfileSelectionForm.AUTH_ENABLED_ICON);
    }

    private void setSucessfulFeedback(OAuth2Profile.AccessTokenStatus status) {
        accessTokenField.setBackground(SUCCESS_COLOR);

        accessTokenStatusIcon.setIcon(SUCCESS_ICON);
        accessTokenStatusIcon.setVisible(true);

        accessTokenStatusText.setText(setWrappedText(status.toString()));
        accessTokenStatusText.setVisible(true);

        disclosureButton.setText(GET_ACCESS_TOKEN_BUTTON_DEFAULT_LABEL);

        inspector.setIcon(ProfileSelectionForm.AUTH_ENABLED_ICON);
    }

    private void setFailedFeedback(OAuth2Profile.AccessTokenStatus status) {
        accessTokenField.setBackground(FAIL_COLOR);

        accessTokenStatusIcon.setIcon(FAIL_ICON);
        accessTokenStatusIcon.setVisible(true);

        accessTokenStatusText.setText(setWrappedText(status.toString()));
        accessTokenStatusText.setVisible(true);

        disclosureButton.setText(GET_ACCESS_TOKEN_BUTTON_DEFAULT_LABEL);

        inspector.setIcon(FAIL_ICON);
    }

    private void setCanceledFeedback() {
        setAccessTokenStatusFeedback(profile.getAccessTokenStartingStatus());
        disclosureButton.setText(GET_ACCESS_TOKEN_BUTTON_RESUME_LABEL);
    }

    private void setDefaultFeedback() {
        accessTokenField.setBackground(DEFAULT_COLOR);

        accessTokenStatusIcon.setIcon(null);
        accessTokenStatusIcon.setVisible(false);

        accessTokenStatusText.setText("");
        accessTokenStatusText.setVisible(false);

        disclosureButton.setText(GET_ACCESS_TOKEN_BUTTON_DEFAULT_LABEL);

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

        // TODO This might be extracted to a common utils class
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
