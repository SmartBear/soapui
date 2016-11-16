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

import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.WssEntry;
import com.eviware.soapui.impl.wsdl.support.wss.crypto.CryptoType;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Merlin;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public abstract class WssEntryBase implements WssEntry, PropertyExpansionContainer {
    private WSSEntryConfig config;
    private OutgoingWss outgoingWss;
    private JComponent configComponent;
    private String label;

    public void init(WSSEntryConfig config, OutgoingWss outgoingWss, String label) {
        this.config = config;
        this.outgoingWss = outgoingWss;
        this.label = label;

        if (config.getConfiguration() == null) {
            config.addNewConfiguration();
        }

        load(new XmlObjectConfigurationReader(config.getConfiguration()));
    }

    public OutgoingWss getOutgoingWss() {
        return outgoingWss;
    }

    public String getPassword() {
        String password = config.getPassword();
        if (StringUtils.isNullOrEmpty(password)) {
            password = outgoingWss.getPassword();
        }

        return password;
    }

    public String getUsername() {
        String username = config.getUsername();
        if (StringUtils.isNullOrEmpty(username)) {
            username = outgoingWss.getUsername();
        }

        return username;
    }

    public void setPassword(String arg0) {
        config.setPassword(arg0);
    }

    public void setUsername(String arg0) {
        config.setUsername(arg0);
    }

    public JComponent getConfigurationPanel() {
        if (configComponent == null) {
            configComponent = buildUI();
        }

        return configComponent;
    }

    public String getLabel() {
        return label;
    }

    protected abstract JComponent buildUI();

    protected abstract void load(XmlObjectConfigurationReader reader);

    public void setConfig(WSSEntryConfig config) {
        this.config = config;
    }

    public void saveConfig() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        save(builder);
        config.getConfiguration().set(builder.finish());
    }

    protected abstract void save(XmlObjectConfigurationBuilder builder);

    public WssContainer getWssContainer() {
        return outgoingWss.getWssContainer();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(getWssContainer().getModelItem(), this);

        addPropertyExpansions(result);

        return result.toArray();
    }

    protected void addPropertyExpansions(PropertyExpansionsResult result) {
        if (StringUtils.hasContent(config.getUsername())) {
            result.extractAndAddAll("username");
        }

        if (StringUtils.hasContent(config.getPassword())) {
            result.extractAndAddAll("password");
        }
    }

    protected int readKeyIdentifierType(XmlObjectConfigurationReader reader) {
        int identifierType = reader.readInt("keyIdentifierType", WSConstants.ISSUER_SERIAL);

        //For backward compatibility see SOAP-2347
        if(identifierType == 0)
        {
            return WSConstants.ISSUER_SERIAL;
        }
        return identifierType;
    }

    public void updateEntryConfig(WSSEntryConfig config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    // Used to save values from table GUI components
    protected List<StringToStringMap> readTableValues(XmlObjectConfigurationReader reader, String parameterName) {
        List<StringToStringMap> result = new ArrayList<StringToStringMap>();
        String[] tableValues = reader.readStrings(parameterName);
        if (tableValues != null && tableValues.length > 0) {
            for (String tableValue : tableValues) {
                result.add(StringToStringMap.fromXml(tableValue));
            }
        }

        return result;
    }

    // Used to read values from table GUI components
    protected void saveTableValues(XmlObjectConfigurationBuilder builder, List<StringToStringMap> tableValues,
                                   String string) {
        for (StringToStringMap tableValue : tableValues) {
            builder.add(string, tableValue.toXml());
        }
    }

    protected Vector<WSEncryptionPart> createWSParts(List<StringToStringMap> parts) {
        Vector<WSEncryptionPart> result = new Vector<WSEncryptionPart>();

        for (StringToStringMap map : parts) {
            if (map.hasValue("id")) {
                result.add(new WSEncryptionPart(map.get("id"), map.get("enc")));
            } else {
                String ns = map.get("namespace");
                if (ns == null) {
                    ns = "";
                }

                String name = map.get("name");
                if (StringUtils.hasContent(name)) {
                    result.add(new WSEncryptionPart(name, ns, map.get("enc")));
                }
            }
        }

        return result;
    }

    protected class KeyIdentifierTypeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value.equals(1)) {
                setText("Binary Security Token");
            } else if (value.equals(2)) {
                setText("Issuer Name and Serial Number");
            } else if (value.equals(3)) {
                setText("X509 Certificate");
            } else if (value.equals(4)) {
                setText("Subject Key Identifier");
            } else if (value.equals(5)) {
                setText("Embedded KeyInfo");
            } else if (value.equals(6)) {
                setText("Embed SecurityToken Reference");
            } else if (value.equals(7)) {
                setText("UsernameToken Signature");
            } else if (value.equals(8)) {
                setText("Thumbprint SHA1 Identifier");
            } else if (value.equals(9)) {
                setText("Custom Reference");
            } else if( value.equals(12) ) {
                setText("Custom Key Identifier");
            }

            return result;
        }
    }

    protected class KeyAliasComboBoxModel extends AbstractListModel implements ComboBoxModel {
        private KeyStore keyStore;
        private Object alias;
        private StringList aliases = new StringList();

        public KeyAliasComboBoxModel(WssCrypto crypto) {
            update(crypto);
        }

        void update(WssCrypto crypto) {
            try {
                if (crypto == null || crypto.getCrypto() == null) {
                    keyStore = null;
                } else {
                    Merlin merlinCrypto = (Merlin) crypto.getCrypto();

                    if (crypto.getType() == CryptoType.KEYSTORE) {
                        keyStore = merlinCrypto.getKeyStore();
                    } else if (crypto.getType() == CryptoType.TRUSTSTORE) {
                        keyStore = merlinCrypto.getTrustStore();
                    }
                }
            } catch (WSSecurityException wssecurityException) {
                wssecurityException.printStackTrace();
            }

            if (keyStore != null) {
                if (!aliases.isEmpty()) {
                    int sz = aliases.size();
                    aliases.clear();
                    fireIntervalRemoved(this, 0, sz - 1);
                }

                try {
                    for (Enumeration e = keyStore.aliases(); e.hasMoreElements(); ) {
                        aliases.add(e.nextElement().toString());
                    }

                    fireIntervalAdded(this, 0, aliases.size() - 1);
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                }
            }
        }

        public Object getSelectedItem() {
            return alias;
        }

        public void setSelectedItem(Object anItem) {
            this.alias = anItem;
        }

        public Object getElementAt(int index) {
            return aliases.get(index);
        }

        public int getSize() {
            return aliases.size();
        }
    }

    public void release() {
    }
}
