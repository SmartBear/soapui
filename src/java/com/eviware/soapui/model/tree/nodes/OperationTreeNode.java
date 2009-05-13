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

package com.eviware.soapui.model.tree.nodes;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.settings.UISettings;

/**
 * SoapUITreeNode for Operation implementations
 * 
 * @author Ole.Matzura
 */

public class OperationTreeNode extends AbstractModelItemTreeNode<Operation>
{
	private List<RequestTreeNode> requestNodes = new ArrayList<RequestTreeNode>();
	private ReorderPropertyChangeListener propertyChangeListener = new ReorderPropertyChangeListener();

	public OperationTreeNode( Operation operation, SoapUITreeModel treeModel )
	{
		super( operation, operation.getInterface(), treeModel );

		for( int c = 0; c < operation.getRequestCount(); c++ )
		{
			Request request = operation.getRequestAt( c );
			request.addPropertyChangeListener( Request.NAME_PROPERTY, propertyChangeListener );
			requestNodes.add( new RequestTreeNode( request, getTreeModel() ) );
		}

		initOrdering( requestNodes, UISettings.ORDER_REQUESTS );
		treeModel.mapModelItems( requestNodes );
	}

	public void release()
	{
		super.release();

		for( RequestTreeNode treeNode : requestNodes )
		{
			treeNode.getModelItem().removePropertyChangeListener( Request.NAME_PROPERTY, propertyChangeListener );
			treeNode.release();
		}
	}

	public Operation getOperation()
	{
		return ( Operation )getModelItem();
	}

	public void requestAdded( Request request )
	{
		RequestTreeNode requestTreeNode = new RequestTreeNode( request, getTreeModel() );
		requestNodes.add( requestTreeNode );
		reorder( false );
		request.addPropertyChangeListener( Request.NAME_PROPERTY, propertyChangeListener );
		getTreeModel().notifyNodeInserted( requestTreeNode );
	}

	public void requestRemoved( Request request )
	{
		SoapUITreeNode requestTreeNode = getTreeModel().getTreeNode( request );
		if( requestNodes.contains( requestTreeNode ) )
		{
			getTreeModel().notifyNodeRemoved( requestTreeNode );
			requestNodes.remove( requestTreeNode );
			request.removePropertyChangeListener( propertyChangeListener );
		}
		else
			throw new RuntimeException( "Removing unkown request" );
	}
}
