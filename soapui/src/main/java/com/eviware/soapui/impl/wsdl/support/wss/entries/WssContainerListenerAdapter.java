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

package com.eviware.soapui.impl.wsdl.support.wss.entries;

import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainerListener;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.WssEntry;

public class WssContainerListenerAdapter implements WssContainerListener {
    public void cryptoAdded(WssCrypto crypto) {
        // TODO Auto-generated method stub

    }

    public void cryptoRemoved(WssCrypto crypto) {
        // TODO Auto-generated method stub

    }

    public void incomingWssAdded(IncomingWss incomingWss) {
        // TODO Auto-generated method stub

    }

    public void incomingWssRemoved(IncomingWss incomingWss) {
        // TODO Auto-generated method stub

    }

    public void outgoingWssAdded(OutgoingWss outgoingWss) {
        // TODO Auto-generated method stub

    }

    public void outgoingWssEntryAdded(WssEntry entry) {
        // TODO Auto-generated method stub

    }

    public void outgoingWssEntryRemoved(WssEntry entry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void outgoingWssEntryMoved(WssEntry entry, int offset) {
        // TODO Auto-generated method stub

    }

    public void outgoingWssRemoved(OutgoingWss outgoingWss) {
        // TODO Auto-generated method stub

    }

    public void cryptoUpdated(WssCrypto crypto) {
        // TODO Auto-generated method stub

    }
}
