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
package com.smartbear.soapui.stepdefs.fest.project;

import com.eviware.soapui.support.editor.inspectors.auth.AuthInspectorFactory;
import com.smartbear.soapui.stepdefs.fest.ScenarioRobot;
import com.smartbear.soapui.utils.fest.SoapProjectUtils;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.openRequestEditor;

public class SoapProjectStepdefs {
    private Robot robot;
    private FrameFixture rootWindow;

    public SoapProjectStepdefs(ScenarioRobot runner) {
        robot = runner.getRobot();
        rootWindow = getMainWindow(robot);
    }

    @Given("^a new SOAP project is created$")
    public void createNewSoapProject() {
        SoapProjectUtils.createNewSoapProject(rootWindow, robot);
    }

    @When("^the user opens the SOAP request editor$")
    public void openSoapRequestEditor() {
        openRequestEditor(rootWindow);
    }

    @When("^clicks on the Auth tab$")
    public void clickOnTheAuthTab() {
        rootWindow.toggleButton(AuthInspectorFactory.INSPECTOR_ID).click();
    }
}