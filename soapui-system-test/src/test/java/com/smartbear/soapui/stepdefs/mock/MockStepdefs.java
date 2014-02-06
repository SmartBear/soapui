package com.smartbear.soapui.stepdefs.mock;

import com.smartbear.soapui.stepdefs.ScenarioRobot;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.JTreeNodeFixture;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.*;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.*;
import static org.junit.Assert.assertTrue;


public class MockStepdefs
{
	private Robot robot;
	private JTreeNodeFixture rightClickMenu;

	public MockStepdefs( ScenarioRobot runner )
	{
		robot = runner.getRobot();
	}

	@When( "^in rest (.*) context$" )
	public void in_rest_tree_node_context(String context) throws Throwable
	{

	}

	@When( "^in soap (.*) context$" )
	public void in_soap_tree_node_context(String context) throws Throwable
	{
		Thread.sleep( 200 );
		if( "operation".equals( context ) )
		{
			rightClickMenu = (JTreeNodeFixture)findSoapOperationPopupMenu( getMainWindow( robot ) ).rightClick();
		}
		if( "request".equals( context ) )
		{
			rightClickMenu = (JTreeNodeFixture)findSoapRequestPopupMenu( getMainWindow( robot ) ).rightClick();
		}
      Thread.sleep( 200 );
	}

	@Then( "^“(.*)” option is available$" )
	public void _option_is_available(String menuItemLabel) throws Throwable
	{
		assertTrue( "Didn't find the " + menuItemLabel + " menu item", doesLabelExist( rightClickMenu, menuItemLabel ) );
	}

}
