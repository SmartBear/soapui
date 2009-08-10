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

import junit.framework.TestCase;

import com.eviware.soapui.support.Tools;

public class JoinRelativeUrlTestCase extends TestCase
{
   public void testJoin() throws Exception
   {
   	assertEquals( "http://test:8080/my/root/test.xsd", Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "test.xsd" ));
   	assertEquals( "http://test:8080/my/root/bu/test.xsd", Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "bu/test.xsd" ));
   	assertEquals( "http://test:8080/my/test.xsd", Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "../test.xsd" ));
   	assertEquals( "http://test:8080/my/root/test.xsd", Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "./test.xsd" ));
   	assertEquals( "http://test:8080/bil/test.xsd", Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "../../bil/test.xsd" ));
   	assertEquals( "http://test:8080/bil/test.xsd", Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "././../../bil/test/.././test.xsd" ));
   	assertEquals( "file:c:\\bil\\xsd\\test.xsd", Tools.joinRelativeUrl( "file:c:\\bil\\test.wsdl", "./xsd/test.xsd" ));
   	assertEquals( "file:c:\\bil\\xsd\\test.xsd", Tools.joinRelativeUrl( "file:c:\\bil\\test\\test\\test.wsdl", "..\\..\\xsd\\test.xsd" ));
   }
}
