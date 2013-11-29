package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.StringListConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.eviware.soapui.utils.ModelItemFactory.makeRestRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the RestRequestParamsPropertyHolder class.
 */
public class RestRequestParamsPropertyHolderTest
{

	private static final String FIRST_PARAMETER_NAME = "first";

	private RestRequestParamsPropertyHolder parametersHolder;
	private RestParamsPropertyHolder methodParams;
	private RestRequest restRequest;

	@Before
	public void setUp() throws SoapUIException
	{
		restRequest = makeRestRequest();
		methodParams = restRequest.getRestMethod().getParams();
		methodParams.addProperty( FIRST_PARAMETER_NAME );
		parametersHolder = ( RestRequestParamsPropertyHolder )restRequest.getParams();
	}

	@Test
	public void addsParameterAtEndOfList() throws Exception
	{
		String newParameterName = "newOne";
		parametersHolder.addProperty( newParameterName );

		assertThat( parametersHolder.getPropertyAt( 1 ).getName(), is( newParameterName ));
	}

	@Test
	public void canSetAndGetValueOfNewParameter() throws Exception
	{
		String newParameterName = "newOne";
		RestParamProperty restParamProperty = parametersHolder.addProperty( newParameterName );
		String parameterValue = "theValue";
		restParamProperty.setValue( parameterValue );

		assertThat( parametersHolder.getProperty( newParameterName ).getValue(), is( parameterValue ));
	}

	@Test
	public void movesParameterCorrectly() throws Exception
	{
		String newParameterName = "newOne";
		parametersHolder.addProperty( newParameterName );
		parametersHolder.moveProperty( newParameterName, 0 );

		assertThat( parametersHolder.getPropertyAt( 0 ).getName(), is( newParameterName ) );
		assertThat( restRequest.getConfig().isSetParameterOrder(), is(true));
		assertThat(restRequest.getConfig().getParameterOrder().getEntryArray( 0 ), is(newParameterName));
	}

	@Test
	public void movesParameterCorrectlyAfterLocationChange() throws Exception
	{
		String newParameterName = "newOne";
		RestParamProperty restParamProperty = parametersHolder.addProperty( newParameterName );
		parametersHolder.setParameterLocation(restParamProperty, NewRestResourceActionBase.ParamLocation.METHOD );
		parametersHolder.moveProperty( newParameterName, 0 );

		assertThat( parametersHolder.getPropertyAt( 0 ).getName(), is( newParameterName ) );
		assertThat( restRequest.getConfig().isSetParameterOrder(), is(true));
		assertThat(restRequest.getConfig().getParameterOrder().getEntryArray( 0 ), is(newParameterName));
	}

	@Test
	public void removesParameterCorrectly() throws Exception
	{
		parametersHolder.removeProperty( "first" );

		assertThat( parametersHolder.getPropertyCount(), is( 0 ) );
		assertThat( methodParams.getPropertyCount(), is(0));
	}

	@Test
	public void detectsParameterAdditionOnMethodLevel() throws Exception
	{
		String newParameterName = "newOne";
		methodParams.addProperty( newParameterName );

		assertThat( parametersHolder.getPropertyAt( 1 ).getName(), is( newParameterName ));
	}

	@Test
	public void detectsParameterRemovalOnMethodLevel() throws Exception
	{
		methodParams.removeProperty( "first" );

		assertThat( parametersHolder.getPropertyCount(), is(0));
	}

	@Test
	public void retainsOrderWhenRenamingParameter() throws Exception
	{
		String originalName = "secondParameter";
		parametersHolder.addProperty( originalName );
		parametersHolder.addProperty("lastParameter");
		String newName ="secondParameter_with_new_name";
		parametersHolder.renameProperty( originalName, newName );

		assertThat(parametersHolder.getPropertyIndex( newName ), is(1) );
	}

	@Test
	public void retainsOrderWhenChangingLocation() throws Exception
	{
		String originalName = "secondParameter";
		parametersHolder.addProperty( originalName );
		parametersHolder.setParameterLocation( parametersHolder.getProperty( FIRST_PARAMETER_NAME ),
				NewRestResourceActionBase.ParamLocation.METHOD );

		assertThat( parametersHolder.getPropertyIndex( FIRST_PARAMETER_NAME ), is( 0 ) );
	}

	@Test
	public void retainsLocationWhenRenamingParameter() throws Exception
	{
		String originalName = "secondParameter";
		parametersHolder.addProperty( originalName );
		String newName ="secondParameter_with_new_name";
		parametersHolder.renameProperty( originalName, newName );

		assertThat(parametersHolder.getProperty( newName ).getParamLocation(), is( NewRestResourceActionBase.ParamLocation.RESOURCE) );
	}

	@Test
	public void retainsValueWhenRenamingParameter() throws Exception
	{
		String originalName = "secondParameter";
		RestParamProperty parameter = parametersHolder.addProperty( originalName );
		String value = "ulysses";
		parameter.setValue( value );
		String newName ="secondParameter_with_new_name";
		parametersHolder.renameProperty( originalName, newName );

		assertThat(parametersHolder.getProperty( newName ).getValue(), is( value ) );
	}

	@Test
	public void detectsParameterNameChange() throws Exception
	{
		String newParameterName = "newOne";
		methodParams.renameProperty( FIRST_PARAMETER_NAME, newParameterName );

		String[] propertyNames = parametersHolder.getPropertyNames();
		assertThat( propertyNames[0], is( newParameterName ) );
		assertThat(parametersHolder.getPropertyAt( 0 ).getName(), is( newParameterName ));
	}

	@Test
	public void handlesParameterLevelChange() throws Exception
	{
		String newParameterName = "newOne";
		RestParamProperty restParamProperty = parametersHolder.addProperty( newParameterName );
		parametersHolder.setParameterLocation( restParamProperty, NewRestResourceActionBase.ParamLocation.METHOD );

		assertThat( methodParams.getPropertyAt( 1 ).getName(), is( newParameterName ));
	}

	@Test
	public void usesParameterOrderFromRestRequestConfiguration() throws Exception
	{
		RestRequestConfig requestConfig = buildRestRequestConfigWithParameters("first", "second", "third");
		RestMethod method = buildRestMethodWithParameters("third", "second", "first");
		RestRequest restRequest = new RestRequest( method, requestConfig, false );

		List<String> parameterNameList = Arrays.asList(restRequest.getPropertyNames());
		assertThat(parameterNameList, is(Arrays.asList("first", "second", "third")));

	}

	@Test
	public void removesNonExistentParameterFromOrderedList() throws Exception
	{
		RestRequestConfig requestConfig = buildRestRequestConfigWithParameters("first", "second", "third");
		RestMethod method = buildRestMethodWithParameters("third", "first");
		RestRequest restRequest = new RestRequest( method, requestConfig, false );

		List<String> parameterNameList = Arrays.asList(restRequest.getPropertyNames());
		assertThat(parameterNameList, is(Arrays.asList("first", "third")));

	}

	@Test
	public void addsNewParameterToOrderedList() throws Exception
	{
		RestRequestConfig requestConfig = buildRestRequestConfigWithParameters("first", "second", "third");
		RestMethod method = buildRestMethodWithParameters("third", "second", "newOne", "first");
		RestRequest restRequest = new RestRequest( method, requestConfig, false );

		List<String> parameterNameList = Arrays.asList(restRequest.getPropertyNames());
		assertThat(parameterNameList, is(Arrays.asList("first", "second", "third", "newOne")));

	}

	private RestMethod buildRestMethodWithParameters(String... parameterNames) throws SoapUIException
	{
		RestMethod method = ModelItemFactory.makeRestMethod();
		RestParamsPropertyHolder methodParams = method.getParams();
		for( String parameterName : parameterNames )
		{
			methodParams.addProperty( parameterName );
		}
		return method;
	}

	private RestRequestConfig buildRestRequestConfigWithParameters(String... parameterNames)
	{
		RestRequestConfig requestConfig = RestRequestConfig.Factory.newInstance();
		StringListConfig parameterOrder = StringListConfig.Factory.newInstance();
		for( String parameterName : parameterNames )
		{
			parameterOrder.addEntry( parameterName );
		}
		requestConfig.setParameterOrder( parameterOrder );
		return requestConfig;
	}
}
