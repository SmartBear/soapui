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

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionGroup;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.SoapUIMultiAction;
import com.eviware.soapui.support.action.SoapUIActionRegistry.SeperatorAction;
import com.eviware.soapui.support.action.SoapUIActionRegistry.SoapUIActionGroupAction;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * Builder for ActionLists for a variety of targets
 * 
 * @author ole.matzura
 */

public class ActionListBuilder
{
	/**
	 * Builds default ActionList for specified ModelItem
	 * 
	 * @param <T>
	 *           the type of ModelItem
	 * @param modelItem
	 *           the target ModelItem
	 * @return the ActionList
	 */

	public static <T extends ModelItem> ActionList buildActions( T modelItem )
	{
		return buildActions( modelItem, "" );
	}

	/**
	 * Creates an ActionList for the specified modelItem
	 */

	public static <T extends ModelItem> ActionList buildActions( T modelItem, String suffix )
	{
		Class<?> clazz = modelItem.getClass();
		ActionList actions = buildActions( clazz.getSimpleName() + suffix + "Actions", modelItem );

		if( actions.getActionCount() == 0 )
		{
			clazz = clazz.getSuperclass();

			while( actions.getActionCount() == 0 && clazz != null && ModelItem.class.isAssignableFrom( clazz ) )
			{
				actions = buildActions( clazz.getSimpleName() + suffix + "Actions", modelItem );
				clazz = clazz.getSuperclass();
			}
		}

		return actions;
	}

	@SuppressWarnings( "hiding" )
	public static <T extends ModelItem> ActionList buildActions( String actionGroup, T modelItem )
	{
		DefaultActionList actions = new DefaultActionList();

		SoapUIActionGroup<T> group = SoapUI.getActionRegistry().getActionGroup( actionGroup );
		if( group != null )
		{
			addActions( modelItem, actions, group );
		}

		return actions;
	}

	/**
	 * Adds the specified ActionMappings to the specified ActionList for the
	 * specified modelItem
	 */

	@SuppressWarnings( { "hiding", "unchecked" } )
	protected static <T extends ModelItem> void addActions( T modelItem, ActionList actions,
			SoapUIActionGroup<T> actionGroup )
	{
		boolean prevWasSeparator = false;
		for( SoapUIActionMapping<? extends ModelItem> mapping : actionGroup.getActionMappings( modelItem ) )
		{
			if( mapping == null )
				continue;

			SoapUIActionMapping<T> actionMapping = ( com.eviware.soapui.support.action.SoapUIActionMapping<T> )mapping;
			SoapUIAction<T> action = ( SoapUIAction<T> )mapping.getAction();

			if( action != null && !action.applies( modelItem ) )
			{
				System.out.println( action + " does not apply to " + modelItem );
			}
			else if( action instanceof SeperatorAction )
			{
				if( !prevWasSeparator )
				{
					actions.addAction( ActionSupport.SEPARATOR_ACTION );
				}
				prevWasSeparator = true;
			}
			else if( action instanceof SoapUIActionGroupAction )
			{
				DefaultActionList subActions = new DefaultActionList( mapping.getName() );
				SoapUIActionGroup<T> subGroup = ( ( SoapUIActionGroupAction<T> )action ).getActionGroup();
				addActions( modelItem, subActions, subGroup );
				ActionSupport.ActionListAction actionListAction = new ActionSupport.ActionListAction( subActions );
				actions.addAction( actionListAction );
				actionListAction.setEnabled( mapping.isEnabled() );
				prevWasSeparator = false;
			}
			else if( action != null )
			{
				SwingActionDelegate<T> actionDelegate = new SwingActionDelegate<T>( actionMapping, modelItem );
				actions.addAction( actionDelegate );
				if( mapping.isDefault() )
					actions.setDefaultAction( actionDelegate );

				actionDelegate.setEnabled( mapping.isEnabled() );
				prevWasSeparator = false;
			}
		}
	}
	
	public static ActionList buildMultiActions( ModelItem[] modelItems )
	{
		DefaultActionList actions = new DefaultActionList();

		SoapUIActionGroup<?> group = SoapUI.getActionRegistry().getActionGroup( "SoapUIMultiActions" );
		if( group != null )
		{
			addMultiActions( modelItems, actions, group );
		}

		return actions;
	}
	
	/**
	 * Adds the specified ActionMappings to the specified ActionList for the
	 * specified modelItem
	 */

	@SuppressWarnings( { "unchecked" } )
	protected static void addMultiActions( ModelItem[] modelItems, ActionList actions, SoapUIActionGroup actionGroup )
	{
		boolean prevWasSeparator = false;
		SoapUIActionMappingList actionMappings = actionGroup.getActionMappings( null );
		for( int c = 0; c < actionMappings.size(); c++ )
		{
			SoapUIActionMapping mapping = ( SoapUIActionMapping )actionMappings.get( c );
			if( mapping == null )
				continue;

			SoapUIAction action = mapping.getAction();

			if( action instanceof SeperatorAction )
			{
				if( !prevWasSeparator )
				{
					actions.addAction( ActionSupport.SEPARATOR_ACTION );
				}
				prevWasSeparator = true;
			}
			else if( action instanceof SoapUIActionGroupAction )
			{
				DefaultActionList subActions = new DefaultActionList( mapping.getName() );
				SoapUIActionGroup subGroup = ( ( SoapUIActionGroupAction )action ).getActionGroup();
				addMultiActions( modelItems, subActions, subGroup );
				ActionSupport.ActionListAction actionListAction = new ActionSupport.ActionListAction( subActions );
				actions.addAction( actionListAction );
				actionListAction.setEnabled( mapping.isEnabled() );
				prevWasSeparator = false;
			}
			else if( action instanceof SoapUIMultiAction )
			{
				List<ModelItem> targets = new ArrayList<ModelItem>();
				for( ModelItem target : modelItems )
				{
					if( action.applies( target ) )
					{
						targets.add( target );
					}
				}

				if( targets.size() > 0 )
				{
					SwingMultiActionDelegate actionDelegate = new SwingMultiActionDelegate( mapping, modelItems );
					actions.addAction( actionDelegate );
					if( mapping.isDefault() )
						actions.setDefaultAction( actionDelegate );

					actionDelegate.setEnabled( mapping.isEnabled() );
					prevWasSeparator = false;
				}
			}
		}
	}
}