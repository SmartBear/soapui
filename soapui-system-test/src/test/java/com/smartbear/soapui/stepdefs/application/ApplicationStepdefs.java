package com.smartbear.soapui.stepdefs.application;

import com.eviware.soapui.support.ConsoleDialogs;
import com.eviware.soapui.support.UISupport;
import com.smartbear.soapui.stepdefs.ScenarioRobot;
import com.smartbear.soapui.utils.fest.ApplicationUtils;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ApplicationStepdefs
{
	private FrameFixture rootWindow;
	private Robot robot;

	public ApplicationStepdefs( ScenarioRobot runner )
	{
		robot = runner.getRobot();
	}

	@When("^SoapUI is started$")
	public void openSoapUI()
	{
		ApplicationUtils.startSoapUI();
		rootWindow = getMainWindow( robot );
	}

	@Then("^ensure that the main window is showing up without error$")
	public void ensureThatTheMainWindowsIsShowing()
	{
		assertThat( rootWindow, not( nullValue() ) );
	}

	@Then("^close SoapUI$")
	public void closeSoapUI()
	{
		rootWindow.close();
		robot.cleanUp();
		UISupport.setDialogs( new ConsoleDialogs() );
	}
}