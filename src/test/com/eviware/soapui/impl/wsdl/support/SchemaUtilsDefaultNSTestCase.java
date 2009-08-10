/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import junit.framework.TestCase;

import java.io.File;

public class SchemaUtilsDefaultNSTestCase extends TestCase
{
	public void testLoadNS() throws Exception
	{
		SoapUI.initDefaultCore();
		File file = new File( "src\\test-resources\\chameleon\\chameleon.wsdl");
		SchemaUtils.loadSchemaTypes( file.toURL().toString(),	new UrlWsdlLoader(file.toURL().toString()) );
	}
}
