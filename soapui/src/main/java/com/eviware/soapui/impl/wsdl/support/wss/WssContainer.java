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

import com.eviware.soapui.impl.wsdl.support.wss.crypto.CryptoType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;

import javax.annotation.Nonnull;
import java.util.List;

public interface WssContainer extends PropertyExpansionContainer {
    public ModelItem getModelItem();

    public void addWssContainerListener(WssContainerListener listener);

    public void removeWssContainerListener(WssContainerListener listener);

    public List<WssCrypto> getCryptoList();

    public WssCrypto addCrypto(String source, String password, @Nonnull CryptoType type);

    public int getCryptoCount();

    public void removeCrypto(@Nonnull WssCrypto crypto);

    public List<IncomingWss> getIncomingWssList();

    public IncomingWss addIncomingWss(String label);

    public int getIncomingWssCount();

    public IncomingWss getIncomingWssAt(int index);

    public void removeIncomingWssAt(int row);

    public List<OutgoingWss> getOutgoingWssList();

    public OutgoingWss addOutgoingWss(String label);

    public int getOutgoingWssCount();

    public OutgoingWss getOutgoingWssAt(int index);

    public void removeOutgoingWssAt(int row);

    public WssCrypto getCryptoByName(String cryptoName);

    public WssCrypto getCryptoByName(String cryptoName, boolean outgoingWSSConfig);

    public OutgoingWss getOutgoingWssByName(String outgoingName);

    public IncomingWss getIncomingWssByName(String incomingName);

    public String[] getCryptoNames();

    public String[] getOutgoingWssNames();

    public String[] getIncomingWssNames();

    public void importConfig(WssContainer wssContainer);
}
