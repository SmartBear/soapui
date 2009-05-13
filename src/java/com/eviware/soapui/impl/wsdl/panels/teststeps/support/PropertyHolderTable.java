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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionImpl;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.model.tree.nodes.PropertyTreeNode.PropertyModelItem;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.xml.XmlUtils;

public class PropertyHolderTable extends JPanel
{
	private final TestPropertyHolder holder;
	private PropertiesModel propertiesModel;
	private RemovePropertyAction removePropertyAction;
	private AddPropertyAction addPropertyAction;
	private InternalTestPropertyListener testPropertyListener;
	private JTable propertiesTable;
	private JXToolBar toolbar;
	private LoadPropertiesAction loadPropertiesAction;
	private MovePropertyUpAction movePropertyUpAction;
	private MovePropertyDownAction movePropertyDownAction;

	public PropertyHolderTable( TestPropertyHolder holder )
	{
		super( new BorderLayout() );
		this.holder = holder;

		loadPropertiesAction = new LoadPropertiesAction();
		testPropertyListener = new InternalTestPropertyListener();
		holder.addTestPropertyListener( testPropertyListener );

		JScrollPane scrollPane = new JScrollPane( buildPropertiesTable() );

		if( getHolder().getModelItem() != null )
		{
			DropTarget dropTarget = new DropTarget( scrollPane, new PropertyHolderTablePropertyExpansionDropTarget() );
			dropTarget.setDefaultActions( DnDConstants.ACTION_COPY_OR_MOVE );
		}

		add( scrollPane, BorderLayout.CENTER );
		add( buildToolbar(), BorderLayout.NORTH );
	}

	protected JTable buildPropertiesTable()
	{
		propertiesModel = new PropertiesModel();
		propertiesTable = new PropertiesHolderJTable();
		propertiesTable.setSurrendersFocusOnKeystroke( true );

		propertiesTable.putClientProperty( "terminateEditOnFocusLost", Boolean.TRUE );
		propertiesTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{
			public void valueChanged( ListSelectionEvent e )
			{
				int selectedRow = propertiesTable.getSelectedRow();
				if( removePropertyAction != null )
					removePropertyAction.setEnabled( selectedRow != -1 );

				if( movePropertyUpAction != null )
					movePropertyUpAction.setEnabled( selectedRow > 0 );

				if( movePropertyDownAction != null )
					movePropertyDownAction.setEnabled( selectedRow >= 0 && selectedRow < propertiesTable.getRowCount() - 1 );
			}
		} );

		propertiesTable.setDragEnabled( true );
		propertiesTable.setTransferHandler( new TransferHandler( "testProperty" ) );

		if( getHolder().getModelItem() != null )
		{
			DropTarget dropTarget = new DropTarget( propertiesTable, new PropertyHolderTablePropertyExpansionDropTarget() );
			dropTarget.setDefaultActions( DnDConstants.ACTION_COPY_OR_MOVE );
		}

		return propertiesTable;
	}

	public class PropertiesHolderJTable extends JTable
	{
		public PropertiesHolderJTable()
		{
			super( propertiesModel );
			setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			// setAutoStartEditOnKeyStroke( true );
			setSurrendersFocusOnKeystroke( true );
			setRowHeight( 19 );
			// setHorizontalScrollEnabled(true);
		}

		public PropertyModelItem getTestProperty()
		{
			int index = getSelectedRow();
			if( index == -1 )
				return null;
			TestProperty property = propertiesModel.getPropertyAtRow( index );
			return new PropertyModelItem( property, true );
		}
	}

	private Component buildToolbar()
	{
		toolbar = UISupport.createSmallToolbar();

		if( holder instanceof MutableTestPropertyHolder )
		{
			removePropertyAction = new RemovePropertyAction();
			addPropertyAction = new AddPropertyAction();
			movePropertyUpAction = new MovePropertyUpAction();
			movePropertyDownAction = new MovePropertyDownAction();

			JButton addPropertyButton = UISupport.createToolbarButton( addPropertyAction );
			toolbar.add( addPropertyButton );
			JButton removePropertyButton = UISupport.createToolbarButton( removePropertyAction );
			toolbar.add( removePropertyButton );

			JButton movePropertyUpButton = UISupport.createToolbarButton( movePropertyUpAction );
			toolbar.add( movePropertyUpButton );
			JButton movePropertyDownButton = UISupport.createToolbarButton( movePropertyDownAction );
			toolbar.add( movePropertyDownButton );
		}

		JButton clearPropertiesButton = UISupport.createToolbarButton( new ClearPropertiesAction() );
		toolbar.add( clearPropertiesButton );
		JButton loadPropertiesButton = UISupport.createToolbarButton( loadPropertiesAction );
		toolbar.add( loadPropertiesButton );

		return toolbar;
	}

	public JXToolBar getToolbar()
	{
		return toolbar;
	}

	public JTable getPropertiesTable()
	{
		return propertiesTable;
	}

	public void release()
	{
		if( propertiesTable.isEditing() )
			propertiesTable.getCellEditor().stopCellEditing();

		holder.removeTestPropertyListener( testPropertyListener );
	}

	public void setEnabled( boolean enabled )
	{
		addPropertyAction.setEnabled( enabled );
		removePropertyAction.setEnabled( enabled );
		propertiesTable.setEnabled( enabled );
		loadPropertiesAction.setEnabled( enabled );

		super.setEnabled( enabled );
	}

	private final class InternalTestPropertyListener implements TestPropertyListener
	{
		private boolean enabled = true;

		public boolean isEnabled()
		{
			return enabled;
		}

		public void setEnabled( boolean enabled )
		{
			this.enabled = enabled;
		}

		public void propertyAdded( String name )
		{
			if( enabled )
				propertiesModel.fireTableDataChanged();
		}

		public void propertyRemoved( String name )
		{
			if( enabled )
				propertiesModel.fireTableDataChanged();
		}

		public void propertyRenamed( String oldName, String newName )
		{
			if( enabled )
				propertiesModel.fireTableDataChanged();
		}

		public void propertyValueChanged( String name, String oldValue, String newValue )
		{
			if( enabled )
				propertiesModel.fireTableDataChanged();
		}

		public void propertyMoved( String name, int oldIndex, int newIndex )
		{
			if( enabled )
				propertiesModel.fireTableDataChanged();
		}
	}

	private class PropertiesModel extends AbstractTableModel
	{
		private StringList names = new StringList();

		public PropertiesModel()
		{
			names = new StringList( holder.getPropertyNames() );
		}

		public int getRowCount()
		{
			return names.size();
		}

		public int getColumnCount()
		{
			return 2;
		}

		@Override
		public void fireTableDataChanged()
		{
			names = new StringList( holder.getPropertyNames() );
			super.fireTableDataChanged();
		}

		public String getColumnName( int columnIndex )
		{
			switch( columnIndex )
			{
			case 0 :
				return "Name";
			case 1 :
				return "Value";
			}

			return null;
		}

		public boolean isCellEditable( int rowIndex, int columnIndex )
		{
			if( columnIndex == 0 )
			{
				return holder instanceof MutableTestPropertyHolder;
			}

			return !holder.getProperty( names.get( rowIndex ) ).isReadOnly();
		}

		public void setValueAt( Object aValue, int rowIndex, int columnIndex )
		{
			TestProperty property = holder.getProperty( names.get( rowIndex ) );
			switch( columnIndex )
			{
			case 0 :
			{
				if( holder instanceof MutableTestPropertyHolder )
				{
					TestProperty prop = holder.getProperty( aValue.toString() );
					if( prop != null && prop != property )
					{
						UISupport.showErrorMessage( "Property name exists!" );
						return;
					}
					( ( MutableTestPropertyHolder )holder ).renameProperty( property.getName(), aValue.toString() );
				}
				break;
			}
			case 1 :
			{
				property.setValue( aValue.toString() );
				break;
			}
			}
		}

		@Override
		public Class<?> getColumnClass( int columnIndex )
		{
			return String.class;
		}

		public TestProperty getPropertyAtRow( int rowIndex )
		{
			return holder.getProperty( names.get( rowIndex ) );
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			TestProperty property = holder.getProperty( names.get( rowIndex ) );
			if( property == null )
				return null;

			switch( columnIndex )
			{
			case 0 :
				return property.getName();
			case 1 :
				return property.getValue();
			}

			return null;
		}
	}

	private class AddPropertyAction extends AbstractAction
	{
		public AddPropertyAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Adds a property to the property list" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String name = UISupport.prompt( "Specify unique property name", "Add Property", "" );
			if( StringUtils.hasContent( name ) )
			{
				if( holder.hasProperty( name ) )
				{
					UISupport.showErrorMessage( "Property name [" + name + "] already exists.." );
					return;
				}

				( ( MutableTestPropertyHolder )holder ).addProperty( name );
				final int row = holder.getPropertyNames().length - 1;
				propertiesModel.fireTableRowsInserted( row, row );
				SwingUtilities.invokeLater( new Runnable()
				{
					public void run()
					{
						requestFocusInWindow();
						scrollRectToVisible( propertiesTable.getCellRect( row, 1, true ) );
						SwingUtilities.invokeLater( new Runnable()
						{
							public void run()
							{
								propertiesTable.editCellAt( row, 1 );
								Component editorComponent = propertiesTable.getEditorComponent();
								if( editorComponent != null )
									editorComponent.requestFocusInWindow();
							}
						} );
					}
				} );

			}
		}
	}

	private class RemovePropertyAction extends AbstractAction
	{
		public RemovePropertyAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Removes the selected property from the property list" );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int row = propertiesTable.getSelectedRow();
			if( row == -1 )
				return;

			UISupport.stopCellEditing( propertiesTable );

			String propertyName = propertiesModel.getValueAt( row, 0 ).toString();
			if( UISupport.confirm( "Remove property [" + propertyName + "]?", "Remove Property" ) )
			{
				( ( MutableTestPropertyHolder )holder ).removeProperty( propertyName );
				propertiesModel.fireTableRowsDeleted( row, row );
			}
		}
	}

	private class ClearPropertiesAction extends AbstractAction
	{
		public ClearPropertiesAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/clear_properties.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Clears all current property values" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( UISupport.confirm( "Clear all property values?", "Clear Properties" ) )
			{
				for( String name : holder.getPropertyNames() )
				{
					holder.getProperty( name ).setValue( null );
				}
			}
		}
	}

	private class MovePropertyUpAction extends AbstractAction
	{
		public MovePropertyUpAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/up_arrow.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Moves selected property up one row" );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = propertiesTable.getSelectedRow();
			if( ix != -1 )
			{
				( ( MutableTestPropertyHolder )holder ).moveProperty( holder.getPropertyAt( ix ).getName(), ix - 1 );
				propertiesTable.setRowSelectionInterval( ix - 1, ix - 1 );
			}
		}
	}

	private class MovePropertyDownAction extends AbstractAction
	{
		public MovePropertyDownAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/down_arrow.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Moves selected property down one row" );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = propertiesTable.getSelectedRow();
			if( ix != -1 )
			{
				( ( MutableTestPropertyHolder )holder ).moveProperty( holder.getPropertyAt( ix ).getName(), ix + 1 );

				propertiesTable.setRowSelectionInterval( ix + 1, ix + 1 );
			}
		}
	}

	private class LoadPropertiesAction extends AbstractAction
	{
		public LoadPropertiesAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/load_properties.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Loads property values from an external file" );
		}

		public void actionPerformed( ActionEvent e )
		{
			File file = UISupport.getFileDialogs().open( this, "Set properties source", null, null, null );
			if( file != null )
			{
				try
				{
					boolean createMissing = holder instanceof MutableTestPropertyHolder
							&& UISupport.confirm( "Create missing properties?", "Set Properties Source" );

					Properties props = new Properties();
					props.load( new FileInputStream( file ) );
					for( Object obj : props.keySet() )
					{
						String name = obj.toString();
						if( holder.hasProperty( name ) )
						{
							holder.setPropertyValue( name, props.getProperty( name ) );
						}
						else if( createMissing )
						{
							( ( MutableTestPropertyHolder )holder ).addProperty( name ).setValue( props.getProperty( name ) );
						}
					}

					UISupport.showInfoMessage( "Loaded " + props.size() + " properties from [" + file.getAbsolutePath()
							+ "]" );
				}
				catch( Exception e1 )
				{
					UISupport.showErrorMessage( "Failed to load properties from [" + file.getAbsolutePath() + "]; " + e1 );
				}
			}
		}
	}

	public TestPropertyHolder getHolder()
	{
		return holder;
	}

	public PropertiesModel getPropertiesModel()
	{
		return propertiesModel;
	}

	public final class PropertyHolderTablePropertyExpansionDropTarget implements DropTargetListener
	{
		public PropertyHolderTablePropertyExpansionDropTarget()
		{
		}

		public void dragEnter( DropTargetDragEvent dtde )
		{
			if( !isAcceptable( dtde.getTransferable(), dtde.getLocation() ) )
				dtde.rejectDrag();
		}

		public void dragExit( DropTargetEvent dtde )
		{
		}

		public void dragOver( DropTargetDragEvent dtde )
		{
			if( !isAcceptable( dtde.getTransferable(), dtde.getLocation() ) )
			{
				dtde.rejectDrag();
			}
			else
			{
				dtde.acceptDrag( dtde.getDropAction() );
			}
		}

		public void drop( DropTargetDropEvent dtde )
		{
			if( !isAcceptable( dtde.getTransferable(), dtde.getLocation() ) )
			{
				dtde.rejectDrop();
			}
			else
			{
				try
				{
					Transferable transferable = dtde.getTransferable();
					Object transferData = transferable.getTransferData( transferable.getTransferDataFlavors()[0] );
					if( transferData instanceof PropertyModelItem )
					{
						dtde.acceptDrop( dtde.getDropAction() );
						PropertyModelItem modelItem = ( PropertyModelItem )transferData;

						String xpath = modelItem.getXPath();
						if( xpath == null && XmlUtils.seemsToBeXml( modelItem.getProperty().getValue() ) )
						{
							xpath = UISupport.selectXPath( "Create PropertyExpansion", "Select XPath below", modelItem
									.getProperty().getValue(), null );

							if( xpath != null )
								xpath = XmlUtils.removeXPathNamespaceDeclarations( xpath );
						}

						PropertyExpansion propertyExpansion = new PropertyExpansionImpl( modelItem.getProperty(), xpath );

						Point point = dtde.getLocation();
						int column = getPropertiesTable().columnAtPoint( point );
						int row = getPropertiesTable().rowAtPoint( point );

						if( row == -1 )
						{
							if( holder instanceof MutableTestPropertyHolder )
							{
								MutableTestPropertyHolder mtph = ( MutableTestPropertyHolder )holder;
								String name = UISupport.prompt( "Specify unique name of property", "Add Property", modelItem
										.getProperty().getName() );
								while( name != null && mtph.hasProperty( name ) )
								{
									name = UISupport.prompt( "Specify unique name of property", "Add Property", modelItem
											.getProperty().getName() );
								}

								if( name != null )
									mtph.addProperty( name ).setValue( propertyExpansion.toString() );
							}
						}
						else
						{
							getPropertiesTable().setValueAt( propertyExpansion.toString(), row, column );
						}

						dtde.dropComplete( true );
					}
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
			}
		}

		public void dropActionChanged( DropTargetDragEvent dtde )
		{
		}

		public boolean isAcceptable( Transferable transferable, Point point )
		{
			int row = getPropertiesTable().rowAtPoint( point );
			if( row >= 0 )
			{
				int column = getPropertiesTable().columnAtPoint( point );
				if( column != 1 )
					return false;

				if( !getPropertiesTable().isCellEditable( row, column ) )
					return false;
			}
			else if( !( getHolder() instanceof MutableTestPropertyHolder ) )
			{
				return false;
			}

			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			for( int i = 0; i < flavors.length; i++ )
			{
				DataFlavor flavor = flavors[i];
				if( flavor.isMimeTypeEqual( DataFlavor.javaJVMLocalObjectMimeType ) )
				{
					try
					{
						Object modelItem = transferable.getTransferData( flavor );
						if( modelItem instanceof PropertyModelItem
								&& ( ( PropertyModelItem )modelItem ).getProperty().getModelItem() != getHolder()
										.getModelItem() )
						{
							return PropertyExpansionUtils.canExpandProperty( getHolder().getModelItem(),
									( ( PropertyModelItem )modelItem ).getProperty() );
						}
					}
					catch( Exception ex )
					{
						SoapUI.logError( ex );
					}
				}
			}

			return false;
		}
	}
}
