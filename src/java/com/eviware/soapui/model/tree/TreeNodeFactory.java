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

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.tree.nodes.OperationTreeNode;
import com.eviware.soapui.model.tree.nodes.RestResourceTreeNode;

public class TreeNodeFactory
{
	public static SoapUITreeNode createTreeNode( ModelItem modelItem, SoapUITreeModel treeModel )
	{
		if( modelItem instanceof WsdlOperation )
			return new OperationTreeNode( ( Operation )modelItem, treeModel );
		else if( modelItem instanceof RestResource )
			return new RestResourceTreeNode( ( RestResource )modelItem, treeModel );

		return null;
	}
}
