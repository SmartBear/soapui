package com.eviware.soapui.impl.wsdl.panels.assertions;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.JTableFactory;
import org.jdesktop.swingx.JXTable;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.util.List;

public class CategoriesListTable extends JXTable
{
	private boolean selectable;
	private List<Integer> selectableIndexes;

	public CategoriesListTable( TableModel tableModel )
	{
		super( tableModel );
		if( UISupport.isMac() )
		{
			JTableFactory.setGridAttributes(this);
		}
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

	@Override
	public Component prepareRenderer( TableCellRenderer renderer, int row, int column )
	{
		Component defaultRenderer = super.prepareRenderer( renderer, row, column );
		JTableFactory.applyStripesToRenderer( row, defaultRenderer );
		return defaultRenderer;
	}

	@Override
	public boolean getShowVerticalLines()
	{
		return UISupport.isMac() ? false : super.getShowVerticalLines();
	}

}
