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

import com.eviware.soapui.config.IncomingWssConfig;
import com.eviware.soapui.config.KeyMaterialCryptoConfig;
import com.eviware.soapui.config.OutgoingWssConfig;
import com.eviware.soapui.config.WssContainerConfig;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.wss.crypto.CryptoType;
import com.eviware.soapui.impl.wsdl.support.wss.crypto.KeyMaterialWssCrypto;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.types.StringList;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultWssContainer implements WssContainer {
    private final ModelItem modelItem;
    private List<WssCrypto> cryptos = new ArrayList<WssCrypto>();
    private List<IncomingWss> incomingWssConfigs = new ArrayList<IncomingWss>();
    private List<OutgoingWss> outgoingWssConfigs = new ArrayList<OutgoingWss>();
    private final WssContainerConfig config;
    private Set<WssContainerListener> listeners = new HashSet<WssContainerListener>();

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public DefaultWssContainer(ModelItem modelItem, WssContainerConfig config) {
        this.modelItem = modelItem;
        this.config = config;

        for (KeyMaterialCryptoConfig cryptoConfig : config.getCryptoList()) {
            cryptos.add(new KeyMaterialWssCrypto(cryptoConfig, this));
        }

        for (IncomingWssConfig wssConfig : config.getIncomingList()) {
            incomingWssConfigs.add(new IncomingWss(wssConfig, this));
        }

        for (OutgoingWssConfig wssConfig : config.getOutgoingList()) {
            outgoingWssConfigs.add(new OutgoingWss(wssConfig, this));
        }
    }

    public ModelItem getModelItem() {
        return modelItem;
    }

    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(getModelItem(), this);

        for (OutgoingWss entry : outgoingWssConfigs) {
            result.addAll(entry.getPropertyExpansions());
        }

        return result.toArray();
    }

    public List<WssCrypto> getCryptoList() {
        return new ArrayList<WssCrypto>(cryptos);
    }

    public WssCrypto addCrypto(String source, String password, CryptoType type) {
        KeyMaterialWssCrypto result = new KeyMaterialWssCrypto(getConfig().addNewCrypto(), this, source, password, type);
        cryptos.add(result);

        fireCryptoAdded(result);

        return result;
    }

    protected void fireCryptoAdded(WssCrypto crypto) {
        for (WssContainerListener listener : listeners.toArray(new WssContainerListener[listeners.size()])) {
            listener.cryptoAdded(crypto);
        }
    }

    protected void fireCryptoRemoved(WssCrypto crypto) {
        for (WssContainerListener listener : listeners.toArray(new WssContainerListener[listeners.size()])) {
            listener.cryptoRemoved(crypto);
        }
    }

    public void fireWssEntryMoved(WssEntry entry, int offset) {
        for (WssContainerListener listener : listeners.toArray(new WssContainerListener[listeners.size()])) {
            listener.outgoingWssEntryMoved(entry, offset);
        }
    }

    public WssContainerConfig getConfig() {
        return config;
    }

    public int getCryptoCount() {
        return cryptos.size();
    }

    @Override
    public void removeCrypto(WssCrypto crypto) {
        int index = cryptos.indexOf(crypto);
        cryptos.remove(crypto);
        fireCryptoRemoved(crypto);
        getConfig().removeCrypto(index);
    }

    public List<IncomingWss> getIncomingWssList() {
        return new ArrayList<IncomingWss>(incomingWssConfigs);
    }

    public IncomingWss addIncomingWss(String label) {
        IncomingWss incomingWss = new IncomingWss(getConfig().addNewIncoming(), this);
        incomingWss.setName(label);
        incomingWssConfigs.add(incomingWss);

        fireIncomingWssAdded(incomingWss);

        return incomingWss;
    }

    public int getIncomingWssCount() {
        return incomingWssConfigs.size();
    }

    public IncomingWss getIncomingWssAt(int index) {
        return incomingWssConfigs.get(index);
    }

    public void removeIncomingWssAt(int row) {
        IncomingWss incomingWss = incomingWssConfigs.remove(row);
        fireIncomingWssRemoved(incomingWss);
        getConfig().removeIncoming(row);
    }

    protected void fireIncomingWssAdded(IncomingWss incomingWss) {
        for (WssContainerListener listener : listeners.toArray(new WssContainerListener[listeners.size()])) {
            listener.incomingWssAdded(incomingWss);
        }
    }

    protected void fireIncomingWssRemoved(IncomingWss incomingWss) {
        for (WssContainerListener listener : listeners.toArray(new WssContainerListener[listeners.size()])) {
            listener.incomingWssRemoved(incomingWss);
        }
    }

    public List<OutgoingWss> getOutgoingWssList() {
        return new ArrayList<OutgoingWss>(outgoingWssConfigs);
    }

    public OutgoingWss addOutgoingWss(String label) {
        OutgoingWss result = new OutgoingWss(getConfig().addNewOutgoing(), this);
        result.setName(label);

        outgoingWssConfigs.add(result);

        fireOutgoingWssAdded(result);

        return result;
    }

    protected void fireOutgoingWssAdded(OutgoingWss result) {
        for (WssContainerListener listener : listeners.toArray(new WssContainerListener[listeners.size()])) {
            listener.outgoingWssAdded(result);
        }
    }

    protected void fireOutgoingWssRemoved(OutgoingWss result) {
        for (WssContainerListener listener : listeners.toArray(new WssContainerListener[listeners.size()])) {
            listener.outgoingWssRemoved(result);
        }
    }

    public int getOutgoingWssCount() {
        return outgoingWssConfigs.size();
    }

    public OutgoingWss getOutgoingWssAt(int index) {
        return outgoingWssConfigs.get(index);
    }

    public void removeOutgoingWssAt(int row) {
        OutgoingWss outgoingWss = outgoingWssConfigs.remove(row);
        fireOutgoingWssRemoved(outgoingWss);
        outgoingWss.release();
        getConfig().removeOutgoing(row);
    }

    public WssCrypto getCryptoByName(String cryptoName, boolean outgoingWSSConfig) {
        for (WssCrypto crypto : cryptos) {
            if (crypto.getLabel().equals(cryptoName)) {
                if (outgoingWSSConfig) {
                    if (crypto.getType() == CryptoType.KEYSTORE) {
                        return crypto;
                    }
                } else {
                    return crypto;
                }
            }
        }

        return null;
    }

    public WssCrypto getCryptoByName(String cryptoName) {
        return getCryptoByName(cryptoName, false);
    }

    public IncomingWss getIncomingWssByName(String incomingName) {
        for (IncomingWss incomingWss : incomingWssConfigs) {
            if (incomingWss.getName().equals(incomingName)) {
                return incomingWss;
            }
        }

        return null;
    }

    public OutgoingWss getOutgoingWssByName(String outgoingName) {
        for (OutgoingWss crypto : outgoingWssConfigs) {
            if (crypto.getName().equals(outgoingName)) {
                return crypto;
            }
        }

        return null;
    }

    public void addWssContainerListener(WssContainerListener listener) {
        listeners.add(listener);
    }

    public void removeWssContainerListener(WssContainerListener listener) {
        listeners.remove(listener);
    }

    public void fireWssEntryAdded(WssEntry newEntry) {
        for (WssContainerListener listener : listeners.toArray(new WssContainerListener[listeners.size()])) {
            listener.outgoingWssEntryAdded(newEntry);
        }
    }

    public void fireWssEntryRemoved(WssEntry entry) {
        for (WssContainerListener listener : listeners.toArray(new WssContainerListener[listeners.size()])) {
            listener.outgoingWssEntryRemoved(entry);
        }
    }

    public String[] getCryptoNames() {
        StringList result = new StringList();

        for (WssCrypto crypto : getCryptoList()) {
            result.add(crypto.getLabel());
        }

        return result.toStringArray();
    }

    public String[] getIncomingWssNames() {
        StringList result = new StringList();

        for (IncomingWss crypto : getIncomingWssList()) {
            result.add(crypto.getName());
        }

        return result.toStringArray();
    }

    public String[] getOutgoingWssNames() {
        StringList result = new StringList();

        for (OutgoingWss crypto : getOutgoingWssList()) {
            result.add(crypto.getName());
        }

        return result.toStringArray();
    }

    // FIXME: Why is this method empty?
    public void importConfig(WssContainer wssContainer) {
    }

    // FIXME: Not used?
    public void resetConfig(WssContainerConfig config) {
        getConfig().set(config);

        for (int c = 0; c < cryptos.size(); c++) {
            ((KeyMaterialWssCrypto) cryptos.get(c)).udpateConfig(getConfig().getCryptoArray(c));
        }

        for (int c = 0; c < incomingWssConfigs.size(); c++) {
            incomingWssConfigs.get(c).updateConfig(getConfig().getIncomingArray(c));
        }

        for (int c = 0; c < outgoingWssConfigs.size(); c++) {
            outgoingWssConfigs.get(c).updateConfig(getConfig().getOutgoingArray(c));
        }
    }

    public void fireCryptoUpdated(KeyMaterialWssCrypto crypto) {
        for (WssContainerListener listener : listeners.toArray(new WssContainerListener[listeners.size()])) {
            listener.cryptoUpdated(crypto);
        }
    }

    public void resolve(ResolveContext<?> context) {
        for (int c = 0; c < cryptos.size(); c++) {
            ((KeyMaterialWssCrypto) cryptos.get(c)).resolve(context);
        }

        for (int c = 0; c < incomingWssConfigs.size(); c++) {
            incomingWssConfigs.get(c).resolve(context);
        }

        for (int c = 0; c < outgoingWssConfigs.size(); c++) {
            outgoingWssConfigs.get(c).resolve(context);
        }
    }

    public void addExternalDependency(List<ExternalDependency> dependencies) {
        for (int c = 0; c < cryptos.size(); c++) {
            ((KeyMaterialWssCrypto) cryptos.get(c)).addExternalDependency(dependencies);
        }
    }

    // FIXME: Why is this method empty?
    public void release() {
    }
}
