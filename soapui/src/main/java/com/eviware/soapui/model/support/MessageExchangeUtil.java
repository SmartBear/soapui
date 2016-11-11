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

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class MessageExchangeUtil {
    public static MessageExchange findMessageExchangeByTestStepId(List<TestStepResult> results, String testStepId) {
        List<TestStepResult> tmpList = Lists.newArrayList(results);
        Collections.reverse(tmpList);

        for (TestStepResult tsr : tmpList) {
            String id = tsr.getTestStep().getId();
            if (id.equals(testStepId) && tsr instanceof MessageExchange) {
                return (MessageExchange) tsr;
            }
        }
        return null;

    }
}
