package com.smartbear.soapui.stepdefs.rest.project;

import com.eviware.soapui.support.editor.inspectors.auth.AuthInspectorFactory;
import com.smartbear.soapui.stepdefs.ScenarioRobot;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static com.smartbear.soapui.utils.fest.RestProjectUtils.createNewRestProject;

public class RestProjectStepdefs
{
	private final Robot robot;
	private final FrameFixture rootWindow;

	public RestProjectStepdefs( ScenarioRobot runner )
	{
		robot = runner.getRobot();
		rootWindow = getMainWindow( robot );
	}

	@Given("^a new REST project is created$")
	public void createRestProject()
	{
		createNewRestProject( rootWindow, robot );
	}

	@When("^the user clicks on the Auth tab$")
	public void clickOnTheAuthTab()
	{
		rootWindow.toggleButton( AuthInspectorFactory.INSPECTOR_ID ).click();
	}
}