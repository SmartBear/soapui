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

package com.eviware.soapui.model.tree.nodes;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;

import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.WorkspaceListenerAdapter;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.settings.UISettings;

/**
 * SoapUITreeNode for Workspace implementations
 * 
 * @author Ole.Matzura
 */

public class WorkspaceTreeNode extends AbstractModelItemTreeNode<Workspace>
{
	private InternalWorkspaceListener workspaceListener = new InternalWorkspaceListener();
	private List<ProjectTreeNode> projectNodes = new ArrayList<ProjectTreeNode>();
	private ReorderPropertyChangeListener propertyChangeListener = new ReorderPropertyChangeListener();

	public WorkspaceTreeNode( Workspace workspace, SoapUITreeModel treeModel )
	{
		super( workspace, null, treeModel );

		workspace.addWorkspaceListener( workspaceListener );

		for( int c = 0; c < workspace.getProjectCount(); c++ )
		{
			Project project = workspace.getProjectAt( c );
			project.addPropertyChangeListener( Project.NAME_PROPERTY, propertyChangeListener );
			projectNodes.add( new ProjectTreeNode( project, this ) );
		}

		initOrdering( projectNodes, UISettings.ORDER_PROJECTS );
		getTreeModel().mapModelItems( projectNodes );
	}

	public void release()
	{
		super.release();
		getWorkspace().removeWorkspaceListener( workspaceListener );

		for( ProjectTreeNode treeNode : projectNodes )
		{
			treeNode.getModelItem().removePropertyChangeListener( Project.NAME_PROPERTY, propertyChangeListener );
			treeNode.release();
		}
	}

	public Workspace getWorkspace()
	{
		return ( Workspace )getModelItem();
	}

	private class InternalWorkspaceListener extends WorkspaceListenerAdapter
	{
		public void projectAdded( Project project )
		{
			ProjectTreeNode projectTreeNode = new ProjectTreeNode( project, WorkspaceTreeNode.this );
			projectNodes.add( projectTreeNode );
			project.addPropertyChangeListener( Project.NAME_PROPERTY, propertyChangeListener );
			reorder( false );
			getTreeModel().notifyNodeInserted( projectTreeNode );
		}

		public void projectRemoved( Project project )
		{
			SoapUITreeNode treeNode = getTreeModel().getTreeNode( project );
			if( projectNodes.contains( treeNode ) )
			{
				getTreeModel().notifyNodeRemoved( treeNode );
				projectNodes.remove( treeNode );
				project.removePropertyChangeListener( propertyChangeListener );
			}
			else
				throw new RuntimeException( "Removing unkown project" );
		}

		public void projectChanged( Project project )
		{
			getTreeModel().notifyStructureChanged(
					new TreeModelEvent( WorkspaceTreeNode.this, new Object[] { getTreeModel().getPath(
							WorkspaceTreeNode.this ) } ) );
		}
	}
}
