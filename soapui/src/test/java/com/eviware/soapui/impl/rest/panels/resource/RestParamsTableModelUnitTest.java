package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.model.testsuite.TestProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation.METHOD;
import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle.QUERY;
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
	public static final String PARAM_NAME = "ParamName";

	private RestParamsTableModel restParamsTableModel;
	private RestParamsPropertyHolder params;
	private RestParamProperty param;

	@Before
	public void setUp()
	{
		params = Mockito.mock( RestParamsPropertyHolder.class );
		param = mock( RestParamProperty.class );
		when( param.getParamLocation() ).thenReturn( NewRestResourceActionBase.ParamLocation.METHOD );
		when( param.getName() ).thenReturn( PARAM_NAME );
		when( params.getProperty(PARAM_NAME) ).thenReturn( param );
		when( params.getPropertyIndex( PARAM_NAME ) ).thenReturn( 0 );
		when( params.size() ).thenReturn( 1 );

		Map<String, TestProperty> properties = new HashMap<String, TestProperty>(  );
		properties.put( PARAM_NAME, param );
		when (params.getProperties()).thenReturn( properties );

		restParamsTableModel = new RestParamsTableModel( params );
	}


	@Test
	public void givenModelWithParamsWhenSetParamsThenModelShouldBeRemovedAsListenerAndAddedAgain()
	{
		restParamsTableModel.setParams( params );
		verify( params, times( 2 ) ).addTestPropertyListener( restParamsTableModel );
		verify( params, times( 1 ) ).removeTestPropertyListener( restParamsTableModel );
	}

	@Test
	public void givenModelWithParamsWhenReleaseItShouldRemoveItselfAsListenerFromParams()
	{
		restParamsTableModel.release();
		verify( params, times( 1 ) ).addTestPropertyListener( restParamsTableModel );
		verify( params, times( 1 ) ).removeTestPropertyListener( restParamsTableModel );

	}

	@Test
	public void givenModelWithParamsWhenSetValueThenShouldSetValueToProperty()
	{
		String value = "New value";
		restParamsTableModel.setValueAt( value, 0, VALUE_COLUMN_INDEX );
		verify( param, times( 1 ) ).setValue( value );
	}

	@Test
	public void givenModelWithParamsWhenSetNameThenShouldRenameProperty()
	{
		String value = "New Name";
		restParamsTableModel.setValueAt( value, 0, NAME_COLUMN_INDEX );
		verify( params, times( 1 ) ).renameProperty(PARAM_NAME, value );
	}

	@Test
	public void givenModelWithParamsWhenSetStyleThenShouldStyleToProperty()
	{
		restParamsTableModel.setValueAt( QUERY, 0, STYLE_COLUMN_INDEX );
		verify( param, times( 1 ) ).setStyle( QUERY );
	}

	@Test
	public void givenModelWithParamsWhenSetLocationAndGetLocationThenShouldReturnSameValue()
	{
		restParamsTableModel.setValueAt( METHOD, 0, LOCATION_COLUMN_INDEX );
		verify( param, times( 1 ) ).setParamLocation( METHOD );
	}
}
