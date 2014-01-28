package com.smartbear.soapui.stepdefs.soap.project;

import com.eviware.soapui.support.editor.inspectors.auth.AuthInspectorFactory;
import com.smartbear.soapui.stepdefs.ScenarioRobot;
import com.smartbear.soapui.utils.fest.SoapProjectUtils;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.openRequestEditor;

public class SoapProjectStepdefs
{
	private Robot robot;
	private FrameFixture rootWindow;

	public SoapProjectStepdefs( ScenarioRobot runner )
	{
		robot = runner.getRobot();
		rootWindow = getMainWindow( robot );
	}

	@Given( "^a new SOAP project is created$" )
	public void createNewSoapProject()
	{
		SoapProjectUtils.createNewSoapProject( rootWindow, robot );
	}

	@When( "^the the user opens the SOAP request editor$" )
	public void openSoapRequestEditor()
	{
		openRequestEditor( rootWindow );
	}

	@When("^clicks on the Auth tab$")
	public void clickOnTheAuthTab()
	{
		rootWindow.toggleButton( AuthInspectorFactory.INSPECTOR_ID ).click();
	}
}