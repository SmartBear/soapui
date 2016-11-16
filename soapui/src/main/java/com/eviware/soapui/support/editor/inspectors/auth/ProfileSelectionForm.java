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

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.rest.OAuth1Profile;
import com.eviware.soapui.impl.rest.OAuth1ProfileContainer;
import com.eviware.soapui.impl.rest.OAuth1ProfileListener;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.OAuth2ProfileListener;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProfileSelectionForm<T extends AbstractHttpRequest> extends AbstractXmlInspector {

    public static final String PROFILE_COMBO_BOX = "Authorization:";

    public static final String BASIC_FORM_LABEL = "Legacy form";
    public static final String WSS_FORM_LABEL = "WSS form";
    public static final String OPTIONS_SEPARATOR = "------------------";
    public static final String DELETE_PROFILE_DIALOG_TITLE = "Delete Profile";
    public static final String RENAME_PROFILE_DIALOG_TITLE = "Rename Profile";
    public static final String EMPTY_PANEL = "EmptyPanel";
    static final ImageIcon AUTH_ENABLED_ICON = UISupport.createImageIcon("/lock.png");
    private static final String OAUTH_2_FORM_LABEL = "OAuth 2 form";
    private static final String OAUTH_1_FORM_LABEL = "OAuth 1 form";
    private static final ImageIcon AUTH_NOT_ENABLED_ICON = null;

    private static final Map<String, ShowOnlineHelpAction> helpActions = new HashMap<String, ShowOnlineHelpAction>();
    private final JPanel outerPanel = new JPanel(new BorderLayout());
    private final JPanel cardPanel = new JPanel(new CardLayout());
    private T request;
    private JComboBox profileSelectionComboBox;
    private CellConstraints cc = new CellConstraints();
    private BasicAuthenticationForm<T> authenticationForm;
    private OAuth2Form oAuth2Form;
    private OAuth1Form oAuth1Form;
    private JButton helpButton;
    private ProfileListener profileListener;
    private WSSAuthenticationForm wssAuthenticationForm;

    protected ProfileSelectionForm(T request) {
        super(AuthInspectorFactory.INSPECTOR_ID, "Authentication and Security-related settings",
                true, AuthInspectorFactory.INSPECTOR_ID);
        this.request = request;

        buildUI();
        profileListener = new ProfileListener();
        getOAuth2ProfileContainer().addOAuth2ProfileListener(profileListener);
        getOAuth1ProfileContainer().addOAuth1ProfileListener(profileListener);
    }

    protected static boolean isReservedProfileName(String newName) {
        return getBasicAuthenticationTypes().contains(newName) || newName.equals(OPTIONS_SEPARATOR);
    }

    protected static ArrayList<String> getBasicAuthenticationTypes() {
        ArrayList<String> options = new ArrayList<String>();
        options.add(AbstractHttpRequest.BASIC_AUTH_PROFILE);
        options.add(CredentialsConfig.AuthType.NTLM.toString());
        options.add(CredentialsConfig.AuthType.SPNEGO_KERBEROS.toString());
        return options;
    }

    @Override
    public JComponent getComponent() {
        profileSelectionComboBox.setSelectedItem(request.getSelectedAuthProfile());
        return outerPanel;
    }

    @Override
    public boolean isEnabledFor(EditorView<XmlDocument> view) {
        return !view.getViewId().equals(RawXmlEditorFactory.VIEW_ID);
    }

    @Override
    public void release() {
        super.release();
        if (oAuth2Form != null) {
            oAuth2Form.release();
        }
        if (oAuth1Form != null) {
            oAuth1Form.release();
        }
        getOAuth2ProfileContainer().removeOAuth2ProfileListener(profileListener);
        getOAuth1ProfileContainer().removeOAuth1ProfileListener(profileListener);
    }

    protected void buildUI() {
        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel comboBoxPanel = createAuthorizationLabelAndComboBox();

        innerPanel.add(comboBoxPanel, BorderLayout.PAGE_START);

        cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cardPanel.add(createEmptyPanel(), EMPTY_PANEL);
        innerPanel.add(cardPanel, BorderLayout.CENTER);

        authenticationForm = new BasicAuthenticationForm<T>(request);
        cardPanel.add(authenticationForm.getComponent(), BASIC_FORM_LABEL);

        if (isSoapRequest(request)) {
            wssAuthenticationForm = new WSSAuthenticationForm((WsdlRequest) request);
            cardPanel.add(wssAuthenticationForm.getComponent(), WSS_FORM_LABEL);
        }

        outerPanel.add(new JScrollPane(innerPanel), BorderLayout.CENTER);
    }

    private JPanel createEmptyPanel() {
        JPanel panelWithText = new JPanel(new BorderLayout());
        String helpText = "<html>\n" +
                "<body>" +
                "</div>" +
                "<div style=\"text-align:center\"><b>Not Yet Configured</b>" +
                "<br>Authorization has not been set for protected services." +
                "<br>Use the <i>Authorization</i> drop down to configure." +
                "</div>" +
                "</body>" +
                "</html>";
        JLabel label = new JLabel(helpText);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panelWithText.add(label, BorderLayout.CENTER);
        panelWithText.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AbstractAuthenticationForm.CARD_BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panelWithText.setBackground(AbstractAuthenticationForm.CARD_BACKGROUND_COLOR);
        return panelWithText;
    }

    private boolean isSoapRequest(T request) {
        return request instanceof WsdlRequest;
    }

    private JPanel createAuthorizationLabelAndComboBox() {
        FormLayout formLayout = new FormLayout("5px:none,left:pref,40px,left:default,5px:grow(1.0)");
        JPanel comboBoxPanel = new JPanel(formLayout);
        comboBoxPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JLabel authorizationLabel = new JLabel(PROFILE_COMBO_BOX);
        authorizationLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        formLayout.appendRow(new RowSpec("top:pref"));
        comboBoxPanel.add(authorizationLabel, cc.xy(2, 1));

        createProfileSelectionComboBox();
        comboBoxPanel.add(profileSelectionComboBox, cc.xy(4, 1));

        JPanel wrapperPanel = new JPanel(new BorderLayout(5, 5));
        wrapperPanel.add(comboBoxPanel, BorderLayout.LINE_START);
        helpButton = UISupport.createFormButton(helpActions.get(EMPTY_PANEL));
        wrapperPanel.add(helpButton, BorderLayout.AFTER_LINE_ENDS);
        return wrapperPanel;
    }

    private void createProfileSelectionComboBox() {
        String[] existingProfiles = createOptionsForAuthorizationCombo(request.getSelectedAuthProfile());

        profileSelectionComboBox = new JComboBox(existingProfiles);
        profileSelectionComboBox.setName(PROFILE_COMBO_BOX);
        profileSelectionComboBox.addItemListener(new ProfileSelectionListener());
    }

    private void setAuthenticationTypeAndShowCard(String selectedOption) {
        if (getAddEditOptions().contains(selectedOption)) {
            performAddEditOperation(request.getSelectedAuthProfile(), selectedOption);
            return;
        }

        if (getBasicAuthenticationTypes().contains(selectedOption)) {
            setIcon(AUTH_ENABLED_ICON);
            setTitle(AuthInspectorFactory.INSPECTOR_ID + " (" + selectedOption + ")");
            request.setSelectedAuthProfileAndAuthType(selectedOption, request.getBasicAuthType(selectedOption));

            if (isSoapRequest(request)) {
                wssAuthenticationForm.setButtonGroupVisibility(selectedOption.equals(AbstractHttpRequest.BASIC_AUTH_PROFILE));
                changeAuthorizationType(WSS_FORM_LABEL, selectedOption);
            } else {
                authenticationForm.setButtonGroupVisibility(selectedOption.equals(AbstractHttpRequest.BASIC_AUTH_PROFILE));
                changeAuthorizationType(BASIC_FORM_LABEL, selectedOption);
            }
        } else if (isRestRequest(request) && getOAuth2ProfileContainer().getOAuth2ProfileNameList().contains(selectedOption)) {
            setTitle(AuthInspectorFactory.INSPECTOR_ID + " (" + selectedOption + ")");
            request.setSelectedAuthProfileAndAuthType(selectedOption, CredentialsConfig.AuthType.O_AUTH_2_0);
            oAuth2Form = new OAuth2Form(getOAuth2ProfileContainer().getProfileByName(selectedOption), this);
            cardPanel.add(oAuth2Form.getComponent(), OAUTH_2_FORM_LABEL);
            changeAuthorizationType(OAUTH_2_FORM_LABEL, selectedOption);

            Analytics.trackAction(SoapUIActions.ASSIGN_O_AUTH.getActionName(), "OAuth2Flow",
                    oAuth2Form.getProfile().getOAuth2Flow().name());
        } else if (isRestRequest(request) && getOAuth1ProfileContainer().getOAuth1ProfileNameList().contains(selectedOption)) {
            setTitle(AuthInspectorFactory.INSPECTOR_ID + " (" + selectedOption + ")");
            request.setSelectedAuthProfileAndAuthType(selectedOption, CredentialsConfig.AuthType.O_AUTH_1_0);
            oAuth1Form = new OAuth1Form(getOAuth1ProfileContainer().getProfileByName(selectedOption), this);
            cardPanel.add(oAuth1Form.getComponent(), OAUTH_1_FORM_LABEL);
            changeAuthorizationType(OAUTH_1_FORM_LABEL, selectedOption);
        } else if (selectedOption.equals(OPTIONS_SEPARATOR)) {
            profileSelectionComboBox.setSelectedIndex(0);
        } else    //selectedItem : No Authorization
        {
            setIcon(AUTH_NOT_ENABLED_ICON);
            setTitle(AuthInspectorFactory.INSPECTOR_ID);
            request.setSelectedAuthProfileAndAuthType(selectedOption, CredentialsConfig.AuthType.NO_AUTHORIZATION);
            changeAuthorizationType(EMPTY_PANEL, selectedOption);
        }
    }

    private void performAddEditOperation(final String currentProfile, String selectedOption) {
        AddEditOptions addEditOption = getAddEditOptionForDescription(selectedOption);
        switch (addEditOption) {
            case ADD:
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new AuthorizationSelectionDialog<T>(request, getBasicAuthenticationTypes());
                        refreshProfileSelectionComboBox(request.getSelectedAuthProfile());
                    }
                });
                break;
            case DELETE:
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        deleteCurrentProfile(currentProfile);
                    }
                });
                break;
            case RENAME:
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        renameCurrentProfile(currentProfile);
                    }
                });
                break;
            default:
                break;
        }
    }

    private void renameCurrentProfile(String profileOldName) {
        String newName = UISupport.prompt("Specify name of Profile", RENAME_PROFILE_DIALOG_TITLE, profileOldName);
        if (newName == null || profileOldName.equals(newName)) {
            profileSelectionComboBox.setSelectedItem(profileOldName);
            return;
        }

        if (newName.trim().equals("")) {
            UISupport.showErrorMessage("New name can't be empty.");
            profileSelectionComboBox.setSelectedItem(profileOldName);
            return;
        }

        if (isReservedProfileName(newName)) {
            UISupport.showErrorMessage("'" + newName + "' is a reserved profile name.");
            profileSelectionComboBox.setSelectedItem(profileOldName);
            return;
        }
        if (getOAuth2ProfileContainer().getOAuth2ProfileNameList().contains(newName)) {
            UISupport.showErrorMessage("There is already a profile named '" + newName + "'");
            profileSelectionComboBox.setSelectedItem(profileOldName);
            return;
        }
        getOAuth2ProfileContainer().renameProfile(profileOldName, newName);
    }

    private void deleteCurrentProfile(String profileName) {
        boolean confirmedDeletion = UISupport.confirm("Do you really want to delete profile '" + profileName + "' ?",
                DELETE_PROFILE_DIALOG_TITLE);
        if (!confirmedDeletion) {
            refreshProfileSelectionComboBox(profileName);
            return;
        }

        if (isRestRequest(request) && getOAuth2ProfileContainer().getOAuth2ProfileNameList().contains(profileName)) {
            getOAuth2ProfileContainer().removeProfile(profileName);
        } else if (isRestRequest(request) && getOAuth1ProfileContainer().getOAuth1ProfileNameList().contains(profileName)) {
            getOAuth1ProfileContainer().removeProfile(profileName);
        } else if (getBasicAuthenticationTypes().contains(profileName)) {
            request.removeBasicAuthenticationProfile(profileName);
        }
        refreshProfileSelectionComboBox(CredentialsConfig.AuthType.NO_AUTHORIZATION.toString());
    }

    private void refreshProfileSelectionComboBox(String selectedProfile) {
        DefaultComboBoxModel model = new DefaultComboBoxModel(createOptionsForAuthorizationCombo(selectedProfile));
        model.setSelectedItem(OPTIONS_SEPARATOR);
        profileSelectionComboBox.setModel(model);

        profileSelectionComboBox.removeItemListener(profileSelectionComboBox.getItemListeners()[0]);
        profileSelectionComboBox.addItemListener(new ProfileSelectionListener());

        profileSelectionComboBox.setSelectedItem(selectedProfile);
    }

    private void changeAuthorizationType(String cardName, String selectedOption) {
        showCard(cardName);
        String helpKey = cardName;
        if (cardName.equals(BASIC_FORM_LABEL) || cardName.equals(WSS_FORM_LABEL)) {
            helpKey = selectedOption;
        }
        helpButton.setAction(helpActions.get(helpKey));

    }

    private void showCard(String cardName) {
        CardLayout layout = (CardLayout) cardPanel.getLayout();
        layout.show(cardPanel, cardName);
    }


    private OAuth2ProfileContainer getOAuth2ProfileContainer() {
        return request.getProject().getOAuth2ProfileContainer();
    }

    private OAuth1ProfileContainer getOAuth1ProfileContainer() {
        return request.getProject().getOAuth1ProfileContainer();
    }

    private String[] createOptionsForAuthorizationCombo(String selectedAuthProfile) {
        ArrayList<String> options = new ArrayList<String>();
        options.add(CredentialsConfig.AuthType.NO_AUTHORIZATION.toString());
        Set<String> basicAuthenticationProfiles = request.getBasicAuthenticationProfiles();
        options.addAll(basicAuthenticationProfiles);

        ArrayList<String> addEditOptions = getAddEditOptions();

        ArrayList<String> oAuth2Profiles = null;
        ArrayList<String> oAuth1Profiles = null;
        if (isRestRequest(request)) {
            oAuth2Profiles = getOAuth2ProfileContainer().getOAuth2ProfileNameList();
            oAuth1Profiles = getOAuth1ProfileContainer().getOAuth1ProfileNameList();
            options.addAll(oAuth2Profiles);
            options.addAll(oAuth1Profiles);
        }
        if (isSoapRequest(request)) {
            if (basicAuthenticationProfiles.size() >= getBasicAuthenticationTypes().size()) {
                addEditOptions.remove(AddEditOptions.ADD.getDescription());
            }
        }
        if (oAuth2Profiles == null || !oAuth2Profiles.contains(selectedAuthProfile)) {
            addEditOptions.remove(AddEditOptions.RENAME.getDescription());
        }

        if (options.size() <= 1 || CredentialsConfig.AuthType.NO_AUTHORIZATION.toString().equals(selectedAuthProfile)) {
            addEditOptions.remove(AddEditOptions.DELETE.getDescription());
        }

        if (!addEditOptions.isEmpty()) {
            options.add(OPTIONS_SEPARATOR);
            options.addAll(addEditOptions);
        }

        return options.toArray(new String[options.size()]);
    }

    private boolean isRestRequest(T request) {
        return request instanceof RestRequest;
    }

    private ArrayList<String> getAddEditOptions() {
        ArrayList<String> addEditOptions = new ArrayList<String>();
        addEditOptions.add(AddEditOptions.ADD.getDescription());
        addEditOptions.add(AddEditOptions.RENAME.getDescription());
        addEditOptions.add(AddEditOptions.DELETE.getDescription());

        return addEditOptions;
    }

    private AddEditOptions getAddEditOptionForDescription(String description) {
        for (AddEditOptions option : AddEditOptions.values()) {
            if (option.getDescription().equals(description)) {
                return option;
            }
        }
        return null;
    }

    public enum AddEditOptions {
        ADD("Add New Authorization..."),
        RENAME("Rename current..."),
        DELETE("Delete current");
        private String description;

        AddEditOptions(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private class ProfileSelectionListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedProfile = (String) e.getItem();

                setAuthenticationTypeAndShowCard(selectedProfile);
                if (!getAddEditOptions().contains(selectedProfile) && !selectedProfile.equals(OPTIONS_SEPARATOR)) {
                    DefaultComboBoxModel profileComboBoXModel = new DefaultComboBoxModel(
                            createOptionsForAuthorizationCombo(selectedProfile));
                    profileComboBoXModel.setSelectedItem(selectedProfile);
                    profileSelectionComboBox.setModel(profileComboBoXModel);
                }
            }
        }
    }

    private class ProfileListener implements OAuth2ProfileListener, OAuth1ProfileListener {
        @Override
        public void profileAdded(OAuth2Profile profile) {
            refreshProfileSelectionComboBox(request.getSelectedAuthProfile());
        }

        @Override
        public void profileRemoved(String profileName) {
            refreshProfileSelectionComboBox(request.getSelectedAuthProfile());
        }

        @Override
        public void profileRenamed(String profileOldName, String newName) {
            refreshProfileSelectionComboBox(request.getSelectedAuthProfile());
        }

        @Override
        public void profileAdded(OAuth1Profile profile) {
            refreshProfileSelectionComboBox(request.getSelectedAuthProfile());
        }
    }

    static {
        helpActions.put(EMPTY_PANEL, new ShowOnlineHelpAction(null, HelpUrls.AUTHORIZATION));
        helpActions.put(AbstractHttpRequest.BASIC_AUTH_PROFILE, new ShowOnlineHelpAction(null, HelpUrls.AUTHORIZATION_BASIC));
        helpActions.put(CredentialsConfig.AuthType.NTLM.toString(), new ShowOnlineHelpAction(null, HelpUrls.AUTHORIZATION_NTLM));
        helpActions.put(CredentialsConfig.AuthType.SPNEGO_KERBEROS.toString(), new ShowOnlineHelpAction(null, HelpUrls.AUTHORIZATION_SPNEGO_KERBEROS));
        helpActions.put(OAUTH_2_FORM_LABEL, new ShowOnlineHelpAction(null, HelpUrls.AUTHORIZATION_OAUTH2));
    }
}
