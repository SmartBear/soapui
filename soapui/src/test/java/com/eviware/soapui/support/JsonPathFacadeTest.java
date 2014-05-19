/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
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
