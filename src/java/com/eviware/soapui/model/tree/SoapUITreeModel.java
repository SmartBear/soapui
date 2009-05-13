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

package com.eviware.soapui.model.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.tree.nodes.PropertiesTreeNode;
import com.eviware.soapui.model.tree.nodes.PropertyTreeNode;
import com.eviware.soapui.model.tree.nodes.WorkspaceTreeNode;
import com.eviware.soapui.model.workspace.Workspace;

/**
 * The Navigator TreeModel
 * 
 * @author Ole.Matzura
 */

public class SoapUITreeModel implements TreeModel
{
	private Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();
	private SoapUITreeNode workspaceNode;
	private final static Logger logger = Logger.getLogger( SoapUITreeModel.class );
	private Map<ModelItem, SoapUITreeNode> modelItemMap = new HashMap<ModelItem, SoapUITreeNode>();
	private boolean showProperties = false;

	public SoapUITreeModel( Workspace workspace )
	{
		workspaceNode = new WorkspaceTreeNode( workspace, this );
		mapModelItem( workspaceNode );
	}

	public Object getRoot()
	{
		return workspaceNode;
	}

	public Object getChild( Object parent, int index )
	{
		SoapUITreeNode treeNode = ( SoapUITreeNode )parent;
		return treeNode.getChildNode( index );
	}

	public int getChildCount( Object parent )
	{
		SoapUITreeNode treeNode = ( SoapUITreeNode )parent;
		return treeNode.getChildCount();
	}

	public boolean isLeaf( Object node )
	{
		SoapUITreeNode treeNode = ( SoapUITreeNode )node;
		return treeNode.isLeaf();
	}

	public void valueForPathChanged( TreePath path, Object newValue )
	{
		SoapUITreeNode treeNode = ( SoapUITreeNode )path.getLastPathComponent();
		if( treeNode.valueChanged( newValue ) )
		{
			// not implemented.. need to expose setName in ModelItem
		}
	}

	public int getIndexOfChild( Object parent, Object child )
	{
		SoapUITreeNode treeNode = ( SoapUITreeNode )parent;
		return treeNode.getIndexOfChild( child );
	}

	public void addTreeModelListener( TreeModelListener l )
	{
		listeners.add( l );
	}

	public void removeTreeModelListener( TreeModelListener l )
	{
		listeners.remove( l );
	}

	public void mapModelItem( SoapUITreeNode soapUITreeNode )
	{
		modelItemMap.put( soapUITreeNode.getModelItem(), soapUITreeNode );
	}

	public void unmapModelItem( ModelItem modelItem )
	{
		if( modelItemMap.containsKey( modelItem ) )
		{
			modelItemMap.remove( modelItem );
		}
		else
		{
			logger.error( "Failed to unmap model item [" + modelItem.getName() + "]" );
			Thread.dumpStack();
		}
	}

	public void notifyNodesInserted( TreeModelEvent e )
	{
		Iterator<TreeModelListener> i = listeners.iterator();
		while( i.hasNext() )
		{
			i.next().treeNodesInserted( e );
		}
	}

	public void notifyNodesRemoved( TreeModelEvent e )
	{
		Iterator<TreeModelListener> i = listeners.iterator();
		while( i.hasNext() )
		{
			i.next().treeNodesRemoved( e );
		}
	}

	public void notifyStructureChanged( TreeModelEvent e )
	{
		Iterator<TreeModelListener> i = listeners.iterator();
		while( i.hasNext() )
		{
			i.next().treeStructureChanged( e );
		}
	}

	public void notifyNodesChanged( TreeModelEvent e )
	{
		Iterator<TreeModelListener> i = listeners.iterator();
		while( i.hasNext() )
		{
			i.next().treeNodesChanged( e );
		}
	}

	public TreePath getPath( SoapUITreeNode treeNode )
	{
		// SoapUITreeNode treeNode = modelItemMap.get( modelItem );
		// if( treeNode == null )
		// throw new RuntimeException( "Missing mapping for modelItem " +
		// modelItem.getName() );

		List<Object> nodes = new ArrayList<Object>();
		if( treeNode != null )
		{
			nodes.add( treeNode );

			treeNode = treeNode.getParentTreeNode();
			while( treeNode != null )
			{
				nodes.add( 0, treeNode );
				treeNode = treeNode.getParentTreeNode();
			}
		}

		return nodes.isEmpty() ? null : new TreePath( nodes.toArray() );
	}

	public void notifyNodeChanged( SoapUITreeNode treeNode )
	{
		SoapUITreeNode parent = treeNode.getParentTreeNode();
		if( parent == null )
		{
			notifyNodesChanged( new TreeModelEvent( this, new Object[] { treeNode } ) );
			return;
		}

		int ix = parent.getIndexOfChild( treeNode );

		if( ix == -1 )
		{
			if( ( !( treeNode instanceof PropertyTreeNode ) && !( treeNode instanceof PropertiesTreeNode ) )
					|| isShowProperties() )
				logger.error( "Changed node [" + treeNode + "] not found in parent [" + parent + "]" );

			return;
		}

		if( !( treeNode instanceof PropertyTreeNode ) || showProperties )
		{
			notifyNodesChanged( new TreeModelEvent( this, getPath( parent ), new int[] { ix }, new Object[] { parent
					.getChildNode( ix ) } ) );
		}
	}

	public void notifyNodeInserted( SoapUITreeNode treeNode )
	{
		SoapUITreeNode parent = treeNode.getParentTreeNode();
		int ix = parent.getIndexOfChild( treeNode );

		if( ix == -1 )
		{
			logger.error( "Inserted node [" + treeNode + "] not found in parent [" + parent + "]" );
			return;
		}

		mapModelItem( treeNode );

		if( !( treeNode instanceof PropertyTreeNode ) || showProperties )
		{
			notifyNodesInserted( new TreeModelEvent( this, getPath( parent ), new int[] { ix }, new Object[] { parent
					.getChildNode( ix ) } ) );
		}
	}

	public void notifyNodeRemoved( SoapUITreeNode treeNode )
	{
		notifyNodeRemoved( treeNode, true );
	}

	public void notifyNodeRemoved( SoapUITreeNode treeNode, boolean release )
	{
		SoapUITreeNode parent = treeNode.getParentTreeNode();
		int ix = parent.getIndexOfChild( treeNode );

		if( ix == -1 )
		{
			logger.error( "Removed node [" + treeNode + "] not found in parent [" + parent + "]" );
			return;
		}

		if( !( treeNode instanceof PropertyTreeNode ) || showProperties )
		{
			notifyNodesRemoved( new TreeModelEvent( this, getPath( parent ), new int[] { ix }, new Object[] { parent
					.getChildNode( ix ) } ) );
		}

		if( release )
			treeNode.release();
	}

	public SoapUITreeNode getTreeNode( ModelItem parentItem )
	{
		return modelItemMap.get( parentItem );
	}

	public TreePath getPath( ModelItem modelItem )
	{
		return getPath( modelItemMap.get( modelItem ) );
	}

	public void mapModelItems( List<? extends SoapUITreeNode> treeNodes )
	{
		Iterator<? extends SoapUITreeNode> iterator = treeNodes.iterator();
		while( iterator.hasNext() )
		{
			SoapUITreeNode item = iterator.next();
			mapModelItem( item );
		}
	}

	public boolean isShowProperties()
	{
		return showProperties;
	}

	public void setShowProperties( boolean showProperties )
	{
		if( this.showProperties != showProperties )
		{
			this.showProperties = showProperties;
			notifyStructureChanged( new TreeModelEvent( this, getPath( workspaceNode ) ) );
		}
	}
}
