/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
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

package com.eviware.soapui.support.xml;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XPathDataTest {

    @Test
    public void test1() throws Exception {
        XPathData data = new XPathData("//in/name", false);
        assertEquals("//in/name", data.getFullPath());
    }

    @Test
    public void testText() throws Exception {
        XPathData data = new XPathData("//in/name/text()", false);
        assertEquals("//in/name/text()", data.getFullPath());
    }

    @Test
    public void testCount() throws Exception {
        XPathData data = new XPathData("count(//in/name)", false);
        assertEquals("count(//in/name)", data.getFullPath());
        assertEquals("count", data.getFunction());
    }

    @Test
    public void testCountWithNamespace() throws Exception {
        String namespace = "declare namespace tes='http://www.example.org/TestService/';\n";
        XPathData data = new XPathData(namespace + "count(//in/name)", false);
        assertEquals(namespace + "count(//in/name)", data.getFullPath());
        assertEquals("count", data.getFunction());
    }

    @Test
    public void testStripXPath() throws Exception {
        assertEquals("//abc", checkStripXPath("//abc"));
        assertEquals("//abc", checkStripXPath("//abc[1]"));
        assertEquals("//abc", checkStripXPath("//abc[a > 3]"));
        assertEquals("//abc", checkStripXPath("//abc/text()"));
        assertEquals("//abc", checkStripXPath("count(//abc)"));
        assertEquals("//abc", checkStripXPath("count( //abc)"));
        assertEquals("//abc", checkStripXPath("exists(//abc)"));
        assertEquals("//abc", checkStripXPath("exists( //abc)"));

        String ns = "declare namespace ns1='http://abc.com';\n";
        assertEquals(ns + "//abc", checkStripXPath(ns + "//abc[1]"));
        assertEquals(ns + "//abc", checkStripXPath(ns + "//abc/text()"));
        assertEquals(ns + "//abc", checkStripXPath(ns + "exists(//abc)"));
    }

    private String checkStripXPath(String org) {
        XPathData xpath = new XPathData(org, true);
        xpath.strip();
        return xpath.getXPath();
    }

    @Test
    public void testReplaceNameInPathOrQuery() throws Exception {
        String exp = "//test:test/bil[@name='test ']/@test > 0 and count(//test[bil/text()='test'] = 5";

        assertEquals("//test:test/bila[@name='test ']/@test > 0 and count(//test[bila/text()='test'] = 5",
                XmlUtils.replaceNameInPathOrQuery(exp, "bil", "bila"));
    }
}
