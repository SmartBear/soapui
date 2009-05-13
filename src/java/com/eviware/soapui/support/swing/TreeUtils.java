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

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * 
 */
public class TreeUtils
{
	public static void expandAll( JTree tree, TreePath parent, boolean expand )
	{
		// Traverse children
		TreeNode node = ( TreeNode )parent.getLastPathComponent();
		if( node.getChildCount() >= 0 )
		{
			for( Enumeration e = node.children(); e.hasMoreElements(); )
			{
				TreeNode n = ( TreeNode )e.nextElement();
				TreePath path = parent.pathByAddingChild( n );
				expandAll( tree, path, expand );
			}
		}

		// Expansion or collapse must be done bottom-up
		if( expand )
		{
			tree.expandPath( parent );
		}
		else
		{
			tree.collapsePath( parent );
		}
	}
}
