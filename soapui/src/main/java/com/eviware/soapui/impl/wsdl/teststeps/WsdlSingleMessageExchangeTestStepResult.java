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

import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.xml.XmlUtils;

import java.io.PrintWriter;

/**
 * TestStep Result for a WsdlMessageExchange
 *
 * @author ole.matzura
 */

public class WsdlSingleMessageExchangeTestStepResult extends WsdlTestStepResult {
    private WsdlMessageExchange messageExchange;
    private boolean addedAction;

    // private StringToStringMap properties;

    public WsdlSingleMessageExchangeTestStepResult(WsdlTestStep step) {
        super(step);
    }

    public void setMessageExchange(WsdlMessageExchange messageExchange) {
        this.messageExchange = messageExchange;
    }

    @Override
    public ActionList getActions() {
        if (!addedAction) {
            addAction(new ShowMessageExchangeAction(messageExchange, "StepResult"), true);
            addedAction = true;
        }

        return super.getActions();
    }

    // public String getRequestContent()
    // {
    // if( isDiscarded() )
    // return "<discarded>";
    //
    // return messageExchange == null ? null :
    // messageExchange.getRequestContent();
    // }
    //
    // public void addProperty( String name, String value )
    // {
    // if( isDiscarded() )
    // return;
    //
    // if( properties == null )
    // properties = new StringToStringMap();
    //
    // properties.put( name, value );
    // }

    public void discard() {
        super.discard();

        messageExchange = null;
        // properties = null;
    }

    public void writeTo(PrintWriter writer) {
        super.writeTo(writer);

        if (isDiscarded()) {
            return;
        }

        writer.println("---------------- Message Exchange ------------------");
        if (messageExchange == null) {
            writer.println("Missing MessageExchange");
        } else {
            writer.println("--- Request");
            if (messageExchange.getRequestHeaders() != null) {
                writer.println("Request Headers: " + messageExchange.getRequestHeaders().toString());
            }

            writer.println(XmlUtils.prettyPrintXml(messageExchange.getRequestContent()));

            writer.println("--- Response");
            if (messageExchange.getResponseHeaders() != null) {
                writer.println("Response Headers: " + messageExchange.getResponseHeaders().toString());
            }

            writer.println(XmlUtils.prettyPrintXml(messageExchange.getResponseContent()));
        }
    }
}
