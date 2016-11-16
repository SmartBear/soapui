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

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import com.eviware.soapui.config.MockOperationDispatchStyleConfig;
import com.eviware.soapui.model.mock.MockOperation;

import java.util.HashMap;
import java.util.Map;

public class MockOperationDispatchRegistry {
    private static Map<String, MockOperationDispatchFactory> factories = new HashMap<String, MockOperationDispatchFactory>();

    static {
        putFactory(MockOperationDispatchStyleConfig.SEQUENCE.toString(), new SequenceMockOperationDispatcher.Factory());
        putFactory(MockOperationDispatchStyleConfig.RANDOM.toString(), new RandomMockOperationDispatcher.Factory());
        putFactory(MockOperationDispatchStyleConfig.SCRIPT.toString(), new ScriptMockOperationDispatcher.Factory());
        putFactory(MockOperationDispatchStyleConfig.XPATH.toString(), new XPathMockOperationDispatcher.Factory());
        putFactory(MockOperationDispatchStyleConfig.QUERY_MATCH.toString(),
                new QueryMatchMockOperationDispatcher.Factory());
    }

    public static void putFactory(String type, MockOperationDispatchFactory factory) {
        factories.put(type, factory);
    }

    public static String[] getDispatchTypes() {
        return factories.keySet().toArray(new String[factories.size()]);
    }

    public static MockOperationDispatcher buildDispatcher(String type, MockOperation mockOperation) {
        return factories.get(type).build(mockOperation);
    }
}
