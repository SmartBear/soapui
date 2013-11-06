/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.action.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import com.eviware.soapui.actions.UpdateableAction;

/**
 * Default ActionList implementation
 * 
 * @author Ole.Matzura
 */

public class DefaultActionList implements ActionList
{
	private List<Action> actions = new ArrayList<Action>();
	private Action defaultAction;
	private final String label;

	public DefaultActionList()
	{
		this( null );
	}

	public DefaultActionList( String label )
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}

	public int getActionCount()
	{
		return actions.size();
	}

	public Action getActionAt( int index )
	{
		return actions.get( index );
	}

	public Action getDefaultAction()
	{
		return defaultAction;
	}

	public void setDefaultAction( Action defaultAction )
	{
		this.defaultAction = defaultAction;
	}

	public void addAction( Action action )
	{
		actions.add( action );
	}

	public void addAction( Action action, boolean isDefault )
	{
		actions.add( action );
		if( isDefault )
			setDefaultAction( action );
	}

	public void addSeparator()
	{
		actions.add( ActionSupport.SEPARATOR_ACTION );
	}

	public void insertAction( Action action, int index )
	{
		actions.add( index, action );
	}

	public void insertSeparator( int index )
	{
		actions.add( index, ActionSupport.SEPARATOR_ACTION );
	}

	public boolean hasDefaultAction()
	{
		return defaultAction != null;
	}

	public void performDefaultAction( ActionEvent event )
	{
		if( defaultAction != null )
			defaultAction.actionPerformed( event );
	}

	public void clear()
	{
		actions.clear();
		defaultAction = null;
	}

	public void dispatchKeyEvent( KeyEvent e )
	{
		if( e.getKeyChar() == KeyEvent.VK_ENTER && defaultAction != null )
		{
			performDefaultAction( new ActionEvent( e.getSource(), 0, null ) );
			e.consume();
		}
		else
		{
			for( int c = 0; c < actions.size(); c++ )
			{
				Action action = actions.get( c );
				KeyStroke acc = ( KeyStroke )action.getValue( Action.ACCELERATOR_KEY );
				if( acc == null )
					continue;

				if( acc.equals( KeyStroke.getKeyStrokeForEvent( e ) ) )
				{
					action.actionPerformed( new ActionEvent( e.getSource(), 0, null ) );
					e.consume();
					return;
				}
			}
		}
	}

	public void addActions( ActionList defaultActions )
	{
		for( int c = 0; c < defaultActions.getActionCount(); c++ )
			addAction( defaultActions.getActionAt( c ) );
	}

	public void setEnabled( boolean b )
	{
		for( int c = 0; c < actions.size(); c++ )
		{
			Action action = actions.get( c );
			action.setEnabled( b );
		}
	}

	public void removeAction( int index )
	{
		actions.remove( index );
	}

	/**
	 * Update all actions that are instances of UpdateableAction.
	 */
	public void update()
	{
		for( Action a : actions )
		{
			if( a instanceof UpdateableAction )
			{
				( ( UpdateableAction )a ).update();
			}
		}
	}

}
