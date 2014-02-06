package com.smartbear.soapui.stepdefs.application;

import com.eviware.soapui.support.ConsoleDialogs;
import com.eviware.soapui.support.UISupport;
import com.smartbear.soapui.stepdefs.ScenarioRobot;
import com.smartbear.soapui.utils.fest.ApplicationUtils;
import com.smartbear.soapui.utils.fest.FestMatchers;
import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JButtonFixture;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.closeApplicationWithoutSaving;
import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ApplicationStepdefs
{
	private Robot robot;

	public ApplicationStepdefs( ScenarioRobot runner )
	{
		robot = runner.getRobot();
	}

	@When( "^SoapUI is started$" )
	public void startSoapUI()
	{
		ApplicationUtils.startSoapUI();
	}

	@Then( "^ensure that the main window is showing up without error$" )
	public void ensureThatTheMainWindowsIsShowing()
	{
		FrameFixture rootWindow = getMainWindow( robot );
		assertThat( rootWindow, not( nullValue() ) );
	}

	@Then( "^close SoapUI$" )
	public void closeSoapUI()
	{
		FrameFixture rootWindow = getMainWindow( robot );
		closeApplicationWithoutSaving(rootWindow, robot);
		robot.cleanUp();
		UISupport.setDialogs( new ConsoleDialogs() );
	}

	@After
	public void closeSoapUIIfRunning()
	{
		try
		{
			FrameFixture mainWindow = getMainWindow( robot );
			if( mainWindow != null )
			{
				closeApplicationWithoutSaving( mainWindow, robot );
			}
		}
		catch( Exception e )
		{
			//Most probably SoapUI is not running.
		}
	}

	@When( "^click \"([^\"]*)\" in \"([^\"]*)\" dialog$" )
	public void click_in_dialog( String buttonLabel, String dialogTitle ) throws Throwable
	{
		DialogFixture dialog = FestMatchers.dialogWithTitle( dialogTitle ).withTimeout( 500 ).using( robot );
		dialog.textBox().focus();

		JButtonFixture button = dialog.button( FestMatchers.buttonWithText( buttonLabel ) );
		button.click();
	}
}