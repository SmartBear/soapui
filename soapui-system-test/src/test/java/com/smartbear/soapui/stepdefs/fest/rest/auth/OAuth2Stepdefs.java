/*
 * Copyright 2004-2014 SmartBear Software
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
package com.smartbear.soapui.stepdefs.fest.rest.auth;

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.editor.inspectors.auth.OAuth2Form;
import com.eviware.soapui.support.editor.inspectors.auth.OAuth2GetAccessTokenForm;
import com.eviware.soapui.support.editor.inspectors.auth.ProfileSelectionForm;
import com.smartbear.soapui.stepdefs.fest.ScenarioRobot;
import com.smartbear.soapui.utils.fest.FestMatchers;
import com.smartbear.soapui.utils.fest.FestUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JComboBoxFixture;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.List;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static com.smartbear.soapui.utils.fest.FestMatchers.buttonWithText;
import static com.smartbear.soapui.utils.fest.FestUtils.findDialog;
import static com.smartbear.soapui.utils.fest.FestUtils.verifyButtonIsNotShowing;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class OAuth2Stepdefs {
    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String AUTHORIZATION_URI = "authorization-uri";
    private static final String ACCESS_TOKEN_URI = "access-token-uri";
    private static final String REDIRECT_URI = "redirect-uri";
    private static final String SCOPE = "scope";
    private static final String ACCESS_TOKEN = "access-token";

    private static final String ADVANCED_OPTIONS_DIALOG_NAME = "OAuth 2 Advanced options";

    private static final String OAUTH_2_COMBOBOX_ITEM = CredentialsConfig.AuthType.O_AUTH_2_0.toString();
    public static final String BUTTON_OK = "OK";

    private FrameFixture rootWindow;
    private final Robot robot;

    public OAuth2Stepdefs(ScenarioRobot runner) {
        robot = runner.getRobot();
        rootWindow = getMainWindow(robot);
    }

    @When("^and fills out all fields$")
    public void fillInAllOAuth2Fields() {
        DialogFixture accessTokenFormDialog = findDialog(OAuth2GetAccessTokenForm.ACCESS_TOKEN_FORM_DIALOG_NAME, robot);
        accessTokenFormDialog.textBox(OAuth2Profile.CLIENT_ID_PROPERTY).setText(CLIENT_ID);
        accessTokenFormDialog.textBox(OAuth2Profile.CLIENT_SECRET_PROPERTY).setText(CLIENT_SECRET);
        accessTokenFormDialog.textBox(OAuth2Profile.AUTHORIZATION_URI_PROPERTY).setText(AUTHORIZATION_URI);
        accessTokenFormDialog.textBox(OAuth2Profile.ACCESS_TOKEN_URI_PROPERTY).setText(ACCESS_TOKEN_URI);
        accessTokenFormDialog.textBox(OAuth2Profile.REDIRECT_URI_PROPERTY).setText(REDIRECT_URI);
        accessTokenFormDialog.textBox(OAuth2Profile.SCOPE_PROPERTY).setText(SCOPE);
    }

    @When("^switches to another Authorization type and then back again to (.+)$")
    public void switchToAnotherAuthorizationTypeAndThenBackAgain(String profileName) {
        selectItemInProfileSelectionComboBox(CredentialsConfig.AuthType.NO_AUTHORIZATION.toString());
        selectItemInProfileSelectionComboBox(profileName);
    }

    @When("^user clicks on Advanced options button$")
    public void clickOnAdvancedOptionsButton() {
        rootWindow.button(OAuth2Form.ADVANCED_OPTIONS_BUTTON_NAME).click();
    }

    @When("^user selects access token position (.+)$")
    public void selectAccessTokenPosition(String accessTokenPosition) {
        getAdvancedDialogFixture().radioButton(accessTokenPosition).click();
    }

    @When("^selects refresh method (.+)$")
    public void selectRefreshMethod(String methodName) {
        getAdvancedDialogFixture().radioButton(methodName).click();
    }

    @When("^closes and reopens the advanced options dialog")
    public void closeAndReOpenAdvancedOptionsDialog() {
        closeAdvancedOptionsDialog();
        clickOnAdvancedOptionsButton();
    }

    @When("^closes the advanced options dialog")
    public void closesAdvancedOptionsDialog() {
        closeAdvancedOptionsDialog();
    }

    @When("^enters the access token$")
    public void entersTheAccessToken() {
        rootWindow.textBox(OAuth2Profile.ACCESS_TOKEN_PROPERTY).setText(ACCESS_TOKEN);
    }

    @When("^clicks on the disclosure button$")
    public void clickOnDisclosureButton() {
        rootWindow.label("oAuth2DisclosureButton").click();
    }

    @When("clicks outside of the Get Access token form$")
    public void clickOutsideOfTheGetAccessTokenForm() {
        rootWindow.focus();
        rootWindow.click();
    }

    @Then("^the OAuth 2 option is not visible in the Authentication Type dropdown$")
    public void verifyThatOAuth2OptionIsNotShownInAuthenticationDropdown() {
        assertThat(getAuthorizationTypeComboBox().contents(), not(hasItemInArray(OAUTH_2_COMBOBOX_ITEM)));
    }

    @Then("^the previously filled fields are still present$")
    public void verifyThatThePreviouslyFilledFieldsAreStillPresent() {
        DialogFixture accessTokenFormDialog = findDialog(OAuth2GetAccessTokenForm.ACCESS_TOKEN_FORM_DIALOG_NAME,
                robot);
        assertThat(accessTokenFormDialog.textBox(OAuth2Profile.CLIENT_ID_PROPERTY).text(), is(CLIENT_ID));
        assertThat(accessTokenFormDialog.textBox(OAuth2Profile.CLIENT_SECRET_PROPERTY).text(), is(CLIENT_SECRET));
        assertThat(accessTokenFormDialog.textBox(OAuth2Profile.AUTHORIZATION_URI_PROPERTY).text(), is(AUTHORIZATION_URI));
        assertThat(accessTokenFormDialog.textBox(OAuth2Profile.ACCESS_TOKEN_URI_PROPERTY).text(), is(ACCESS_TOKEN_URI));
        assertThat(accessTokenFormDialog.textBox(OAuth2Profile.REDIRECT_URI_PROPERTY).text(), is(REDIRECT_URI));
        assertThat(accessTokenFormDialog.textBox(OAuth2Profile.SCOPE_PROPERTY).text(), is(SCOPE));
    }

    @Then("^access token position is (.+)$")
    public void verifyAccessTokenPosition(String expectedAccessTokenPosition) {
        getAdvancedDialogFixture().radioButton(expectedAccessTokenPosition).requireSelected();
    }

    @Then("^refresh method is (.+)$")
    public void verifyRefreshMethod(String expectedRefreshMethod) {
        getAdvancedDialogFixture().radioButton(expectedRefreshMethod).requireSelected();
    }

    @Then("^access token is present$")
    public void verifyThatAccessTokenIsPresent() {
        robot.waitForIdle();
        assertThat(rootWindow.textBox(OAuth2Profile.ACCESS_TOKEN_PROPERTY).text(), is(ACCESS_TOKEN));
    }

    @Then("the Get Access token form is closed$")
    public void verifyThatTheAccessTokenFormIsNotVisible() {
        FestUtils.verifyDialogIsNotShowing(OAuth2GetAccessTokenForm.ACCESS_TOKEN_FORM_DIALOG_NAME, robot);
    }

    @When("^the user selects (.+) in the authorization drop down$")
    public void selectItemInProfileSelectionComboBox(String itemName) {
        JComboBoxFixture comboBox = getProfileSelectionComboBox();
        comboBox.selectItem(itemName);
    }

    @Then("^refresh button is visible$")
    public void verifyThatRefreshButtonIsVisible() throws Throwable {
        rootWindow.button(OAuth2Form.REFRESH_ACCESS_TOKEN_BUTTON_NAME).requireVisible();
    }

    @Then("^refresh button is not visible$")
    public void verifyThatRefreshButtonIsNotVisible() throws Throwable {
        verifyButtonIsNotShowing(rootWindow, OAuth2Form.REFRESH_ACCESS_TOKEN_BUTTON_NAME);
    }

    @And("^sets refresh method to (.+)$")
    public void setRefreshMethod(String methodName) throws Throwable {
        clickOnAdvancedOptionsButton();
        selectRefreshMethod(methodName);
        closeAdvancedOptionsDialog();
    }

    @And("^selects the OAuth 2 flow (.+)$")
    public void selectOAuth2Flow(String flowName) throws Throwable {
        DialogFixture accessTokenFormDialog = findDialog(OAuth2GetAccessTokenForm.ACCESS_TOKEN_FORM_DIALOG_NAME,
                robot);
        accessTokenFormDialog.comboBox(OAuth2GetAccessTokenForm.OAUTH_2_FLOW_COMBO_BOX_NAME).selectItem(flowName);
    }

    @Then("^(.+) field is not visible$")
    public void verifyClientIdFieldIsNotVisible(String fieldName) throws Throwable {
        DialogFixture accessTokenFormDialog = findDialog(OAuth2GetAccessTokenForm.ACCESS_TOKEN_FORM_DIALOG_NAME, robot);
        FestUtils.verifyTextFieldIsNotShowingInDialog(accessTokenFormDialog, fieldName);
    }

    @When("^the user creates an OAuth 2.0 profile with name (.+)$")
    public void createOAuth2Profile(String profileName) throws Throwable {
        selectItemInProfileSelectionComboBox(ProfileSelectionForm.AddEditOptions.ADD.getDescription());
        selectAuthType("OAuth 2.0");
        setProfileNameAndClickOk(profileName);
    }

    @When("^the user creates basic authentication profile for authentication type (.+)$")
    public void createBasicAuthProfileWithName(String profileName) throws Throwable {
        selectItemInProfileSelectionComboBox(ProfileSelectionForm.AddEditOptions.ADD.getDescription());
        selectAuthType(profileName);
        clickOk(getAuthorizationSelectionDialog());
    }

    @Then("^new profile selected with name (.+)$")
    public void verifyTheProfileIsSelected(String profileName) throws Throwable {
        getProfileSelectionComboBox().requireSelection(profileName);
    }

    @And("^user confirms for deletion$")
    public void confirmDeletion() throws Throwable {

        FestMatchers.dialogWithTitle(ProfileSelectionForm.DELETE_PROFILE_DIALOG_TITLE)
                .using(robot).button(buttonWithText("Yes")).click();
    }

    @Then("^the profile with name (.+) is deleted$")
    public void verifyProfileDoesNotExist(String profileName) throws Throwable {
        for (String profile : getProfileSelectionComboBox().contents()) {
            assertThat(profileName, is(Matchers.not(profile)));
        }
    }

    @And("^the changes the name to (.+)$")
    public void setNewProfileName(String newName) throws Throwable {
        DialogFixture renameProfileDialog = FestMatchers.dialogWithTitle(ProfileSelectionForm.RENAME_PROFILE_DIALOG_TITLE)
                .using(robot);
        renameProfileDialog.textBox().setText(newName);
        renameProfileDialog.button(buttonWithText("OK")).click();
    }

    @Then("^available options in authorization drop down are (.+)$")
    public void verifyAddEditOptionsInProfileSelectionComboBox(String values) throws Throwable {
        String[] expectedAddEditOptions = (values + "," + ProfileSelectionForm.OPTIONS_SEPARATOR).split(",");
        List<String> expectedOptionsList = Arrays.asList(expectedAddEditOptions);
        String[] actualOptions = getProfileSelectionComboBox().contents();

        for (String actualOption : actualOptions) {
            assertThat(expectedOptionsList, hasItem(actualOption));
        }
    }

    @And("^user selects to add new profile$")
    public void selectAddNewAuthorizationInProfileSelectionComboBox() throws Throwable {
        selectItemInProfileSelectionComboBox(ProfileSelectionForm.AddEditOptions.ADD.getDescription());
    }

    @And("^closes the authorization type selection dialog$")
    public void closeAuthorizationSelectionDialog() throws Throwable {
        getAuthorizationSelectionDialog().close();
    }


    private void closeAdvancedOptionsDialog() {
        DialogFixture dialogFixture = getAdvancedDialogFixture();
        dialogFixture.button(BUTTON_OK).click();
    }

    private DialogFixture getAdvancedDialogFixture() {
        return rootWindow.dialog(ADVANCED_OPTIONS_DIALOG_NAME);
    }

    private void setProfileNameAndClickOk(String profileName) {
        DialogFixture authorizationSelectionDialog = getAuthorizationSelectionDialog();
        authorizationSelectionDialog.textBox("Profile name").setText(profileName);
        clickOk(authorizationSelectionDialog);
    }

    private void clickOk(DialogFixture authorizationSelectionDialog) {
        authorizationSelectionDialog.button("OK").click();
    }

    private void selectAuthType(String authType) {
        getAuthorizationTypeComboBox().selectItem(authType);
    }

    private JComboBoxFixture getAuthorizationTypeComboBox() {
        return getAuthorizationSelectionDialog().comboBox("Type");
    }

    private DialogFixture getAuthorizationSelectionDialog() {
        return findDialog("Add Authorization", robot);
    }


    private JComboBoxFixture getProfileSelectionComboBox() {
        JComboBoxFixture comboBox = rootWindow.comboBox(ProfileSelectionForm.PROFILE_COMBO_BOX);
        comboBox.focus();
        return comboBox;
    }

}