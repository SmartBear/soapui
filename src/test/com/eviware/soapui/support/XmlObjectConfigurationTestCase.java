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


package com.eviware.soapui.support;

import junit.framework.TestCase;

import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;

public class XmlObjectConfigurationTestCase extends TestCase
{
	public void testConfiguration() throws Exception
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( "testFloat", (float)0.123 );
		builder.add( "testInt", 123 );
		builder.add( "testString", "1234" );
		
		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( builder.finish() );
		assertEquals( (float)0.123, reader.readFloat( "testFloat", 0));
		assertEquals( 123, reader.readInt( "testInt", 0 ));
		assertEquals( "1234", reader.readString( "testString", null ));
	}
}
