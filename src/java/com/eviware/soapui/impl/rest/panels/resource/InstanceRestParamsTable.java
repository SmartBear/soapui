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

package com.eviware.soapui.impl.rest.panels.resource;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

public class InstanceRestParamsTable extends RestParamsTable
{
	private JTable paramsTable;

	public InstanceRestParamsTable( RestParamsPropertyHolder params )
	{
		super( params, false );
	}

	public JTable getParamsTable()
	{
		return paramsTable;
	}

	protected void init( RestParamsPropertyHolder params, boolean showInspector )
	{
		paramsTableModel = new InstanceRestParamsTableModel( params );
		paramsTable = new JTable( paramsTableModel );
		paramsTable.setRowHeight( 19 );
		paramsTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		add( buildToolbar(), BorderLayout.NORTH );
		add( new JScrollPane( paramsTable ), BorderLayout.CENTER );
	}

	protected Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.add( UISupport.createToolbarButton( defaultParamsAction, paramsTable.getRowCount() > 0 ) );
		toolbar.add( UISupport.createToolbarButton( clearParamsAction, paramsTable.getRowCount() > 0 ) );
		toolbar.addSeparator();

		insertAdditionalButtons( toolbar );

		toolbar.addGlue();

		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.WADL_PARAMS_HELP_URL ) ) );

		return toolbar;
	}

	private class InstanceRestParamsTableModel extends RestParamsTableModel
	{

		public InstanceRestParamsTableModel( RestParamsPropertyHolder params )
		{
			super( params );
		}

		@Override
		public boolean isCellEditable( int rowIndex, int columnIndex )
		{
			return columnIndex == 1;
		}

		public int getColumnCount()
		{
			return 2;
		}

		@Override
		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "Name";
			case 1 :
				return "Value";
			}
			return null;
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			RestParamProperty prop = params.getPropertyAt( rowIndex );

			switch( columnIndex )
			{
			case 0 :
				return prop.getName();
			case 1 :
				return prop.getValue();
			}

			return null;
		}

		@Override
		public void setValueAt( Object value, int rowIndex, int columnIndex )
		{
			RestParamProperty prop = params.getPropertyAt( rowIndex );

			switch( columnIndex )
			{
			case 0 :
				params.renameProperty( prop.getName(), value.toString() );
				return;
			case 1 :
				prop.setValue( value.toString() );
				return;
			}
		}

	}
}
