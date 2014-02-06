package com.smartbear.soapui.stepdefs.rest.project;

import com.eviware.soapui.support.editor.inspectors.auth.AuthInspectorFactory;
import com.smartbear.soapui.stepdefs.ScenarioRobot;
import com.smartbear.soapui.utils.fest.RestProjectUtils;
import com.smartbear.soapui.utils.fest.WorkspaceUtils;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.fixture.JTreeNodeFixture;

import java.util.List;

import static com.eviware.soapui.impl.rest.panels.resource.RestParamsTable.REST_PARAMS_TABLE;
import static com.smartbear.soapui.utils.fest.ApplicationUtils.doesLabelExist;
import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static com.smartbear.soapui.utils.fest.RestProjectUtils.*;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.findSoapOperationPopupMenu;
import static com.smartbear.soapui.utils.fest.SoapProjectUtils.findSoapRequestPopupMenu;
import static org.fest.swing.data.TableCell.row;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RestProjectStepdefs
{
	private final Robot robot;
	private final FrameFixture rootWindow;
	private final List<String> existingProjectNameList;
	private int newProjectIndexInNavigationTree;
	private JTreeNodeFixture currentTreeNode;

	public RestProjectStepdefs( ScenarioRobot runner )
	{
		robot = runner.getRobot();
		rootWindow = getMainWindow( robot );
		//This is required to find the name of the newly created project
		existingProjectNameList = WorkspaceUtils.getProjectNameList();
	}

	@Given( "^a new REST project is created$" )
	public void createNewRestProject()
	{
		RestProjectUtils.createNewRestProject( rootWindow, robot );
		/*
		FEST doesn't handle the path when the node names include /, which is generally the case in resource name in REST
		Project. Hence we need to use the index to traverse the new projects and it's children in the navigation tree.
		 */
		newProjectIndexInNavigationTree = findTheIndexOfCurrentProjectInNavigationTree();
	}

   @When( "^context is open$" )
   public void _add_to_mock_service_option_is_available_MB()
   {
      JTreeNodeFixture popupMenu = findRestRequestPopupMenu( getMainWindow( robot ), newProjectIndexInNavigationTree );
      //assertTrue( "Didn't find the " + menuItemLabel + " menu item", doesLabelExist( popupMenu, menuItemLabel ) );
   }


	@When( "^in rest (.*) context$" )
	public void _in_tree_node_context(String context) throws Throwable
	{
		Thread.sleep( 200 );
		if( "resource".equals( context ) )
		{
			currentTreeNode = findRestResourcePopupMenu( getMainWindow( robot ), newProjectIndexInNavigationTree );
		}
		if( "request".equals( context ) )
		{
			currentTreeNode = findRestRequestPopupMenu( getMainWindow( robot ), newProjectIndexInNavigationTree );
		}
		Thread.sleep( 200 );
	}

	@When( "^right clicking the current rest context")
	public void _right_clicking_the_current_rest_context() throws Throwable
	{
		currentTreeNode = ( JTreeNodeFixture )currentTreeNode.rightClick();
	}

	@Then( "^“(.*)” rest option is available$" )
	public void _rest_option_is_available(String menuItemLabel) throws Throwable
	{
		assertTrue( "Didn't find the " + menuItemLabel + " menu item", doesLabelExist( currentTreeNode, menuItemLabel ) );
	}



	@When( "^the user clicks on the Auth tab$" )
	public void clickOnTheAuthTab()
	{
		rootWindow.toggleButton( AuthInspectorFactory.INSPECTOR_ID ).click();
	}

	@When( "^user adds a parameter in request editor with name (.+) and value (.+)$" )
	public void addRestParameterInRequestEditor( String name, String value )
	{
		JPanelFixture requestEditor = findRequestEditor( rootWindow, newProjectIndexInNavigationTree, robot );
		addNewParameter( requestEditor, robot, name, value );
	}

	@When( "^user adds a parameter in method editor with name (.+) and value (.+)$" )
	public void addRestParameterInMethodEditor( String name, String value )
	{
		JPanelFixture methodEditor = findMethodEditor( rootWindow, newProjectIndexInNavigationTree, robot );
		addNewParameter( methodEditor, robot, name, value );
	}

	@When( "^user adds a parameter in resource editor with name (.+) and value (.+)$" )
	public void addRestParameterInResourceEditor( String name, String value )
	{
		JPanelFixture resourceEditor = findResourceEditor( rootWindow, newProjectIndexInNavigationTree, robot );
		addNewParameter( resourceEditor, robot, name, value );
	}

	@When( "^user changes the level to (.+) for parameter with name (.+)$" )
	public void changesParameterLevel( String newLevel, String parameterName )
	{
		JPanelFixture requestEditor = findRequestEditor( rootWindow, newProjectIndexInNavigationTree, robot );
		changeParameterLevel( requestEditor, parameterName, newLevel, robot );
	}

	@Then( "^request editor has parameter with name (.+) and value (.+) at row (.+)$" )
	public void verifyRequestEditorShowsParameter( String parameterName, String parameterValue, Integer index )
	{
		JPanelFixture requestEditor = findRequestEditor( rootWindow, newProjectIndexInNavigationTree, robot );
		verifyParamValues( requestEditor, index, parameterName, parameterValue );
	}

	@Then( "^resource editor has parameter with name (.+) and with empty value at row (.+)$" )
	public void verifyResourceEditorShowsTheParameterWithEmptyValue( String parameterName, Integer index )
	{
		verifyResourceEditorShowsTheParameter( parameterName, "", index );
	}

	@Then( "^resource editor has parameter with name (.+) and value (.+) at row (.+)$" )
	public void verifyResourceEditorShowsTheParameter( String parameterName, String parameterValue, Integer index )
	{
		JPanelFixture resourceEditor = findResourceEditor( rootWindow, newProjectIndexInNavigationTree, robot );
		verifyParamValues( resourceEditor, index, parameterName, parameterValue );
	}

	@Then( "^method editor has parameter with name (.+) and value (.+) at row (.+)$" )
	public void verifyMethodEditorShowsTheParameter( String parameterName, String parameterValue, Integer index )
	{
		JPanelFixture methodEditor = findMethodEditor( rootWindow, newProjectIndexInNavigationTree, robot );
		verifyParamValues( methodEditor, index, parameterName, parameterValue );
	}

	@Then( "^method editor has no parameters$" )
	public void verifyMethodEditorHasEmptyParameterTable()
	{
		JPanelFixture methodEditor = findMethodEditor( rootWindow, newProjectIndexInNavigationTree, robot );
		verifyEmptyTable( methodEditor );
	}

	@Then( "^“(.*)” option is available on REST Request$" )
	public void _add_to_mock_service_option_is_available(String menuItemLabel) throws Throwable
	{
		JTreeNodeFixture popupMenu = findRestRequestPopupMenu( getMainWindow( robot ), newProjectIndexInNavigationTree );
		assertTrue( "Didn't find the " + menuItemLabel + " menu item", doesLabelExist( popupMenu, menuItemLabel ) );
	}

    @Then( "^“(.*)” option is available on REST Resource" )
    public void _add_to_mock_service_option_is_available_resource(String menuItemLabel) throws Throwable
    {
        JTreeNodeFixture popupMenu = findRestResourcePopupMenu( getMainWindow( robot ), newProjectIndexInNavigationTree );
        assertTrue( "Didn't find the " + menuItemLabel + " menu item", doesLabelExist( popupMenu, menuItemLabel ) );
    }

	private void verifyEmptyTable( JPanelFixture parentPanel )
	{
		JTableFixture restParamsTable = parentPanel.table( REST_PARAMS_TABLE );
		assertThat( restParamsTable.target.getRowCount(), is( 0 ) );
	}

	private void verifyParamValues( JPanelFixture parentPanel, int rowNum, String paramName, String paramValue )
	{
		JTableFixture paramTableInResourceEditor = parentPanel.table( REST_PARAMS_TABLE );
		assertThat( paramTableInResourceEditor.cell( row( rowNum ).column( 0 ) ).value(), is( paramName ) );
		assertThat( paramTableInResourceEditor.cell( row( rowNum ).column( 1 ) ).value(), is( paramValue ) );
	}

	private int findTheIndexOfCurrentProjectInNavigationTree()
	{
		List<String> projectNameListWithNewProject = WorkspaceUtils.getProjectNameList();
		projectNameListWithNewProject.removeAll( existingProjectNameList );
		String projectName = projectNameListWithNewProject.get( 0 );

		return WorkspaceUtils.getProjectNameList().indexOf( projectName );
	}

}