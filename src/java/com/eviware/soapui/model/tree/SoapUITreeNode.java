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

import javax.swing.JPopupMenu;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.swing.ActionList;

/**
 * Behaviour for navigator tree nodes
 * 
 * @author Ole.Matzura
 */

public interface SoapUITreeNode
{
	public int getChildCount();

	public int getIndexOfChild( Object child );

	public boolean valueChanged( Object newValue );

	public SoapUITreeNode getChildNode( int index );

	public boolean isLeaf();

	public JPopupMenu getPopup();

	public SoapUITreeNode getParentTreeNode();

	public void release();

	public ActionList getActions();

	public void reorder( boolean notify );

	public ModelItem getModelItem();
}
