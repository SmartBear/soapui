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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.RestParameterConfig;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class RestResourceTest
{

	private RestResource restResource;

	@Before
	public void setUp() throws XmlException, IOException, SoapUIException
	{
		WsdlProject project = new WsdlProject();
		RestService restService = ( RestService )project.addNewInterface( "Test", RestServiceFactory.REST_TYPE );
		restResource = restService.addNewResource( "Resource", "/test" );
	}

	@Test
	public void shouldGetTemplateParams() throws Exception
	{
		assertEquals( restResource.getDefaultParams().length, 0 );

		restResource.setPath( "/{id}/test" );
		assertEquals( restResource.getDefaultParams().length, 0 );
		assertEquals( "/{id}/test", restResource.getFullPath() );

		RestResource subResource = restResource.addNewChildResource( "Child", "{test}/test" );
		assertEquals( "/{id}/test/{test}/test", subResource.getFullPath() );
	}

	@Test
	public void shouldIgnoreMatrixParamsOnPath() throws Exception
	{
		restResource.setPath( "/maps/api/geocode/xml;Param2=matrixValue2;address=16" );

		// asserts full path does not have the matrix params
		assertEquals( "/maps/api/geocode/xml", restResource.getFullPath() );

		RestResource subResource = restResource.addNewChildResource( "Child", "{test}/test/version;ver=2" );

		// asserts child resource's path does not have the matrix params
		assertEquals( "{test}/test/version", subResource.getPath() );

		// asserts child resource's full path does not have the matrix params
		assertEquals( "/maps/api/geocode/xml/{test}/test/version", subResource.getFullPath() );
	}

	@Test
	public void shouldListenToChangesInConfiguredParameters() throws Exception
	{
		RestService parentService = restResource.getService();
		RestResourceConfig config = RestResourceConfig.Factory.newInstance();
		RestParametersConfig restParametersConfig = config.addNewParameters();
		RestParameterConfig parameterConfig = restParametersConfig.addNewParameter();
		String parameterName = "theName";
		parameterConfig.setName( parameterName );
		parameterConfig.setStyle( RestParameterConfig.Style.Enum.forInt( RestParamsPropertyHolder.ParameterStyle.QUERY.ordinal()) );
		config.setPath( "/actual_path");

		RestResource restResource = new RestResource( parentService, config );
		restResource.getParams().getProperty( parameterName ).setStyle( RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
		assertThat( restResource.getPath(), containsString(parameterName ));

	}

	@Test
	public void shouldRemoveFormerTemplateParametersFromPath() throws Exception
	{
		RestService parentService = restResource.getService();
		RestResourceConfig config = RestResourceConfig.Factory.newInstance();
		RestParametersConfig restParametersConfig = config.addNewParameters();
		RestParameterConfig parameterConfig = restParametersConfig.addNewParameter();
		String parameterName = "theName";
		parameterConfig.setName( parameterName );
		parameterConfig.setStyle( RestParameterConfig.Style.Enum.forInt( RestParamsPropertyHolder.ParameterStyle.TEMPLATE.ordinal() ) );
		config.setPath( "/actual_path");

		RestResource restResource = new RestResource( parentService, config );
		restResource.getParams().getProperty( parameterName ).setStyle( RestParamsPropertyHolder.ParameterStyle.QUERY);
		assertThat( restResource.getPath(), not( containsString( parameterName ) ));

	}

}
