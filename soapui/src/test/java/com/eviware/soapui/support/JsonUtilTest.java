/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import static org.junit.Assert.*;

/**
 * @author joel.jonsson
 */
public class JsonUtilTest {
    @Test
    public void canParseWithSurroundingWhitespace() {
        new JsonUtil().parseTrimmedText("    \n\n   {1:2}\n\n \t   ");
    }

    @Test
    public void canParseWithPrecedingWhile1() {
        new JsonUtil().parseTrimmedText(" \n  while(1);  \n\n   {1:2}\n\n \t   ");
    }

    @Test
    public void parseTrimmedTextToJsonNode_preservesBigDecimalPrecision() throws Exception {
        String input = "{ \"i_am_a_big_decimal\": 123456789123456789123456789 }";
        JsonNode node = JsonUtil.parseTrimmedTextToJsonNode(input);
        String output = JsonUtil.format(node);
        assertTrue("BigDecimal should not use scientific notation", !output.contains("E"));
        assertTrue("BigDecimal value should be preserved exactly", output.contains("123456789123456789123456789"));
    }

    // now null output produces null while empty output produces an empty object
    @Test
    public void parseTrimmedTextToJsonNode_returnsNullForNullInput() throws Exception {
        assertNull(JsonUtil.parseTrimmedTextToJsonNode(null));
    }

    @Test
    public void parseTrimmedTextToJsonNode_returnsNonNullForEmptyObject() throws Exception {
        JsonNode node = JsonUtil.parseTrimmedTextToJsonNode("{}");
        assertNotNull("Empty JSON object should not return null", node);
        // formatter produces { } (with a space)
        assertEquals("{ }", JsonUtil.format(node).trim());
    }

    @Test
    public void format_doesNotUseScientificNotationForLargeNumbers() throws Exception {
        String input = "{ \"value\": 9007199254740993 }";
        JsonNode node = JsonUtil.parseTrimmedTextToJsonNode(input);
        String output = JsonUtil.format(node);
        assertTrue(output.contains("9007199254740993"));
    }
}
