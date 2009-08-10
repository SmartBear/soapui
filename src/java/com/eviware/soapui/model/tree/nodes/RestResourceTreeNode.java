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
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.tree.TreeNodeFactory;

/**
 * SoapUITreeNode for Operation implementations
 * 
 * @author Ole.Matzura
 */

public class RestResourceTreeNode extends AbstractModelItemTreeNode<RestResource> implements PropertyChangeListener
{
	private List<RestResourceTreeNode> resourceNodes = new ArrayList<RestResourceTreeNode>();
	private List<RestMethodTreeNode> methodNodes = new ArrayList<RestMethodTreeNode>();
	private final RestResource restResource;

	private ReorderPropertyChangeListener propertyChangeListener = new ReorderPropertyChangeListener();

	public RestResourceTreeNode( RestResource restResource, SoapUITreeModel treeModel )
	{
		super( restResource, restResource.getParent(), treeModel );
		this.restResource = restResource;
		treeModel.mapModelItem( this );

		for( int c = 0; c < restResource.getChildResourceCount(); c++ )
		{
			resourceNodes.add( new RestResourceTreeNode( restResource.getChildResourceAt( c ), getTreeModel() ) );
		}
		treeModel.mapModelItems( resourceNodes );

		for( int c = 0; c < restResource.getRestMethodCount(); c++ )
		{
			methodAdded( restResource.getRestMethodAt( c ) );
		}
	}

	@Override
	public SoapUITreeNode getParentTreeNode()
	{
		return restResource.getParentResource() == null ? super.getParentTreeNode() : getTreeModel().getTreeNode(
				restResource.getParentResource() );
	}

	@Override
	public String toString()
	{
		return restResource.getName() + " [" + restResource.getFullPath() + "]";
	}

	@Override
	public int getChildCount()
	{
		return restResource.getRestMethodCount() + restResource.getChildResourceCount();
	}

	@Override
	public SoapUITreeNode getChildNode( int index )
	{
		int childCount = methodNodes.size();
		if( index < childCount )
			return methodNodes.get( index );
		else
			return resourceNodes.get( index - childCount );
	}

	@Override
	public int getIndexOfChild( Object child )
	{
		int result = methodNodes.indexOf( child );
		if( result == -1 )
		{
			result = resourceNodes.indexOf( child );
			if( result >= 0 )
				result += methodNodes.size();
		}

		return result;
	}

	public void release()
	{
		super.release();

		for( RestMethodTreeNode treeNode : methodNodes )
		{
			treeNode.getModelItem().removePropertyChangeListener( Request.NAME_PROPERTY, propertyChangeListener );
			treeNode.release();
		}
		
		for( RestResourceTreeNode resource : resourceNodes )
		{
			resource.release();
		}
	}

	public void addChildResource( RestResource restResource )
	{
		RestResourceTreeNode operationTreeNode = ( RestResourceTreeNode )TreeNodeFactory.createTreeNode( restResource,
				getTreeModel() );

		resourceNodes.add( operationTreeNode );
		getTreeModel().notifyNodeInserted( operationTreeNode );
	}

	public void removeChildResource( RestResourceTreeNode childResource )
	{
		if( resourceNodes.contains( childResource ) )
		{
			getTreeModel().notifyNodeRemoved( childResource );
			resourceNodes.remove( childResource );
		}
	}

	/*
	 * public void requestAdded(Request request) { if (request instanceof
	 * RestRequest) { RestMethod method = ((RestRequest)
	 * request).getRestMethod(); RestMethodTreeNode node = (RestMethodTreeNode)
	 * getTreeModel() .getTreeNode(method); if (methodNodes.contains(node)) {
	 * node.requestAdded(request); } } }
	 * 
	 * public void requestRemoved(Request request) { if (request instanceof
	 * RestRequest) { RestMethod method = ((RestRequest)
	 * request).getRestMethod(); RestMethodTreeNode node = (RestMethodTreeNode)
	 * getTreeModel() .getTreeNode(method); if (methodNodes.contains(node)) {
	 * node.requestRemoved(request); } } }
	 */

	public void methodAdded( RestMethod method )
	{
		RestMethodTreeNode methodTreeNode = new RestMethodTreeNode( method, getTreeModel() );
		methodNodes.add( methodTreeNode );
		reorder( false );
		method.addPropertyChangeListener( Request.NAME_PROPERTY, propertyChangeListener );
		getTreeModel().notifyNodeInserted( methodTreeNode );
	}

	public void methodRemoved( RestMethod method )
	{
		SoapUITreeNode methodTreeNode = getTreeModel().getTreeNode( method );
		if( methodNodes.contains( methodTreeNode ) )
		{
			getTreeModel().notifyNodeRemoved( methodTreeNode );
			methodNodes.remove( methodTreeNode );
			method.removePropertyChangeListener( propertyChangeListener );
		}
		else
			throw new RuntimeException( "Removing unknown method" );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		super.propertyChange( evt );
		if( evt.getPropertyName().equals( "childMethods" ) )
		{
			if( evt.getNewValue() != null )
			{
				methodAdded( ( RestMethod )evt.getNewValue() );
			}
			else
			{
				methodRemoved( ( RestMethod )evt.getOldValue() );
			}
		}
	}
}
