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

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.actions.MarkerAction;
import com.eviware.soapui.support.components.JXToolBar;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;

public class JXSoapUIActionListToolBar extends JXToolBar
{
	private Map<String, Action> actionMap = new HashMap<String, Action>();

	@SuppressWarnings( "unchecked" )
	public JXSoapUIActionListToolBar( ActionList actions, ModelItem modelItem )
	{
		addSpace( 1 );
		setRollover( true );
		putClientProperty( Options.HEADER_STYLE_KEY, HeaderStyle.SINGLE );
		setBorder( BorderFactory.createEmptyBorder( 3, 0, 3, 0 ) );

		for( int i = 0; i < actions.getActionCount(); i++ )
		{
			Action action = actions.getActionAt( i );

			if( action instanceof MarkerAction )
				continue;

			if( action == ActionSupport.SEPARATOR_ACTION )
			{
				addSeparator();
			}
			else if( action instanceof ActionSupport.ActionListAction )
			{
				// JMenu subMenu = buildMenu(
				// ((ActionListAction)action).getActionList() );
				// if( subMenu == null )
				// subMenu = new JMenu(
				// ((ActionListAction)action).getActionList().getLabel() );
				// menu.add( subMenu);
			}
			else if( action != null )
			{
				JComponent component = null;

				if( action instanceof SoapUIActionMarker )
				{
					SoapUIAction soapUIAction = ( ( SoapUIActionMarker )action ).getSoapUIAction();
					component = ActionComponentRegistry.buildActionComponent( soapUIAction, modelItem );
					actionMap.put( soapUIAction.getId(), action );
				}

				if( component != null )
					add( component );
				else
					add( action );
			}
		}
	}

	public JXSoapUIActionListToolBar( ModelItem modelItem )
	{
		this( ActionListBuilder.buildActions( modelItem, "EditorToolbar" ), modelItem );
	}

	public void setEnabled( String actionId, boolean enabled )
	{
		if( actionMap.containsKey( actionId ) )
			actionMap.get( actionId ).setEnabled( enabled );
	}

}
