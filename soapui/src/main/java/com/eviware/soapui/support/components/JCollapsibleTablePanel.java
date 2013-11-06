/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.support.components;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.eviware.soapui.support.UISupport;

@SuppressWarnings( "serial" )
public class JCollapsibleTablePanel extends JCollapsiblePanel
{

	private JTable table;
	private String title;

	public JCollapsibleTablePanel( JTable table, String title )
	{
		super( new JPanel(), title );
		setTable( table );
		setMinusIcon( UISupport.createImageIcon( "/minus.gif" ) );
		setPlusIcon( UISupport.createImageIcon( "/plus.gif" ) );
		this.title = title;
	}

	private void setTable( JTable table )
	{
		this.table = table;
		this.table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		JPanel content = new JPanel( new BorderLayout() );
		content.add( table, BorderLayout.CENTER );
		setContentPanel( content );
	}

	public JTable getTable()
	{
		return table;
	}

	public String getTitle()
	{
		return title;
	}
}
