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

package com.eviware.soapui.support.dnd;

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JComponent;

public interface SoapUIDragAndDropable<T>
{
	public JComponent getComponent();

	public void setDragInfo( String dropInfo );

	public void selectModelItem( T modelItem );

	public T getModelItemForLocation( int x, int y );

	public Rectangle getModelItemBounds( T modelItem );

	public Component getRenderer( T modelItem );

	public void toggleExpansion( T modelItem );
}
