package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;

import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eviware.soapui.model.TestPropertyHolder;

public class DebugPropertyHolderTable extends PropertyHolderTable
{

	public DebugPropertyHolderTable( TestPropertyHolder holder )
	{
		super( holder );
	}

	protected JTable buildPropertiesTable()
	{
		propertiesModel = new DefaultPropertyTableHolderModel( holder )
		{
			public boolean isCellEditable( int rowIndex, int columnIndex )
			{
				return false;
			};
		};
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

		// Set render this only for value column. In this cell render we handle password shadowing.
		propertiesTable.getColumnModel().getColumn( 1 ).setCellRenderer( new PropertiesTableCellRenderer() );
		return propertiesTable;
	}

}
