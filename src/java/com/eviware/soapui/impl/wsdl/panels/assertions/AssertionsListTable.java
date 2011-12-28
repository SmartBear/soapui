package com.eviware.soapui.impl.wsdl.panels.assertions;

import java.util.List;

import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

public class AssertionsListTable extends JXTable
{
	private List<Integer> nonSelectableIndexes;

	public AssertionsListTable( TableModel tableModel )
	{
		super( tableModel );
	}

	public void setNonSelectableIndexes( List<Integer> nonSelectableIndexes )
	{
		this.nonSelectableIndexes = nonSelectableIndexes;
	}

	@Override
	public void changeSelection( int rowIndex, int columnIndex, boolean toggle, boolean extend )
	{
		if( nonSelectableIndexes != null && nonSelectableIndexes.contains( rowIndex ) )
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

}
