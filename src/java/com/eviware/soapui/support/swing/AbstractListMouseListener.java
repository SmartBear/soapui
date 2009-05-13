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

package com.eviware.soapui.support.swing;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JPopupMenu;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionSupport;

/**
 * Abstract MouseListener for JLists that displays a row-sensitive popup-menu
 * 
 * @author ole.matzura
 */

public abstract class AbstractListMouseListener extends MouseAdapter
{
	private boolean enablePopup;
	private JPopupMenu menu;

	protected abstract ActionList getActionsForRow( JList list, int row );

	public AbstractListMouseListener()
	{
		this( true );
	}

	public AbstractListMouseListener( boolean enablePopup )
	{
		this.enablePopup = enablePopup;
	}

	public void mouseClicked( MouseEvent e )
	{
		if( e.getClickCount() < 2 )
			return;

		JList list = ( JList )e.getSource();

		int selectedIndex = list.getSelectedIndex();

		ActionList actions = selectedIndex == -1 ? getDefaultActions() : getActionsForRow( list, selectedIndex );

		if( actions != null )
			actions.performDefaultAction( new ActionEvent( this, 0, null ) );
	}

	protected ActionList getDefaultActions()
	{
		return null;
	}

	public void mousePressed( MouseEvent e )
	{
		if( e.isPopupTrigger() )
			showPopup( e );
	}

	public void mouseReleased( MouseEvent e )
	{
		if( e.isPopupTrigger() )
			showPopup( e );
	}

	public void showPopup( MouseEvent e )
	{
		if( !enablePopup )
			return;

		ActionList actions = null;
		JList list = ( JList )e.getSource();
		int row = list.locationToIndex( e.getPoint() );
		if( row == -1 || !list.getCellBounds( row, row ).contains( e.getPoint() ) )
		{
			if( list.getSelectedIndex() != -1 )
				list.clearSelection();

			actions = getDefaultActions();
		}
		else
		{
			if( list.getSelectedIndex() != row )
			{
				list.setSelectedIndex( row );
			}

			actions = getActionsForRow( list, row );
		}

		if( actions == null || actions.getActionCount() == 0 )
			return;

		JPopupMenu popup = menu == null ? ActionSupport.buildPopup( actions ) : menu;
		UISupport.showPopup( popup, list, e.getPoint() );
	}

	public void setPopupMenu( JPopupMenu menu )
	{
		this.menu = menu;
	}
}