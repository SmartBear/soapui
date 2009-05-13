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
import java.awt.event.KeyEvent;

import javax.swing.Action;

/**
 * A simple list of actions
 * 
 * @author Ole.Matzura
 */

public interface ActionList
{
	public int getActionCount();

	public Action getActionAt( int index );

	public Action getDefaultAction();

	public boolean hasDefaultAction();

	public void performDefaultAction( ActionEvent event );

	public void addAction( Action action );

	public void addSeparator();

	public void insertAction( Action action, int index );

	public void insertSeparator( int index );

	public String getLabel();

	public void clear();

	public void dispatchKeyEvent( KeyEvent e );

	public void addActions( ActionList defaultActions );

	public void setDefaultAction( Action action );

	public void removeAction( int index );
}
