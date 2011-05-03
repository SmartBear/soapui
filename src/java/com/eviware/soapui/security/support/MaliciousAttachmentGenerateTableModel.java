package com.eviware.soapui.security.support;

import java.io.File;

import com.eviware.soapui.impl.wsdl.AttachmentContainer;
import com.eviware.soapui.security.tools.AttachmentElement;

public class MaliciousAttachmentGenerateTableModel extends MaliciousAttachmentTableModel
{

	public MaliciousAttachmentGenerateTableModel( AttachmentContainer request )
	{
		super( request );
	}

	public Class<?> getColumnClass( int columnIndex )
	{
		return columnIndex == 2 ? Boolean.class : columnIndex == 1 ? String.class : String.class;
	}

	public boolean isCellEditable( int row, int col )
	{
		if( col > 0 )
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
		return 3;
	}

	public String getColumnName( int column )
	{
		switch( column )
		{
		case 0 :
			return "Size";
		case 1 :
			return "Content type";
		case 2 :
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
				return element.getSize();
			case 1 :
				return element.getContentType();
			case 2 :
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
		case 0 :
			element.setAttachment( ( File )aValue );
			break;
		case 1 :
			element.setContentType( ( String )aValue );
			break;
		case 2 :
			element.setEnabled( ( Boolean )aValue );
			break;
		}
	}

}
