package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import static com.eviware.soapui.utils.CommonMatchers.endsWith;
import static com.eviware.soapui.utils.CommonMatchers.startsWith;
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
	private ParametersField parametersField;

	@Before
	public void setUp() throws Exception
	{
		RestRequest request = ModelItemFactory.makeRestRequest();
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
		assertThat( parametersField.getText(), endsWith( "?" + QUERY_PARAM_NAME + "=" + QUERY_PARAM_VALUE ));
	}

	@Test
	public void displaysMatrixParameter() throws Exception
	{
		assertThat( parametersField.getText(), startsWith( ";" + MATRIX_PARAM_NAME + "=" + MATRIX_PARAM_VALUE));
	}

}
