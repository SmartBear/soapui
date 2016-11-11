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

import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XmlObjectConfigurationTest {

    @Test
    public void testConfiguration() throws Exception {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add("testFloat", (float) 0.123);
        builder.add("testInt", 123);
        builder.add("testString", "1234");

        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(builder.finish());
        assertEquals((float) 0.123, reader.readFloat("testFloat", 0), 0);
        assertEquals(123, reader.readInt("testInt", 0));
        assertEquals("1234", reader.readString("testString", null));
    }
}
