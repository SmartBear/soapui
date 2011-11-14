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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

@SuppressWarnings( "serial" )
public class JCollapsibleTablePanel extends JCollapsiblePanel
{

	private JTable table;
	private JTable table2;

	
	public JCollapsibleTablePanel( JTable table, String title )
	{
		super( new JPanel(), title );
		setTables( table, null );
	}

	public JCollapsibleTablePanel( JTable table, JTable table2, String title )
	{
		super( new JPanel(), title );
		setTables( table, table2 );
	}

	public void setTables( JTable table, JTable table2 )
	{
		this.table = table;
		if( table2 != null )
			this.table2 = table2;
		else
		{
			this.table2 = new JTable();
			this.table2.setVisible( false );
		}
		this.table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		this.table2.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		JPanel content = new JPanel( new BorderLayout() );
		JSplitPane sp = new JSplitPane( JSplitPane.VERTICAL_SPLIT, this.table, this.table2 );
		sp.setContinuousLayout( true );
		sp.setDividerSize( 0 );
		content.add( sp, BorderLayout.CENTER );
		setContentPanel( content );
	}

	public JTable[] getTables()
	{
		return new JTable[] { table, table2 };
	}
}
