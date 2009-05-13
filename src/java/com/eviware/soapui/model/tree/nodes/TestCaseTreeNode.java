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

import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.AbstractTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.tree.nodes.support.WsdlLoadTestsModelItem;
import com.eviware.soapui.model.tree.nodes.support.WsdlTestStepsModelItem;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;

/**
 * SoapUITreeNode for TestCase implementations
 * 
 * @author Ole.Matzura
 */

public class TestCaseTreeNode extends AbstractModelItemTreeNode<TestCase>
{
	private TestStepsTreeNode testStepsNode;
	private LoadTestsTreeNode loadTestsNode;
	private PropertiesTreeNode<?> propertiesTreeNode;
	private List<SoapUITreeNode> childNodes = new ArrayList<SoapUITreeNode>();

	public TestCaseTreeNode( TestCase testCase, SoapUITreeModel treeModel )
	{
		super( testCase, testCase.getTestSuite(), treeModel );

		testStepsNode = new TestStepsTreeNode();
		loadTestsNode = new LoadTestsTreeNode();

		getTreeModel().mapModelItem( testStepsNode );
		getTreeModel().mapModelItem( loadTestsNode );

		propertiesTreeNode = PropertiesTreeNode.createDefaultPropertiesNode( testCase, getTreeModel() );
		getTreeModel().mapModelItem( propertiesTreeNode );

		childNodes.add( propertiesTreeNode );
		childNodes.add( testStepsNode );
		childNodes.add( loadTestsNode );
	}

	public void release()
	{
		super.release();

		for( SoapUITreeNode treeNode : childNodes )
		{
			if( !( treeNode instanceof PropertiesTreeNode ) )
				getTreeModel().unmapModelItem( treeNode.getModelItem() );

			treeNode.release();
		}
	}

	public int getChildCount()
	{
		int propMod = getTreeModel().isShowProperties() ? 0 : 1;
		return childNodes.size() - propMod;
	}

	public SoapUITreeNode getChildNode( int index )
	{
		int propMod = getTreeModel().isShowProperties() ? 0 : 1;

		return childNodes.get( index + propMod );
	}

	public int getIndexOfChild( Object child )
	{
		int propMod = getTreeModel().isShowProperties() ? 0 : 1;
		if( child == propertiesTreeNode && propMod == 1 )
			return 0;
		return childNodes.indexOf( child ) - propMod;
	}

	public LoadTestsTreeNode getLoadTestsNode()
	{
		return loadTestsNode;
	}

	public TestStepsTreeNode getTestStepsNode()
	{
		return testStepsNode;
	}

	public TestCase getTestCase()
	{
		return getModelItem();
	}

	public class TestStepsTreeNode extends AbstractTreeNode<WsdlTestStepsModelItem>
	{
		private List<TestStepTreeNode> testStepNodes = new ArrayList<TestStepTreeNode>();

		protected TestStepsTreeNode()
		{
			super( new WsdlTestStepsModelItem( getTestCase() ) );

			for( int c = 0; c < getTestCase().getTestStepCount(); c++ )
			{
				TestStep testStep = getTestCase().getTestStepAt( c );
				testStepNodes.add( createTestStepTreeNode( testStep ) );
			}

			getTreeModel().mapModelItems( testStepNodes );
		}

		private TestStepTreeNode createTestStepTreeNode( TestStep testStep )
		{
			return new TestStepTreeNode( testStep, getModelItem(), getTreeModel() );
		}

		public int getChildCount()
		{
			return testStepNodes.size();
		}

		public int getIndexOfChild( Object child )
		{
			return testStepNodes.indexOf( child );
		}

		public SoapUITreeNode getChildNode( int index )
		{
			return testStepNodes.get( index );
		}

		public SoapUITreeNode getParentTreeNode()
		{
			return TestCaseTreeNode.this;
		}

		public void testStepInserted( TestStep testStep, int index )
		{
			TestStepTreeNode testStepTreeNode = createTestStepTreeNode( testStep );
			testStepNodes.add( index, testStepTreeNode );
			getTreeModel().notifyNodeInserted( testStepTreeNode );
			getTreeModel().notifyNodeChanged( this );
		}

		public void testStepRemoved( TestStep testStep, int index )
		{
			SoapUITreeNode treeNode = getTreeModel().getTreeNode( testStep );
			if( testStepNodes.contains( treeNode ) )
			{
				getTreeModel().notifyNodeRemoved( treeNode );
				testStepNodes.remove( treeNode );
			}
			else
				throw new RuntimeException( "Removing unkown testStep" );
		}

		public void testStepMoved( TestStep testStep, int fromIndex, int offset )
		{
			testStepRemoved( testStep, fromIndex );
			testStepInserted( testStep, fromIndex + offset );
		}

		public ActionList getActions()
		{
			return ActionListBuilder.buildActions( "TestStepsTreeNodeActions", TestCaseTreeNode.this.getModelItem() );
		}

		public void release()
		{
			for( TestStepTreeNode testStepNode : testStepNodes )
				testStepNode.release();

			getModelItem().release();
		}
	}

	public class LoadTestsTreeNode extends AbstractTreeNode<WsdlLoadTestsModelItem>
	{
		private List<LoadTestTreeNode> loadTestNodes = new ArrayList<LoadTestTreeNode>();

		protected LoadTestsTreeNode()
		{
			super( new WsdlLoadTestsModelItem( getTestCase() ) );

			for( int c = 0; c < getTestCase().getLoadTestCount(); c++ )
			{
				loadTestNodes
						.add( new LoadTestTreeNode( getTestCase().getLoadTestAt( c ), getModelItem(), getTreeModel() ) );
			}

			getTreeModel().mapModelItems( loadTestNodes );
		}

		public int getChildCount()
		{
			return loadTestNodes.size();
		}

		public int getIndexOfChild( Object child )
		{
			return loadTestNodes.indexOf( child );
		}

		public SoapUITreeNode getChildNode( int index )
		{
			return loadTestNodes.get( index );
		}

		public SoapUITreeNode getParentTreeNode()
		{
			return TestCaseTreeNode.this;
		}

		public void loadTestInserted( LoadTest loadTest )
		{
			LoadTestTreeNode loadTestTreeNode = new LoadTestTreeNode( loadTest, getModelItem(), getTreeModel() );
			loadTestNodes.add( loadTestTreeNode );
			getTreeModel().notifyNodeInserted( loadTestTreeNode );
			getTreeModel().notifyNodeChanged( this );
		}

		public void loadTestRemoved( LoadTest loadTest )
		{
			SoapUITreeNode treeNode = getTreeModel().getTreeNode( loadTest );
			if( loadTestNodes.contains( treeNode ) )
			{
				getTreeModel().notifyNodeRemoved( treeNode );
				loadTestNodes.remove( treeNode );
			}
			else
				throw new RuntimeException( "Removing unkown loadTest" );
		}

		public void release()
		{
			for( LoadTestTreeNode loadTestNode : loadTestNodes )
				loadTestNode.release();
		}

		public ActionList getActions()
		{
			return ActionListBuilder.buildActions( "LoadTestsTreeNodeActions", TestCaseTreeNode.this.getModelItem() );
		}
	}

	public void testStepInserted( TestStep testStep, int index )
	{
		testStepsNode.testStepInserted( testStep, index );
	}

	public void testStepRemoved( TestStep testStep, int index )
	{
		testStepsNode.testStepRemoved( testStep, index );
	}

	public void loadTestInserted( LoadTest loadTest )
	{
		loadTestsNode.loadTestInserted( loadTest );
	}

	public void loadTestRemoved( LoadTest loadTest )
	{
		loadTestsNode.loadTestRemoved( loadTest );
	}

	public void testStepMoved( TestStep testStep, int fromIndex, int offset )
	{
		testStepsNode.testStepMoved( testStep, fromIndex, offset );
	}
}
