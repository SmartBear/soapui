package com.smartbear.soapui.stepdefs.soap.project;

import com.eviware.soapui.support.editor.inspectors.auth.AuthInspectorFactory;
import com.smartbear.soapui.stepdefs.ScenarioRobot;
import com.smartbear.soapui.utils.fest.SoapProjectUtils;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTreeNodeFixture;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.doesLabelExist;
import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.findSoapOperationPopupMenu;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.findSoapRequestPopupMenu;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.openRequestEditor;
import static org.junit.Assert.assertTrue;

public class SoapProjectStepdefs
{
	private Robot robot;
	private FrameFixture rootWindow;
	private JTreeNodeFixture currentTreeNode;

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

	@When( "^the user opens the SOAP request editor$" )
	public void openSoapRequestEditor()
	{
		openRequestEditor( rootWindow );
	}

	@When("^clicks on the Auth tab$")
	public void clickOnTheAuthTab()
	{
		rootWindow.toggleButton( AuthInspectorFactory.INSPECTOR_ID ).click();
	}

	@When( "^in soap (.*) context$" )
	public void _in_tree_node_context(String context) throws Throwable
	{
		Thread.sleep( 200 );
		if( "operation".equals( context ) )
		{
			currentTreeNode = findSoapOperationPopupMenu( getMainWindow( robot ) );
		}
		if( "request".equals( context ) )
		{
			currentTreeNode = findSoapRequestPopupMenu( getMainWindow( robot ) );
		}
		Thread.sleep( 200 );
	}

	@When( "^right clicking the current soap context")
	public void _right_clicking_the_current_soap_context() throws Throwable
	{
		currentTreeNode = ( JTreeNodeFixture )currentTreeNode.rightClick();
	}


	@Then( "^“(.*)” soap option is available$" )
	public void _option_is_available(String menuItemLabel) throws Throwable
	{
		assertTrue( "Didn't find the " + menuItemLabel + " menu item", doesLabelExist( currentTreeNode, menuItemLabel ) );
	}

}