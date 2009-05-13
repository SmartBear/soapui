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

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.InterfaceListener;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.tree.TreeNodeFactory;

/**
 * SoapUITreeNode for Interface implementations
 * 
 * @author Ole.Matzura
 */

public class InterfaceTreeNode extends AbstractModelItemTreeNode<Interface>
{
	private InternalInterfaceListener interfaceListener;
	private List<OperationTreeNode> operationNodes = new ArrayList<OperationTreeNode>();

	public InterfaceTreeNode( Interface iface, SoapUITreeModel treeModel )
	{
		super( iface, iface.getProject(), treeModel );

		interfaceListener = new InternalInterfaceListener();
		iface.addInterfaceListener( interfaceListener );

		for( int c = 0; c < iface.getOperationCount(); c++ )
		{
			operationNodes.add( ( OperationTreeNode )TreeNodeFactory.createTreeNode( iface.getOperationAt( c ),
					getTreeModel() ) );
		}

		treeModel.mapModelItems( operationNodes );
	}

	public void release()
	{
		super.release();

		getInterface().removeInterfaceListener( interfaceListener );

		for( OperationTreeNode treeNode : operationNodes )
			treeNode.release();
	}

	public Interface getInterface()
	{
		return ( Interface )getModelItem();
	}

	public int getChildCount()
	{
		return operationNodes.size();
	}

	public int getIndexOfChild( Object child )
	{
		return operationNodes.indexOf( child );
	}

	public SoapUITreeNode getChildNode( int index )
	{
		return operationNodes.get( index );
	}

	private class InternalInterfaceListener implements InterfaceListener
	{
		public void requestAdded( Request request )
		{
			OperationTreeNode operationTreeNode = ( OperationTreeNode )getTreeModel().getTreeNode( request.getOperation() );
			if( operationTreeNode != null )
				operationTreeNode.requestAdded( request );
		}

		public void requestRemoved( Request request )
		{
			OperationTreeNode operationTreeNode = ( OperationTreeNode )getTreeModel().getTreeNode( request.getOperation() );
			if( operationTreeNode != null )
				operationTreeNode.requestRemoved( request );
		}

		public void operationAdded( Operation operation )
		{
			if( operation instanceof RestResource )
			{
				RestResource restResource = ( RestResource )operation;
				if( restResource.getParentResource() != null )
				{
					RestResourceTreeNode treeNode = ( RestResourceTreeNode )getTreeModel().getTreeNode(
							restResource.getParentResource() );
					treeNode.addChildResource( restResource );
					return;
				}
			}

			OperationTreeNode operationTreeNode = ( OperationTreeNode )TreeNodeFactory.createTreeNode( operation,
					getTreeModel() );

			operationNodes.add( operationTreeNode );
			getTreeModel().notifyNodeInserted( operationTreeNode );
		}

		public void operationRemoved( Operation operation )
		{
			SoapUITreeNode treeNode = getTreeModel().getTreeNode( operation );
			if( operationNodes.contains( treeNode ) )
			{
				getTreeModel().notifyNodeRemoved( treeNode );
				operationNodes.remove( treeNode );
			}
			else if( treeNode instanceof RestResourceTreeNode )
			{
				SoapUITreeNode parentNode = treeNode.getParentTreeNode();
				if( parentNode instanceof RestResourceTreeNode )
				{
					( ( RestResourceTreeNode )parentNode ).removeChildResource( ( RestResourceTreeNode )treeNode );
				}
			}
			else
				throw new RuntimeException( "Removing unkown operation" );
		}

		public void operationUpdated( Operation operation )
		{
		}
	}
}
