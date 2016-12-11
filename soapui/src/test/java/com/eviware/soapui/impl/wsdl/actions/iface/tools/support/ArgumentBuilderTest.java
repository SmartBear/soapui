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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import com.eviware.soapui.support.types.StringToStringMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArgumentBuilderTest {

    @Test
    public void testUnix() throws Exception {
        ArgumentBuilder builder = new ArgumentBuilder(new StringToStringMap());
        builder.startScript("tcpmon", null, ".sh");

        assertEquals("sh", builder.getArgs().get(0));
        assertEquals("-c", builder.getArgs().get(1));

        assertEquals("./tcpmon.sh", builder.getArgs().get(2));

        builder.addArgs(new String[]{"test"});
        assertEquals("./tcpmon.sh test", builder.getArgs().get(2));

        builder.addArgs(new String[]{"te st"});
        assertEquals("./tcpmon.sh test te%20st", builder.getArgs().get(2));
    }
}
