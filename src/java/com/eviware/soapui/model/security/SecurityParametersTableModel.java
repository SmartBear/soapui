package com.eviware.soapui.model.security;

import javax.swing.table.DefaultTableModel;

import com.eviware.soapui.security.support.SecurityCheckedParameterHolder;
import com.eviware.soapui.security.support.SecurityCheckedParameterImpl;

public class SecurityParametersTableModel extends DefaultTableModel
{

	private String[] columnNames = new String[] { "Label", "Name", "XPath", "Used" };
	private SecurityCheckedParameterHolder holder;

	public SecurityParametersTableModel( SecurityCheckedParameterHolder holder )
	{
		this.holder = holder;
	}

	@Override
	public int getColumnCount()
	{
		return 4;
	}

	@Override
	public String getColumnName( int column )
	{
		return columnNames[column];
	}

	@Override
	public boolean isCellEditable( int row, int column )
	{
		return true;
	}

	@Override
	public Object getValueAt( int row, int column )
	{
		SecurityCheckedParameter param = holder.getParameterList().get( row );
		switch( column )
		{
		case 0 :
			return param.getLabel();
		case 1 :
			return param.getName();
		case 2 :
			return param.getXPath();
		case 3 :
			return param.isChecked();
		}
		return super.getValueAt( row, column );
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		return columnIndex == 3 ? Boolean.class : String.class;
	}

	@Override
	public void setValueAt( Object aValue, int row, int column )
	{
		SecurityCheckedParameterImpl param = ( SecurityCheckedParameterImpl )holder.getParameterList().get( row );
		switch( column )
		{
		case 0 :
			param.setLabel( ( String )aValue );
			break;
		case 1 :
			param.setName( ( String )aValue );
			break;
		case 2 :
			param.setXPath( ( String )aValue );
			break;
		case 3 :
			param.setChecked( ( Boolean )aValue );
		}
	}

	public void addParameter()
	{
		holder.addParameter( "New Parameter" + holder.getParameterList().size() );
		fireTableDataChanged();
	}

	@Override
	public int getRowCount()
	{
		return holder == null ? 0 : holder.getParameterList().size();
	}

	public void removeRows( int[] selectedRows )
	{
		holder.removeParameters(selectedRows);
	}

}
