/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation.METHOD;
import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle.QUERY;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-08-29
 * Time: 16:50
 * To change this template use File | Settings | File Templates.
 */
public class RestParamsTableModelUnitTest
{
	private static final int NAME_COLUMN_INDEX = 0;
	public static final int VALUE_COLUMN_INDEX = 1;
	private static final int STYLE_COLUMN_INDEX = 2;
	private static final int LOCATION_COLUMN_INDEX = 3;
	public static final String PARAM_NAME_1 = "ParamName1";
	public static final String PARAM_NAME_2 = "ParamName2";

	private RestParamsTableModel restParamsTableModel;
	private RestParamsPropertyHolder params;
	private RestRequest restRequest;

	@Before
	public void setUp() throws SoapUIException
	{
		restRequest = ModelItemFactory.makeRestRequest();
		params = restRequest.getParams();
		RestParamProperty param = params.addProperty( PARAM_NAME_1 );
		param.setParamLocation( METHOD );
		RestParamProperty param2 = params.addProperty( PARAM_NAME_2 );
		param2.setParamLocation( METHOD );

		restParamsTableModel = new RestParamsTableModel( params );
	}


	@Test
	public void removesAndAddThePropertyListenerAgainWhenParamIsSet()
	{
		mockParams();
		restParamsTableModel = new RestParamsTableModel( params );
		restParamsTableModel.setParams( params );
		verify( params, times( 1 ) ).removeTestPropertyListener( any( TestPropertyListener.class ) );
		verify( params, times( 2 ) ).addTestPropertyListener( any( TestPropertyListener.class ) );
	}

	@Test
	public void removesListenerOnRelease()
	{
		mockParams();
		restParamsTableModel.setParams( params );
		restParamsTableModel.release();
		verify( params, times( 1 ) ).addTestPropertyListener( any( TestPropertyListener.class ) );
		verify( params, times( 1 ) ).removeTestPropertyListener( any( TestPropertyListener.class ) );

	}

	@Test
	public void setsValueToPropertyWhenSetValueAtIsInvoked()
	{
		String value = "New value";
		restParamsTableModel.setValueAt( value, 0, VALUE_COLUMN_INDEX );
		assertThat( ( String )restParamsTableModel.getValueAt( 0, VALUE_COLUMN_INDEX ), is( value ) );
	}

	@Test
	public void renamesThePropertyIfSetValueIsInvokedOnFirstColumn()
	{
		String value = "New Name";
		restParamsTableModel.setValueAt( value, 0, NAME_COLUMN_INDEX );
		assertThat( ( String )restParamsTableModel.getValueAt( 0, NAME_COLUMN_INDEX ), is( value ) );
	}

	@Test
	public void changesPropertyStyleWhenSetValueIsInvokedonStyleColumn()
	{
		restParamsTableModel.setValueAt( QUERY, 0, STYLE_COLUMN_INDEX );
		assertThat( ( RestParamsPropertyHolder.ParameterStyle )restParamsTableModel.getValueAt( 0, STYLE_COLUMN_INDEX ),
				is( QUERY ) );
	}

	@Test
	public void givenModelWithParamsWhenSetLocationAndGetLocationThenShouldReturnSameValue()
	{
		restParamsTableModel.setValueAt( METHOD, 0, LOCATION_COLUMN_INDEX );
		assertThat( ( NewRestResourceActionBase.ParamLocation )restParamsTableModel.getValueAt( 0, LOCATION_COLUMN_INDEX ),
				is( METHOD ) );
	}

	private void mockParams()
	{
		params = mock( RestParamsPropertyHolder.class );
		RestRequest restRequest = mock( RestRequest.class );

		RestResource resource = mock( RestResource.class );
		RestParamsPropertyHolder resourceParams = mock( XmlBeansRestParamsTestPropertyHolder.class );
		when( resource.getParams() ).thenReturn( resourceParams );
		when( restRequest.getResource() ).thenReturn( resource );

		RestMethod restMethod = mock( RestMethod.class );
		RestParamsPropertyHolder methodParams = mock( XmlBeansRestParamsTestPropertyHolder.class );
		when( restMethod.getParams() ).thenReturn( methodParams );
		when( restRequest.getRestMethod() ).thenReturn( restMethod );

		when( params.getModelItem() ).thenReturn( restRequest );

	}
}
