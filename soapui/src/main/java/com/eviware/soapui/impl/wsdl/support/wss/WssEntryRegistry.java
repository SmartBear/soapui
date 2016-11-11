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

import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.entries.AutomaticSAMLEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.EncryptionEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.ManualSAMLEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.SignatureEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.TimestampEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.UsernameEntry;
import com.eviware.soapui.support.registry.AbstractRegistry;

public class WssEntryRegistry extends AbstractRegistry<WssEntry, WSSEntryConfig, OutgoingWss> {
    private static WssEntryRegistry instance;

    public WssEntryRegistry() {
        mapType(UsernameEntry.TYPE, UsernameEntry.class);
        mapType(TimestampEntry.TYPE, TimestampEntry.class);
        mapType(ManualSAMLEntry.TYPE, ManualSAMLEntry.class);
        mapType(AutomaticSAMLEntry.TYPE, AutomaticSAMLEntry.class);
        mapType(SignatureEntry.TYPE, SignatureEntry.class);
        mapType(EncryptionEntry.TYPE, EncryptionEntry.class);
    }

    public static synchronized WssEntryRegistry get() {
        if (instance == null) {
            instance = new WssEntryRegistry();
        }

        return instance;
    }

    @Override
    protected WSSEntryConfig addNewConfig(OutgoingWss container) {
        return container.getConfig().addNewEntry();
    }
}
