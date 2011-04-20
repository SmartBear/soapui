/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionImpl;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.TestPropertyUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.model.tree.nodes.PropertyTreeNode.PropertyModelItem;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

public class PropertyHolderTable extends JPanel
{
	protected final TestPropertyHolder holder;
	protected PropertyHolderTableModel propertiesModel;
	protected RemovePropertyAction removePropertyAction;
	protected AddPropertyAction addPropertyAction;
	protected InternalTestPropertyListener testPropertyListener;
	protected JTable propertiesTable;
	protected JXToolBar toolbar;
	protected LoadPropertiesAction loadPropertiesAction;
	protected MovePropertyUpAction movePropertyUpAction;
	protected MovePropertyDownAction movePropertyDownAction;

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
		propertiesModel = new DefaultPropertyTableHolderModel( holder );
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

			toolbar.addRelatedGap();
			JButton movePropertyUpButton = UISupport.createToolbarButton( movePropertyUpAction );
			toolbar.add( movePropertyUpButton );
			JButton movePropertyDownButton = UISupport.createToolbarButton( movePropertyDownAction );
			toolbar.add( movePropertyDownButton );

			if( !( holder instanceof AMFRequestTestStep || holder instanceof JdbcRequestTestStep ) )
			{
				toolbar.addRelatedGap();
				toolbar.add( UISupport.createToolbarButton( new SortPropertiesAction() ) );
				toolbar.addRelatedGap();
			}
		}

		JButton clearPropertiesButton = UISupport.createToolbarButton( new ClearPropertiesAction() );
		toolbar.add( clearPropertiesButton );
		JButton loadPropertiesButton = UISupport.createToolbarButton( loadPropertiesAction );
		toolbar.add( loadPropertiesButton );
		toolbar.add( UISupport.createToolbarButton( new SavePropertiesAction() ) );

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

		@SuppressWarnings( "unused" )
		public boolean isEnabled()
		{
			return enabled;
		}

		@SuppressWarnings( "unused" )
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

	protected class RemovePropertyAction extends AbstractAction
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

	protected class ClearPropertiesAction extends AbstractAction
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

	protected class MovePropertyUpAction extends AbstractAction
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

	protected class MovePropertyDownAction extends AbstractAction
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

	protected class LoadPropertiesAction extends AbstractAction
	{
		private XFormDialog dialog;

		public LoadPropertiesAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/load_properties.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Loads property values from an external file" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( dialog == null )
				dialog = ADialogBuilder.buildDialog( LoadOptionsForm.class );

			dialog.getFormField( LoadOptionsForm.DELETEREMAINING )
					.setEnabled( holder instanceof MutableTestPropertyHolder );
			dialog.getFormField( LoadOptionsForm.CREATEMISSING ).setEnabled( holder instanceof MutableTestPropertyHolder );

			if( dialog.show() )
			{
				try
				{
					BufferedReader reader = new BufferedReader( new FileReader( dialog.getValue( LoadOptionsForm.FILE ) ) );

					String line = reader.readLine();
					int count = 0;

					Set<String> names = new HashSet<String>( Arrays.asList( holder.getPropertyNames() ) );

					while( line != null )
					{
						if( line.trim().length() > 0 && !( line.charAt( 0 ) == '#' ) )
						{
							int ix = line.indexOf( '=' );
							if( ix > 0 )
							{
								String name = line.substring( 0, ix ).trim();
								String value = line.length() > ix ? line.substring( ix + 1 ) : "";

								// read multiline value
								if( value.endsWith( "\\" ) )
								{
									value = value.substring( 0, value.length() - 1 );

									String ln = reader.readLine();
									while( ln != null && ln.endsWith( "\\" ) )
									{
										value += ln.substring( 0, ln.length() - 1 );
										ln = reader.readLine();
									}

									if( ln != null )
										value += ln;
									if( ln == null )
										break;
								}

								if( holder.hasProperty( name ) )
								{
									count++ ;
									holder.setPropertyValue( name, value );
								}
								else if( dialog.getBooleanValue( LoadOptionsForm.CREATEMISSING )
										&& holder instanceof MutableTestPropertyHolder )
								{
									TestProperty prop = ( ( MutableTestPropertyHolder )holder ).addProperty( name );
									if( !prop.isReadOnly() )
										prop.setValue( value );
									count++ ;
								}

								names.remove( name );
							}
						}

						line = reader.readLine();
					}

					if( dialog.getBooleanValue( LoadOptionsForm.DELETEREMAINING )
							&& holder instanceof MutableTestPropertyHolder )
					{
						for( String name : names )
						{
							( ( MutableTestPropertyHolder )holder ).removeProperty( name );
						}
					}

					reader.close();
					UISupport.showInfoMessage( "Added/Updated " + count + " properties from file" );
				}
				catch( Exception ex )
				{
					UISupport.showErrorMessage( ex );
				}
			}
		}
	}

	private class SavePropertiesAction extends AbstractAction
	{
		public SavePropertiesAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/set_properties_target.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Saves current property-values to a file" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( holder.getPropertyCount() == 0 )
			{
				UISupport.showErrorMessage( "No properties to save!" );
				return;
			}

			File file = UISupport.getFileDialogs().saveAs( this, "Save Properties" );
			if( file != null )
			{
				try
				{
					int cnt = TestPropertyUtils.saveTo( holder, file.getAbsolutePath() );
					UISupport.showInfoMessage( "Saved " + cnt + " propert" + ( ( cnt == 1 ) ? "y" : "ies" ) + " to file" );
				}
				catch( IOException e1 )
				{
					UISupport.showErrorMessage( e1 );
				}
			}
		}
	}

	private class SortPropertiesAction extends AbstractAction
	{
		public SortPropertiesAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/arrow_down.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Sorts properties alphabetically" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( holder.getPropertyCount() == 0 )
			{
				UISupport.showErrorMessage( "No properties to sort!" );
				return;
			}

			try
			{
				UISupport.setHourglassCursor();
				TestPropertyUtils.sortProperties( ( MutableTestPropertyHolder )holder );
			}
			finally
			{
				UISupport.resetCursor();
			}

		}
	}

	@AForm( name = "Load Properties", description = "Set load options below" )
	private static interface LoadOptionsForm
	{
		@AField( name = "File", description = "The Properties file to load", type = AFieldType.FILE )
		public static final String FILE = "File";

		@AField( name = "Create Missing", description = "Creates Missing Properties", type = AFieldType.BOOLEAN )
		public static final String CREATEMISSING = "Create Missing";

		@AField( name = "Delete Remaining", description = "Deletes properties not in file", type = AFieldType.BOOLEAN )
		public static final String DELETEREMAINING = "Delete Remaining";
	}

	public TestPropertyHolder getHolder()
	{
		return holder;
	}

	public PropertyHolderTableModel getPropertiesModel()
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
								xpath = PropertyExpansionUtils.shortenXPathForPropertyExpansion( xpath, modelItem.getProperty()
										.getValue() );
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
