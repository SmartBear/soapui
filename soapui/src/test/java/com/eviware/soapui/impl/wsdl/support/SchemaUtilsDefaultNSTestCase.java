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

package com.eviware.soapui.impl.wsdl.support;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.junit.Test;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;

public class SchemaUtilsDefaultNSTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( SchemaUtilsDefaultNSTestCase.class );
	}

	@Test
	public void testLoadNS() throws Exception
	{
		SoapUI.initDefaultCore();
		String wsdlUriString = SchemaUtilsDefaultNSTestCase.class.getResource( "/chameleon/chameleon.wsdl" ).toString();
		SchemaTypeSystem sts = SchemaUtils.loadSchemaTypes( wsdlUriString, new UrlWsdlLoader( wsdlUriString ) );
		assertNotNull( sts );
	}
}
