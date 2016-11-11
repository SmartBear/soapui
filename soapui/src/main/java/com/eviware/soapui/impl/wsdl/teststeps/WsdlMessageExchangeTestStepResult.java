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

import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.MessageExchangeTestStepResult;

import java.util.ArrayList;
import java.util.List;

public class WsdlMessageExchangeTestStepResult extends WsdlTestStepResult implements MessageExchangeTestStepResult {
    private List<MessageExchange> exchanges = new ArrayList<MessageExchange>();

    public WsdlMessageExchangeTestStepResult(WsdlTestStep testStep) {
        super(testStep);
    }

    public MessageExchange[] getMessageExchanges() {
        return exchanges == null ? new MessageExchange[0] : exchanges.toArray(new MessageExchange[exchanges.size()]);
    }

    public void addMessageExchange(MessageExchange messageExchange) {
        if (exchanges != null) {
            exchanges.add(messageExchange);
        }
    }

    public void addMessages(MessageExchange[] messageExchanges) {
        if (exchanges != null) {
            for (MessageExchange messageExchange : messageExchanges) {
                exchanges.add(messageExchange);
            }
        }
    }

    @Override
    public void discard() {
        super.discard();

        exchanges = null;
    }
}
