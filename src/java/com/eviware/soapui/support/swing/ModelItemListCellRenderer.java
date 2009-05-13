/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.eviware.soapui.model.ModelItem;

@SuppressWarnings( "serial" )
public class ModelItemListCellRenderer extends DefaultListCellRenderer
{

	@Override
	public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus )
	{
		Component result = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

		if( value instanceof ModelItem )
		{
			ModelItem item = ( ModelItem )value;
			setIcon( item.getIcon() );
			setText( item.getName() );
		}

		return result;
	}

}
