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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * 
 * @author lars
 */
public class TreePathUtils
{
	public static TreePath getPath( TreeNode treeNode )
	{
		List<Object> nodes = new ArrayList<Object>();
		if( treeNode != null )
		{
			nodes.add( treeNode );
			treeNode = treeNode.getParent();
			while( treeNode != null )
			{
				nodes.add( 0, treeNode );
				treeNode = treeNode.getParent();
			}
		}

		return nodes.isEmpty() ? null : new TreePath( nodes.toArray() );
	}
}
