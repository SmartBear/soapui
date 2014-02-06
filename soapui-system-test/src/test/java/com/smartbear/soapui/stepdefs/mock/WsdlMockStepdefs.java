package com.smartbear.soapui.stepdefs.mock;

import com.smartbear.soapui.stepdefs.ScenarioRobot;
import com.smartbear.soapui.utils.fest.RestProjectUtils;
import com.smartbear.soapui.utils.fest.SoapProjectUtils;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.*;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.doesLabelExist;
import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static com.smartbear.soapui.utils.fest.ApplicationUtils.startSoapUI;
import static com.smartbear.soapui.utils.fest.RestProjectUtils.createNewRestProject;
import static com.smartbear.soapui.utils.fest.RestProjectUtils.findRestRequestPopupMenu;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.createNewSoapProject;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.findSoapOperationPopupMenu;
import static com.smartbear.soapui.utils.fest.RestProjectUtils.findRestRequestPopupMenu;
import static org.junit.Assert.assertTrue;


public class WsdlMockStepdefs
{
	private Robot robot;
	private FrameFixture rootWindow;
	private JTreeNodeFixture rightClickMenu;

	public WsdlMockStepdefs( ScenarioRobot runner )
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
			rightClickMenu = findSoapOperationPopupMenu( getMainWindow( robot ) );
            Thread.sleep( 2000 );
		}
        Thread.sleep( 200 );
	}

	@Then( "^“(.*)” option is available$" )
	public void _add_to_mock_service_option_is_available(String menuItemLabel) throws Throwable
	{
		assertTrue( "Didn't find the " + menuItemLabel + " menu item", doesLabelExist( rightClickMenu, menuItemLabel ) );
        Thread.sleep( 2000 );
	}

}
