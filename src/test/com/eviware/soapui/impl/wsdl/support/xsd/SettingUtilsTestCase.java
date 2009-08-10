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
package com.eviware.soapui.impl.wsdl.support.xsd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import com.eviware.soapui.config.StringListConfig;

/**
 * 
 * @author lars
 */
public class SettingUtilsTestCase extends TestCase
{
   public void testqnameValues2String() throws Exception
   {
      LinkedHashMap<QName, String[]> valueMap = new LinkedHashMap<QName, String[]>();
      valueMap.put(new QName("x"), new String[] { "1", "2", "3" } );
      valueMap.put(new QName("ns2", "y"), new String[] { "a", "b", "c" } );
      
      ArrayList<String> expected = new ArrayList<String>();
      expected.add("x=1,2,3");
      expected.add("y@ns2=a,b,c");
      String result = SettingUtils.qnameValues2String(valueMap);
      StringListConfig config = StringListConfig.Factory.parse(result);
      assertEquals(expected, config.getEntryList());
      
      assertEquals(valueMap2String(valueMap),
            valueMap2String(SettingUtils.string2QNameValues(result)));
   }
   
   private static String valueMap2String(Map<QName, String[]> valueMap)
   {
      StringBuffer buf = new StringBuffer();
      for(QName qname : valueMap.keySet())
      {
         String[] values = valueMap.get(qname);
         buf.append(qname.toString()).append("=");
         buf.append(Arrays.toString(values));
         buf.append("\n");
      }
      return buf.toString();
   }
}
