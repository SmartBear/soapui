package com.eviware.soapui.model.propertyexpansion.resolvers;

import junit.framework.TestCase;

public class ResolverUtilsTestCase extends TestCase
{
   public void testExtractXPathPropertyValue() throws Exception
   {
   	assertEquals( "audi", ResolverUtils.extractXPathPropertyValue("<test><bil>audi</bil></test>", "//bil"));
   	assertEquals( "<test><bil>audi</bil><bil>bmw</bil></test>", 
   			ResolverUtils.extractXPathPropertyValue("<test><bil>audi</bil><bil>bmw</bil></test>", "//test"));
   	assertEquals( "audi", 
   			ResolverUtils.extractXPathPropertyValue("<test><bil>audi</bil><bil>bmw</bil></test>", "//test/bil[1]"));
   }
}
