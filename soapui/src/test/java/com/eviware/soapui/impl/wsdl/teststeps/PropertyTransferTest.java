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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.PropertyTransferConfig;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.support.DefaultTestStepProperty;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.JsonPathFacade;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.eviware.soapui.utils.CommonMatchers.aNumber;
import static com.eviware.soapui.utils.CommonMatchers.anEmptyString;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class PropertyTransferTest {

    private PropertyTransfer transfer;
    private DefaultTestStepProperty sourceProperty;
    private DefaultTestStepProperty targetProperty;
    private WsdlSubmitContext submitContext;

    @Before
    public void setUp() throws Exception {
        sourceProperty = new DefaultTestStepProperty("source", null);
        targetProperty = new DefaultTestStepProperty("target", null);
        transfer = new PropertyTransfer(null, PropertyTransferConfig.Factory.newInstance()) {
            @Override
            public TestProperty getSourceProperty() {
                return sourceProperty;
            }

            @Override
            public TestProperty getTargetProperty() {
                return targetProperty;
            }
        };
        submitContext = mock(WsdlSubmitContext.class);
    }

    @Test
    public void translatesOldXQueryBooleanToXQueryPathLanguage() throws Exception {
        transfer.setUseXQuery(true);

        assertThat(transfer.getSourcePathLanguage(), is(PathLanguage.XQUERY));
        assertThat(transfer.getTargetPathLanguage(), is(PathLanguage.XQUERY));
    }

    @Test
    public void testStringToStringTransfer() throws Exception {
        sourceProperty.setValue("Test");

        transfer.transferProperties(submitContext);

        assertThat(targetProperty.getValue(), is("Test"));
    }

    @Test
    public void testStringToXmlTransfer() throws Exception {
        sourceProperty.setValue("audi");
        targetProperty.setValue("<bil><name>bmw</name></bil>");

        transfer.setTargetPath("//name/text()");
        transfer.transferProperties(submitContext);
        assertThat(targetProperty.getValue(), is("<bil><name>audi</name></bil>"));

        targetProperty.setValue("<bil><name test=\"test\">bmw</name></bil>");
        transfer.transferProperties(submitContext);
        assertThat(targetProperty.getValue(), is("<bil><name test=\"test\">audi</name></bil>"));

        transfer.setTargetPath("//name/@test");
        transfer.transferProperties(submitContext);
        assertThat(targetProperty.getValue(), is("<bil><name test=\"audi\">audi</name></bil>"));
    }

    @Test
    public void testXmlToStringTransfer() throws Exception {
        sourceProperty.setValue("<bil><name>audi</name></bil>");
        targetProperty.setValue("");
        transfer.setSourcePath("//name/text()");

        transfer.transferProperties(submitContext);
        assertThat(targetProperty.getValue(), is("audi"));
    }

    @Test
    public void testXmlToStringNullTransfer() throws Exception {
        sourceProperty.setValue("<bil></bil>");
        targetProperty.setValue("");

        transfer.setSourcePath("//name/text()");

        transfer.transferProperties(submitContext);
        assertThat(targetProperty.getValue(), is(nullValue()));
    }

    @Test
    public void testTextXmlToXmlTransfer() throws Exception {
        sourceProperty.setValue("<bil><name>audi</name></bil>");
        targetProperty.setValue("<bil><name>bmw</name></bil>");

        transfer.setSourcePath("//name/text()");
        transfer.setTargetPath("//name/text()");

        transfer.transferProperties(submitContext);
        assertThat(targetProperty.getValue(), is(sourceProperty.getValue()));

        targetProperty.setValue("<bil><name test=\"test\">bmw</name></bil>");
        transfer.transferProperties(submitContext);

        assertThat(targetProperty.getValue(), is("<bil><name test=\"test\">audi</name></bil>"));
    }

    @Test
    public void testTextContentXmlToXmlTransfer() throws Exception {
        sourceProperty.setValue("<bil><name>audi</name></bil>");
        targetProperty.setValue("<bil><name2>bmw</name2></bil>");

        transfer.setTransferTextContent(true);
        transfer.setSourcePath("//name");
        transfer.setTargetPath("//name2");

        transfer.transferProperties(submitContext);

        assertThat(targetProperty.getValue(), is("<bil><name2>audi</name2></bil>"));
    }

    @Test
    public void testTextXmlToXmlNullTransfer() throws Exception {
        sourceProperty.setValue("<bil><name/></bil>");
        targetProperty.setValue("<bil><name>bmw</name></bil>");

        transfer.setSourcePath("//name/text()");
        transfer.setTargetPath("//name/text()");

        transfer.transferProperties(submitContext);

        assertThat(targetProperty.getValue(), is("<bil><name/></bil>"));
    }

    @Test
    public void testAttributeXmlToXmlTransfer() throws Exception {
        sourceProperty.setValue("<bil><name value=\"fiat\" value2=\"volvo\">alfa</name></bil>");
        targetProperty.setValue("<bil><name test=\"test\">bmw</name></bil>");

        transfer.setSourcePath("//name/@value");
        transfer.setTargetPath("//name/text()");

        transfer.transferProperties(submitContext);

        assertThat(targetProperty.getValue(), is("<bil><name test=\"test\">fiat</name></bil>"));

        transfer.setSourcePath("//name/text()");
        transfer.setTargetPath("//name/@test");

        transfer.transferProperties(submitContext);

        assertThat(targetProperty.getValue(), is("<bil><name test=\"alfa\">fiat</name></bil>"));

        transfer.setSourcePath("//name/@value2");
        transfer.transferProperties(submitContext);
        assertThat(targetProperty.getValue(), is("<bil><name test=\"volvo\">fiat</name></bil>"));
    }

    @Test
    public void testElementXmlToXmlTransfer() throws Exception {
        sourceProperty.setValue("<bil><name>audi</name></bil>");
        targetProperty.setValue("<bil><test/></bil>");

        transfer.setSourcePath("//bil");
        transfer.setTargetPath("//bil");

        transfer.setTransferTextContent(false);
        transfer.transferProperties(submitContext);
        assertThat(targetProperty.getValue(), is(sourceProperty.getValue()));

        targetProperty.setValue("<bil><name></name></bil>");

        transfer.setSourcePath("//bil/name/text()");
        transfer.setTargetPath("//bil/name");

        transfer.transferProperties(submitContext);
        assertThat(targetProperty.getValue(), is(sourceProperty.getValue()));
    }

    @Test
    public void testElementWithNsXmlToXmlTransfer() throws Exception {
        sourceProperty.setValue("<ns1:bil xmlns:ns1=\"ns1\"><ns1:name>audi</ns1:name></ns1:bil>");
        targetProperty.setValue("<bil><name/></bil>");

        transfer.setTransferTextContent(false);
        transfer.setSourcePath("declare namespace ns='ns1';//ns:bil/ns:name");
        transfer.setTargetPath("//bil/name");

        transfer.transferProperties(submitContext);
        assertThat(targetProperty.getValue(), is("<bil xmlns:ns1=\"ns1\"><ns1:name>audi</ns1:name></bil>"));
    }

    @Test
    public void supportsJsonPathInSource() throws Exception {
        sourceProperty.setValue("{ persons: [" +
                "{ firstName: 'Anders', lastName: 'And' }," +
                "{ firstName: 'Anders', lastName: 'And' }" +
                "] }");
        transfer.setSourcePath("$.persons[0].firstName");
        transfer.setSourcePathLanguage(PathLanguage.JSONPATH);
        transfer.transferProperties(submitContext);

        assertThat(targetProperty.getValue(), is("Anders"));
    }

    @Test
    public void supportsJsonPathInTarget() throws Exception {
        String newName = "New_Name";
        sourceProperty.setValue(newName);
        targetProperty.setValue("{ persons: [" +
                "{ firstName: 'Anders', lastName: 'And' }," +
                "{ firstName: 'Anders', lastName: 'And' }" +
                "] }");
        String path = "$.persons[0].firstName";
        transfer.setTargetPath(path);
        transfer.setTargetPathLanguage(PathLanguage.JSONPATH);
        transfer.transferProperties(submitContext);

        assertThat(new JsonPathFacade(targetProperty.getValue()).readStringValue(path), is(newName));
    }

    @Test
    public void transfersJsonNumberAsNumber() throws Exception {
        sourceProperty.setValue("{ numbers : [1, 2, 42]}");
        targetProperty.setValue("{ numbers : [1, 2, 3]}");
        String path = "$.numbers[2]";
        transfer.setSourcePath(path);
        transfer.setSourcePathLanguage(PathLanguage.JSONPATH);
        transfer.setTargetPath(path);
        transfer.setTargetPathLanguage(PathLanguage.JSONPATH);
        transfer.transferProperties(submitContext);

        Object insertedValue = new JsonPathFacade(targetProperty.getValue()).readObjectValue(path);
        assertThat(insertedValue, is(aNumber()));
    }

    @Test
    public void transfersJsonNodesAsNodes() throws Exception {
        sourceProperty.setValue("{ numbers : { key1: 1, key2: 2} }");
        targetProperty.setValue("{ numbers : [1, 2, 3] }");
        String path = "$.numbers";
        transfer.setSourcePath(path);
        transfer.setSourcePathLanguage(PathLanguage.JSONPATH);
        transfer.setTargetPath(path);
        transfer.setTargetPathLanguage(PathLanguage.JSONPATH);
        transfer.transferProperties(submitContext);

        Object insertedValue = new JsonPathFacade(targetProperty.getValue()).readObjectValue(path);
        assertTrue("Expected a map object but got " + insertedValue, insertedValue instanceof Map);
    }

    @Test
    public void doesNotRemoveExpansionsFromInternalTextProperty() throws Exception {
        String value = "${= someCode() }";
        sourceProperty.setValue(value);
        transfer.transferProperties(submitContext);

        assertThat(targetProperty.getValue(), is(value));
    }

    @Test
    public void removesExpansionsFromResponseProperty() throws Exception {
        verifyPropertyIsSanitized(WsdlTestStepWithProperties.RESPONSE);
    }

    @Test
    public void removesExpansionsFromRawResponseProperty() throws Exception {
        verifyPropertyIsSanitized(WsdlTestStepWithProperties.RAW_RESPONSE);
    }

    @Test
    public void removesExpansionsFromResponseAsXmlProperty() throws Exception {
        verifyPropertyIsSanitized(WsdlTestStepWithProperties.RESPONSE_AS_XML);
    }

    @Test
    public void doesNotRemoveExpansionsFromInternalTransferredXml() throws Exception {
        String originalValue = "<a><b>Attack here:${= attack() }</b></a>";
        sourceProperty.setValue(originalValue);
        String path = "/a/b";
        targetProperty.setValue("<a><b>some content</b></a>");
        transfer.setSourcePath(path);
        transfer.setSourcePathLanguage(PathLanguage.XPATH);
        transfer.setTargetPath(path);
        transfer.setTargetPathLanguage(PathLanguage.XPATH);
        transfer.transferProperties(submitContext);

        assertThat(targetProperty.getValue(), is(originalValue));
    }

    @Test
    public void removesExpansionsWhenTransferringXmlFromResponse() throws Exception {
        String originalValue = "<a><b>Attack here:${= attack() }</b></a>";
        sourceProperty.setValue(originalValue);
        sourceProperty.setName(WsdlTestStepWithProperties.RESPONSE);
        String path = "/a/b";
        targetProperty.setValue("<a><b>some content</b></a>");
        transfer.setSourcePath(path);
        transfer.setSourcePathLanguage(PathLanguage.XPATH);
        transfer.setTargetPath(path);
        transfer.setTargetPathLanguage(PathLanguage.XPATH);
        transfer.transferProperties(submitContext);

        assertThat(targetProperty.getValue(), is("<a><b>Attack here:</b></a>"));
    }

    /* Helper methods */

    private void verifyPropertyIsSanitized(String propertyName) throws PropertyTransferException {
        sourceProperty.setValue("${= attack() }");
        sourceProperty.setName(propertyName);
        transfer.transferProperties(submitContext);

        assertThat(targetProperty.getValue(), is(anEmptyString()));
    }


}
