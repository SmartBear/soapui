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

package com.eviware.soapui.support.editor.inspectors.attachments;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import com.eviware.soapui.impl.wsdl.AttachmentContainer;
import com.eviware.soapui.impl.wsdl.MutableAttachmentContainer;
import com.eviware.soapui.impl.wsdl.support.WsdlAttachment;
import com.eviware.soapui.model.iface.Attachment;

/**
 * TableModel for Request Attachments
 * 
 * @author emibre
 */

public class AttachmentsTableModel extends AbstractTableModel implements PropertyChangeListener, AttachmentTableModel
{

	private AttachmentContainer container;

	/** Creates a new instance of AttachmentTableModel */
	public AttachmentsTableModel( AttachmentContainer request )
	{
		this.container = request;

		this.container.addAttachmentsChangeListener( this );
	}

	public void release()
	{
		container.removeAttachmentsChangeListener( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.panels.attachments.AttachmentTableModel#addFile
	 * (java.io.File, boolean)
	 */
	public void addFile( File file, boolean cacheInRequest ) throws IOException
	{
		if( container instanceof MutableAttachmentContainer )
		{
			( ( MutableAttachmentContainer )container ).attachFile( file, cacheInRequest );
		}

		this.fireTableRowsInserted( container.getAttachmentCount(), container.getAttachmentCount() );
	}

	public void removeAttachment( int[] rowIndexes )
	{
		Arrays.sort( rowIndexes );
		for( int i = rowIndexes.length - 1; i >= 0; i-- )
			removeAttachment( rowIndexes[i] );
	}

	public void removeAttachment( int rowIndex )
	{
		if( container instanceof MutableAttachmentContainer )
		{
			( ( MutableAttachmentContainer )container ).removeAttachment( container.getAttachmentAt( rowIndex ) );
			this.fireTableRowsDeleted( rowIndex, rowIndex );
		}
	}

	public int getRowCount()
	{
		return container.getAttachmentCount();
	}

	public int getColumnCount()
	{
		return container instanceof MutableAttachmentContainer ? 7 : 6;
	}

	public Attachment getAttachmentAt( int rowIndex )
	{
		return container.getAttachmentAt( rowIndex );
	}

	public Object getValueAt( int rowIndex, int columnIndex )
	{
		if( rowIndex > getRowCount() )
			return null;

		Attachment att = container.getAttachmentAt( rowIndex );

		switch( columnIndex )
		{
		case 0 :
			return att.isCached() ? att.getName() : att.getUrl();
		case 1 :
			return att.getContentType();
		case 2 :
			return att.getSize();
		case 3 :
			return att.getPart();
		case 4 :
			return att.getAttachmentType();
		case 5 :
			return att.getContentID();
		case 6 :
			return att.isCached();
		default :
			return null;
		}
	}

	public int findColumn( String columnName )
	{
		if( columnName.equals( "Name" ) )
			return 0;
		else if( columnName.equals( "Content type" ) )
			return 1;
		else if( columnName.equals( "Size" ) )
			return 2;
		else if( columnName.equals( "Part" ) )
			return 3;
		else if( columnName.equals( "Type" ) )
			return 4;

		return -1;
	}

	public String getColumnName( int column )
	{
		if( column == 0 )
			return "Name";
		else if( column == 1 )
			return "Content type";
		else if( column == 2 )
			return "Size";
		else if( column == 3 )
			return "Part";
		else if( column == 4 )
			return "Type";
		else if( column == 5 )
			return "ContentID";
		else if( column == 6 )
			return "Cached";
		else
			return null;
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		return columnIndex == 6 ? Boolean.class : super.getColumnClass( columnIndex );
	}

	public boolean isCellEditable( int rowIndex, int columnIndex )
	{
		return container instanceof MutableAttachmentContainer
				&& ( columnIndex == 0 || columnIndex == 1 || columnIndex == 3 || columnIndex == 5 );
	}

	public void setValueAt( Object aValue, int rowIndex, int columnIndex )
	{
		if( !( container instanceof MutableAttachmentContainer ) )
			return;

		WsdlAttachment att = ( WsdlAttachment )container.getAttachmentAt( rowIndex );
		if( columnIndex == 0 )
		{
			if( att.isCached() )
				att.setName( ( String )aValue );
			else
				att.setUrl( aValue.toString() );
		}
		else if( columnIndex == 1 )
			att.setContentType( ( String )aValue );
		else if( columnIndex == 3 )
			att.setPart( ( String )aValue );
		else if( columnIndex == 5 )
			att.setContentID( ( String )aValue );

		fireTableRowsUpdated( rowIndex, rowIndex );
	}

	/**
	 * Update table when attachments or response changes
	 */

	public void propertyChange( PropertyChangeEvent evt )
	{
		fireTableDataChanged();
	}
}
