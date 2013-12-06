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

import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.config.OAuth2ProfileContainerConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
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
	private OAuth2ProfileConfig oAuth2Profile;
	private OAuth2ProfileContainerConfig oAuth2ProfileContainerConfig;
	private String projectFileName = "OAuthTestProject.xml";

	@Before
	public void setUp() throws XmlException, IOException, SoapUIException
	{
		project = new WsdlProject();
		RestService restService = ( RestService )project.addNewInterface( "Test", RestServiceFactory.REST_TYPE );
		restService.addNewResource( "Resource", "/test" );



		oAuth2ProfileContainerConfig = OAuth2ProfileContainerConfig.Factory.newInstance();

		oAuth2Profile = OAuth2ProfileConfig.Factory.newInstance();
		oAuth2Profile.setClientID( "google" );
		oAuth2Profile.setAccessTokenURI( "http://google.com/accessTokenURI" );
		oAuth2Profile.setAuthorizeURI( "http://google.com/auth" );
		oAuth2Profile.setClientSecret( "XYSDKMLL" );
		oAuth2Profile.setAccessToken( "ACDFECDSFKJFK#SDFSD8df" );

		oAuth2ProfileContainerConfig.addNewOAuth2Profile();

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
		/*
		project.getConfig().setOAuthConfig( oAuth2Profile )
		project.saveAs( projectFileName );

		WsdlProject retrievedProject = new WsdlProject( projectFileName );

		assertThat( retrievedProject.getConfig().getOAuthConfig().getClientID(), is( oAuth2Profile.getClientID() ) );
		assertThat( retrievedProject.getConfig().getOAuthConfig().getAccessTokenURI(), is( oAuth2Profile.getAccessTokenURI() ) );
		assertThat( retrievedProject.getConfig().getOAuthConfig().getAuthorizeURI(), is( oAuth2Profile.getAuthorizeURI() ) );
		assertThat( retrievedProject.getConfig().getOAuthConfig().getClientSecret(), is( oAuth2Profile.getClientSecret() ) );
		assertThat( retrievedProject.getConfig().getOAuthConfig().getAccessToken(), is( oAuth2Profile.getAccessToken() ) );

		*/

	}
}
