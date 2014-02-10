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
package com.eviware.soapui.utils;

import com.eviware.soapui.SoapUI;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.*;

/**
 * Utility class used for generic operations on a SOAP project
 */
public class SoapProjectUtils
{
	private static final String NEW_SOAP_PROJECT_MENU_ITEM_NAME = "New SOAP Project";
	private static final String NEW_SOAP_PROJECT_DIALOG_NAME = "New SOAP Project";
	private static final String OK_BUTTON_NAME = "OK";
	private static final String WSDL_FIELD_NAME = "Initial WSDL";
	private static final String ROOT_FOLDER = SoapProjectUtils.class.getResource( "/" ).getPath();
	private static final String TEST_WSDL = ROOT_FOLDER + "test.wsdl";
	private static final String PROJECT_NAME = "test";
	private static final String INTERFACE_NAME = "GeoCode_Binding";
	private static final String OPERATION_NAME = "geocode";
	private static final String REQUEST_NAME = "Request 1";
	private static final int NEW_PROJECT_TIMEOUT = 2000;

	public static void createNewSoapProject( FrameFixture rootWindow, Robot robot )
	{
		openCreateNewSoapProjectDialog( rootWindow );
		enterProjectNameAndWsdlUrlAndClickOk( robot );
	}

	public static void openRequestEditor( FrameFixture rootWindow )
	{
		JTreeFixture tree = WorkspaceUtils.getNavigatorPanel( rootWindow ).tree();

		waitForProjectToLoad();

		tree.expandPath( getOperationPath() );
		tree.node( getRequestPath() ).doubleClick();
	}

	private static String getOperationPath()
	{
		return SoapUI.getWorkspace().getName() + "/" + PROJECT_NAME + "/" + INTERFACE_NAME + "/" + OPERATION_NAME;
	}

	private static String getRequestPath()
	{
		return SoapUI.getWorkspace().getName() + "/" + PROJECT_NAME + "/" + INTERFACE_NAME + "/" + OPERATION_NAME + "/"
				+ REQUEST_NAME;
	}

	private static void openCreateNewSoapProjectDialog( FrameFixture rootWindow )
	{
		JPopupMenuFixture workspace = WorkspaceUtils.rightClickOnWorkspace( rootWindow );
		workspace.menuItem( FestMatchers.menuItemWithText( NEW_SOAP_PROJECT_MENU_ITEM_NAME ) ).click();
	}

	private static void enterProjectNameAndWsdlUrlAndClickOk( Robot robot )
	{
		DialogFixture newProjectDialog = FestMatchers.dialogWithTitle( NEW_SOAP_PROJECT_DIALOG_NAME )
				.withTimeout( NEW_PROJECT_TIMEOUT ).using( robot );

		newProjectDialog.textBox( WSDL_FIELD_NAME ).setText( TEST_WSDL );

		JButtonFixture buttonOK = newProjectDialog.button( FestMatchers.buttonWithText( OK_BUTTON_NAME ) );
		buttonOK.click();
	}

	// There might be a more elegant way to wait
	private static void waitForProjectToLoad()
	{
		try
		{
			Thread.sleep( NEW_PROJECT_TIMEOUT );
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
	}
}