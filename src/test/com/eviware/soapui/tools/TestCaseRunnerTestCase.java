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


package com.eviware.soapui.tools;

import com.eviware.soapui.support.Tools;

import junit.framework.TestCase;

public class TestCaseRunnerTestCase extends TestCase
{
    public void testReplaceHost() throws Exception
    {
   	 assertEquals( "http://test2:8080/test", 
   			 Tools.replaceHost( "http://test:8080/test", "test2" ));

   	 assertEquals( "http://test2/test", 
   				 Tools.replaceHost( "http://test/test", "test2" ));

   	 assertEquals( "http://test2:8080", 
   				 Tools.replaceHost( "http://test:8080", "test2" ));

   	 assertEquals( "http://test2", 
   				 Tools.replaceHost( "http://test", "test2" ));
   	 
   	 assertEquals( "http://test2:8081", 
   				 Tools.replaceHost( "http://test:8080", "test2:8081" ));
   	 
   	 assertEquals( "http://test2:8081/test", 
   				 Tools.replaceHost( "http://test:8080/test", "test2:8081" ));
    }
}
