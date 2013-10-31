package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.SoapUI;
import org.fest.swing.core.*;
import org.fest.swing.core.Robot;
import org.fest.swing.finder.DialogFinder;
import org.fest.swing.fixture.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;

import java.awt.event.KeyEvent;

import static com.eviware.soapui.utils.FestMatchers.*;
import static com.eviware.soapui.impl.rest.panels.request.RestRequestDesktopPanel.REST_REQUEST_DESKTOP_PANEL;
import static com.eviware.soapui.impl.rest.panels.resource.RestParamsTable.REST_PARAMS_TABLE;
import static com.eviware.soapui.impl.wsdl.panels.teststeps.support.AddParamAction.ADD_PARAM_ACTION_NAME;
import static com.eviware.soapui.ui.Navigator.NAVIGATOR;
import static org.fest.swing.data.TableCell.row;
import static org.fest.swing.finder.WindowFinder.findDialog;
import static org.fest.swing.finder.WindowFinder.findFrame;
import static org.fest.swing.launcher.ApplicationLauncher.application;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-10-29
 * Time: 14:37
 * To change this template use File | Settings | File Templates.
 */
public class SynchParametersTest
{
	private Robot robot;

	@Before
	public void setUp()
	{
		application( SoapUI.class ).start();
		robot = BasicRobot.robotWithCurrentAwtHierarchy();
	}

	@Test
	@Ignore
	public void test() throws InterruptedException
	{
		FrameFixture rootWindow = frameWithTitle( "SoapUI" ).using( robot );

		JPopupMenuFixture projects = rightClickOnProjectsMenu( rootWindow );

		JMenuItemFixture createNewRestProjectMenu = projects.menuItem( menuItemWithText( "New REST Project" ) );


		Thread createRestProjectThread = createThreadToEnterURIAndClickOk();
		createRestProjectThread.start();

		createNewRestProjectMenu.click();

		//Finish creating the rest project
		createRestProjectThread.join();

		JPanelFixture jPanelFixture = rootWindow.panel( REST_REQUEST_DESKTOP_PANEL );
		jPanelFixture.button( ADD_PARAM_ACTION_NAME ).click();
		JTableFixture restParamsTable = jPanelFixture.table( REST_PARAMS_TABLE );

		robot.waitForIdle();
		JTableCellFixture cellFixture = restParamsTable.cell( row( 0 ).column( 0 ) );
	   JTextComponentFixture textBox = new JTextComponentFixture( robot, ( JTextField )cellFixture.editor() );
		textBox.enterText( "ParamName" );
		textBox.pressKey( KeyEvent.VK_ENTER );

		robot.waitForIdle();
		textBox = new JTextComponentFixture(robot, (JTextField)restParamsTable.cell( row( 0 ).column( 1 ) ).editor());
		textBox.enterText( "value" );
		textBox.pressKey( KeyEvent.VK_ENTER );

		Thread.sleep( 2000 );
		rootWindow.close();

		DialogFixture confirmationDialog = dialogWithTitle( "Question" ).using( robot );
		confirmationDialog.button(buttonWithText( "Yes" )).click();

		DialogFixture saveProjectDialog = dialogWithTitle( "Save Project" ).using( robot );
		while(saveProjectDialog != null)
		{
			saveProjectDialog.button(buttonWithText( "No" )).click();
			saveProjectDialog = dialogWithTitle( "Save Project" ).using( robot );
		}

	}

	private JPopupMenuFixture rightClickOnProjectsMenu( FrameFixture frame )
	{
		JPanelFixture navigator = frame.panel( NAVIGATOR );
		return navigator.tree().showPopupMenuAt( "Projects" );
	}

	private Thread createThreadToEnterURIAndClickOk()
	{
		return new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				DialogFixture newRestProjectDialog = dialogWithTitle( "New REST Project" ).withTimeout( 2000 )
						.using( robot );

				newRestProjectDialog.textBox().focus();
				newRestProjectDialog.textBox().click();
				newRestProjectDialog.textBox().setText( "http://soapui.org" );

				JButtonFixture buttonOK = newRestProjectDialog.button( buttonWithText( "OK" ) );
				buttonOK.click();
			}
		} );
	}
}
