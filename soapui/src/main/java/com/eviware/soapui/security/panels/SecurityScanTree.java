/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.panels;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;

@SuppressWarnings( "serial" )
public class SecurityScanTree extends DefaultTreeModel
{

	private SecurityTest securityTest;
	private SecurityTreeRootNode treeNode;

	public SecurityScanTree( SecurityTest securityTest, SecurityTreeRootNode treeNode )
	{
		super( treeNode );

		this.securityTest = securityTest;
		this.treeNode = treeNode;
	}

	public void insertNodeInto( TestStep testStep )
	{
		TestStepNode testStepNode = new TestStepNode( ( SecurityTreeRootNode )root, testStep, securityTest
				.getSecurityScansMap().get( testStep.getId() ) );
		insertNodeInto( testStepNode, ( MutableTreeNode )root, root.getChildCount() );
		nodeStructureChanged( root );
	}

	public void removeTestStep( TestStep testStep )
	{
		TestStepNode node = getTestStepNode( testStep );
		removeNodeFromParent( node );
	}

	/**
	 * @param testStep
	 * @return
	 */
	protected TestStepNode getTestStepNode( TestStep testStep )
	{
		for( int cnt = 0; cnt < root.getChildCount(); cnt++ )
		{
			TestStepNode node = ( TestStepNode )root.getChildAt( cnt );
			if( node.getTestStep().getId().equals( testStep.getId() ) )
				return node;
		}
		return null;
	}

	protected SecurityScanNode getSecurityScanNode( SecurityScan securityCheck )
	{
		TestStepNode testStepNode = getTestStepNode( securityCheck.getTestStep() );
		for( int cnt = 0; cnt < testStepNode.getChildCount(); cnt++ )
		{
			SecurityScanNode node = ( SecurityScanNode )testStepNode.getChildAt( cnt );
			if( node.getSecurityScan().getType().equals( securityCheck.getType() ) )
				return node;
		}
		return null;
	}

	public void addSecurityScanNode( JTree tree, SecurityScan securityCheck )
	{
		TestStepNode node = getTestStepNode( securityCheck.getTestStep() );
		if( node != null )
		{
			SecurityScanNode newNode = new SecurityScanNode( securityCheck );
			insertNodeInto( newNode, node, node.getChildCount() );
			nodeStructureChanged( node );
			for( int row = 0; row < tree.getRowCount(); row++ )
			{
				tree.expandRow( row );
			}
			tree.setSelectionInterval( getIndexOfChild( node, newNode ) + 1, getIndexOfChild( node, newNode ) + 1 );
		}
	}

	public void removeSecurityScanNode( SecurityScan securityCheck )
	{
		TestStepNode testStepNode = getTestStepNode( securityCheck.getTestStep() );
		SecurityScanNode node = getSecurityScanNode( securityCheck );
		removeNodeFromParent( node );
		nodeStructureChanged( testStepNode );
	}

	/**
	 * 
	 * moves test step
	 * 
	 * returns new index/row where test step is inserted
	 * 
	 * @param testStep
	 * @param index
	 * @param offset
	 * @return
	 */
	public TreePath moveTestStepNode( TestStep testStep, int index, int offset )
	{
		TestStepNode node = getTestStepNode( testStep );
		int index2 = getIndexOfChild( root, node );
		removeNodeFromParent( node );
		insertNodeInto( node, ( MutableTreeNode )root, index2 + offset );

		return new TreePath( node.getPath() );
	}

	public void release()
	{
		if( securityTest != null )
		{
			securityTest.removePropertyChangeListener( treeNode );
		}
	}

}
