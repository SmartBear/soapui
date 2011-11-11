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
package com.eviware.soapui.support.components;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class JCollapsibleTablePanel extends JCollapsiblePanel
{

	private JTable table;

	public JCollapsibleTablePanel( JTable table, String title )
	{
		super( new JPanel(), title );
		setTable( table );
	}

	public void setTable( JTable table )
	{
		this.table = table;
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		JPanel content = new JPanel(new BorderLayout());
		content.add( table );
		setContentPanel( content );
	}

	public JTable getTable()
	{
		return table;
	}
}
