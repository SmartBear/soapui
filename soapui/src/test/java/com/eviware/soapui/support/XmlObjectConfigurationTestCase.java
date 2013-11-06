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

package com.eviware.soapui.support;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;

public class XmlObjectConfigurationTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( XmlObjectConfigurationTestCase.class );
	}

	@Test
	public void testConfiguration() throws Exception
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( "testFloat", ( float )0.123 );
		builder.add( "testInt", 123 );
		builder.add( "testString", "1234" );

		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( builder.finish() );
		assertEquals( ( float )0.123, reader.readFloat( "testFloat", 0 ), 0 );
		assertEquals( 123, reader.readInt( "testInt", 0 ) );
		assertEquals( "1234", reader.readString( "testString", null ) );
	}
}
