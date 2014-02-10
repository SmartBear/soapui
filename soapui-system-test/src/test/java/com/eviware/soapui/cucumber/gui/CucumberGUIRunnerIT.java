package com.eviware.soapui.cucumber.gui;

import com.eviware.soapui.SoapUISystemProperties;
import cucumber.junit.Cucumber;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;

@Ignore
@RunWith(Cucumber.class)
@Cucumber.Options(format = { "json-pretty:target/cucumber-gui-report.json" }, features = "src/test/resources/features/gui")
public class CucumberGUIRunnerIT
{
	private static NoExitSecurityManagerInstaller noExitSecurityManagerInstaller;

	@BeforeClass
	public static void setUp()
	{
		noExitSecurityManagerInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager( new ExitCallHook()
		{
			@Override
			public void exitCalled( int status )
			{
				System.out.print( "Exit status : " + status );
			}
		} );

	}

	@AfterClass
	public static void tearDown()
	{
		noExitSecurityManagerInstaller.uninstall();
	}
}
