package com.eviware.soapui.impl.rest;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RestServiceTest
{

	private RestService restService;
	RestResource restResource;

	@Before
	public void setUp() throws XmlException, IOException, SoapUIException
	{
		WsdlProject project = new WsdlProject();
		restService = ( RestService )project.addNewInterface( "Test", RestServiceFactory.REST_TYPE );
		restResource = restService.addNewResource( "Resource", "/test" );
	}

	@Test
	public void deletingResourceDeletesAllChildResources() throws Exception
	{
		// restResource -> childResourceA
		RestResource childResourceA = restResource.addNewChildResource( "ChildA", "/childPath" );

		// childResourceA -> grandChildAA, grandChildAB
		RestResource grandChildAA = childResourceA.addNewChildResource( "GrandChildAA", "/grandChildPathAA" );
		childResourceA.addNewChildResource( "GrandChildAB", "/grandChildPathAB" );

		// grandChildAA -> greatGrandChildAAA
		grandChildAA.addNewChildResource( "GreatGrandChildAAA", "/greatGrandChildAAA" );

		restService.deleteResource( restResource );

		assertThat( restService.getChildren().size(), is( 0 ) );
		assertThat( restResource.getChildResourceList().size(), is( 0 ) );
		assertThat( childResourceA.getChildResourceList().size(), is( 0 ) );
		assertThat( grandChildAA.getChildResourceList().size(), is( 0 ) );

	}

	@Test
	public void deletingResourceDoesNotDeleteSiblings() throws Exception
	{
		Map<String, RestResource> expectedResourceList = restService.getResources();

		RestResource siblingResourceA = restService.addNewResource( "SiblingA", "/siblingPath" );

		assertThat( restService.getChildren().size(), is( 2 ) );

		restService.deleteResource( siblingResourceA );

		assertThat( restService.getChildren().size(), is( 1 ) );
		assertThat( restService.getResources(), is( expectedResourceList ) );

	}
}
