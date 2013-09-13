package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for RestRequest
 */
public class RestRequestTest
{

	public static final String PARAMETER_NAME = "paramName";
	public static final String PARAMETER_VALUE = "paramValue";

	private RestRequest request;


	@Before
	public void setUp() throws Exception
	{
		 request = new RestRequest( ModelItemFactory.makeRestMethod(), RestRequestConfig.Factory.newInstance(), false);

	}

	@Test
	public void holdsAndReturnsParameters()
	{
		RestParamProperty parameter = request.getParams().addProperty( PARAMETER_NAME );
		parameter.setValue( PARAMETER_VALUE );
		RestParamProperty returnedParameter = request.getParams().getProperty( PARAMETER_NAME );
		assertThat( returnedParameter.getValue(), is( PARAMETER_VALUE ));
	}

	@Test
	public void retainsParameterValueWhenChangingItsLevel()
	{
		RestParamProperty parameter = request.getParams().addProperty( PARAMETER_NAME );
		parameter.setValue( PARAMETER_VALUE );
		parameter.setParamLocation( NewRestResourceActionBase.ParamLocation.RESOURCE );
		RestParamProperty returnedParameter = request.getParams().getProperty( PARAMETER_NAME );
		returnedParameter.setParamLocation( NewRestResourceActionBase.ParamLocation.METHOD );

		returnedParameter = request.getParams().getProperty( PARAMETER_NAME );
		assertThat( returnedParameter.getValue(), is( PARAMETER_VALUE ));
	}





}
