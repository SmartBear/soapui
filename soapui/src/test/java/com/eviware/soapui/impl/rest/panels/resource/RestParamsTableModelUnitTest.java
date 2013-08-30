package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation.METHOD;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
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

	private RestParamsTableModel restParamsTableModel;
	private RestParamsPropertyHolder params;

	@Before
	public void setUp()
	{
		params = Mockito.mock( RestParamsPropertyHolder.class );
		restParamsTableModel = new RestParamsTableModel( params, NewRestResourceActionBase.ParamLocation.RESOURCE );
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
		RestParamProperty param = mock( RestParamProperty.class );
		when( params.getPropertyAt( 0 ) ).thenReturn( param );
		String value = "New value";
		restParamsTableModel.setValueAt( value, 0, VALUE_COLUMN_INDEX );
		verify( param, times( 1 ) ).setValue( value );
	}

	@Test
	public void givenModelWithParamsWhenSetNameThenShouldRenameProperty()
	{
		RestParamProperty param = mock( RestParamProperty.class );
		String name = "Name";
		when( param.getName() ).thenReturn( name );
		when( params.getPropertyAt( 0 ) ).thenReturn( param );
		String value = "New Name";
		restParamsTableModel.setValueAt( value, 0, NAME_COLUMN_INDEX );
		verify( params, times( 1 ) ).renameProperty(name, value );
	}

	@Test
	public void givenModelWithParamsWhenSetStyleThenShouldStyleToProperty()
	{
		RestParamProperty param = mock( RestParamProperty.class );
		when( params.getPropertyAt( 0 ) ).thenReturn( param );
		String value = "New value";
		restParamsTableModel.setValueAt( value, 0, STYLE_COLUMN_INDEX );
		verify( param, times( 1 ) ).setValue( value );
	}

	@Test
	public void givenModelWithParamsWhenSetLocationAndGetLocationThenShouldReturnSameValue()
	{
		RestParamProperty param = mock( RestParamProperty.class );
		when( params.getPropertyAt( 0 ) ).thenReturn( param );
		restParamsTableModel.setValueAt( METHOD, 0, LOCATION_COLUMN_INDEX );
		Assert.assertThat( restParamsTableModel.getParamLocationAt( 0 ), Is.is(METHOD) );
	}
}
