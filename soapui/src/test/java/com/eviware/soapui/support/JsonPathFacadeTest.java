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

import com.jayway.jsonpath.JsonPath;
import org.junit.Test;

import java.util.List;

import static com.eviware.soapui.utils.CommonMatchers.aCollectionWithSize;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JsonPathFacadeTest {

    public static final String SIMPLE_JSON = "{" +
            "children: [" +
            "'string'," +
            "{ childProperty: 'propValue'}" +
            "]" +
            "}";

    @Test
    public void simpleStringReadWorks() throws Exception {
        String jsonPathExpression = "$.children[1].childProperty";
        assertThat(new JsonPathFacade(SIMPLE_JSON).readStringValue(jsonPathExpression), is("propValue"));
    }

    @Test
    public void simpleObjectReadWorks() throws Exception {
        List arrayElements = new JsonPathFacade(SIMPLE_JSON).readObjectValue("$.children");
        assertThat(arrayElements, is(aCollectionWithSize(2)));
    }

    @Test
    public void simpleWriteWorks() throws Exception {
        verifyJsonWrite(SIMPLE_JSON, "$.children[1].childProperty");
    }

    @Test
    public void writeIntoArrayQueryWorks() throws Exception {
        String json = "{" +
                "customers: [" +
                "{ id: 1, name: 'Lisa' }," +
                "{ id: 2, name: 'Anna' }" +
                "]" +
                "}";
        String jsonPathExpression = "$.customers[?(@.id == 2)].name";
        verifyJsonWrite(json, jsonPathExpression);
    }

    @Test
    public void multipleWritesWork() throws Exception {
        String json = "{" +
                "customers: [" +
                "{ id: 1, name: 'Lisa' }," +
                "{ id: 2, name: 'Anna' }" +
                "]" +
                "}";
        String jsonPathExpression = "$..name";
        verifyJsonWrite(json, jsonPathExpression);
    }

    private void verifyJsonWrite(String json, String jsonPathExpression) {
        JsonPathFacade jsonPathFacade = new JsonPathFacade(json);
        jsonPathFacade.writeValue(jsonPathExpression, "newValue");

        String newJson = jsonPathFacade.getCurrentJson();
        Object read = JsonPath.read(newJson, jsonPathExpression);
        if (read instanceof List) {
            List valueList = (List) read;
            for (Object value : valueList) {
                assertThat(value, is((Object) "newValue"));
            }
        } else {
            assertThat(read, is((Object) "newValue"));
        }
    }
}
