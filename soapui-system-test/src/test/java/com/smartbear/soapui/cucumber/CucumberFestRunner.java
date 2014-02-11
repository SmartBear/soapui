package com.smartbear.soapui.cucumber;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.project.Project;
import com.smartbear.soapui.utils.IntegrationTest;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.List;

@Category( IntegrationTest.class )
@RunWith( Cucumber.class )
@CucumberOptions(
		glue = "com.smartbear.soapui.stepdefs",
		features = "src/test/resources/features/",
		tags = "@Automated",
		format = "json:target/cucumber.json" )
public class CucumberFestRunner
{
	public static final int WAIT_FOR_LAST_TEST_TO_SHUTDOWN = 3000;
	private static NoExitSecurityManagerInstaller noExitSecurityManagerInstaller;

	@BeforeClass
	public static void setUp()
	{
		System.out.println("Installing jvm exit protection");
		System.setProperty( "soapui.jxbrowser.disable", "true" );
		noExitSecurityManagerInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager( new ExitCallHook()
		{
			@Override
			public void exitCalled( int status )
			{
				System.out.println( "Exit status : " + status );
			}
		} );
	}

	@AfterClass
	public static void tearDown() throws InterruptedException
	{
		// TODO This is needed to ensure that the last test have stopped before uninstalling, we need a more
		// clever way to wait for the test though
		Thread.sleep( WAIT_FOR_LAST_TEST_TO_SHUTDOWN );
		System.out.println( "Shuting down jvm exit protection" );
		noExitSecurityManagerInstaller.uninstall();
	}

}