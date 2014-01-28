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

import com.eviware.soapui.impl.rest.panels.method.RestMethodDesktopPanel;
import com.eviware.soapui.impl.rest.panels.request.RestRequestDesktopPanel;
import com.eviware.soapui.impl.rest.panels.resource.RestResourceDesktopPanel;
import org.fest.swing.core.KeyPressInfo;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.*;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.eviware.soapui.impl.rest.panels.resource.RestParamsTable.REST_PARAMS_TABLE;
import static com.eviware.soapui.impl.wsdl.panels.teststeps.support.AddParamAction.ADD_PARAM_ACTION_NAME;
import static org.fest.swing.data.TableCell.row;

/**
 * Utility class used for generic operations on a REST project
 */
public final class RestProjectUtils
{
	private static final int NEW_PROJECT_TIMEOUT = 2000;
	private static final String URI = "http://soapui.org";
	private static final String NEW_REST_PROJECT_MENU_ITEM = "New REST Project";
	private static final String NEW_REST_PROJECT_DIALOG_NAME = "New REST Project";
	private static final String OK_BUTTON_NAME = "OK";

	private static final int REST_RESOURCE_POSITION_IN_TREE = 3;
	private static final int REST_REQUEST_POSITION_IN_TREE = 5;
	private static final int REST_METHOD_POSITION_IN_TREE = 4;


	private RestProjectUtils()
	{
		throw new AssertionError();
	}

	public static void createNewRestProject( FrameFixture rootWindow, Robot robot )
	{
		openCreateNewRestProjectDialog( rootWindow );
		enterURIandClickOk( robot );
	}

	public static JPanelFixture findRequestEditor( FrameFixture rootWindow, int projectIndexInTree, Robot robot )
	{
		openPanelByClickingOnTheNavigationElement( projectIndexInTree, rootWindow, REST_REQUEST_POSITION_IN_TREE, robot );
		return rootWindow.panel( RestRequestDesktopPanel.REST_REQUEST_EDITOR );
	}

	public static JPanelFixture findMethodEditor( FrameFixture rootWindow, int projectIndexInTree, Robot robot )
	{
		openPanelByClickingOnTheNavigationElement( projectIndexInTree, rootWindow, REST_METHOD_POSITION_IN_TREE, robot );
		return rootWindow.panel( RestMethodDesktopPanel.REST_METHOD_EDITOR );
	}


	public static JPanelFixture findResourceEditor( FrameFixture rootWindow, int projectIndexInTree, Robot robot )
	{
		openPanelByClickingOnTheNavigationElement( projectIndexInTree, rootWindow, REST_RESOURCE_POSITION_IN_TREE, robot );
		return rootWindow.panel( RestResourceDesktopPanel.REST_RESOURCE_EDITOR );
	}

	public static void addNewParameter( JPanelFixture parentPanel, Robot robot, String paramName, String paramValue )
	{
		parentPanel.button( ADD_PARAM_ACTION_NAME ).click();
		JTableFixture restParamsTable = parentPanel.table( REST_PARAMS_TABLE );

		robot.waitForIdle();
		int rowNumToEdit = restParamsTable.target.getRowCount() - 1;
		editTableCell( paramName, restParamsTable, robot, rowNumToEdit, 0 );
		editTableCell( paramValue, restParamsTable, robot, rowNumToEdit, 1 );
	}

	public static void changeParameterLevel( JPanelFixture parentPanel, String parameterName, String newLevel )
	{
		JTableFixture restParamsTable = parentPanel.table( REST_PARAMS_TABLE );
		for( int rowCounter = 0; rowCounter < restParamsTable.rowCount(); rowCounter++ )
		{
			String paramNameAtIndex = restParamsTable.cell( row( rowCounter ).column( 0 ) ).value();
			if( paramNameAtIndex.equals( parameterName ) )
			{
				restParamsTable.cell( row( rowCounter ).column( 3 ) ).enterValue( newLevel );
				return;
			}
		}
	}

	private static void enterURIandClickOk( Robot robot )
	{
		DialogFixture newProjectDialog = FestMatchers.dialogWithTitle( NEW_REST_PROJECT_DIALOG_NAME )
				.withTimeout( NEW_PROJECT_TIMEOUT )
				.using( robot );

		newProjectDialog.textBox().focus();
		newProjectDialog.textBox().click();
		newProjectDialog.textBox().setText( URI );

		JButtonFixture buttonOK = newProjectDialog.button( FestMatchers.buttonWithText( OK_BUTTON_NAME ) );
		buttonOK.click();
	}

	private static void openCreateNewRestProjectDialog( FrameFixture rootWindow )
	{
		JPopupMenuFixture workspace = WorkspaceUtils.rightClickOnWorkspace( rootWindow );
		workspace.menuItem( FestMatchers.menuItemWithText( NEW_REST_PROJECT_MENU_ITEM ) ).click();
	}

	private static void openPanelByClickingOnTheNavigationElement( int newProjectIndexInTree, FrameFixture frame,
																						int panelPositionInNavigationTree, Robot robot )
	{
		WorkspaceUtils.getNavigatorPanel( frame ).tree().node( newProjectIndexInTree + panelPositionInNavigationTree )
				.click();
		robot.pressAndReleaseKeys( KeyEvent.VK_ENTER );
	}

	private static void editTableCell( String paramValue, JTableFixture table, Robot robot, int row, int column )
	{
		robot.waitForIdle();
		JTextField tableCellEditor = ( JTextField )table.cell( row( row ).column( column ) ).editor();
		new JTextComponentFixture( robot, tableCellEditor )
				.enterText( paramValue )
				.pressAndReleaseKey( KeyPressInfo.keyCode( KeyEvent.VK_ENTER ) );
	}
}