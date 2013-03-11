package com.eviware.soapui.impl.wsdl.panels.assertions;

import java.util.List;

import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

public class CategoriesListTable extends JXTable
{
	private boolean selectable;
	private List<Integer> selectableIndexes;

	public CategoriesListTable( TableModel tableModel )
	{
		super( tableModel );
	}

	public void setSelectable( boolean selectable )
	{
		this.selectable = selectable;
	}

	public void setSelectableIndexes( List<Integer> selectableIndexes )
	{
		this.selectableIndexes = selectableIndexes;
	}

	@Override
	public void changeSelection( int rowIndex, int columnIndex, boolean toggle, boolean extend )
	{
		if( !selectable )
			rowIndex = -1;
		else if( selectableIndexes != null && !selectableIndexes.contains( rowIndex ) )
		{
			int currentIndex = getSelectedRow();
			if( rowIndex != currentIndex )
			{
				rowIndex = -1;
			}
		}
		// make the selection change
		super.changeSelection( rowIndex, columnIndex, toggle, extend );
	}

	public boolean isSelectable( int index )
	{
		return selectableIndexes.contains( index );
	}

	public List<Integer> getSelectableIndexes()
	{
		return selectableIndexes;
	}

}
