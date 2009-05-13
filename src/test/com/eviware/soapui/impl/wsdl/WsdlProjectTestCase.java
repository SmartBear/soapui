/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.support.TestCaseWithJetty;

public class WsdlProjectTestCase extends TestCaseWithJetty
{
	public void testComplexLoad() throws Exception
	{
		WsdlProject project = new WsdlProject();
		WsdlInterface[] wsdls = WsdlImporter.importWsdl(project,
				"http://localhost:8082/test8/TestService.wsdl");

		assertEquals(1, wsdls.length);
	}
	
	public void testClasspathLoad() throws Exception
	{
		String str = SoapUI.class.getResource( "/sample-soapui-project.xml" ).toURI().toString();
		
		WsdlProject project = new WsdlProject( str );
	}

	public void testInit() throws Exception
	{
		assertTrue( new WsdlProject().isCacheDefinitions() );
	}
	
	
//	public void testImport() throws Exception
//	{
//		String url = "http://queue.amazonaws.com/doc/2006-04-01/QueueService.wsdl";
//
//		WsdlProject project = new WsdlProject();
//
//		// import amazon wsdl
//		WsdlInterface[] result = project.importWsdl(url, true);
//
//		assertEquals(2, result.length);
//	}
	/*
	public void testImport2() throws Exception
	{
		String url = "file:forumwsdl/invoke.wsdl";

		SoapUI.getSettings().setString( ProxySettings.HOST, "intra0.frec.bull.fr" );
      SoapUI.getSettings().setString(ProxySettings.PORT, "8080" );
      SoapUI.getSettings().setString(ProxySettings.EXCLUDES, "localhost" ); 
		
		WsdlProject project = new WsdlProject();

		// import amazon wsdl
		WsdlInterface[] result = project.importWsdl(url, true);

		assertEquals(1, result.length);
	}*/
}
