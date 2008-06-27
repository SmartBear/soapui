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
package com.eviware.soapui.support.xml;

import junit.framework.TestCase;

public class XPathDataTestCase extends TestCase
{
   public void test1() throws Exception
   {
      XPathData data = new XPathData("//in/name", false);
      assertEquals("//in/name", data.getFullPath());
   }
   
   public void testText() throws Exception
   {
      XPathData data = new XPathData("//in/name/text()", false);
      assertEquals("//in/name/text()", data.getFullPath());
   }
   
   public void testCount() throws Exception
   {
      XPathData data = new XPathData("count(//in/name)", false);
      assertEquals("count(//in/name)", data.getFullPath());
      assertEquals("count", data.getFunction());
   }
   
   public void testCountWithNamespace() throws Exception
   {
      String namespace = "declare namespace tes='http://www.example.org/TestService/';\n";
      XPathData data = new XPathData(namespace + "count(//in/name)", false);
      assertEquals(namespace + "count(//in/name)", data.getFullPath());
      assertEquals("count", data.getFunction());
   }
   
   public void testStripXPath() throws Exception
   {
      checkStripXPath("//abc", "//abc");
      checkStripXPath("//abc", "//abc[1]");
      checkStripXPath("//abc", "//abc[a > 3]");
      checkStripXPath("//abc", "//abc/text()");
      checkStripXPath("//abc", "count(//abc)");
      checkStripXPath("//abc", "count( //abc)");
      checkStripXPath("//abc", "exists(//abc)");
      checkStripXPath("//abc", "exists( //abc)");

      String ns = "declare namespace ns1='http://abc.com';\n";
      checkStripXPath(ns + "//abc", ns + "//abc[1]");
      checkStripXPath(ns + "//abc", ns + "//abc/text()");
      checkStripXPath(ns + "//abc", ns + "exists(//abc)");
   }
   
   private void checkStripXPath(String expected, String org)
   {
      XPathData xpath = new XPathData(org, true);
      xpath.strip();
      assertEquals(expected, xpath.getXPath());
   }
   
   public void testReplaceNameInPathOrQuery() throws Exception
   {
   	String exp = "//test:test/bil[@name='test ']/@test > 0 and count(//test[bil/text()='test'] = 5";
   	
   	assertEquals( "//test:test/bila[@name='test ']/@test > 0 and count(//test[bila/text()='test'] = 5", 
   				XmlUtils.replaceNameInPathOrQuery( exp, "bil", "bila" ));
   }
}
