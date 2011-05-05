package com.eviware.soapui.security.support;

import javax.swing.table.AbstractTableModel;

import com.eviware.soapui.config.MaliciousAttachmentConfig;
import com.eviware.soapui.security.tools.AttachmentHolder;
import com.eviware.soapui.support.UISupport;

public abstract class MaliciousAttachmentTableModel extends AbstractTableModel // AttachmentsTableModel
{

	// public MaliciousAttachmentTableModel( AttachmentContainer request )
	// {
	// super( request );
	// }

	protected AttachmentHolder holder = new AttachmentHolder();

	public int getRowCount()
	{
		return holder.size();
	}

	public void removeResult( int i )
	{
		if( UISupport.confirm( "Remove selected attachments?", "Remove Attachments" ) )
		{
			holder.removeElement( i );
			// removeAttachment( new int[] { i } );
			fireTableDataChanged();
		}
	}

	public void clear()
	{
		holder.clear();
		fireTableDataChanged();
	}

	public MaliciousAttachmentConfig getRowValue( int rowIndex )
	{
		return holder.getList().get( rowIndex );
	}

	public abstract int getColumnCount();

	public abstract String getColumnName( int column );

	public abstract Object getValueAt( int rowIndex, int columnIndex );

	public abstract void addResult( MaliciousAttachmentConfig config );
}
