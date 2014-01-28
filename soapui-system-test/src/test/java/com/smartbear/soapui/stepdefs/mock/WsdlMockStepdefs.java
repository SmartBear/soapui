package com.smartbear.soapui.stepdefs.mock;

import com.smartbear.soapui.stepdefs.ScenarioRobot;
import com.smartbear.soapui.utils.fest.WorkspaceUtils;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.exception.LocationUnavailableException;
import org.fest.swing.fixture.*;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static com.smartbear.soapui.utils.fest.ApplicationUtils.startSoapUI;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.createNewSoapProject;
import static org.junit.Assert.assertTrue;

public class WsdlMockStepdefs
{
	private Robot robot;
	private FrameFixture rootWindow;
	private JTreeNodeFixture treeNode;

	public WsdlMockStepdefs( ScenarioRobot runner )
	{
		robot = runner.getRobot();
	}

	@Given( "^SoapUI Project exists$" )
	public void SoapUI_Project_exists() throws Throwable
	{
		startSoapUI();
		rootWindow = getMainWindow( robot );
		createNewSoapProject( rootWindow, robot );
	}

	@When( "^in (.*) context$" )
	public void in_tree_node_context(String NodeLabel) throws Throwable
	{
		JTreeFixture tree = WorkspaceUtils.getNavigatorPanel( rootWindow ).tree();
		treeNode = getTreeNode( tree, "Projects/test/GeoCode_Binding/geocode" );
	}

	@Then( "^“add to mock service” option is available$" )
	public void _add_to_mock_service_option_is_available() throws Throwable
	{
		JPopupMenuFixture menuItem = getPopupMenuFixture();
		assertTrue( "Didn't find the MockService menu item", doesLabelExist( menuItem, "MockService" ) );
	}

	private boolean doesLabelExist( JPopupMenuFixture menuItem, String mockService )
	{
		boolean foundLabel = false;
		for(String label : menuItem.menuLabels() )
		{
			if(label.contains( mockService ))
			{
				foundLabel = true;
			}
		}
		return foundLabel;
	}

	private JPopupMenuFixture getPopupMenuFixture()
	{
		JTreePathFixture contextFixture = ( JTreePathFixture )treeNode.rightClick();
		return contextFixture.showPopupMenu();
	}

	protected JTreeNodeFixture getTreeNode(JTreeFixture tree, String path) throws Throwable
	{
		JTreeNodeFixture treeNode = null;

		try
		{
		   treeNode = tree.node( path );
		}
		catch(LocationUnavailableException e)
		{
			Thread.sleep(200);
			return getTreeNode( tree, path );
		}
		return treeNode;
	}

}
