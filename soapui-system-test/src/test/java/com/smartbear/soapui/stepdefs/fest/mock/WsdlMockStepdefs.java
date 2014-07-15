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
package com.smartbear.soapui.stepdefs.fest.mock;

import com.smartbear.soapui.stepdefs.fest.ScenarioRobot;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTreeNodeFixture;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.doesLabelExist;
import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.findSoapOperationPopupMenu;
import static org.junit.Assert.assertTrue;

public class WsdlMockStepdefs {
    private Robot robot;
    private FrameFixture rootWindow;
    private JTreeNodeFixture rightClickMenu;

    public WsdlMockStepdefs(ScenarioRobot runner) {
        robot = runner.getRobot();
    }

    @When("^in rest (.*) context$")
    public void in_rest_tree_node_context(String context) throws Throwable {
    }

    @When("^in soap (.*) context$")
    public void in_soap_tree_node_context(String context) throws Throwable {
        Thread.sleep(200);
        if ("operation".equals(context)) {
            rightClickMenu = findSoapOperationPopupMenu(getMainWindow(robot));
        }
    }

    @Then("^“(.*)” option is available$")
    public void _add_to_mock_service_option_is_available(String menuItemLabel) throws Throwable {
        assertTrue("Didn't find the " + menuItemLabel + " menu item", doesLabelExist(rightClickMenu, menuItemLabel));
    }
}