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

package com.eviware.soapui.support;

import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class XmlUtilsTest {

    @Test
    public void testGetElementIndex() throws Exception {
        Document dom = XmlUtils.parseXml("<h1><p>p1</p><h2>lkj</h2><p>p2</p></h1>");
        NodeList nl = dom.getDocumentElement().getElementsByTagName("p");

        assertEquals(1, XmlUtils.getElementIndex(nl.item(0)));
        assertEquals(2, XmlUtils.getElementIndex(nl.item(1)));
    }

    @Test
    public void testGetElementPath() throws Exception {
        Document dom = XmlUtils.parseXml("<h1><p>p1</p><h2>lkj</h2><p>p2</p></h1>");
        NodeList nl = dom.getDocumentElement().getElementsByTagName("p");

        assertEquals("/h1[1]/p[1]", XmlUtils.getElementPath((Element) nl.item(0)));
        assertEquals("/h1[1]/p[2]", XmlUtils.getElementPath((Element) nl.item(1)));
    }

    @Test
    public void testTransferValues() throws Exception {
        String doc1 = "<h1><p>p1</p><h2 test=\"bil\">lkj</h2></h1>";
        String doc2 = "<h1><p>string</p><h2>string</h2><p>p2</p></h1>";

        String result = XmlUtils.transferValues(doc1, doc2);
        assertEquals("<h1><p>p1</p><h2 test=\"bil\">lkj</h2><p>p2</p></h1>", result);
    }

    @Test
    public void testTransferValuesWithList() throws Exception {
        String doc1 = "<h1><p>p1</p><p>p2</p><h2 test=\"bil\">lkj</h2></h1>";
        String doc2 = "<h1><p>string</p><h2>string</h2><p>p2</p></h1>";

        String result = XmlUtils.transferValues(doc1, doc2);
        assertEquals("<h1><p>p1</p><p>p2</p><h2 test=\"bil\">lkj</h2><p>p2</p></h1>", result);
    }

    @Test
    public void testTransferValuesNS() throws Exception {
        String doc1 = "<ns:h1 xmlns:ns=\"test\"><ns:p>p1</ns:p><ns:h2 test=\"bil\">lkj</ns:h2></ns:h1>";
        String doc2 = "<ns:h1 xmlns:ns=\"test\"><ns:p>string</ns:p><ns:h2>string</ns:h2><ns:p>p2</ns:p></ns:h1>";

        String result = XmlUtils.transferValues(doc1, doc2);
        assertEquals("<ns:h1 xmlns:ns=\"test\"><ns:p>p1</ns:p><ns:h2 test=\"bil\">lkj</ns:h2><ns:p>p2</ns:p></ns:h1>",
                result);
    }

    @Test
    public void testCreateXPath() throws Exception {
        String str = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:ord=\"http://www.example.org/OrderService/\">"
                + "<soapenv:Header/><soapenv:Body><ord:purchaseOrder><productId>?</productId>"
                + "</ord:purchaseOrder></soapenv:Body></soapenv:Envelope>";

        XmlObject xml = XmlUtils.createXmlObject(str);
        XmlObject xmlobj = xml.selectPath("//productId")[0];
        String xpath = XmlUtils.createXPath(xmlobj.getDomNode());
        assertEquals(xmlobj, xml.selectPath(xpath)[0]);

        System.out.println("before removal: " + xpath);
        xpath = XmlUtils.removeXPathNamespaceDeclarations(xpath);
        System.out.println("after removal:" + xpath);

        String ns = XmlUtils.declareXPathNamespaces(xml);
        System.out.println("extracted namespaces:" + ns);

        assertEquals(xmlobj, xml.selectPath(ns + xpath)[0]);
    }

    @Test
    public void testCreateXPath2() throws Exception {
        String str = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:ord=\"http://www.example.org/OrderService/\">"
                + "<soapenv:Header/><soapenv:Body><purchaseOrder xmlns=\"http://test\"><productId>?</productId>"
                + "</purchaseOrder></soapenv:Body></soapenv:Envelope>";

        XmlObject xml = XmlUtils.createXmlObject(str);
        XmlObject xmlobj = xml.selectPath("declare namespace ns='http://test';//ns:productId")[0];
        String xpath = XmlUtils.createXPath(xmlobj.getDomNode());
        System.out.println("created path: " + xpath);
        assertEquals(xmlobj, xml.selectPath(xpath)[0]);
    }

    @Test
    public void testGetFirstChildElementNS() throws XmlException {
        String xml = "<SofEnvelope><partnerid>test</partnerid><sessionID>asdadasdasd</sessionID></SofEnvelope>";
        XmlObject xmlObject = XmlUtils.createXmlObject(xml);

        Element documentElement = ((Document) xmlObject.getDomNode()).getDocumentElement();
        assertNotNull(XmlUtils.getFirstChildElementNS(documentElement, new QName("", "partnerid")));
        assertNotNull(XmlUtils.getFirstChildElementNS(documentElement, new QName(null, "partnerid")));

        assertNotNull(XmlUtils.getFirstChildElementNS(documentElement, "", "partnerid"));
        assertNotNull(XmlUtils.getFirstChildElementNS(documentElement, null, "partnerid"));
    }

    @Test
    public void stripsWhitespaces() throws Exception {
        assertEquals("<content/>", XmlUtils.stripWhitespaces("<content>   </content>"));
        assertEquals("<content><test>bil</test></content>",
                XmlUtils.stripWhitespaces("<content>  <test>  bil   </test>   </content>"));
    }
}
