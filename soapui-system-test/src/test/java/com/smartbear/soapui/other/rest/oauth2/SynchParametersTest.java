package com.smartbear.soapui.other.rest.oauth2;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.ConsoleDialogs;
import com.eviware.soapui.support.UISupport;
import com.smartbear.soapui.utils.IntegrationTest;
import com.smartbear.soapui.utils.fest.ApplicationUtils;
import com.smartbear.soapui.utils.fest.RestProjectUtils;
import com.smartbear.soapui.utils.fest.WorkspaceUtils;
import com.smartbear.soapui.utils.fest.FestMatchers;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.KeyPressInfo;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.*;
import org.junit.experimental.categories.Category;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;
import static com.eviware.soapui.impl.rest.panels.method.RestMethodDesktopPanel.REST_METHOD_EDITOR;
import static com.eviware.soapui.impl.rest.panels.request.RestRequestDesktopPanel.REST_REQUEST_EDITOR;
import static com.eviware.soapui.impl.rest.panels.resource.RestParamsTable.REST_PARAMS_TABLE;
import static com.eviware.soapui.impl.rest.panels.resource.RestResourceDesktopPanel.REST_RESOURCE_EDITOR;
import static com.eviware.soapui.impl.wsdl.panels.teststeps.support.AddParamAction.ADD_PARAM_ACTION_NAME;
import static com.smartbear.soapui.utils.fest.FestMatchers.frameWithTitle;
import static org.fest.swing.data.TableCell.row;
import static org.fest.swing.launcher.ApplicationLauncher.application;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Prakash
 */
// TODO This fails when running it together with the other tests, find out why (might have to do with static SoapUI state)
@Ignore
@Category( IntegrationTest.class )
public class SynchParametersTest
{
	private static final int REST_RESOURCE_POSITION_IN_TREE = 3;
	private static final int REST_REQUEST_POSITION_IN_TREE = 5;
	private static final int REST_METHOD_POSITION_IN_TREE = 4;
	private static NoExitSecurityManagerInstaller noExitSecurityManagerInstaller;
	private Robot robot;
	private List<String> existingProjectsNameList;

	@BeforeClass
	public static void setUpOnce()
	{
		noExitSecurityManagerInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager( new ExitCallHook()
		{
			@Override
			public void exitCalled( int status )
			{
				System.out.print( "Exit status : " + status );
			}
		} );

		//FailOnThreadViolationRepaintManager.install();
	}

	@AfterClass
	public static void tearDownOnce() throws InterruptedException
	{
		noExitSecurityManagerInstaller.uninstall();
	}

	@Before
	public void setUp() throws InterruptedException
	{
		System.setProperty( "soapui.jxbrowser.disable", "true" );

		application( SoapUI.class ).start();
		robot = BasicRobot.robotWithCurrentAwtHierarchy();

		existingProjectsNameList = createProjectNameList();
	}

	@After
	public void tearDown()
	{
		robot.cleanUp();
		UISupport.setDialogs( new ConsoleDialogs() );
	}

	@Test
	public void testParameterSync() throws InterruptedException
	{
		FrameFixture rootWindow = frameWithTitle( "SoapUI" ).using( robot );

		RestProjectUtils.createNewRestProject( rootWindow, robot );

		int newProjectIndexInTree = findTheIndexOfCurrentProjectInNavigationTree();
		JPanelFixture resourceEditor = openResourceEditor( newProjectIndexInTree, rootWindow );

		JPanelFixture requestEditor = openRequestEditor( newProjectIndexInTree, rootWindow );

		addNewParameter( requestEditor, "Address", "Stockholm" );
		verifyParamValues( requestEditor, 0, "Address", "Stockholm" );
		verifyParamValues( resourceEditor, 0, "Address", "" );

		openResourceEditor( newProjectIndexInTree, rootWindow );

		addNewParameter( resourceEditor, "resParam", "value1" );
		verifyParamValues( resourceEditor, 1, "resParam", "value1" );
		verifyParamValues( requestEditor, 1, "resParam", "value1" );

		JPanelFixture methodEditor = openMethodEditor( newProjectIndexInTree, rootWindow );
		addNewParameter( methodEditor, "mParam", "mValue" );
		verifyParamValues( methodEditor, 0, "mParam", "mValue" );
		verifyParamValues( requestEditor, 2, "mParam", "mValue" );

		openRequestEditor( newProjectIndexInTree, rootWindow );
		changeParameterLevel( requestEditor, 2, ParamLocation.RESOURCE );
		verifyEmptyTable( methodEditor );
		verifyParamValues( resourceEditor, 2, "mParam", "mValue" );


		openResourceEditor( newProjectIndexInTree, rootWindow );

		ApplicationUtils.closeApplicationWithoutSaving( rootWindow, robot );
	}

	private int findTheIndexOfCurrentProjectInNavigationTree()
	{
		List<String> projectNameListWithNewProject = createProjectNameList();
		projectNameListWithNewProject.removeAll( existingProjectsNameList );
		String projectName = projectNameListWithNewProject.get( 0 );

		return createProjectNameList().indexOf( projectName );
	}

	private List<String> createProjectNameList()
	{
		List<String> projectNameList = new ArrayList<String>();
		for( Project project : SoapUI.getWorkspace().getProjectList() )
		{
			projectNameList.add( project.getName() );
		}
		Collections.sort( projectNameList );
		return projectNameList;
	}

	private void verifyParamValues( JPanelFixture parentPanel, int rowNum, String paramName, String paramValue )
			throws InterruptedException
	{
		Thread.sleep( 500 );
		JTableFixture paramTableInResourceEditor = parentPanel.table( REST_PARAMS_TABLE );
		assertThat( paramTableInResourceEditor.cell( row( rowNum ).column( 0 ) ).value(), is( paramName ) );
		assertThat( paramTableInResourceEditor.cell( row( rowNum ).column( 1 ) ).value(), is( paramValue ) );
	}

	private void addNewParameter( JPanelFixture parentPanel, String paramName, String paramValue )
			throws InterruptedException
	{
		parentPanel.button( ADD_PARAM_ACTION_NAME ).click();
		JTableFixture restParamsTable = parentPanel.table( REST_PARAMS_TABLE );

		robot.waitForIdle();
		int rowNumToEdit = restParamsTable.target.getRowCount() - 1;
		editTableCell( paramName, restParamsTable, rowNumToEdit, 0 );
		Thread.sleep( 200 );
		editTableCell( paramValue, restParamsTable, rowNumToEdit, 1 );
	}

	private void editTableCell( String paramValue, JTableFixture restParamsTable, int rowNumToEdit, int column )
	{
		robot.waitForIdle();
		JTextField tableCellEditor = ( JTextField )restParamsTable.cell( row( rowNumToEdit ).column( column ) ).editor();
		new JTextComponentFixture( robot, tableCellEditor )
				.enterText( paramValue )
				.pressAndReleaseKey( KeyPressInfo.keyCode( KeyEvent.VK_ENTER ) );
	}

	private void changeParameterLevel( JPanelFixture parentPanel, int rownum, ParamLocation newLocation )
	{
		JTableFixture restParamsTable = parentPanel.table( REST_PARAMS_TABLE );
		restParamsTable.cell( row( rownum ).column( 3 ) ).enterValue( newLocation.toString() );
	}

	public void verifyEmptyTable( JPanelFixture parentPanel )
	{
		JTableFixture restParamsTable = parentPanel.table( REST_PARAMS_TABLE );
		assertThat( restParamsTable.target.getRowCount(), is( 0 ) );
	}

	private JPanelFixture openMethodEditor( int newPojectIndexInTree, FrameFixture frame )
	{
		return getPanelFixture( newPojectIndexInTree, frame, REST_METHOD_POSITION_IN_TREE, REST_METHOD_EDITOR );
	}

	private JPanelFixture openResourceEditor( int newPojectIndexInTree, FrameFixture frame )
	{
		return getPanelFixture( newPojectIndexInTree, frame, REST_RESOURCE_POSITION_IN_TREE, REST_RESOURCE_EDITOR );
	}

	private JPanelFixture openRequestEditor( int newPojectIndexInTree, FrameFixture frame )
	{
		return getPanelFixture( newPojectIndexInTree, frame, REST_REQUEST_POSITION_IN_TREE, REST_REQUEST_EDITOR );
	}

	private JPanelFixture getPanelFixture( int newProjectIndexInTree, FrameFixture frame,
														int panelPositionInNavigationTree, String panelName )
	{
		WorkspaceUtils.getNavigatorPanel( frame ).tree().node( newProjectIndexInTree + panelPositionInNavigationTree )
				.click();
		robot.pressAndReleaseKeys( KeyEvent.VK_ENTER );
		return frame.panel( panelName );
	}
}
