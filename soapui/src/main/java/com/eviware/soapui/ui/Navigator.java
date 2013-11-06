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

package com.eviware.soapui.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.tree.SoapUITreeNodeRenderer;
import com.eviware.soapui.model.tree.nodes.ProjectTreeNode;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIMultiAction;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.components.JXToolBar;

/**
 * The SoapUI navigator tree
 * 
 * @author Ole.Matzura
 */

public class Navigator extends JPanel
{
	private Workspace workspace;
	private JTree mainTree;
	private SoapUITreeModel treeModel;
	private Set<NavigatorListener> listeners = new HashSet<NavigatorListener>();

	public Navigator( Workspace workspace )
	{
		super( new BorderLayout() );
		this.workspace = workspace;

		buildUI();
	}

	private void buildUI()
	{
		treeModel = new SoapUITreeModel( workspace );

		mainTree = new NavigatorTree( treeModel );
		mainTree.setRootVisible( true );
		mainTree.setExpandsSelectedPaths( true );
		mainTree.setScrollsOnExpand( true );
		mainTree.setToggleClickCount( 0 );
		mainTree.addMouseListener( new TreeMouseListener() );
		mainTree.addTreeSelectionListener( new InternalTreeSelectionListener() );
		mainTree.setCellRenderer( new SoapUITreeNodeRenderer() );
		mainTree.setBorder( null );
		mainTree.getSelectionModel().setSelectionMode( TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION );
		mainTree.addKeyListener( new TreeKeyListener() );

		ToolTipManager.sharedInstance().registerComponent( mainTree );

		add( new JScrollPane( mainTree ), BorderLayout.CENTER );
		add( buildToolbar(), BorderLayout.NORTH );
		setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) );
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();

		JToggleButton toggleButton = new JToggleButton( new TogglePropertiesAction() );
		toggleButton.setToolTipText( "Toggles displaying of Test Properties in tree" );
		toggleButton.setSize( 10, 12 );
		toolbar.addFixed( toggleButton );
		toolbar.addGlue();

		return toolbar;
	}

	private static class NavigatorTree extends JTree implements Autoscroll
	{
		public NavigatorTree( SoapUITreeModel treeModel )
		{
			super( treeModel );
		}

		private static final int AUTOSCROLL_MARGIN = 12;

		public void autoscroll( Point pt )
		{
			// Figure out which row we�re on.
			int nRow = getRowForLocation( pt.x, pt.y );

			// If we are not on a row then ignore this autoscroll request
			if( nRow < 0 )
				return;

			Rectangle raOuter = getBounds();
			// Now decide if the row is at the top of the screen or at the
			// bottom. We do this to make the previous row (or the next
			// row) visible as appropriate. If we�re at the absolute top or
			// bottom, just return the first or last row respectively.

			nRow = ( pt.y + raOuter.y <= AUTOSCROLL_MARGIN ) // Is row at top of
			// screen?
			? ( nRow <= 0 ? 0 : nRow - 1 ) // Yes, scroll up one row
					: ( nRow < getRowCount() - 1 ? nRow + 1 : nRow ); // No,
			// scroll
			// down one
			// row

			scrollRowToVisible( nRow );
		}

		// Calculate the insets for the *JTREE*, not the viewport
		// the tree is in. This makes it a bit messy.
		public Insets getAutoscrollInsets()
		{
			Rectangle raOuter = getBounds();
			Rectangle raInner = getParent().getBounds();
			return new Insets( raInner.y - raOuter.y + AUTOSCROLL_MARGIN, raInner.x - raOuter.x + AUTOSCROLL_MARGIN,
					raOuter.height - raInner.height - raInner.y + raOuter.y + AUTOSCROLL_MARGIN, raOuter.width
							- raInner.width - raInner.x + raOuter.x + AUTOSCROLL_MARGIN );
		}
	}

	public Project getCurrentProject()
	{
		TreePath path = mainTree.getSelectionPath();
		if( path == null )
			return null;

		Object node = ( Object )path.getLastPathComponent();
		while( node != null && !( node instanceof ProjectTreeNode ) )
		{
			path = path.getParentPath();
			node = ( path == null ? null : path.getLastPathComponent() );
		}

		if( node == null )
			return null;

		return ( ( ProjectTreeNode )node ).getProject();
	}

	public void addNavigatorListener( NavigatorListener listener )
	{
		listeners.add( listener );
	}

	public void removeNavigatorListener( NavigatorListener listener )
	{
		listeners.remove( listener );
	}

	public void selectModelItem( ModelItem modelItem )
	{
		TreePath path = treeModel.getPath( modelItem );
		mainTree.setSelectionPath( path );
		mainTree.expandPath( path );
		mainTree.scrollPathToVisible( path );
	}

	public TreePath getTreePath( ModelItem modelItem )
	{
		return treeModel.getPath( modelItem );
	}

	public JTree getMainTree()
	{
		return mainTree;
	}

	public ModelItem getSelectedItem()
	{
		TreePath path = mainTree.getSelectionPath();
		if( path == null )
			return null;

		return ( ( SoapUITreeNode )path.getLastPathComponent() ).getModelItem();
	}

	private final class TreeKeyListener extends KeyAdapter
	{
		public void keyPressed( KeyEvent e )
		{
			TreePath selectionPath = mainTree.getSelectionPath();
			if( selectionPath == null || mainTree.getSelectionCount() == 0 )
				return;

			if( mainTree.getSelectionCount() == 1 )
			{
				SoapUITreeNode lastPathComponent = ( SoapUITreeNode )selectionPath.getLastPathComponent();
				ActionList actions = lastPathComponent.getActions();
				if( actions != null )
				{
					actions.dispatchKeyEvent( e );
				}

				if( !e.isConsumed() )
				{
					KeyStroke ks = KeyStroke.getKeyStrokeForEvent( e );
					if( ks.equals( UISupport.getKeyStroke( "alt C" ) ) )
					{
						mainTree.collapsePath( selectionPath );
						e.consume();
					}
					else if( ks.equals( UISupport.getKeyStroke( "alt E" ) ) )
					{
						mainTree.collapsePath( selectionPath );
						int row = mainTree.getSelectionRows()[0];
						TreePath nextPath = mainTree.getPathForRow( row + 1 );

						TreePath path = mainTree.getPathForRow( row );
						while( path != null && !path.equals( nextPath ) )
						{
							mainTree.expandRow( row );
							path = mainTree.getPathForRow( ++row );
						}

						e.consume();
					}
				}
			}
			else
			{
				TreePath[] selectionPaths = mainTree.getSelectionPaths();
				List<ModelItem> targets = new ArrayList<ModelItem>();
				for( TreePath treePath : selectionPaths )
				{
					SoapUITreeNode node = ( SoapUITreeNode )treePath.getLastPathComponent();
					targets.add( node.getModelItem() );
				}

				if( targets.size() > 0 )
				{
					ActionList actions = ActionListBuilder
							.buildMultiActions( targets.toArray( new ModelItem[targets.size()] ) );
					if( actions.getActionCount() > 0 )
					{
						actions.dispatchKeyEvent( e );
					}
				}
			}
		}
	}

	public class InternalTreeSelectionListener implements TreeSelectionListener
	{
		public void valueChanged( TreeSelectionEvent e )
		{
			Object obj = e.getPath().getLastPathComponent();
			if( obj instanceof SoapUITreeNode )
			{
				SoapUITreeNode treeNode = ( SoapUITreeNode )obj;

				if( !listeners.isEmpty() )
				{
					TreePath newPath = e.getNewLeadSelectionPath();
					NavigatorListener[] array = listeners.toArray( new NavigatorListener[listeners.size()] );
					for( NavigatorListener listener : array )
					{
						listener.nodeSelected( newPath == null ? null : treeNode );
					}
				}
			}
		}
	}

	public class TreeMouseListener extends MouseAdapter
	{
		private final class CollapseRowAction extends AbstractAction
		{
			private final int row;

			public CollapseRowAction( int row )
			{
				this.row = row;
			}

			public void actionPerformed( ActionEvent e )
			{
				collapseAll( mainTree.getPathForRow( row ) );
				mainTree.collapseRow( row );
			}

			private void collapseAll( TreePath tp )
			{
				if( tp == null )
					return;

				Object node = tp.getLastPathComponent();
				TreeModel model = mainTree.getModel();
				if( !model.isLeaf( node ) )
				{
					mainTree.collapsePath( tp );
					for( int i = 0; i < model.getChildCount( node ); i++ )
					{
						// for (int i = node.childCount()-4;i>=0;i--){
						collapseAll( tp.pathByAddingChild( model.getChild( node, i ) ) );
					}
					mainTree.collapsePath( tp );
				}
			}
		}

		private final class ExpandRowAction extends AbstractAction
		{
			private final int row;

			public ExpandRowAction( int row )
			{
				this.row = row;
			}

			public void actionPerformed( ActionEvent e )
			{
				mainTree.expandRow( row );
				expandAll( mainTree.getPathForRow( row ) );
			}

			private void expandAll( TreePath tp )
			{
				if( tp == null )
					return;

				Object node = tp.getLastPathComponent();
				TreeModel model = mainTree.getModel();
				if( !model.isLeaf( node ) )
				{
					mainTree.expandPath( tp );
					for( int i = 0; i < model.getChildCount( node ); i++ )
					{
						expandAll( tp.pathByAddingChild( model.getChild( node, i ) ) );
					}
				}
			}

		}

		private ActionList actions;

		public void mouseClicked( MouseEvent e )
		{
			if( e.isPopupTrigger() )
				showPopup( e );
			else if( e.getClickCount() < 2 )
				return;
			if( mainTree.getSelectionCount() == 1 )
			{
				int row = mainTree.getRowForLocation( e.getX(), e.getY() );
				TreePath path = mainTree.getSelectionPath();
				if( path == null && row == -1 )
					return;

				if( path == null || mainTree.getRowForPath( path ) != row )
					mainTree.setSelectionRow( row );

				SoapUITreeNode node = ( SoapUITreeNode )path.getLastPathComponent();
				actions = node.getActions();
				if( actions != null )
				{
					SwingUtilities.invokeLater( new Runnable()
					{
						public void run()
						{
							if( actions != null )
							{
								actions.performDefaultAction( new ActionEvent( mainTree, 0, null ) );
								actions = null;
							}
						}
					} );
				}
			}
		}

		public void mousePressed( MouseEvent e )
		{
			if( e.isPopupTrigger() )
				showPopup( e );
		}

		public void mouseReleased( MouseEvent e )
		{
			if( e.isPopupTrigger() )
				showPopup( e );
		}

		private void showPopup( MouseEvent e )
		{
			if( mainTree.getSelectionCount() < 2 )
			{
				TreePath path = mainTree.getPathForLocation( ( int )e.getPoint().getX(), ( int )e.getPoint().getY() );
				if( path == null )
				{
					int row = ( int )e.getPoint().getY() / mainTree.getRowHeight();
					if( row != -1 )
					{
						JPopupMenu collapsePopup = new JPopupMenu();
						collapsePopup.add( "Collapse" ).addActionListener( new CollapseRowAction( row ) );
						collapsePopup.add( "Expand" ).addActionListener( new ExpandRowAction( row ) );
						collapsePopup.show( mainTree, e.getX(), e.getY() );
					}

					return;
				}
				SoapUITreeNode node = ( SoapUITreeNode )path.getLastPathComponent();

				JPopupMenu popupMenu = node.getPopup();
				if( popupMenu == null )
					return;

				mainTree.setSelectionPath( path );

				popupMenu.show( mainTree, e.getX(), e.getY() );
			}
			else
			{
				TreePath[] selectionPaths = mainTree.getSelectionPaths();
				List<ModelItem> targets = new ArrayList<ModelItem>();
				for( TreePath treePath : selectionPaths )
				{
					SoapUITreeNode node = ( SoapUITreeNode )treePath.getLastPathComponent();
					targets.add( node.getModelItem() );
				}

				if( targets.size() > 0 )
				{
					ActionList actions = ActionListBuilder
							.buildMultiActions( targets.toArray( new ModelItem[targets.size()] ) );
					if( actions.getActionCount() > 0 )
					{
						JPopupMenu popup = new JPopupMenu();
						ActionSupport.addActions( actions, popup );
						popup.show( mainTree, e.getX(), e.getY() );
					}
				}
			}
		}
	}

	public boolean isVisible( TreePath path )
	{
		return mainTree.isVisible( path );
	}

	public boolean isExpanded( TreePath path )
	{
		return mainTree.isExpanded( path );
	}

	private class TogglePropertiesAction extends AbstractAction
	{
		public TogglePropertiesAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/toggle_properties.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			Enumeration<TreePath> expandedDescendants = mainTree.getExpandedDescendants( getTreePath( workspace ) );
			TreePath selectionPath = mainTree.getSelectionPath();

			treeModel.setShowProperties( !treeModel.isShowProperties() );

			while( expandedDescendants != null && expandedDescendants.hasMoreElements() )
			{
				mainTree.expandPath( expandedDescendants.nextElement() );
			}

			if( selectionPath != null )
				mainTree.setSelectionPath( selectionPath );
		}
	}

	public class MultiActionInvoker extends AbstractAction
	{
		private final SoapUIMultiAction action;
		private final List<ModelItem> targets;

		public MultiActionInvoker( SoapUIMultiAction action, List<ModelItem> targets )
		{
			super( action.getName() );

			this.action = action;
			this.targets = targets;
		}

		public void actionPerformed( ActionEvent e )
		{
			action.perform( targets.toArray( new ModelItem[targets.size()] ), null );
		}
	}
}
