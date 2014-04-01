/*
 * Copyright 2004-2014 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
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
