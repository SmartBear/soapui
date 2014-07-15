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
package com.smartbear.soapui.stepdefs.fest.rest.project;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.panels.request.RestRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.editor.inspectors.auth.AuthInspectorFactory;
import com.smartbear.soapui.stepdefs.fest.ScenarioRobot;
import com.smartbear.soapui.utils.fest.RestProjectUtils;
import com.smartbear.soapui.utils.fest.WorkspaceUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.fixture.JTreeNodeFixture;

import java.util.List;

import static com.eviware.soapui.impl.rest.panels.resource.RestParamsTable.REST_PARAMS_TABLE;
import static com.smartbear.soapui.utils.fest.ApplicationUtils.doesLabelExist;
import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static com.smartbear.soapui.utils.fest.RestProjectUtils.*;
import static org.fest.swing.data.TableCell.row;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RestProjectStepdefs {
    private final Robot robot;
    private final FrameFixture rootWindow;
    private final List<String> existingProjectNameList;
    private int newProjectIndexInNavigationTree;
    private String projectName;

    public RestProjectStepdefs(ScenarioRobot runner) {
        robot = runner.getRobot();
        rootWindow = getMainWindow(robot);
        //This is required to find the name of the newly created project
        existingProjectNameList = WorkspaceUtils.getProjectNameList();
    }

    @Given("^a new REST project is created$")
    public void createNewRestProject() {
        RestProjectUtils.createNewRestProject(rootWindow, robot);
          /*
		FEST doesn't handle the path when the node names include /, which is generally the case in resource name in REST
		Project. Hence we need to use the index to traverse the new projects and it's children in the navigation tree.
		 */
        newProjectIndexInNavigationTree = findTheIndexOfCurrentProjectInNavigationTree();
    }

    @Given("^a new REST project is created with URI (.+)$")
    public void createNewRestProjectWithUri(String uri) {
        RestProjectUtils.createNewRestProjectWithUri(rootWindow, robot, uri);
        newProjectIndexInNavigationTree = findTheIndexOfCurrentProjectInNavigationTree();
    }

    @When("^the user clicks on the Auth tab$")
    public void clickOnTheAuthTab() {
        rootWindow.toggleButton(AuthInspectorFactory.INSPECTOR_ID).click();
    }

    @When("^user adds a parameter in request editor with name (.+) and value (.+)$")
    public void addRestParameterInRequestEditor(String name, String value) {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        addNewParameter(requestEditor, robot, name, value);
    }

    @When("^user adds a parameter in method editor with name (.+) and value (.+)$")
    public void addRestParameterInMethodEditor(String name, String value) {
        JPanelFixture methodEditor = findMethodEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        addNewParameter(methodEditor, robot, name, value);
    }

    @When("^user adds a parameter in resource editor with name (.+) and value (.+)$")
    public void addRestParameterInResourceEditor(String name, String value) {
        JPanelFixture resourceEditor = findResourceEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        addNewParameter(resourceEditor, robot, name, value);
    }

    @When("^user changes the name to (.+) for parameter with name (.+)$")
    public void changesParameterName(String newName, String parameterName) throws Throwable {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        changeParameterName(requestEditor, parameterName, newName, robot);
    }

    @And("^user changes the value to (.+) for parameter with value (.+)$")
    public void changesParameterValue(String newValue, String parameterValue) throws Throwable {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        changeParameterValue(requestEditor, parameterValue, newValue, robot);
    }

    @When("^user changes the level to (.+) for parameter with name (.+)$")
    public void changesParameterLevel(String newLevel, String parameterName) {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        changeParameterLevel(requestEditor, parameterName, newLevel, robot);
    }

    @And("^in resource editor user changes the style to (.+) for parameter with name (.+)$")
    public void changesParameterStyleInResourceEditor(String newStyle, String parameterName) {
        JPanelFixture resourceEditor = findResourceEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        changeParameterStyle(resourceEditor, parameterName, newStyle, robot);
    }

    @And("^user adds a custom property to project with name (.+) and value (.+)$")
    public void user_adds_a_custom_property_to_project_with_name_prop_and_value(String propertyName, String propValue) {
        RestProjectUtils.addCustomProperty(newProjectIndexInNavigationTree, rootWindow, propertyName, propValue, robot);
    }

    @And("^in method editor user changes the style to (.+) for parameter with name (.+)$")
    public void changesParameterStyleInMethodEditor(String newStyle, String parameterName) {
        JPanelFixture methodEditor = findMethodEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        changeParameterStyle(methodEditor, parameterName, newStyle, robot);
    }

    @And("^user changes the style to (.+) for parameter with name (.+)$")
    public void changesParameterStyleInRequestEditor(String newStyle, String parameterName) {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        changeParameterStyle(requestEditor, parameterName, newStyle, robot);
    }

    @When("^in resource editor user changes the name to (.+) for parameter with name (.+)$")
    public void changesParameterNameInResourceEditor(String newName, String parameterName) throws Throwable {
        JPanelFixture resourceEditor = findResourceEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        changeParameterName(resourceEditor, parameterName, newName, robot);
    }

    @And("^in method editor user changes the value to (.+) for parameter with value (.+)$")
    public void changesParameterValueInMethodEditor(String newValue, String parameterValue) throws Throwable {
        JPanelFixture methodEditor = findMethodEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        changeParameterValue(methodEditor, parameterValue, newValue, robot);
    }

    @Then("^request editor has parameter with name (.+) and value (.+) at row (.+)$")
    public void verifyRequestEditorShowsParameter(String parameterName, String parameterValue, Integer index) {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        verifyParamValues(requestEditor, index, parameterName, parameterValue);
    }

    @Then("^resource editor has parameter with name (.+) and with empty value at row (.+)$")
    public void verifyResourceEditorShowsTheParameterWithEmptyValue(String parameterName, Integer index) {
        verifyResourceEditorShowsTheParameter(parameterName, "", index);
    }

    @Then("^resource editor has parameter with name (.+) and value (.+) at row (.+)$")
    public void verifyResourceEditorShowsTheParameter(String parameterName, String parameterValue, Integer index) {
        JPanelFixture resourceEditor = findResourceEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        verifyParamValues(resourceEditor, index, parameterName, parameterValue);
    }

    @Then("^method editor has parameter with name (.+) and value (.+) at row (.+)$")
    public void verifyMethodEditorShowsTheParameter(String parameterName, String parameterValue, Integer index) {
        JPanelFixture methodEditor = findMethodEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        verifyParamValues(methodEditor, index, parameterName, parameterValue);
    }

    @Then("^request editor has parameter with name (.+) and style (.+) at row (\\d+)$")
    public void verifyRequestEditorParameterStyle(String parameterName, String parameterStyle, Integer index) {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        verifyParamStyles(requestEditor, index, parameterName, parameterStyle);
    }

    @Then("^request editor has no parameters$")
    public void verifyRequestEditorHasEmptyParameterTable() {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        verifyEmptyTable(requestEditor);
    }

    @Then("^resource editor has no parameters$")
    public void verifyResourceEditorHasEmptyParameterTable() {
        JPanelFixture resourceEditor = findResourceEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        verifyEmptyTable(resourceEditor);
    }

    @Then("^method editor has no parameters$")
    public void verifyMethodEditorHasEmptyParameterTable() {
        JPanelFixture methodEditor = findMethodEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        verifyEmptyTable(methodEditor);
    }

    @Then("^“(.*)” option is available on REST Request$")
    public void _add_to_mock_service_option_is_available(String menuItemLabel) throws Throwable {
        JTreeNodeFixture popupMenu = findRestRequestPopupMenu(getMainWindow(robot), newProjectIndexInNavigationTree);
        assertTrue("Didn't find the " + menuItemLabel + " menu item", doesLabelExist(popupMenu, menuItemLabel));
    }


    private void verifyEmptyTable(JPanelFixture parentPanel) {
        JTableFixture restParamsTable = parentPanel.table(REST_PARAMS_TABLE);
        assertThat(restParamsTable.target.getRowCount(), is(0));
    }

    private void verifyParamValues(JPanelFixture parentPanel, int rowNum, String paramName, String paramValue) {
        JTableFixture paramTableInResourceEditor = parentPanel.table(REST_PARAMS_TABLE);
        assertThat(paramTableInResourceEditor.cell(row(rowNum).column(0)).value(), is(paramName));
        assertThat(paramTableInResourceEditor.cell(row(rowNum).column(1)).value(), is(paramValue));

    }

    private void verifyParamStyles(JPanelFixture parentPanel, int rowNum, String paramName, String paramStyle) {
        JTableFixture paramTableInResourceEditor = parentPanel.table(REST_PARAMS_TABLE);
        assertThat(paramTableInResourceEditor.cell(row(rowNum).column(0)).value(), is(paramName));
        assertThat(paramTableInResourceEditor.cell(row(rowNum).column(2)).value(), is(paramStyle));
    }

    private int findTheIndexOfCurrentProjectInNavigationTree() {
        this.projectName = findProjectName();
        return WorkspaceUtils.getProjectNameList().indexOf(projectName) + 1;
    }

    private String findProjectName() {
        List<String> projectNameListWithNewProject = WorkspaceUtils.getProjectNameList();
        projectNameListWithNewProject.removeAll(existingProjectNameList);
        return projectNameListWithNewProject.get(0);
    }

    @And("^there is a refresh token in the profile with name (.+)$")
    public void setRefreshTokenInOAuth2Profile(String profileName) throws Throwable {
        String projectName = WorkspaceUtils.getNavigatorPanel(rootWindow).tree().node(newProjectIndexInNavigationTree).value();
        WsdlProject project = (WsdlProject) SoapUI.getWorkspace().getProjectByName(projectName);
        for (OAuth2Profile profile : project.getOAuth2ProfileContainer().getOAuth2ProfileList()) {
            if (profile.getName().equals(profileName)) {
                profile.setRefreshToken("Dummy#Refresh#Token");
            }
        }
    }

    @When("^user deletes the parameter in request editor at row (\\d+)$")
    public void deleteParameterInRequestEditor(int rowNum) throws Throwable {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        RestProjectUtils.deleteParameter(requestEditor, rowNum);
    }

    @When("^user deletes the parameter in resource editor with name (.+)$")
    public void deleteParameterInResourceEditor(String paramName) throws Throwable {
        JPanelFixture resourceEditor = findResourceEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        RestProjectUtils.deleteParameter(resourceEditor, paramName);
    }

    @And("^user deletes the parameter in method editor with name (.+)$")
    public void deleteParameterInMethodEditor(String paramName) throws Throwable {
        JPanelFixture methodEditor = findMethodEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        RestProjectUtils.deleteParameter(methodEditor, paramName);
    }

    @And("^Parameters field is empty in top URI bar$")
    public void verifyParametersFieldIsEmpty() throws Throwable {
        rootWindow.textBox("ParametersField").requireEmpty();
    }

    @And("^Parameters field in top URI bar has value (.+)$")
    public void verifyParametersFieldValue(String expectedValue) {
        rootWindow.textBox("ParametersField").requireText(expectedValue);
    }

    @When("^user move up the parameter in request editor with name (.+)$")
    public void moveUpParameterInRequestEditor(String paramName) throws Throwable {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        RestProjectUtils.moveUpParameter(requestEditor, paramName);
    }

    @And("^user move down the parameter in request editor with name (.+)$")
    public void moveDownParameterInRequestEditor(String paramName) throws Throwable {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        RestProjectUtils.moveDownParameter(requestEditor, paramName);
    }

    @When("^user move up the parameter in resource editor with name (.+)$")
    public void moveUpParameterInResourceEditor(String paramName) throws Throwable {
        JPanelFixture resourceEditor = findResourceEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        RestProjectUtils.moveUpParameter(resourceEditor, paramName);
    }

    @And("^user move down the parameter in method editor with name (.+)$")
    public void moveDownParameterInMethodEditor(String paramName) throws Throwable {
        JPanelFixture methodEditor = findMethodEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        RestProjectUtils.moveDownParameter(methodEditor, paramName);
    }

    @When("^user clicks the revert all parameters values button$")
    public void revertParameterValue() throws Throwable {
        JPanelFixture requestEditor = findRequestEditor(rootWindow, newProjectIndexInNavigationTree, robot);
        RestProjectUtils.revertParameterValue(requestEditor);
    }

    @And("^user creates a method named List under the resource named Search$")
    public void user_creates_a_method_named_List_under_the_resource_named_Search() throws Throwable {

        WorkspaceUtils.getNavigatorPanel(rootWindow).tree().node(3).rightClick();
    }

    @And("^user creates a method with name (.+) under the resource with name (.+) for interface (.+)$")
    public void createMethodUnderResource(String methodName, String resourceName, String interfaceName) {
        String path = WorkspaceUtils.getProjectNavigationPath(projectName) + interfaceName + WorkspaceUtils.NAVIGATION_TREE_PATH_SEPARATOR + resourceName;

        addNewRESTMethodAtPath(robot, rootWindow, methodName, path);
    }

    @And("^user open the request at path (.+)$")
    public void openTreeItemWithPath(String path) {
        String fullPath = WorkspaceUtils.getProjectNavigationPath(projectName) + path;
        WorkspaceUtils.getNavigationTree(rootWindow).node(fullPath).doubleClick();
    }

    @When("^user changes the parameter level to (.+) for parameter with name (.+) in request editor for request with path (.+)$")
    public void changeParameterLevelInRequestEditor(String newLevel, String paramName, String reqPath) {
        closeAlreadyOpenedDesktopEditors(rootWindow, RestRequestDesktopPanel.REST_REQUEST_EDITOR);
        openTreeItemWithPath(reqPath);
        JPanelFixture requestEditor = locateRequestEditor(rootWindow);
        changeParameterLevel(requestEditor, paramName, newLevel, robot);
    }
}
