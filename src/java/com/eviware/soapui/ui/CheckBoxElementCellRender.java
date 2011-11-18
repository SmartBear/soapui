package com.eviware.soapui.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class CheckBoxElementCellRender implements ListCellRenderer
{

	private CheckBoxComboBox cb;

	public CheckBoxElementCellRender( CheckBoxComboBox checkBoxComboBox )
	{
		this.cb = checkBoxComboBox;
	}

	@Override
	public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus )
	{
		Component cmp = cb.getElementAt( index );
		if( list.getWidth() < cmp.getWidth() )
			list.setMinimumSize( new Dimension( cmp.getWidth(), list.getHeight() ) );
		return cmp ;
	}

}
