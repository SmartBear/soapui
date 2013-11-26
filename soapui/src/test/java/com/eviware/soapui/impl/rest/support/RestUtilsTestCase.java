/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.utils.ModelItemFactory;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;

import static com.eviware.soapui.utils.ModelItemFactory.makeRestRequest;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class RestUtilsTestCase
{

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( RestUtilsTestCase.class );
	}

	@Test
	public void extractsTemplateParams() throws Exception
	{
		String path = "/{id}/test/{test}/test";

		String[] params = RestUtils.extractTemplateParams( path );
		assertEquals( params.length, 2 );
		assertEquals( "id", params[0] );
		assertEquals( "test", params[1] );
	}

	@Test
	public void extractsTemplateParameterFromCurlyBrackets() throws Exception
	{
		String path = "/{id}/42";

		RestParamsPropertyHolder params = ModelItemFactory.makeRestRequest().getParams();
		String extractedPath = RestUtils.extractParams( path, params, true, RestUtils.TemplateExtractionOption.EXTRACT_TEMPLATE_PARAMETERS );
		assertThat( extractedPath, is( path ) );
		assertEquals( params.getPropertyCount(), 1 );
		RestParamProperty id = params.getProperty( "id" );
		assertThat( id.getStyle(), is( RestParamsPropertyHolder.ParameterStyle.TEMPLATE ) );
		assertThat( id.getValue(), is( "id" ) );
	}

	@Test
	public void extractsTemplateParameterFromInteger() throws Exception
	{
		String path = "/{id}/42";

		RestParamsPropertyHolder params = ModelItemFactory.makeRestRequest().getParams();
		String extractedPath = RestUtils.extractParams( path, params, true, RestUtils.TemplateExtractionOption.IGNORE_TEMPLATE_PARAMETERS );
		assertThat( extractedPath, is( "/{id}/42" ) );
		assertEquals( params.getPropertyCount(), 0 );
	}

	@Test
	public void extractsEmbeddedTemplateParameters() throws Exception
	{
		String path = "/conversation/date-{date}/time-{time}?userId=1234";

		RestParamsPropertyHolder params = ModelItemFactory.makeRestRequest().getParams();
		String extractedPath = RestUtils.extractParams( path, params, true, RestUtils.TemplateExtractionOption.EXTRACT_TEMPLATE_PARAMETERS );
		assertThat( extractedPath, is( "/conversation/date-{date}/time-{time}" ) );
		assertThat( params.getProperty("date").getStyle(), is( RestParamsPropertyHolder.ParameterStyle.TEMPLATE));
		assertThat( params.getProperty("time").getStyle(), is( RestParamsPropertyHolder.ParameterStyle.TEMPLATE));
	}

	@Test
	public void expandsRestRequestPathsWithoutTemplateParameters() throws Exception
	{
		RestRequest restRequest = makeRestRequest();
		restRequest.getResource().setPath( "/the/path" );
		addParameter( restRequest, RestParamsPropertyHolder.ParameterStyle.QUERY, "queryName", "queryValue" );
		addParameter( restRequest, RestParamsPropertyHolder.ParameterStyle.MATRIX, "matrixName", "theMatrixValue" );
		addParameter( restRequest, RestParamsPropertyHolder.ParameterStyle.TEMPLATE, "templateName", "templateValue" );
		addParameter( restRequest, RestParamsPropertyHolder.ParameterStyle.MATRIX, "matrixName2", "theMatrixValue2" );
		addParameter( restRequest, RestParamsPropertyHolder.ParameterStyle.QUERY, "queryName2", "queryValue2");

		assertThat(RestUtils.expandPath( "/the/path", restRequest.getParams(), restRequest ),
				is("/the/path;matrixName=theMatrixValue;matrixName2=theMatrixValue2?queryName=queryValue&queryName2=queryValue2"));
	}

	@Test
	public void expandsRestRequestPathsWithTemplateParameter() throws Exception
	{
		RestRequest restRequest = makeRestRequest();
		String templateParameterName = "templateName";
		String templateParameterValue = "templateValue";
		restRequest.getResource().setPath( "/the/{" + templateParameterName + "}/path" );
		addParameter( restRequest, RestParamsPropertyHolder.ParameterStyle.TEMPLATE, templateParameterName, templateParameterValue );

		assertThat(RestUtils.expandPath( restRequest.getResource().getFullPath(), restRequest.getParams(), restRequest ),
				is( "/the/" + templateParameterValue + "/path" ));
	}

	private void addParameter( RestRequest restRequest, RestParamsPropertyHolder.ParameterStyle style, String name, String value )
	{
		RestParamsPropertyHolder params = restRequest.getParams();
		RestParamProperty restParamProperty = params.addProperty( name );
		restParamProperty.setStyle( style );
		restParamProperty.setValue( value );
	}
}
