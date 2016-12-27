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

package com.eviware.soapui.testondemand;

import com.eviware.soapui.SoapUI;
import com.google.common.base.Charsets;
import flex.messaging.util.URLDecoder;

import java.io.UnsupportedEncodingException;

/**
 * @author Erik R. Yverling
 *         <p/>
 *         An AlertSite location for running the Test On Demand.
 */
public class Location {
    private String code;
    private String name;
    private String[] serverIPAddresses;

    public Location(String code, String name, String[] serverIPAddresses) {
        this.code = code;
        this.name = name;
        this.serverIPAddresses = serverIPAddresses;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return getURLDecodedName();
    }

    public String[] getServerIPAddresses() {
        return serverIPAddresses;
    }

    private String getURLDecodedName() {
        // We'll return the encoded name if the decoding fails
        String decodedName = name;
        try {
            decodedName = URLDecoder.decode(name, Charsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            SoapUI.logError(e);
        }
        return decodedName;
    }

    @Override
    public String toString() {
        return getURLDecodedName();
    }
}
