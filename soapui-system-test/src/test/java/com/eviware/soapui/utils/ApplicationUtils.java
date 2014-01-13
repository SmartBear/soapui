package com.eviware.soapui.utils;

import com.eviware.soapui.SoapUI;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.exception.WaitTimedOutError;
import org.fest.swing.finder.DialogFinder;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;

import java.awt.*;

import static org.fest.swing.finder.WindowFinder.findDialog;

/**
 * Utility class used for generic operations on application level
 */
public class ApplicationUtils
{
	private static final String CONFIRMATION_DIALOG_NAME = "Question";
	private static final String SAVE_PROJECT_DIALOG_NAME = "Save Project";
	private static final String NO_BUTTON_NAME = "No";
	private static final String YES_BUTTONM_NAME = "Yes";
	private static final int SAVE_PROJECT_DIALOG_TIMEOUT = 3000;

	private Robot robot;

	public ApplicationUtils( Robot robot )
	{
		this.robot = robot;
	}

	public void closeApplicationWithoutSaving( FrameFixture rootWindow )
	{
		rootWindow.close();

		DialogFixture confirmationDialog = FestMatchers.dialogWithTitle( CONFIRMATION_DIALOG_NAME ).using( robot );
		confirmationDialog.button( FestMatchers.buttonWithText( YES_BUTTONM_NAME ) ).click();

		DialogFixture saveProjectDialog = FestMatchers.dialogWithTitle( SAVE_PROJECT_DIALOG_NAME ).withTimeout( SAVE_PROJECT_DIALOG_TIMEOUT ).using( robot );
		saveProjectDialog.button( FestMatchers.buttonWithText( NO_BUTTON_NAME ) ).click();
	}
}