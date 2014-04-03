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

package com.eviware.soapui.impl.wsdl.teststeps;

import static org.junit.Assert.assertEquals;

import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import com.eviware.soapui.config.PropertyTransferConfig;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.support.DefaultTestStepProperty;

public class PropertyTransferTestCase {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(PropertyTransferTestCase.class);
    }

    private PropertyTransfer transfer;
    private DefaultTestStepProperty sourceProperty;
    private DefaultTestStepProperty targetProperty;

    @Before
    public void setUp() throws Exception {
        transfer = new PropertyTransfer(null, PropertyTransferConfig.Factory.newInstance());
        sourceProperty = new DefaultTestStepProperty("source", null);
        targetProperty = new DefaultTestStepProperty("target", null);
    }

    @Test
    public void testStringToStringTransfer() throws Exception {
        PropertyTransfer transfer = new PropertyTransfer(null, PropertyTransferConfig.Factory.newInstance());
        DefaultTestStepProperty sourceProperty = new DefaultTestStepProperty("source", null);
        sourceProperty.setValue("Test");

        DefaultTestStepProperty targetProperty = new DefaultTestStepProperty("target", null);
        transfer.transferStringToString(sourceProperty, targetProperty);

        assertEquals(sourceProperty.getValue(), targetProperty.getValue());
    }

    @Test
    public void testStringToXmlTransfer() throws Exception {
        sourceProperty.setValue("audi");
        targetProperty.setValue("<bil><name>bmw</name></bil>");

        transfer.setTargetPath("//name/text()");

        transfer.transferStringToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));
        assertEquals("<bil><name>audi</name></bil>", targetProperty.getValue());

        targetProperty.setValue("<bil><name test=\"test\">bmw</name></bil>");
        transfer.transferStringToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));

        assertEquals("<bil><name test=\"test\">audi</name></bil>", targetProperty.getValue());

        transfer.setTargetPath("//name/@test");

        transfer.transferStringToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));
        assertEquals("<bil><name test=\"audi\">audi</name></bil>", targetProperty.getValue());
    }

    @Test
    public void testXmlToStringTransfer() throws Exception {
        sourceProperty.setValue("<bil><name>audi</name></bil>");
        targetProperty.setValue("");

        transfer.setSourcePath("//name/text()");

        transfer.transferXPathToString(sourceProperty, targetProperty, new WsdlSubmitContext(null));
        assertEquals("audi", targetProperty.getValue());
    }

    @Test
    public void testXmlToStringNullTransfer() throws Exception {
        sourceProperty.setValue("<bil></bil>");
        targetProperty.setValue("");

        transfer.setSourcePath("//name/text()");

        transfer.transferXPathToString(sourceProperty, targetProperty, new WsdlSubmitContext(null));
        assertEquals(null, targetProperty.getValue());
    }

    @Test
    public void testTextXmlToXmlTransfer() throws Exception {
        sourceProperty.setValue("<bil><name>audi</name></bil>");
        targetProperty.setValue("<bil><name>bmw</name></bil>");

        transfer.setSourcePath("//name/text()");
        transfer.setTargetPath("//name/text()");

        transfer.transferXPathToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));
        assertEquals(sourceProperty.getValue(), targetProperty.getValue());

        targetProperty.setValue("<bil><name test=\"test\">bmw</name></bil>");
        transfer.transferXPathToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));

        assertEquals("<bil><name test=\"test\">audi</name></bil>", targetProperty.getValue());
    }

    @Test
    public void testTextContentXmlToXmlTransfer() throws Exception {
        sourceProperty.setValue("<bil><name>audi</name></bil>");
        targetProperty.setValue("<bil><name2>bmw</name2></bil>");

        transfer.setTransferTextContent(true);
        transfer.setSourcePath("//name");
        transfer.setTargetPath("//name2");

        transfer.transferXPathToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));

        assertEquals("<bil><name2>audi</name2></bil>", targetProperty.getValue());
    }

    @Test
    public void testTextXmlToXmlNullTransfer() throws Exception {
        sourceProperty.setValue("<bil><name/></bil>");
        targetProperty.setValue("<bil><name>bmw</name></bil>");

        transfer.setSourcePath("//name/text()");
        transfer.setTargetPath("//name/text()");

        transfer.transferXPathToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));

        assertEquals("<bil><name/></bil>", targetProperty.getValue());
    }

    @Test
    public void testAttributeXmlToXmlTransfer() throws Exception {
        sourceProperty.setValue("<bil><name value=\"fiat\" value2=\"volvo\">alfa</name></bil>");
        targetProperty.setValue("<bil><name test=\"test\">bmw</name></bil>");

        transfer.setSourcePath("//name/@value");
        transfer.setTargetPath("//name/text()");

        transfer.transferXPathToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));

        assertEquals("<bil><name test=\"test\">fiat</name></bil>", targetProperty.getValue());

        transfer.setSourcePath("//name/text()");
        transfer.setTargetPath("//name/@test");

        transfer.transferXPathToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));

        assertEquals("<bil><name test=\"alfa\">fiat</name></bil>", targetProperty.getValue());

        transfer.setSourcePath("//name/@value2");
        transfer.transferXPathToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));
        assertEquals("<bil><name test=\"volvo\">fiat</name></bil>", targetProperty.getValue());
    }

    @Test
    public void testElementXmlToXmlTransfer() throws Exception {
        sourceProperty.setValue("<bil><name>audi</name></bil>");
        targetProperty.setValue("<bil><test/></bil>");

        transfer.setSourcePath("//bil");
        transfer.setTargetPath("//bil");

        transfer.setTransferTextContent(false);
        transfer.transferXPathToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));
        assertEquals(sourceProperty.getValue(), targetProperty.getValue());

        targetProperty.setValue("<bil><name></name></bil>");

        transfer.setSourcePath("//bil/name/text()");
        transfer.setTargetPath("//bil/name");

        transfer.transferXPathToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));
        assertEquals(sourceProperty.getValue(), targetProperty.getValue());
    }

    @Test
    public void testElementWithNsXmlToXmlTransfer() throws Exception {
        sourceProperty.setValue("<ns1:bil xmlns:ns1=\"ns1\"><ns1:name>audi</ns1:name></ns1:bil>");
        targetProperty.setValue("<bil><name/></bil>");

        transfer.setTransferTextContent(false);
        transfer.setSourcePath("declare namespace ns='ns1';//ns:bil/ns:name");
        transfer.setTargetPath("//bil/name");

        transfer.transferXPathToXml(sourceProperty, targetProperty, new WsdlSubmitContext(null));
        assertEquals("<bil xmlns:ns1=\"ns1\"><ns1:name>audi</ns1:name></bil>", targetProperty.getValue());
    }

}
