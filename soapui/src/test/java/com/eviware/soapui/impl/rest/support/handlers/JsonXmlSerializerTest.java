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
package com.eviware.soapui.impl.rest.support.handlers;

import com.eviware.soapui.support.JsonUtil;
import org.junit.Before;
import org.junit.Test;
import net.sf.json.JSON;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JsonXmlSerializerTest {

    private JsonXmlSerializer serializer;

    @Before
    public void setUp() throws Exception {
        serializer = new JsonXmlSerializer();
    }

    @Test
    public void serializesJsonWithVanillaNames() throws Exception {
        JSON parse = new JsonUtil().parseTrimmedText("{ name: 'Barack', surname: 'Obama', profession: 'president'}");

        assertThat(serializer.write(parse), is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<o>" +
                "<name type=\"string\">Barack</name>" +
                "<profession type=\"string\">president</profession>" +
                "<surname type=\"string\">Obama</surname>" +
                "</o>\r\n"));
    }

    @Test
    public void serializesJsonWithDollarSign() throws Exception {
        JSON parse = new JsonUtil().parseTrimmedText("{ $: 'value' }");

        assertThat(serializer.write(parse), is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<o>" +
                "<_ type=\"string\">value</_>" +
                "</o>\r\n"));
    }
}
