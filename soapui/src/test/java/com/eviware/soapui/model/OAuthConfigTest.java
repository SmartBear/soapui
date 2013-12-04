/*
 * soapUI, copyright (C) 2004-2013 smartbear.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 *
 */
package com.eviware.soapui.model;

import com.eviware.soapui.config.OAuthConfigConfig;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class OAuthConfigTest
{

	private WsdlProject project;
	private OAuthConfigConfig oAuthConfig;
	private String projectFileName = "OAuthTestProject.xml";

	@Before
	public void setUp() throws XmlException, IOException, SoapUIException
	{
		project = new WsdlProject();
		RestService restService = ( RestService )project.addNewInterface( "Test", RestServiceFactory.REST_TYPE );
		restService.addNewResource( "Resource", "/test" );

		oAuthConfig = OAuthConfigConfig.Factory.newInstance();
		oAuthConfig.setClientID( "google" );
		oAuthConfig.setAccessTokenURI("http://google.com/accessTokenURI");
		oAuthConfig.setAuthorizeURI( "http://google.com/auth" );
		oAuthConfig.setClientSecret( "XYSDKMLL" );
		oAuthConfig.setAccessToken( "ACDFECDSFKJFK#SDFSD8df" );
	}

	@After
	public  void tearDown()
	{
		File file = new File(projectFileName);
		file.delete();
	}

	@Test
	public void basicOAuthConfigIsSaved() throws Exception
	{
		project.getConfig().setOAuthConfig( oAuthConfig );
		project.saveAs( projectFileName );

		WsdlProject retrievedProject = new WsdlProject( projectFileName );

		assertThat( retrievedProject.getConfig().getOAuthConfig().getClientID(), is( oAuthConfig.getClientID() ) );
		assertThat( retrievedProject.getConfig().getOAuthConfig().getAccessTokenURI(), is( oAuthConfig.getAccessTokenURI() ) );
		assertThat( retrievedProject.getConfig().getOAuthConfig().getAuthorizeURI(), is( oAuthConfig.getAuthorizeURI() ) );
		assertThat( retrievedProject.getConfig().getOAuthConfig().getClientSecret(), is( oAuthConfig.getClientSecret() ) );
		assertThat( retrievedProject.getConfig().getOAuthConfig().getAccessToken(), is( oAuthConfig.getAccessToken() ) );

	}
}
