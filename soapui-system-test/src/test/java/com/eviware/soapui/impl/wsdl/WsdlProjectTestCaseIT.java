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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.support.JettyTestCaseBase;
import org.junit.Test;

import static org.junit.Assert.*;

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
}
