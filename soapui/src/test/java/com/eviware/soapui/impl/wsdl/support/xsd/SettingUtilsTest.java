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

package com.eviware.soapui.impl.wsdl.support.xsd;

import com.eviware.soapui.config.StringListConfig;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author lars
 */
public class SettingUtilsTest {

    @Test
    public void testQnameValues2String() throws Exception {
        LinkedHashMap<QName, String[]> valueMap = new LinkedHashMap<QName, String[]>();
        valueMap.put(new QName("x"), new String[]{"1", "2", "3"});
        valueMap.put(new QName("ns2", "y"), new String[]{"a", "b", "c"});

        ArrayList<String> expected = new ArrayList<String>();
        expected.add("x=1,2,3");
        expected.add("y@ns2=a,b,c");
        String result = SettingUtils.qnameValues2String(valueMap);
        StringListConfig config = StringListConfig.Factory.parse(result);
        assertEquals(expected, config.getEntryList());

        assertEquals(valueMap2String(valueMap), valueMap2String(SettingUtils.string2QNameValues(result)));
    }

    private static String valueMap2String(Map<QName, String[]> valueMap) {
        StringBuffer buf = new StringBuffer();
        for (QName qname : valueMap.keySet()) {
            String[] values = valueMap.get(qname);
            buf.append(qname.toString()).append("=");
            buf.append(Arrays.toString(values));
            buf.append("\n");
        }
        return buf.toString();
    }
}
