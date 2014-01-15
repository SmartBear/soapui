package com.eviware.soapui.cucumber.gui;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

import static org.junit.Assert.assertTrue;

public class CucumberGUIRunnerStepdefs
{

	FESTRunner runner = new FESTRunner();

	@Given("^the soapUI OS is installed in windows-(\\d+)-bit$")
	public void soapUIIsInstalled(String version) throws Throwable
	{
	}

	@When("^user open the SoapUI OS$")
	public void usingASpecifictVersion() throws Throwable
	{
		runner.startSoapUI();
	}

	@Then( "^ensure the starter page is showing up without error$" )
	public void EnsureThatTheStarterPageIsShowingUp() throws Throwable
	{
		assertTrue( "The main window is titled SoapUI", runner.getFrame().target.getTitle().contains( "SoapUI" ) );

		runner.shutdown();
	}
}