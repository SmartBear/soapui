/*
 * SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.smartbear.soapui.utils.fest;

import com.eviware.soapui.support.editor.inspectors.auth.OAuth2AuthenticationInspector;
import org.fest.swing.core.Robot;
import org.fest.swing.exception.ComponentLookupException;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;

import static org.junit.Assert.fail;

/**
 *
 */
public class FestUtils
{
	public static void verifyDialogIsNotShowing( String dialogName, Robot robot )
	{
		try
		{
			findDialog( dialogName, robot );
			fail( "Dialog: " + dialogName + " is still visible" );
		}
		catch( ComponentLookupException e )
		{
		}
	}

	public static DialogFixture findDialog( String dialogName, Robot robot )
	{
		return new DialogFixture( robot, dialogName );
	}

	public static void verifyButtonIsNotShowing( FrameFixture rootWindow, String buttonName )
	{
		try
		{
			rootWindow.button( buttonName );
			fail( "Button: " + buttonName + " is still visible" );
		}
		catch( ComponentLookupException e )
		{
		}
	}

	public static void verifyTextFieldIsNotShowingInDialog( DialogFixture rootWindow, String fieldName )
	{
		try
		{
			rootWindow.textBox( fieldName );
			fail( "Text field: " + fieldName + " is still visible" );
		}
		catch( ComponentLookupException e )
		{
		}
	}
}
