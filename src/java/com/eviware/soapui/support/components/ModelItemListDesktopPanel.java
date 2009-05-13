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

package com.eviware.soapui.support.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;
import com.eviware.soapui.support.ListDataListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.ModelItemListMouseListener;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

public class ModelItemListDesktopPanel extends DefaultDesktopPanel
{
	private final ModelItem[] modelItems;
	private JList list;
	private ItemsListModel listModel;
	private InternalTreeModelListener treeModelListener;
	private Map<ModelItem, StringList> detailInfo = new HashMap<ModelItem, StringList>();
	private JList detailList;
	private DetailsListModel detailListModel;

	public ModelItemListDesktopPanel( String title, String description, ModelItem[] modelItems )
	{
		super( title, description, new JPanel( new BorderLayout() ) );
		this.modelItems = modelItems;

		buildUI();

		treeModelListener = new InternalTreeModelListener();
		SoapUI.getNavigator().getMainTree().getModel().addTreeModelListener( treeModelListener );
	}

	public void addDetails( ModelItem modelItem, String details )
	{
		if( !detailInfo.containsKey( modelItem ) )
			detailInfo.put( modelItem, new StringList() );

		detailInfo.get( modelItem ).add( details );
	}

	private void buildUI()
	{
		JPanel p = ( JPanel )getComponent();

		p.add( UISupport.buildDescription( getTitle(), getDescription(), null ), BorderLayout.NORTH );
		p.add( buildModelItemList(), BorderLayout.CENTER );
	}

	@Override
	public boolean onClose( boolean canCancel )
	{
		SoapUI.getNavigator().getMainTree().getModel().removeTreeModelListener( treeModelListener );
		return super.onClose( canCancel );
	}

	private Component buildModelItemList()
	{
		listModel = new ItemsListModel( modelItems );

		list = new JList( listModel );
		list.setCellRenderer( new ItemListCellRenderer() );
		ModelItemListMouseListener modelItemListMouseListener = new ModelItemListMouseListener();
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add( new RemoveAction() );
		popupMenu.add( new HighlightAction() );

		modelItemListMouseListener.setPopupMenu( popupMenu );
		list.addMouseListener( modelItemListMouseListener );
		listModel.addListDataListener( new ListDataListenerAdapter()
		{

			@Override
			public void intervalRemoved( ListDataEvent e )
			{
				if( listModel.isEmpty() )
				{
					SwingUtilities.invokeLater( new Runnable()
					{

						public void run()
						{
							SoapUI.getDesktop().closeDesktopPanel( ModelItemListDesktopPanel.this );
						}
					} );
				}
			}
		} );

		list.addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				detailListModel.refresh();
			}
		} );

		JInspectorPanelImpl inspectorPanel = new JInspectorPanelImpl( new JScrollPane( list ) );
		inspectorPanel.addInspector( new JComponentInspector<JComponent>( buildDetails(), "Details",
				"Shows detailed information for the selected item", true ) );

		return inspectorPanel;
	}

	private JComponent buildDetails()
	{
		detailListModel = new DetailsListModel();
		detailList = new JList( detailListModel );
		return new JScrollPane( detailList );
	}

	private class DetailsListModel extends AbstractListModel
	{
		public Object getElementAt( int index )
		{
			ModelItem modelItem = ( ModelItem )list.getSelectedValue();
			if( modelItem == null || !detailInfo.containsKey( modelItem ) )
				return null;
			else
				return detailInfo.get( modelItem ).get( index );
		}

		public void refresh()
		{
			fireIntervalAdded( this, 0, getSize() - 1 );
		}

		public int getSize()
		{
			ModelItem modelItem = ( ModelItem )list.getSelectedValue();
			if( modelItem == null || !detailInfo.containsKey( modelItem ) )
				return 0;
			else
				return detailInfo.get( modelItem ).size();
		}
	}

	private class ItemsListModel extends DefaultListModel
	{
		public ItemsListModel( ModelItem[] modelItems )
		{
			for( ModelItem item : modelItems )
				addElement( item );
		}

		public void nodesChanged()
		{
			fireContentsChanged( this, 0, getSize() - 1 );
		}
	}

	private class RemoveAction extends AbstractAction
	{
		public RemoveAction()
		{
			super( "Remove" );
			putValue( SHORT_DESCRIPTION, "Removes this item from the list" );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = list.getSelectedIndex();
			if( ix != -1 && UISupport.confirm( "Remove selected item from list?", "Remove Item" ) )
				listModel.remove( ix );
		}
	}

	private class HighlightAction extends AbstractAction
	{
		public HighlightAction()
		{
			super( "Select in Tree" );
			putValue( SHORT_DESCRIPTION, "Selects this node in the Navigator Tree" );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = list.getSelectedIndex();
			if( ix != -1 )
				UISupport.select( ( ModelItem )listModel.getElementAt( ix ) );
		}
	}

	private final class InternalTreeModelListener implements TreeModelListener
	{
		public void treeNodesChanged( TreeModelEvent e )
		{
			listModel.nodesChanged();
		}

		public void treeNodesInserted( TreeModelEvent e )
		{
		}

		public void treeNodesRemoved( TreeModelEvent e )
		{
			SwingUtilities.invokeLater( new Runnable()
			{

				public void run()
				{
					for( int c = 0; c < listModel.getSize(); c++ )
					{
						if( SoapUI.getNavigator().getTreePath( ( ModelItem )listModel.elementAt( c ) ) == null )
						{
							listModel.remove( c );
							c-- ;
						}
					}
				}
			} );
		}

		public void treeStructureChanged( TreeModelEvent e )
		{
		}
	}

	private class ItemListCellRenderer extends DefaultListCellRenderer
	{
		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			JLabel label = ( JLabel )super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

			if( value instanceof ModelItem )
			{
				ModelItem item = ( ModelItem )value;
				TreePath treePath = SoapUI.getNavigator().getTreePath( item );

				if( treePath == null )
				{
					if( !( item instanceof EmptyModelItem ) )
					{
						// listModel.setElementAt( new EmptyModelItem( "<removed>",
						// item.getIcon()), index );
					}

					label.setText( "<removed>" );
					label.setToolTipText( null );
				}
				else
				{
					String str = item.getName() + " [";

					for( int c = 1; c < treePath.getPathCount(); c++ )
					{
						SoapUITreeNode comp = ( SoapUITreeNode )treePath.getPathComponent( c );
						if( comp.getModelItem() instanceof EmptyModelItem )
							continue;

						if( c > 1 )
							str += " - ";

						str += comp.toString();
					}

					str += "]";

					label.setText( str );
					label.setToolTipText( item.getDescription() );
				}

				label.setIcon( item.getIcon() );
				label.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
			}

			return label;
		}
	}
}
