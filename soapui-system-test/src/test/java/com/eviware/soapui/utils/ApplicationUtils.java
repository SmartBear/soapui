package com.eviware.soapui.utils;

import org.fest.swing.core.Robot;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;

/**
 * Utility class used for generic operations on application level
 */
public class ApplicationUtils
{
	private static final String CONFIRMATION_DIALOG_NAME = "Question";
	private static final String SAVE_PROJECT_DIALOG_NAME = "Save Project";
	private static final String NO_BUTTON_NAME = "No";
	private static final String YES_BUTTONM_NAME = "Yes";

	public static void closeApplicationWithoutSaving( FrameFixture rootWindow, Robot robot )
	{
		rootWindow.close();

		DialogFixture confirmationDialog = FestMatchers.dialogWithTitle( CONFIRMATION_DIALOG_NAME ).using( robot );
		confirmationDialog.button( FestMatchers.buttonWithText( YES_BUTTONM_NAME ) ).click();

		DialogFixture saveProjectDialog = FestMatchers.dialogWithTitle( SAVE_PROJECT_DIALOG_NAME ).using( robot );
		try
		{
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
}