/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui;

import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.ImportWsdlProjectAction;
import com.eviware.soapui.impl.actions.SwitchWorkspaceAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.ui.desktop.DesktopListener;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * Workspace/Deskopt Listener that updates the recent menus..
 * 
 * @author ole.matzura
 */

public class RecentItemsListener implements WorkspaceListener, DesktopListener
{
	private static final String RECENT_WORKSPACES_SETTING = "RecentWorkspaces";
	private static final String RECENT_PROJECTS_SETTING = "RecentProjects";
	private JMenu recentProjectsMenu;
	private JMenu recentWorkspacesMenu;
	private JMenu recentEditorsMenu;
	private boolean switchingWorkspace;

	public RecentItemsListener( JMenu recentWorkspacesMenu2, JMenu recentProjectsMenu2, JMenu recentEditorsMenu2 )
	{
		recentWorkspacesMenu = recentWorkspacesMenu2;
		recentProjectsMenu = recentProjectsMenu2;
		recentEditorsMenu = recentEditorsMenu2;
		recentEditorsMenu.add( "- empty -" ).setEnabled( false );
		recentEditorsMenu.getPopupMenu().addPopupMenuListener( new PopupMenuListener()
		{

			public void popupMenuCanceled( PopupMenuEvent e )
			{
			}

			public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
			{
			}

			public void popupMenuWillBecomeVisible( PopupMenuEvent e )
			{
				for( int c = 0; c < recentEditorsMenu.getItemCount(); c++ )
				{
					ShowEditorAction action = ( ShowEditorAction )recentEditorsMenu.getItem( c ).getAction();
					if( action == null )
						continue;

					if( action.isReleased() )
					{
						recentEditorsMenu.remove( c );
						c-- ;
					}
					else
					{
						try
						{
							action.update();
						}
						catch( Throwable e1 )
						{
							recentEditorsMenu.remove( c );
							c-- ;
						}
					}
				}

				if( recentEditorsMenu.getItemCount() == 0 )
					recentEditorsMenu.add( "- empty -" ).setEnabled( false );

			}
		} );

		updateRecentWorkspacesMenu();
		updateRecentProjectsMenu();
	}

	private void updateRecentWorkspacesMenu()
	{
		String recent = SoapUI.getSettings().getString( RECENT_WORKSPACES_SETTING, null );
		StringToStringMap history = recent == null ? new StringToStringMap() : StringToStringMap.fromXml( recent );

		recentWorkspacesMenu.removeAll();

		if( history.size() > 0 )
		{
			for( Iterator<String> i = history.keySet().iterator(); i.hasNext(); )
			{
				String filePath = i.next();
				DefaultActionMapping<WorkspaceImpl> mapping = new DefaultActionMapping<WorkspaceImpl>(
						SwitchWorkspaceAction.SOAPUI_ACTION_ID, null, null, false, filePath );
				String wsName = history.get( filePath );

				if( SoapUI.getWorkspace().getPath().equals( filePath ) )
					continue;

				mapping.setName( wsName );
				mapping.setDescription( "Switches to the [" + wsName + "] workspace" );

				AbstractAction delegate = new SwingActionDelegate( mapping, SoapUI.getWorkspace() );
				recentWorkspacesMenu.add( new JMenuItem( delegate ) );
			}
		}
		else
		{
			recentWorkspacesMenu.add( "- empty -" ).setEnabled( false );
		}
	}

	private void updateRecentProjectsMenu()
	{
		recentProjectsMenu.removeAll();

		String recent = SoapUI.getSettings().getString( RECENT_PROJECTS_SETTING, null );
		StringToStringMap history = recent == null ? new StringToStringMap() : StringToStringMap.fromXml( recent );

		if( history.size() > 0 )
		{
			for( Iterator<String> i = history.keySet().iterator(); i.hasNext(); )
			{
				String filePath = i.next();
				DefaultActionMapping<WorkspaceImpl> mapping = new DefaultActionMapping<WorkspaceImpl>(
						ImportWsdlProjectAction.SOAPUI_ACTION_ID, null, null, false, filePath );
				String wsName = history.get( filePath );
				mapping.setName( wsName );
				mapping.setDescription( "Switches to the [" + wsName + "] project" );

				AbstractAction delegate = new SwingActionDelegate( mapping, SoapUI.getWorkspace() );
				recentProjectsMenu.add( new JMenuItem( delegate ) );
			}
		}
		else
		{
			recentProjectsMenu.add( "- empty -" ).setEnabled( false );
		}
	}

	public void projectAdded( Project project )
	{
		if( switchingWorkspace )
			return;

		String filePath = ( ( WsdlProject )project ).getPath();
		if( filePath == null )
			return;

		String recent = SoapUI.getSettings().getString( RECENT_PROJECTS_SETTING, null );
		if( recent != null )
		{
			StringToStringMap history = StringToStringMap.fromXml( recent );
			history.remove( filePath );
			SoapUI.getSettings().setString( RECENT_PROJECTS_SETTING, history.toXml() );
		}

		for( int c = 0; c < recentProjectsMenu.getItemCount(); c++ )
		{
			SwingActionDelegate action = ( SwingActionDelegate )recentProjectsMenu.getItem( c ).getAction();
			if( action == null )
				continue;

			SoapUIActionMapping mapping = action.getMapping();
			if( filePath.equals( mapping.getParam() ) )
			{
				recentProjectsMenu.remove( c );
				break;
			}
		}

		if( recentProjectsMenu.getItemCount() == 0 )
			recentProjectsMenu.add( "- empty -" ).setEnabled( false );
	}

	public void projectChanged( Project project )
	{
	}

	public void projectRemoved( Project project )
	{
		if( switchingWorkspace )
			return;

		String filePath = ( ( WsdlProject )project ).getPath();

		String recent = SoapUI.getSettings().getString( RECENT_PROJECTS_SETTING, null );
		StringToStringMap history = recent == null ? new StringToStringMap() : StringToStringMap.fromXml( recent );
		history.put( filePath, project.getName() );
		SoapUI.getSettings().setString( RECENT_PROJECTS_SETTING, history.toXml() );

		DefaultActionMapping<WorkspaceImpl> mapping = new DefaultActionMapping<WorkspaceImpl>(
				ImportWsdlProjectAction.SOAPUI_ACTION_ID, null, null, false, filePath );
		mapping.setName( project.getName() );
		mapping.setDescription( "Switches to the [" + project.getName() + "] project" );

		AbstractAction delegate = new SwingActionDelegate( mapping, SoapUI.getWorkspace() );
		recentProjectsMenu.add( new JMenuItem( delegate ) );

		SwingActionDelegate action = ( SwingActionDelegate )recentProjectsMenu.getItem( 0 ).getAction();
		if( action == null )
			recentProjectsMenu.remove( 0 );

		removeProjectEditors( project );
	}

	private void removeProjectEditors( Project project )
	{
		for( int c = 0; c < recentEditorsMenu.getItemCount(); c++ )
		{
			ShowEditorAction action = ( ShowEditorAction )recentEditorsMenu.getItem( c ).getAction();
			if( action == null )
				continue;

			if( action.isReleased() )
			{
				recentEditorsMenu.remove( c );
				c-- ;
			}
			else
			{
				try
				{
					action.update();
					if( dependsOnProject( action.getModelItem(), project ) )
					{
						recentEditorsMenu.remove( c );
						c-- ;
					}
				}
				catch( Throwable e1 )
				{
					recentEditorsMenu.remove( c );
					c-- ;
				}
			}
		}
	}

	private boolean dependsOnProject( ModelItem modelItem, Project project )
	{
		if( modelItem instanceof Interface )
		{
			return ( ( Interface )modelItem ).getProject() == project;
		}
		else if( modelItem instanceof Operation )
		{
			return ( ( Operation )modelItem ).getInterface().getProject() == project;
		}
		else if( modelItem instanceof Request )
		{
			return ( ( Request )modelItem ).getOperation().getInterface().getProject() == project;
		}
		else if( modelItem instanceof TestSuite )
		{
			return ( ( TestSuite )modelItem ).getProject() == project;
		}
		else if( modelItem instanceof TestCase )
		{
			return ( ( TestCase )modelItem ).getTestSuite().getProject() == project;
		}
		else if( modelItem instanceof TestStep )
		{
			return ( ( TestStep )modelItem ).getTestCase().getTestSuite().getProject() == project;
		}
		else if( modelItem instanceof LoadTest )
		{
			return ( ( LoadTest )modelItem ).getTestCase().getTestSuite().getProject() == project;
		}
		else if( modelItem instanceof MockService )
		{
			return ( ( MockService )modelItem ).getProject() == project;
		}
		else if( modelItem instanceof MockOperation )
		{
			return ( ( MockOperation )modelItem ).getMockService().getProject() == project;
		}
		else if( modelItem instanceof MockResponse )
		{
			return ( ( MockResponse )modelItem ).getMockOperation().getMockService().getProject() == project;
		}

		return false;
	}

	public void workspaceSwitched( Workspace workspace )
	{
		switchingWorkspace = false;

		String filePath = workspace.getPath();

		String recent = SoapUI.getSettings().getString( RECENT_WORKSPACES_SETTING, null );
		if( recent != null )
		{
			StringToStringMap history = StringToStringMap.fromXml( recent );
			history.remove( filePath );
			SoapUI.getSettings().setString( RECENT_WORKSPACES_SETTING, history.toXml() );
		}

		for( int c = 0; c < recentWorkspacesMenu.getItemCount(); c++ )
		{
			SwingActionDelegate action = ( SwingActionDelegate )recentWorkspacesMenu.getItem( c ).getAction();
			if( action == null )
				continue;

			SoapUIActionMapping mapping = action.getMapping();
			if( filePath.equals( mapping.getParam() ) )
			{
				recentWorkspacesMenu.remove( c );
				break;
			}
		}

		if( recentWorkspacesMenu.getItemCount() == 0 )
			recentWorkspacesMenu.add( "- empty -" ).setEnabled( false );
	}

	public void workspaceSwitching( Workspace workspace )
	{
		switchingWorkspace = true;
		recentEditorsMenu.removeAll();
		if( recentEditorsMenu.getItemCount() == 0 )
			recentEditorsMenu.add( "- empty -" ).setEnabled( false );

		String filePath = workspace.getPath();
		DefaultActionMapping<WorkspaceImpl> mapping = new DefaultActionMapping<WorkspaceImpl>(
				SwitchWorkspaceAction.SOAPUI_ACTION_ID, null, null, false, filePath );
		mapping.setName( workspace.getName() );
		mapping.setDescription( "Switches to the [" + workspace.getName() + "] workspace" );

		AbstractAction delegate = new SwingActionDelegate( mapping, SoapUI.getWorkspace() );
		recentWorkspacesMenu.add( new JMenuItem( delegate ) );

		String recent = SoapUI.getSettings().getString( RECENT_WORKSPACES_SETTING, null );
		StringToStringMap history = recent == null ? new StringToStringMap() : StringToStringMap.fromXml( recent );
		history.put( filePath, workspace.getName() );
		SoapUI.getSettings().setString( RECENT_WORKSPACES_SETTING, history.toXml() );

		SwingActionDelegate action = ( SwingActionDelegate )recentWorkspacesMenu.getItem( 0 ).getAction();
		if( action == null )
			recentWorkspacesMenu.remove( 0 );

		recentEditorsMenu.removeAll();
	}

	public void desktopPanelClosed( DesktopPanel desktopPanel )
	{
		ModelItem modelItem = desktopPanel.getModelItem();
		if( modelItem == null )
			return;

		recentEditorsMenu.add( new JMenuItem( new ShowEditorAction( modelItem ) ) );

		ShowEditorAction action = ( ShowEditorAction )recentEditorsMenu.getItem( 0 ).getAction();
		if( action == null )
			recentEditorsMenu.remove( 0 );
	}

	public void desktopPanelCreated( DesktopPanel desktopPanel )
	{
		for( int c = 0; c < recentEditorsMenu.getItemCount(); c++ )
		{
			ShowEditorAction action = ( ShowEditorAction )recentEditorsMenu.getItem( c ).getAction();
			if( action == null )
				continue;

			if( action.isReleased() )
			{
				recentEditorsMenu.remove( c );
				c-- ;
			}
			else if( action.getModelItem().equals( desktopPanel.getModelItem() ) )
			{
				recentEditorsMenu.remove( c );
				break;
			}
		}

		if( recentEditorsMenu.getItemCount() == 0 )
			recentEditorsMenu.add( "- empty -" ).setEnabled( false );
	}

	public void desktopPanelSelected( DesktopPanel desktopPanel )
	{
	}

	private static class ShowEditorAction extends AbstractAction
	{
		private Reference<ModelItem> ref;

		public ShowEditorAction( ModelItem modelItem )
		{
			super( modelItem.getName() );

			putValue( Action.SHORT_DESCRIPTION, "Reopen editor for [" + modelItem.getName() + "]" );
			ref = new WeakReference<ModelItem>( modelItem );
		}

		public ModelItem getModelItem()
		{
			return ref.get();
		}

		public void update()
		{
			ModelItem modelItem = ref.get();
			if( modelItem == null )
				return;

			putValue( Action.NAME, modelItem.getName() );
			putValue( Action.SHORT_DESCRIPTION, "Reopen editor for [" + modelItem.getName() + "]" );
		}

		public boolean isReleased()
		{
			return ref.get() == null;
		}

		public void actionPerformed( ActionEvent e )
		{
			ModelItem modelItem = ref.get();
			if( modelItem != null )
				UISupport.showDesktopPanel( modelItem );
			else
				UISupport.showErrorMessage( "Item [" + getValue( Action.NAME ) + "] is no longer available" );
		}
	}
}