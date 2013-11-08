package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.utils.FestMatchers;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.fixture.*;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;
import static com.eviware.soapui.impl.rest.panels.method.RestMethodDesktopPanel.REST_METHOD_EDITOR;
import static com.eviware.soapui.impl.rest.panels.request.RestRequestDesktopPanel.REST_REQUEST_EDITOR;
import static com.eviware.soapui.impl.rest.panels.resource.RestParamsTable.REST_PARAMS_TABLE;
import static com.eviware.soapui.impl.rest.panels.resource.RestResourceDesktopPanel.REST_RESOURCE_EDITOR;
import static com.eviware.soapui.impl.wsdl.panels.teststeps.support.AddParamAction.ADD_PARAM_ACTION_NAME;
import static com.eviware.soapui.ui.Navigator.NAVIGATOR;
import static com.eviware.soapui.utils.FestMatchers.frameWithTitle;
import static org.fest.swing.data.TableCell.row;
import static org.fest.swing.launcher.ApplicationLauncher.application;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-10-29
 * Time: 14:37
 * To change this template use File | Settings | File Templates.
 */
public class SynchParametersTest
{
	private static final int REST_RESOURCE_POSITION_IN_TREE = 3;
	private static final int REST_REQUEST_POSITION_IN_TREE = 5;
	private static final int REST_METHOD_POSITION_IN_TREE = 4;
	private static NoExitSecurityManagerInstaller noExitSecurityManagerInstaller;
	private Robot robot;

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
		FailOnThreadViolationRepaintManager.install();
	}

	@AfterClass
	public static void tearDown()
	{
		noExitSecurityManagerInstaller.uninstall();
	}

	@Before
	public void setUp()
	{
		application( SoapUI.class ).start();
		robot = BasicRobot.robotWithCurrentAwtHierarchy();
	}

	@Test
	public void testParameterSync() throws InterruptedException
	{
		FrameFixture rootWindow = frameWithTitle( "SoapUI" ).using( robot );
		rootWindow.maximize();

		createNewRestProject( rootWindow );

		JPanelFixture resourceEditor = openResourceEditor( rootWindow );

		JPanelFixture requestEditor = openRequestEditor( rootWindow );

		addNewParameter( requestEditor, "Address", "Stockholm" );
		verifyParamValues( requestEditor, 0, "Address", "Stockholm" );
		verifyParamValues( resourceEditor, 0, "Address", "" );

		openResourceEditor( rootWindow );

		addNewParameter( resourceEditor, "resParam", "value1" );
		verifyParamValues( resourceEditor, 1, "resParam", "value1" );
		verifyParamValues( requestEditor, 1, "resParam", "value1" );

		JPanelFixture methodEditor = openMethodEditor( rootWindow );
		addNewParameter( methodEditor, "mParam", "mValue" );
		verifyParamValues( methodEditor, 0, "mParam", "mValue" );
		verifyParamValues( requestEditor, 2, "mParam", "mValue" );


		changeParameterLevel( methodEditor, 0, ParamLocation.RESOURCE );
		verifyEmptyTable( methodEditor );
		verifyParamValues( resourceEditor, 2, "mParam", "mValue" );


		openResourceEditor( rootWindow );

		closeWindow( rootWindow );

	}

	private void createNewRestProject( FrameFixture rootWindow )
	{
		openCreateNewRestProjectDialog( rootWindow );

		enterURIandClickOk();
	}

	private void closeWindow( FrameFixture rootWindow ) throws InterruptedException
	{
		Thread.sleep( 2000 );
		rootWindow.close();

		DialogFixture confirmationDialog = FestMatchers.dialogWithTitle( "Question" ).using( robot );
		confirmationDialog.button( FestMatchers.buttonWithText( "Yes" ) ).click();

		try
		{
			DialogFixture saveProjectDialog = FestMatchers.dialogWithTitle( "Save Project" ).using( robot );
			while( saveProjectDialog != null )
			{
				saveProjectDialog.button( FestMatchers.buttonWithText( "No" ) ).click();
				saveProjectDialog = FestMatchers.dialogWithTitle( "Save Project" ).using( robot );
			}
		}
		catch( Exception e )
		{
			//Do nothing
		}
	}

	private void verifyParamValues( JPanelFixture parentPanel, int rowNum, String paramName, String paramValue )
	{
		JTableFixture paramTableInResourceEditor = parentPanel.table( REST_PARAMS_TABLE );
		assertThat( paramTableInResourceEditor.cell( row( rowNum ).column( 0 ) ).value(), is( paramName ) );
		assertThat( paramTableInResourceEditor.cell( row( rowNum ).column( 1 ) ).value(), is( paramValue ) );
	}

	private void openCreateNewRestProjectDialog( FrameFixture rootWindow )
	{
		JPopupMenuFixture projects = rightClickOnProjectsMenu( rootWindow );

		JMenuItemFixture createNewRestProjectMenu = projects.menuItem( FestMatchers.menuItemWithText( "New REST Project" ) );
		createNewRestProjectMenu.click();
	}

	private void addNewParameter( JPanelFixture parentPanel, String paramName, String paramValue )
	{
		parentPanel.button( ADD_PARAM_ACTION_NAME ).click();
		JTableFixture restParamsTable = parentPanel.table( REST_PARAMS_TABLE );

		robot.waitForIdle();
		int rowNumToEdit = restParamsTable.target.getRowCount() - 1;
		editTableCell( paramName, restParamsTable, rowNumToEdit, 0 );
		editTableCell( paramValue, restParamsTable, rowNumToEdit, 1 );
	}

	private void editTableCell( String paramValue, JTableFixture restParamsTable, int rowNumToEdit, int column )
	{
		robot.waitForIdle();
		JTextField tableCellEditor = ( JTextField )restParamsTable.cell( row( rowNumToEdit ).column( column ) ).editor();
		new JTextComponentFixture( robot, tableCellEditor )
				.enterText( paramValue )
				.pressKey( KeyEvent.VK_ENTER );
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

	private JPanelFixture openMethodEditor( FrameFixture frame )
	{
		return getPanelFixture( frame, REST_METHOD_POSITION_IN_TREE, REST_METHOD_EDITOR );
	}

	private JPanelFixture openResourceEditor( FrameFixture frame )
	{
		return getPanelFixture( frame, REST_RESOURCE_POSITION_IN_TREE, REST_RESOURCE_EDITOR );
	}

	private JPanelFixture openRequestEditor( FrameFixture frame )
	{
		return getPanelFixture( frame, REST_REQUEST_POSITION_IN_TREE, REST_REQUEST_EDITOR );
	}

	private JPanelFixture getPanelFixture( FrameFixture frame, int panelPositionInNavigationTree, String panelName )
	{
		getNavigatorPanel( frame ).tree().node( panelPositionInNavigationTree ).doubleClick();
		return frame.panel( panelName );
	}

	private JPopupMenuFixture rightClickOnProjectsMenu( FrameFixture frame )
	{
		return getNavigatorPanel( frame ).tree().showPopupMenuAt( "Projects" );
	}

	private JPanelFixture getNavigatorPanel( FrameFixture frame )
	{
		return frame.panel( NAVIGATOR );
	}

	private void enterURIandClickOk()
	{
		DialogFixture newRestProjectDialog = FestMatchers.dialogWithTitle( "New REST Project" ).withTimeout( 2000 )
				.using( robot );

		newRestProjectDialog.textBox().focus();
		newRestProjectDialog.textBox().click();
		newRestProjectDialog.textBox().setText( "http://soapui.org" );

		JButtonFixture buttonOK = newRestProjectDialog.button( FestMatchers.buttonWithText( "OK" ) );
		buttonOK.click();
	}
}
