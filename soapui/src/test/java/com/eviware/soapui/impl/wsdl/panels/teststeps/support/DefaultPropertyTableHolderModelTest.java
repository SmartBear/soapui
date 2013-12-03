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
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DefaultPropertyTableHolderModelTest
{

	public static final String FIRST_PARAM_NAME = "Param1";
	public static final String PARAM_VALUE = "ParamValue";
	public static final String EMPTY_STRING = "";
	private RestParamsPropertyHolder methodParams;
	private DefaultPropertyTableHolderModel tableHolderModel;
	private RestParamsPropertyHolder requestParams;
	private DefaultPropertyTableHolderModel requestTableHolderModel;

	@Before
	public void setUp() throws Exception
	{
		methodParams = ModelItemFactory.makeRestMethod().getParams();
		methodParams.addProperty( FIRST_PARAM_NAME );
		tableHolderModel = createDefaultPropertyHolderTableModel( methodParams );
		requestParams = ModelItemFactory.makeRestRequest().getParams();
		requestTableHolderModel = createDefaultPropertyHolderTableModel( requestParams );
	}

	@Test
	public void doesNotSetDefaultValueIfModelItemIsRestRequest() throws Exception
	{
		RestParamProperty property = requestParams.getPropertyAt( 0 );
		assertThat( property.getValue(), is( PARAM_VALUE ) );
		assertThat( property.getDefaultValue(), is( EMPTY_STRING ) );
	}

	@Test
	public void setsDefaultValueIfModelItemIsRestMethod() throws Exception
	{
		RestParamProperty property = methodParams.getPropertyAt( 0 );
		assertThat( property.getValue(), is( PARAM_VALUE ) );
		assertThat( property.getDefaultValue(), is( PARAM_VALUE ) );
	}

	@Test
	public void handlesParameterMoveCorrectlyForMethodParameters() throws Exception
	{
		String lastParameterName = "lastOne";
		methodParams.addProperty( lastParameterName );
		tableHolderModel.moveProperty( lastParameterName, 1, 0 );

		String[] propertyNames = tableHolderModel.getPropertyNames();
		assertThat( propertyNames[0], is(lastParameterName) );
		assertThat(tableHolderModel.getPropertyAtRow( 0 ).getName(), is(lastParameterName));
	}

	@Test
	public void detectsParameterNameChange() throws Exception
	{
		String newParameterName = "lastOne";
		methodParams.renameProperty( FIRST_PARAM_NAME, newParameterName );

		String[] propertyNames = tableHolderModel.getPropertyNames();
		assertThat( propertyNames[0], is(newParameterName) );
		assertThat(tableHolderModel.getPropertyAtRow( 0 ).getName(), is(newParameterName));
	}

	@Test
	public void handlesParameterMoveCorrectlyForRequestParameters() throws Exception
	{
		String lastParameterName = "lastOne";
		requestParams.addProperty( lastParameterName );
		requestTableHolderModel.moveProperty( lastParameterName, 1, 0 );

		String[] propertyNames = requestTableHolderModel.getPropertyNames();
		assertThat( propertyNames[0], is(lastParameterName) );
		assertThat(requestTableHolderModel.getPropertyAtRow( 0 ).getName(), is(lastParameterName));
	}


	/* helper */

	private DefaultPropertyTableHolderModel createDefaultPropertyHolderTableModel( RestParamsPropertyHolder params )
	{
		params.addProperty( FIRST_PARAM_NAME);
		DefaultPropertyTableHolderModel tableHolderModel =
				new DefaultPropertyTableHolderModel<RestParamsPropertyHolder>( params );
		tableHolderModel.setValueAt( PARAM_VALUE, 0, 1 );
		return tableHolderModel;
	}
}
