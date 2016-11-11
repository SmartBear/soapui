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

package com.eviware.soapui.impl.wsdl.support.wss;

// FIXME Consider break this interface in sub interfaces as many implementing classes only use cryptoUpdated 
public interface WssContainerListener {
    public void outgoingWssEntryAdded(WssEntry entry);

    public void outgoingWssEntryRemoved(WssEntry entry);

    public void outgoingWssEntryMoved(WssEntry entry, int offset);

    public void cryptoAdded(WssCrypto crypto);

    public void cryptoRemoved(WssCrypto crypto);

    public void cryptoUpdated(WssCrypto crypto);

    public void incomingWssAdded(IncomingWss incomingWss);

    public void incomingWssRemoved(IncomingWss incomingWss);

    public void outgoingWssAdded(OutgoingWss outgoingWss);

    public void outgoingWssRemoved(OutgoingWss outgoingWss);

}
