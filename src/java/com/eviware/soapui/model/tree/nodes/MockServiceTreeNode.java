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

import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.mock.MockServiceListener;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.settings.UISettings;

/**
 * SoapUITreeNode for MockService implementations
 * 
 * @author Ole.Matzura
 */

public class MockServiceTreeNode extends AbstractModelItemTreeNode<MockService>
{
	private ReorderPropertyChangeListener propertyChangeListener = new ReorderPropertyChangeListener();
	private List<MockOperationTreeNode> mockOperationNodes = new ArrayList<MockOperationTreeNode>();
	private InternalMockServiceListener mockServiceListener;
	private PropertiesTreeNode<?> propertiesTreeNode;

	public MockServiceTreeNode( MockService mockService, SoapUITreeModel treeModel )
	{
		super( mockService, mockService.getProject(), treeModel );

		mockServiceListener = new InternalMockServiceListener();
		mockService.addMockServiceListener( mockServiceListener );

		for( int c = 0; c < mockService.getMockOperationCount(); c++ )
		{
			MockOperation mockOperation = mockService.getMockOperationAt( c );
			mockOperation.addPropertyChangeListener( MockService.NAME_PROPERTY, propertyChangeListener );
			mockOperationNodes.add( new MockOperationTreeNode( mockOperation, getTreeModel() ) );
		}

		initOrdering( mockOperationNodes, UISettings.ORDER_MOCKOPERATION );

		getTreeModel().mapModelItems( mockOperationNodes );

		propertiesTreeNode = PropertiesTreeNode.createDefaultPropertiesNode( mockService, getTreeModel() );
		getTreeModel().mapModelItem( propertiesTreeNode );
	}

	public void release()
	{
		super.release();

		getModelItem().removeMockServiceListener( mockServiceListener );

		for( MockOperationTreeNode treeNode : mockOperationNodes )
		{
			treeNode.getModelItem().removePropertyChangeListener( propertyChangeListener );
			treeNode.release();
		}

		propertiesTreeNode.release();
	}

	public MockService getMockService()
	{
		return ( MockService )getModelItem();
	}

	public int getChildCount()
	{
		int propMod = getTreeModel().isShowProperties() ? 1 : 0;
		return mockOperationNodes.size() + propMod;
	}

	public int getIndexOfChild( Object child )
	{
		int propMod = getTreeModel().isShowProperties() ? 1 : 0;
		if( propMod == 1 && child == propertiesTreeNode )
			return 0;

		int ix = mockOperationNodes.indexOf( child );
		return ix == -1 ? ix : ix + propMod;
	}

	public SoapUITreeNode getChildNode( int index )
	{
		int propMod = getTreeModel().isShowProperties() ? 1 : 0;
		return index == 0 && propMod == 1 ? propertiesTreeNode : mockOperationNodes.get( index - propMod );
	}

	private final class InternalMockServiceListener implements MockServiceListener
	{
		public void mockOperationAdded( MockOperation mockOperation )
		{
			MockOperationTreeNode mockOperationTreeNode = new MockOperationTreeNode( mockOperation, getTreeModel() );
			mockOperationNodes.add( mockOperationTreeNode );
			mockOperation.addPropertyChangeListener( propertyChangeListener );
			getTreeModel().notifyNodeInserted( mockOperationTreeNode );
		}

		public void mockOperationRemoved( MockOperation mockOperation )
		{
			SoapUITreeNode treeNode = getTreeModel().getTreeNode( mockOperation );
			if( mockOperationNodes.contains( treeNode ) )
			{
				getTreeModel().notifyNodeRemoved( treeNode );
				mockOperation.removePropertyChangeListener( propertyChangeListener );
				mockOperationNodes.remove( treeNode );
			}
			else
				throw new RuntimeException( "Removing unkown mockOperation" );
		}

		public void mockResponseAdded( MockResponse mockResponse )
		{
			MockOperationTreeNode operationTreeNode = ( MockOperationTreeNode )getTreeModel().getTreeNode(
					mockResponse.getMockOperation() );
			if( operationTreeNode != null )
				operationTreeNode.mockResponseAdded( mockResponse );
			else
				throw new RuntimeException( "Adding mockResponse to unknwown MockOperation" );
		}

		public void mockResponseRemoved( MockResponse mockResponse )
		{
			MockOperationTreeNode operationTreeNode = ( MockOperationTreeNode )getTreeModel().getTreeNode(
					mockResponse.getMockOperation() );
			if( operationTreeNode != null )
				operationTreeNode.mockResponseRemoved( mockResponse );
			else
				throw new RuntimeException( "Removing mockResponse from unknown MockOperation" );
		}
	}
}
