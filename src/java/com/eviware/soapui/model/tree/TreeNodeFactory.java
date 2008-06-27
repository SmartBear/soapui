package com.eviware.soapui.model.tree;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.tree.nodes.OperationTreeNode;
import com.eviware.soapui.model.tree.nodes.RestResourceTreeNode;

public class TreeNodeFactory
{
	public static SoapUITreeNode createTreeNode( ModelItem modelItem, SoapUITreeModel treeModel)
	{
		if( modelItem instanceof WsdlOperation )
			return new OperationTreeNode( (Operation) modelItem, treeModel );
		else if( modelItem instanceof RestResource )
			return new RestResourceTreeNode( (RestResource) modelItem, treeModel );
		
		return null;
	}
}
