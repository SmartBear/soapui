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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.OutgoingWssConfig;
import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.entries.ManualSAMLEntry;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.resolver.ResolveContext;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class OutgoingWss implements PropertyExpansionContainer {
    private static final String OLD_MANUAL_SAML_ENTRY_TYPE = "SAML";

    public static final String WSSENTRY_PROPERTY = OutgoingWss.class.getName() + "@wssEntry";

    private static final int MOVE_DOWN = 1;
    private static final int MOVE_UP = -1;

    private OutgoingWssConfig config;
    private List<WssEntry> entries = new ArrayList<WssEntry>();
    private final DefaultWssContainer container;

    public OutgoingWss(OutgoingWssConfig config, DefaultWssContainer container) {
        this.config = config;
        this.container = container;

        for (WSSEntryConfig entryConfig : config.getEntryList()) {

            convertOldManualSAMLEntry(entryConfig);

            WssEntry entry = WssEntryRegistry.get().build(entryConfig, this);
            if (entry != null) {
                entries.add(entry);
            }
        }
    }

    public WssContainer getWssContainer() {
        return container;
    }

    public String getName() {
        return config.getName();
    }

    public String getPassword() {
        return config.getPassword();
    }

    public String getUsername() {
        return config.getUsername();
    }

    public void setName(String arg0) {
        config.setName(arg0);
    }

    public void setPassword(String arg0) {
        config.setPassword(arg0);
    }

    public void setUsername(String arg0) {
        config.setUsername(arg0);
    }

    public String getActor() {
        return config.getActor();
    }

    public boolean getMustUnderstand() {
        return config.getMustUnderstand();
    }

    public void setActor(String arg0) {
        config.setActor(arg0);
    }

    public void setMustUnderstand(boolean arg0) {
        config.setMustUnderstand(arg0);
    }

    public WssEntry addEntry(String type) {
        WssEntry newEntry = WssEntryRegistry.get().create(type, this);
        entries.add(newEntry);

        container.fireWssEntryAdded(newEntry);

        return newEntry;
    }

    public void removeEntry(WssEntry entry) {
        int index = entries.indexOf(entry);

        container.fireWssEntryRemoved(entries.remove(index));
        config.removeEntry(index);
        entry.release();
    }

    public void moveEntry(WssEntry entry, int offset) {
        int indexBeforeMove = entries.indexOf(entry);
        if ((offset == MOVE_UP && indexBeforeMove > 0)
                || (offset == MOVE_DOWN && indexBeforeMove < entries.size() - 1)) {
            WssEntry adjacentEntry = entries.get(indexBeforeMove + offset);

            entries.set(indexBeforeMove + offset, entry);
            entries.set(indexBeforeMove, adjacentEntry);

            WSSEntryConfig entryConfig = (WSSEntryConfig) config.getEntryList().get(indexBeforeMove).copy();
            WSSEntryConfig adjacentEntryConfig = (WSSEntryConfig) config.getEntryList().get(indexBeforeMove + offset)
                    .copy();

            config.getEntryList().set(indexBeforeMove + offset, entryConfig);
            config.getEntryList().set(indexBeforeMove, adjacentEntryConfig);

            entry.updateEntryConfig(config.getEntryList().get(indexBeforeMove + offset));
            adjacentEntry.updateEntryConfig(config.getEntryList().get(indexBeforeMove));

            container.fireWssEntryMoved(entry, offset);
        }
    }

    public OutgoingWssConfig getConfig() {
        return config;
    }

    public void processOutgoing(Document soapDocument, PropertyExpansionContext context) throws WSSecurityException {
        Element header = WSSecurityUtil.findWsseSecurityHeaderBlock(soapDocument, soapDocument.getDocumentElement(),
                false);

        while (header != null) {
            header.getParentNode().removeChild(header);
            header = WSSecurityUtil.findWsseSecurityHeaderBlock(soapDocument, soapDocument.getDocumentElement(), false);
        }

        WSSecHeader secHeader = new WSSecHeader();

        if (StringUtils.hasContent(getActor())) {
            secHeader.setActor(getActor());
        }

        secHeader.setMustUnderstand(getMustUnderstand());

        secHeader.insertSecurityHeader(soapDocument);

        for (WssEntry entry : entries) {
            try {
                entry.process(secHeader, soapDocument, context);
            } catch (Throwable e) {
                SoapUI.logError(e);
            }
        }
    }

    public List<WssEntry> getEntries() {
        return entries;
    }

    public void updateConfig(OutgoingWssConfig config) {
        this.config = config;

        for (int c = 0; c < entries.size(); c++) {
            entries.get(c).updateEntryConfig(this.config.getEntryArray(c));
        }
    }

    public void release() {
        for (WssEntry entry : entries) {
            entry.release();
        }
    }

    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(getWssContainer().getModelItem(), this);

        result.extractAndAddAll("username");
        result.extractAndAddAll("password");

        for (WssEntry entry : entries) {
            if (entry instanceof PropertyExpansionContainer) {
                result.addAll(((PropertyExpansionContainer) entry).getPropertyExpansions());
            }
        }

        return result.toArray();
    }

    public void resolve(ResolveContext<?> context) {
    }

    // Used to support backwards compatibility (< 4.5 - 4.5)
    private void convertOldManualSAMLEntry(WSSEntryConfig entryConfig) {
        if (entryConfig.getType().equals(OLD_MANUAL_SAML_ENTRY_TYPE)) {
            entryConfig.setType(ManualSAMLEntry.TYPE);
        }
    }
}
