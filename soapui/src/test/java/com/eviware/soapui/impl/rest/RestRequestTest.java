package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.StringToStringMapConfig;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import static com.eviware.soapui.utils.ModelItemMatchers.hasARestParameterNamed;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for RestRequest
 */
public class RestRequestTest
{

	private static final String PARAMETER_NAME = "paramName";
	private static final String PARAMETER_VALUE = "paramValue";

	private RestRequest request;


	@Before
	public void setUp() throws Exception
	{
		 request = new RestRequest( ModelItemFactory.makeRestMethod(), RestRequestConfig.Factory.newInstance(), false);
	}

	@Test
	public void holdsAndReturnsParameters()
	{
		addRequestParameter(PARAMETER_NAME, PARAMETER_VALUE);
		assertThat( request, hasARestParameterNamed(PARAMETER_NAME).withValue( PARAMETER_VALUE ));
	}

	@Test
	public void retainsParameterValueWhenChangingItsLevel()
	{
		RestParamProperty parameter = request.getParams().addProperty( PARAMETER_NAME );
		parameter.setValue( PARAMETER_VALUE );
		parameter.setParamLocation( NewRestResourceActionBase.ParamLocation.RESOURCE );
		RestParamProperty returnedParameter = request.getParams().getProperty( PARAMETER_NAME );
		returnedParameter.setParamLocation( NewRestResourceActionBase.ParamLocation.METHOD );

		assertThat( request, hasARestParameterNamed( PARAMETER_NAME ).withValue( PARAMETER_VALUE ) );
	}

	@Test
	public void updatesConfigWhenParameterOrderIsModified() throws Exception
	{
		addRequestParameter( "someName", "someValue" );
		String lastParameterName = "otherName";
		addRequestParameter( lastParameterName, "someValue" );
		request.getParams().moveProperty( lastParameterName, 0 );

		StringToStringMapConfig parameterOrder = request.getConfig().getParameterOrder();
		assertThat(parameterOrder, is(notNullValue()));
		assertThat(getEntryValue(parameterOrder, lastParameterName), is("0"));
	}

	private String getEntryValue( StringToStringMapConfig parameterOrder, String key )
	{
		for( StringToStringMapConfig.Entry entry : parameterOrder.getEntryList() )
		{
			if (entry.getKey().equals(key))
			{
				return entry.getValue();
			}
		}
		return null;
	}

	private RestParamProperty addRequestParameter( String name, String value )
	{
		RestParamProperty parameter = request.getParams().addProperty( name );
		parameter.setValue( value );
		return parameter;
	}
}
