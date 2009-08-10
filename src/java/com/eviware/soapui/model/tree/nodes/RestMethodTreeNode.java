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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;

/**
 * SoapUITreeNode for RestRequest implementations
 * 
 * @author dain.nilsson
 */

public class RestMethodTreeNode extends AbstractModelItemTreeNode<RestMethod>
{
	private List<RequestTreeNode> requestNodes = new ArrayList<RequestTreeNode>();
	private ReorderPropertyChangeListener propertyChangeListener = new ReorderPropertyChangeListener();

	public RestMethodTreeNode( RestMethod method, SoapUITreeModel treeModel )
	{
		super( method, method.getParent(), treeModel );
		treeModel.mapModelItem( this );

		for( RestRequest request : method.getRequestList() )
		{
			requestAdded( request );
		}
	}

	@Override
	public int getChildCount()
	{
		return requestNodes.size();
	}

	@Override
	public SoapUITreeNode getChildNode( int index )
	{
		return requestNodes.get( index );
	}

	@Override
	public int getIndexOfChild( Object child )
	{
		return requestNodes.indexOf( child );
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
			throw new RuntimeException( "Removing unknown request" );
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

	public void propertyChange( PropertyChangeEvent evt )
	{
		super.propertyChange( evt );
		if( evt.getPropertyName().equals( "childRequests" ) )
		{
			if( evt.getNewValue() != null )
			{
				requestAdded( ( RestRequest )evt.getNewValue() );
			}
			else
			{
				requestRemoved( ( RestRequest )evt.getOldValue() );
			}
		}
	}
}
