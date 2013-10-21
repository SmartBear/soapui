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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DefaultPropertyTableHolderModelTest
{

	public static final String PARAM_VALUE = "ParamValue";
	public static final String EMPTY_STRING = "";

	@Test
	public void doesNotSetDefaultValueIfModelItemIsRestRequest() throws Exception
	{
		RestParamsPropertyHolder params = ModelItemFactory.makeRestRequest().getParams();
		createDefaultPropertyHolderTableModel( params );

		RestParamProperty property = params.getPropertyAt( 0 );
		assertThat( property.getValue(), is( PARAM_VALUE ) );
		assertThat( property.getDefaultValue(), is( EMPTY_STRING ) );
	}

	@Test
	public void setsDefaultValueIfModelItemIsRestRequest() throws Exception
	{
		RestParamsPropertyHolder params = ModelItemFactory.makeRestMethod().getParams();
		createDefaultPropertyHolderTableModel( params );

		RestParamProperty property = params.getPropertyAt( 0 );
		assertThat( property.getValue(), is( PARAM_VALUE ) );
		assertThat( property.getDefaultValue(), is( PARAM_VALUE ) );
	}

	private void createDefaultPropertyHolderTableModel( RestParamsPropertyHolder params )
	{
		params.addProperty( "Param1" );
		DefaultPropertyTableHolderModel tableHolderModel =
				new DefaultPropertyTableHolderModel<RestParamsPropertyHolder>( params );
		tableHolderModel.setValueAt( PARAM_VALUE, 0, 1 );
	}
}
