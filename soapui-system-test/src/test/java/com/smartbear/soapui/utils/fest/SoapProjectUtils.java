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

import com.eviware.soapui.SoapUI;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.*;

/**
 * Utility class used for generic operations on a SOAP project
 */
public final class SoapProjectUtils
{
	public static final String ROOT_FOLDER = SoapProjectUtils.class.getResource( "/" ).getPath();

	private static final String NEW_SOAP_PROJECT_MENU_ITEM_NAME = "New SOAP Project";
	private static final String NEW_SOAP_PROJECT_DIALOG_NAME = "New SOAP Project";
	private static final String OK_BUTTON_NAME = "OK";
	private static final String WSDL_FIELD_NAME = "Initial WSDL";
	private static final String TEST_WSDL = ROOT_FOLDER + "wsdls/test.wsdl";
	private static final String PROJECT_NAME = "test";
    //private static final String PROJECT_NAME = "REST Project 1";
	private static final String INTERFACE_NAME = "GeoCode_Binding";
    //private static final String INTERFACE_NAME = "http://maps.googleapis.com";
	private static final String OPERATION_NAME = "geocode";
    //private static final String OPERATION_NAME = "xml";
	private static final String REQUEST_NAME = "Request 1";
	private static final int NEW_PROJECT_TIMEOUT = 2000;

	private SoapProjectUtils()
	{
		throw new AssertionError();
	}

	public static void createNewSoapProject( FrameFixture rootWindow, Robot robot )
	{
		openCreateNewSoapProjectDialog( rootWindow );
		enterProjectNameAndWsdlUrlAndClickOk( robot );
	}

	public static void openRequestEditor( FrameFixture rootWindow )
	{
		JTreeNodeFixture node = getTreeNode( rootWindow, getOperationPath() );
		node.doubleClick();
	}

	private static JTreeNodeFixture getTreeNode( FrameFixture rootWindow, String path )
	{
		JTreeFixture tree = WorkspaceUtils.getNavigatorPanel( rootWindow ).tree();

		waitForProjectToLoad();

		tree.expandPath( getOperationPath() );
		return tree.node( path );
	}

	public static JTreeNodeFixture findSoapOperationPopupMenu( FrameFixture rootWindow )
	{
		return getTreeNode( rootWindow, getOperationPath() );
	}

	public static JTreeNodeFixture findSoapRequestPopupMenu( FrameFixture rootWindow )
	{
		return getTreeNode( rootWindow, getRequestPath() );
	}

	private static String getOperationPath()
	{
       // return SoapUI.getWorkspace().getName() + "/" + "REST Project 1" + "/" + "http://example.org" + "/" + "Sub-resource";
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