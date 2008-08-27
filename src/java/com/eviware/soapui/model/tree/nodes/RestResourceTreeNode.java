/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.tree.TreeNodeFactory;

/**
 * SoapUITreeNode for Operation implementations
 * 
 * @author Ole.Matzura
 */

public class RestResourceTreeNode extends OperationTreeNode
{
	private List<RestResourceTreeNode> resourceNodes = new ArrayList<RestResourceTreeNode>();
	private final RestResource restResource;

	public RestResourceTreeNode(RestResource restResource, SoapUITreeModel treeModel )
   {
      super( restResource, treeModel );
		this.restResource = restResource;
		
		for( int c = 0; c < restResource.getResourceCount(); c++ )
		{
			resourceNodes.add( new RestResourceTreeNode( restResource.getResourcetAt(c), getTreeModel() )); 
		}
		
		treeModel.mapModelItems( resourceNodes );
   }
	
   @Override
	public SoapUITreeNode getParentTreeNode()
	{
   	return restResource.getParentResource() == null ? super.getParentTreeNode() : 
   		getTreeModel().getTreeNode( restResource.getParentResource() );
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if( evt.getPropertyName().equals("resources") && evt.getSource() == restResource )
		{
			if( evt.getNewValue() != null && evt.getOldValue() == null )
			{
				RestResourceTreeNode resourceNode = new RestResourceTreeNode( (RestResource) evt.getNewValue(), getTreeModel() );
				resourceNodes.add( resourceNode );
				getTreeModel().notifyNodeInserted( resourceNode );
			}
			else if( evt.getOldValue() != null && evt.getNewValue() == null )
			{
				SoapUITreeNode resourceNode = getTreeModel().getTreeNode( (ModelItem) evt.getOldValue() );
				if( resourceNodes.contains( resourceNode ))
				{
				   getTreeModel().notifyNodeRemoved( resourceNode );
				   resourceNodes.remove( resourceNode );
				}
				else throw new RuntimeException( "Removing unkown Resource" );
			}
		}
		else
		{
			super.propertyChange(evt);
		}
	}
	
	@Override
	public String toString()
	{
		return restResource.getName() + " [" + restResource.getFullPath() + "]";
	}

	@Override
	public int getChildCount()
	{
		return super.getChildCount() + restResource.getResourceCount();
	}

	@Override
	public SoapUITreeNode getChildNode(int index)
	{
		int childCount = super.getChildCount();
		if( index < childCount)
			return super.getChildNode(index);
		else
			return resourceNodes.get( index-childCount );
	}

	@Override
	public int getIndexOfChild(Object child)
	{
		int result = super.getIndexOfChild(child);
		if( result == -1 )
		{
			result = resourceNodes.indexOf(child);
			if( result >= 0 )
				result += super.getChildCount();
		}
		
		return result;
	}

	public void release()
	{
		super.release();
	}

    public void addSubResource(RestResource restResource) {
        RestResourceTreeNode operationTreeNode = (RestResourceTreeNode) TreeNodeFactory.createTreeNode( restResource, getTreeModel() );

          resourceNodes.add( operationTreeNode );
         getTreeModel().notifyNodeInserted( operationTreeNode );
    }
}
