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

package com.eviware.soapui.model.tree;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.tree.TreeNode;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;

/**
 * Base implementation of SoapUITreeNode interface
 * 
 * @author Ole.Matzura
 */

public abstract class AbstractTreeNode<T extends ModelItem> implements SoapUITreeNode
{
	private T modelItem;

	public AbstractTreeNode( T modelItem )
	{
		this.modelItem = modelItem;
	}

	public boolean valueChanged( Object newValue )
	{
		return false;
	}

	public boolean isLeaf()
	{
		return getChildCount() == 0;
	}

	public JPopupMenu getPopup()
	{
		return ActionSupport.buildPopup( getActions() );
	}

	public ActionList getActions()
	{
		return ActionListBuilder.buildActions( modelItem );
	}

	public T getModelItem()
	{
		return modelItem;
	}

	public String toString()
	{
		return modelItem.getName();
	}

	public void reorder( boolean notify )
	{
	}

	public Enumeration children()
	{
		Vector<TreeNode> children = new Vector<TreeNode>();
		for( int c = 0; c < getChildCount(); c++ )
			children.add( getChildAt( c ) );

		return children.elements();
	}

	public boolean getAllowsChildren()
	{
		return !isLeaf();
	}

	public TreeNode getChildAt( int childIndex )
	{
		return getChildNode( childIndex );
	}

	public int getIndex( TreeNode node )
	{
		return getIndexOfChild( node );
	}

	public TreeNode getParent()
	{
		return getParentTreeNode();
	}

}
