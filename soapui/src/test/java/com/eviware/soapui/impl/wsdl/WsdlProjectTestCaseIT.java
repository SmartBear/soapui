/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.SaveStatus;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.support.JettyTestCaseBase;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.XFileDialogs;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class WsdlProjectTestCaseIT extends JettyTestCaseBase
{

	@Test
	public void testComplexLoad() throws Exception
	{
		replaceInFile( "test8/TestService.wsdl", "8082", "" + getPort() );
		WsdlProject project = new WsdlProject();
		WsdlInterface[] wsdls = WsdlImporter.importWsdl( project, "http://localhost:" + getPort() + "/test8/TestService.wsdl" );

		assertEquals( 1, wsdls.length );
	}

	@Test
	public void testClasspathLoad() throws Exception
	{
		String str = SoapUI.class.getResource( "/sample-soapui-project.xml" ).toURI().toString();

		assertNotNull( new WsdlProject( str ) );
	}

	public void testInit() throws Exception
	{
		assertTrue( new WsdlProject().isCacheDefinitions() );
	}

	@Test
	public void saveIsConsideredSuccessfulIfProjectIsClosed() throws XmlException, IOException, SoapUIException
	{
		WsdlProject project = new WsdlProject( "", null, true, false, "", null );
		project.setName( "ProjectName" );
		SaveStatus saved = project.save();
		assertThat( saved, equalTo( SaveStatus.SUCCESS ) );
	}

	@Test
	public void projectIsNotSavedIfSaveAsDialogIsCancelled() throws IOException
	{
		XFileDialogs fileDialogs = mock( XFileDialogs.class );
		UISupport.setFileDialogs( fileDialogs );
		WsdlProject project = new WsdlProject( null, null, true, true, "ProjectName", null );

		SaveStatus saveResult = project.save();

		assertNull( project.getPath() );
		assertThat( saveResult, equalTo( SaveStatus.CANCELLED ) );
	}

}
