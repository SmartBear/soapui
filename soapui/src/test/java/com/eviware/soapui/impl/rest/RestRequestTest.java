package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.StringListConfig;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import static com.eviware.soapui.utils.ModelItemMatchers.hasARestParameterNamed;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

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
	public void migratesAcceptValue() throws Exception
	{
		RestRequestConfig requestConfig = RestRequestConfig.Factory.newInstance();
		String contentType = "image/jpeg";
		requestConfig.setAccept( contentType );
		request = new RestRequest( ModelItemFactory.makeRestMethod(), requestConfig, false);

		assertThat(request.getRequestHeaders().get(RestRequest.ACCEPT_HEADER_NAME), hasItem( contentType ));
		assertThat(requestConfig.getAccept(), is(nullValue()));
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
		addRequestParameter( lastParameterName, "someOtherValue" );
		request.getParams().moveProperty( lastParameterName, 0 );

		StringListConfig parameterOrder = request.getConfig().getParameterOrder();
		assertThat(parameterOrder, is(notNullValue()));
		assertThat(parameterOrder.getEntryArray( 0 ), is(lastParameterName));
	}

	private RestParamProperty addRequestParameter( String name, String value )
	{
		RestParamProperty parameter = request.getParams().addProperty( name );
		parameter.setValue( value );
		return parameter;
	}
}
