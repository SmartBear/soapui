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

package com.eviware.soapui.support.action.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import com.eviware.soapui.support.actions.MarkerAction;
import com.eviware.soapui.support.components.JXToolBar;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * ActionList-related utilities
 * 
 * @author Ole.Matzura
 */

public class ActionSupport
{
	public static JPopupMenu buildPopup( ActionList actions )
	{
		if( actions == null || actions.getActionCount() == 0 )
			return null;

		JPopupMenu popup = new JPopupMenu( actions.getLabel() );

		return ActionSupport.addActions( actions, popup );
	}

	public static JMenu buildMenu( ActionList actions )
	{
		if( actions == null || actions.getActionCount() == 0 )
			return null;

		JMenu menu = new JMenu( actions.getLabel() );

		return ActionSupport.addActions( actions, menu );
	}

	public static JPopupMenu addActions( ActionList actions, JPopupMenu popup )
	{
		if( actions == null || actions.getActionCount() == 0 )
			return popup;

		for( int i = 0; i < actions.getActionCount(); i++ )
		{
			Action action = actions.getActionAt( i );
			if( action instanceof MarkerAction )
				continue;

			if( action == ActionSupport.SEPARATOR_ACTION )
				popup.addSeparator();
			else if( action instanceof ActionSupport.ActionListAction )
			{
				ActionList actionList = ( ( ActionListAction )action ).getActionList();
				if( actionList == null || actionList.getActionCount() == 0 )
					System.err.println( "null/empty ActionList in action " + action.getValue( Action.NAME ) );
				else
					popup.add( buildMenu( actionList ) );
			}
			else
				popup.add( action );
		}

		return popup;
	}

	public static JMenu addActions( ActionList actions, JMenu menu )
	{
		if( actions == null || menu == null )
			return menu;

		for( int i = 0; i < actions.getActionCount(); i++ )
		{
			Action action = actions.getActionAt( i );

			if( action instanceof MarkerAction )
				continue;

			if( action == ActionSupport.SEPARATOR_ACTION )
			{
				menu.addSeparator();
			}
			else if( action instanceof ActionSupport.ActionListAction )
			{
				JMenu subMenu = buildMenu( ( ( ActionListAction )action ).getActionList() );
				if( subMenu == null )
					subMenu = new JMenu( ( ( ActionListAction )action ).getActionList().getLabel() );
				menu.add( subMenu );
			}
			else if( action != null )
			{
				menu.add( action );
			}
		}

		return menu;
	}

	public final static Action SEPARATOR_ACTION = new AbstractAction()
	{
		public void actionPerformed( ActionEvent e )
		{
		}
	};

	public static class ActionListAction extends AbstractAction
	{
		private final ActionList actionList;

		public ActionListAction( ActionList actionList )
		{
			this.actionList = actionList;
		}

		public ActionList getActionList()
		{
			return actionList;
		}

		public void actionPerformed( ActionEvent e )
		{
			Action defaultAction = actionList.getDefaultAction();
			if( defaultAction != null )
				defaultAction.actionPerformed( e );
		}
	};

	public static JPopupMenu insertActions( ActionList actions, JPopupMenu popup, int index )
	{
		for( int i = 0; i < actions.getActionCount(); i++ )
		{
			Action action = actions.getActionAt( i );
			if( action instanceof MarkerAction )
				continue;

			if( action == ActionSupport.SEPARATOR_ACTION )
				popup.insert( new JPopupMenu.Separator(), index + i );
			else if( action instanceof ActionSupport.ActionListAction )
				popup.insert( buildMenu( ( ( ActionSupport.ActionListAction )action ).getActionList() ), index + i );
			else
				popup.insert( action, index + i );
		}

		return popup;
	}

	public static void addActions( ActionList actionList, ButtonBarBuilder builder )
	{
		for( int c = 0; c < actionList.getActionCount(); c++ )
		{
			Action action = actionList.getActionAt( c );
			if( action == SEPARATOR_ACTION )
			{
				builder.addUnrelatedGap();
			}
			else
			{
				if( c > 0 )
					builder.addRelatedGap();

				builder.addFixed( new JButton( action ) );
			}
		}
	}

	public static void addActions( ActionList actionList, JXToolBar toolbar )
	{
		for( int c = 0; c < actionList.getActionCount(); c++ )
		{
			Action action = actionList.getActionAt( c );
			if( action == SEPARATOR_ACTION )
			{
				toolbar.addUnrelatedGap();
			}
			else
			{
				if( c > 0 )
					toolbar.addRelatedGap();

				toolbar.addFixed( new JButton( action ) );
			}
		}
	}
}
