package com.eviware.soapui.security.support;

import com.eviware.soapui.impl.wsdl.AttachmentContainer;
import com.eviware.soapui.security.tools.AttachmentElement;

public class MaliciousAttachmentReplaceTableModel extends MaliciousAttachmentTableModel
{

	public MaliciousAttachmentReplaceTableModel( AttachmentContainer request )
	{
		super( request );
	}

	public Class<?> getColumnClass( int columnIndex )
	{
		return columnIndex == 3 ? Boolean.class : columnIndex == 2 ? String.class : String.class;
	}

	public boolean isCellEditable( int row, int col )
	{
		if( col > 1 )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public int getColumnCount()
	{
		return 4;
	}

	public String getColumnName( int column )
	{
		switch( column )
		{
		case 0 :
			return "With";
		case 1 :
			return "Size";
		case 2 :
			return "Content type";
		case 3 :
			return "Enable";
		}

		return null;
	}

	public Object getValueAt( int rowIndex, int columnIndex )
	{
		AttachmentElement element = holder.getList().get( rowIndex );

		if( element != null )
		{
			switch( columnIndex )
			{
			case 0 :
				return element.isCached() ? element.getName() : element.getPath();
			case 1 :
				return element.getSize();
			case 2 :
				return element.getContentType();
			case 3 :
				return element.isEnabled();
			}
		}

		return null;
	}

	public void setValueAt( Object aValue, int row, int column )
	{
		if( holder.getList().isEmpty() )
		{
			return;
		}
		AttachmentElement element = holder.getList().get( row );

		switch( column )
		{
		case 2 :
			element.setContentType( ( String )aValue );
			break;
		case 3 :
			element.setEnabled( ( Boolean )aValue );
			break;
		}
	}

}
