package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class WsdlProjectTest
{
	private WsdlProject project;

	@Before
	public void setUp() throws Exception
	{
		String fileName = SoapUI.class.getResource( "/soapui-projects/BasicMock-soapui-4.6.3-Project.xml" ).toURI().toString();
		project = new WsdlProject( fileName );
	}

	@Test
	public void shouldSaveRestMockServices() throws Exception
	{
		String expectedName = "Teh Awesome Mock Service";
		project.addNewRestMockService( expectedName );

		WsdlProject reloadedProject = saveAndReloadProject( project );

		RestMockService restMockService = reloadedProject.getRestMockServiceByName( expectedName );
		assertThat( restMockService, notNullValue() );
		assertThat( restMockService.getName(), is(expectedName) );
	}

	protected WsdlProject saveAndReloadProject(WsdlProject project) throws Exception
	{
		File tempFile = File.createTempFile( "soapuitemptestfile", ".xml" );
		project.saveIn( tempFile );
		return new WsdlProject( tempFile.toURI().toString() );
	}
}
