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
package com.smartbear.soapui.stepdefs.application;

import com.eviware.soapui.support.ConsoleDialogs;
import com.eviware.soapui.support.UISupport;
import com.smartbear.soapui.stepdefs.ScenarioRobot;
import com.smartbear.soapui.utils.fest.ApplicationUtils;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.closeApplicationWithoutSaving;
import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;

public class ApplicationStepdefs {
    private Robot robot;

    public ApplicationStepdefs(ScenarioRobot runner) {
        robot = runner.getRobot();
    }

    @Before
    public void startSoapUI() {
        ApplicationUtils.startSoapUI();
    }

    @After
    public void closeSoapUIIfRunning() {
        try {
            FrameFixture mainWindow = getMainWindow(robot);
            if (mainWindow != null) {
                closeApplicationWithoutSaving(mainWindow, robot);
            }
        } catch (Exception e) {
            //Most probably SoapUI is not running.
        }
        robot.cleanUp();
        UISupport.setDialogs(new ConsoleDialogs());
    }
}