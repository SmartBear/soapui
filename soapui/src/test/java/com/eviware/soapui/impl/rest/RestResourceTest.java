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
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.eviware.soapui.utils.CommonMatchers.anEmptyArray;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class RestResourceTest
{

	private RestResource restResource;
	private RestService parentService;

	@Before
	public void setUp() throws XmlException, IOException, SoapUIException
	{
		WsdlProject project = new WsdlProject();
		RestService restService = ( RestService )project.addNewInterface( "Test", RestServiceFactory.REST_TYPE );
		restResource = restService.addNewResource( "Resource", "/test" );
		parentService = restResource.getService();
	}

	@Test
	public void shouldGetTemplateParams() throws Exception
	{
		assertThat( restResource.getDefaultParams(), is( anEmptyArray() ) );

		restResource.setPath( "/{id}/test" );
		assertThat( restResource.getDefaultParams(), is( anEmptyArray() ) );
		assertThat( restResource.getFullPath(), is("/{id}/test") );

		RestResource subResource = restResource.addNewChildResource( "Child", "{test}/test" );
		assertThat( subResource.getFullPath(), is( "/{id}/test/{test}/test" ) );
	}

	@Test
	public void ignoresMatrixParamsOnPath() throws Exception
	{
		String matrixParameterString = ";Param2=matrixValue2;address=16";
		restResource.setPath( "/maps/api/geocode/xml" + matrixParameterString );

		assertThat( restResource.getFullPath(), not( containsString( matrixParameterString ) ) );

		String childResourceParameterString = ";ver=2";
		RestResource childResource = restResource.addNewChildResource( "Child", "{test}/test/version" + childResourceParameterString );
		assertThat( childResource.getPath(), not(containsString( childResourceParameterString )) );

		assertThat( childResource.getFullPath(), not(containsString( matrixParameterString )) );
		assertThat( childResource.getFullPath(), not(containsString( childResourceParameterString )) );
	}

	@Test
	public void ignoresMatrixParamsWithoutValueOnPath() throws Exception
	{
		String matrixParameterString = ";Param2=1;address=";
		restResource.setPath( "/maps/api/geocode/xml" + matrixParameterString );

		assertThat( restResource.getFullPath(), not( containsString( matrixParameterString ) ) );

		String childResourceParameterString = ";ver=";
		RestResource childResource = restResource.addNewChildResource( "Child", "{test}/test/version" + childResourceParameterString );
		assertThat( childResource.getPath(), not(containsString( childResourceParameterString )) );

		assertThat( childResource.getFullPath(), not(containsString( matrixParameterString )) );
		assertThat( childResource.getFullPath(), not(containsString( childResourceParameterString )) );
	}

	@Test
	public void listensToChangesInConfiguredParameters() throws Exception
	{
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
	public void removesFormerTemplateParametersFromPath() throws Exception
	{
		RestResourceConfig config = RestResourceConfig.Factory.newInstance();
		RestParametersConfig restParametersConfig = config.addNewParameters();
		RestParameterConfig parameterConfig = restParametersConfig.addNewParameter();
		String parameterName = "theName";
		parameterConfig.setName( parameterName );
		parameterConfig.setStyle( RestParameterConfig.Style.Enum.forInt( RestParamsPropertyHolder.ParameterStyle.TEMPLATE.ordinal() ) );
		config.setPath( "/actual_path" );

		RestResource restResource = new RestResource( parentService, config );
		restResource.getParams().getProperty( parameterName ).setStyle( RestParamsPropertyHolder.ParameterStyle.QUERY );
		assertThat( restResource.getPath(), not( containsString( parameterName ) ) );

	}

	@Test
	public void considersBasePathWhenAddingTemplateParameter() throws Exception
	{
		String parameterName = "version";
		String parameterInPath = "{" + parameterName + "}";
		parentService.setBasePath( "/base/" + parameterInPath);
		RestParamProperty parameter = restResource.addProperty( parameterName );
		parameter.setStyle( RestParamsPropertyHolder.ParameterStyle.TEMPLATE );

		assertThat(restResource.getPath(), not(containsString( parameterInPath )));
	}

	@Test
	public void deletingResourceDeletesAllChildResources() throws Exception
	{
		// restResource -> childResourceA, childResourceB
		RestResource childResourceA = restResource.addNewChildResource( "ChildA", "/childPathA" );
		restResource.addNewChildResource( "ChildB", "/childPathB" );

		// childResourceA -> grandChildAA, grandChildAB
		RestResource grandChildAA = childResourceA.addNewChildResource( "GrandChildAA", "/grandChildPathAA" );
		childResourceA.addNewChildResource( "GrandChildAB", "/grandChildPathAB" );

		// grandChildAA -> greatGrandChildAAA
		grandChildAA.addNewChildResource( "GreatGrandChildAAA", "/greatGrandChildAAA" );

		restResource.deleteResource( childResourceA );

		assertThat( restResource.getChildResourceList().size(), is( 1 ) ); // ensure it does not delete the sibling
		assertThat( childResourceA.getChildResourceList().size(), is( 0 ) );
		assertThat( grandChildAA.getChildResourceList().size(), is( 0 ) );

	}

}
