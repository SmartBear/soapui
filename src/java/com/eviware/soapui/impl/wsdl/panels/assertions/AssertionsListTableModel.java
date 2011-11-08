/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.panels.assertions;

import java.util.LinkedHashSet;

import javax.swing.table.DefaultTableModel;

public class AssertionsListTableModel extends DefaultTableModel
{
	LinkedHashSet<AssertionListEntry> listEntriesSet;

	public AssertionsListTableModel()
	{
	}

	public void setListEntriesSet( LinkedHashSet<AssertionListEntry> listEntriesSet )
	{
		this.listEntriesSet = listEntriesSet;
	}

	@Override
	public int getColumnCount()
	{
		return 1;
	}

	@Override
	public int getRowCount()
	{
		if( listEntriesSet != null )
		{
			return listEntriesSet.size();
		}
		else
		{
			return 1;
		}
	}

	@Override
	public Object getValueAt( int rowIndex, int columnIndex )
	{
		if( listEntriesSet != null )
			return listEntriesSet.toArray()[rowIndex];
		else
			return null;
	}
}
