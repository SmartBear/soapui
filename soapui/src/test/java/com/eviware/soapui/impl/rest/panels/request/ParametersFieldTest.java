package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.utils.ModelItemFactory;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import static org.junit.Assert.assertThat;

/**
 * Unit tests for the ParameterField class
 */
public class ParametersFieldTest
{
	private static final String MATRIX_PARAM_NAME = "TheMatrix";
	private static final String MATRIX_PARAM_VALUE = "Blue_pill";
	private static final String QUERY_PARAM_NAME = "query";
	private static final String QUERY_PARAM_VALUE = "whopper";
	private RestRequest request;
	private ParametersField parametersField;

	@Before
	public void setUp() throws Exception
	{
		request = ModelItemFactory.makeRestRequest();
		RestParamProperty matrixParam = request.getParams().addProperty( MATRIX_PARAM_NAME );
		matrixParam.setStyle( RestParamsPropertyHolder.ParameterStyle.MATRIX );
		matrixParam.setValue( MATRIX_PARAM_VALUE );
		RestParamProperty queryParam = request.getParams().addProperty( QUERY_PARAM_NAME );
		queryParam.setStyle( RestParamsPropertyHolder.ParameterStyle.QUERY );
		queryParam.setValue( QUERY_PARAM_VALUE );

		parametersField = new ParametersField( request );
	}

	@Test
	public void displaysQueryParameter() throws Exception
	{
		//TODO: Replace with the new CommonMatchers.endsWith() method when this is merged back
		assertThat( parametersField.getText(), endsWith( "?" + QUERY_PARAM_NAME + "=" + QUERY_PARAM_VALUE ));
	}

	@Test
	public void displaysMatrixParameter() throws Exception
	{
		//TODO: Replace with the new CommonMatchers.startsWith() method when this is merged back
		assertThat( parametersField.getText(), startsWith( ";" + MATRIX_PARAM_NAME + "=" + MATRIX_PARAM_VALUE));
	}

	private Matcher<String> endsWith(final String suffix)
	{
		return new TypeSafeMatcher<String>()
		{
			@Override
			public boolean matchesSafely( String s )
			{
				return s.endsWith( suffix );
			}

			@Override
			public void describeTo( Description description )
			{
				description.appendText( "a string ending with " + suffix );
			}
		};
	}

	private Matcher<String> startsWith(final String prefix)
	{
		return new TypeSafeMatcher<String>()
		{
			@Override
			public boolean matchesSafely( String s )
			{
				return s.startsWith( prefix );
			}

			@Override
			public void describeTo( Description description )
			{
				description.appendText( "a string starting with " + prefix );
			}
		};
	}
}
