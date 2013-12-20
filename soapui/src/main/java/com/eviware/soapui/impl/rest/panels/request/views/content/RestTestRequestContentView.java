/*
 * SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.rest.panels.request.views.content;


import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel;

public class RestTestRequestContentView extends RestRequestContentView
{

	public RestTestRequestContentView( AbstractHttpXmlRequestDesktopPanel.HttpRequestMessageEditor restRequestMessageEditor, RestRequestInterface restRequest )
	{
		super( restRequestMessageEditor, restRequest );
	}

	@Override
	protected RestParamsTable buildParamsTable()
	{
		RestParamsTableModel restTestParamsTableModel = new RestParamsTableModel( super.getRestRequest().getParams() )
		{
			public int getColumnCount()
			{
				return 4;
			}

			@Override
			public void setValueAt( Object value, int rowIndex, int columnIndex )
			{
				RestParamProperty prop = params.getProperty( ( String )getValueAt( rowIndex, 0 ) );
				if( columnIndex == 1 )
					prop.setValue( value.toString() );
			}

			@Override
			public String getColumnName( int columnIndex )
			{
				if( columnIndex == 1 )
				{
					return "Value";
				}

				return super.getColumnName( columnIndex );
			}

			@Override
			public boolean isCellEditable( int rowIndex, int columnIndex )
			{
				// Only value is editable
				return columnIndex == 1;
			}
		};

		return new RestParamsTable( super.getRestRequest().getParams(), false, restTestParamsTableModel, NewRestResourceActionBase.ParamLocation.RESOURCE, false, true );
	}

}
