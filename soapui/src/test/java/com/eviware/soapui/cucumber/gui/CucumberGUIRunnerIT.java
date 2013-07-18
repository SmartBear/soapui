package com.eviware.soapui.cucumber.gui;

import com.eviware.soapui.SoapUISystemProperties;
import cucumber.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@Cucumber.Options(format = {"json-pretty:target/cucumber-gui-report.json"}, features = "src/test/resources/features/gui")
public class CucumberGUIRunnerIT
{
	// TODO Using default log level for debugging on Jenkins for now.

	private static final String oldLog4jConfigFile = System.getProperty( SoapUISystemProperties.SOAPUI_LOG4j_CONFIG_FILE, "" );

	@BeforeClass
	public static void setUp()
	{
		System.setProperty( SoapUISystemProperties.SOAPUI_LOG4j_CONFIG_FILE, "");

	}

	@AfterClass
	public static void tearDown()
	{
		System.setProperty( SoapUISystemProperties.SOAPUI_LOG4j_CONFIG_FILE, oldLog4jConfigFile);
	}
}
