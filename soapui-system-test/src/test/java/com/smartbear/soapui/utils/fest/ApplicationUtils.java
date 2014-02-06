package com.smartbear.soapui.utils.fest;

import com.eviware.soapui.SoapUI;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTreeNodeFixture;

import static com.smartbear.soapui.utils.fest.FestMatchers.frameWithTitle;
import static org.fest.swing.launcher.ApplicationLauncher.application;

/**
 * Utility class used for generic operations on application level
 */
public final class ApplicationUtils
{
	private static final String MAIN_WINDOW_TITLE = "SoapUI";
	private static final String CONFIRMATION_DIALOG_NAME = "Question";
	private static final String SAVE_PROJECT_DIALOG_NAME = "Save Project";
	private static final String NO_BUTTON_NAME = "No";
	private static final String YES_BUTTON_NAME = "Yes";
	private static final int MAIN_WINDOW_TIMEOUT = 3000;

	private ApplicationUtils()
	{
		throw new AssertionError();
	}

	public static void startSoapUI()
	{
		application( SoapUI.class ).start();
	}

	public static FrameFixture getMainWindow( Robot robot )
	{
		FrameFixture rootWindow = frameWithTitle( MAIN_WINDOW_TITLE ).withTimeout( MAIN_WINDOW_TIMEOUT ).using( robot );
		rootWindow.show();
		rootWindow.maximize();
		return rootWindow;
	}

	public static void closeApplicationWithoutSaving( FrameFixture rootWindow, Robot robot )
	{
		rootWindow.close();

		DialogFixture confirmationDialog = FestMatchers.dialogWithTitle( CONFIRMATION_DIALOG_NAME ).using( robot );
		confirmationDialog.button( FestMatchers.buttonWithText( YES_BUTTON_NAME ) ).click();

		try
		{
			DialogFixture saveProjectDialog = FestMatchers.dialogWithTitle( SAVE_PROJECT_DIALOG_NAME ).using( robot );
			//Sometimes we have more than one projects modified, then we need to ignore save dialog for each one of them
			while( saveProjectDialog != null )
			{
				saveProjectDialog.button( FestMatchers.buttonWithText( NO_BUTTON_NAME ) ).click();
				saveProjectDialog = FestMatchers.dialogWithTitle( SAVE_PROJECT_DIALOG_NAME ).using( robot );
			}
		}
		catch( Exception e )
		{

		}
	}

	public static boolean doesLabelExist( JTreeNodeFixture menuItem, String mockService )
	{
		boolean foundLabel = false;
		for(String label : menuItem.showPopupMenu().menuLabels() )
		{
			if(label.contains( mockService ))
			{
				foundLabel = true;
			}
		}
		return foundLabel;
	}

	public static JTreeNodeFixture findTreeNode( FrameFixture frame, String path )
	{
		return WorkspaceUtils.getNavigatorPanel( frame ).tree().node( path );
	}


}