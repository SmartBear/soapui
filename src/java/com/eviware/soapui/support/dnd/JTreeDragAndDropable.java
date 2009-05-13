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
import javax.swing.JTree;

public abstract class JTreeDragAndDropable<T> implements SoapUIDragAndDropable<T>
{
	private JTree tree;

	public JTreeDragAndDropable( JTree tree )
	{
		this.tree = tree;
	}

	public JTree getTree()
	{
		return tree;
	}

	public Component getCellRendererComponent( Object lastPathComponent, boolean b, boolean object, boolean object2,
			int i, boolean c )
	{
		return tree.getCellRenderer().getTreeCellRendererComponent( tree, lastPathComponent, b, object, object2, i, c );
	}

	public JComponent getComponent()
	{
		return tree;
	}

	public void setDragInfo( String dropInfo )
	{
		tree.setToolTipText( dropInfo );
	}

	public Rectangle getModelItemBounds( T path )
	{
		return tree.getRowBounds( getRowForModelItem( path ) );
	}

	public T getModelItemForLocation( int x, int y )
	{
		int rowForLocation = tree.getRowForLocation( x, y );
		if( rowForLocation == -1 )
			rowForLocation = tree.getClosestRowForLocation( x, y );

		return getModelItemAtRow( rowForLocation );
	}

	public void selectModelItem( T path )
	{
		int row = getRowForModelItem( path );
		tree.setSelectionRow( row );
	}

	public void toggleExpansion( T last )
	{
		int row = getRowForModelItem( last );
		if( tree.isExpanded( row ) )
			tree.collapseRow( row );
		else
			tree.expandRow( row );
	}

	public abstract int getRowForModelItem( T modelItem );

	public abstract T getModelItemAtRow( int row );

}
