/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
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

import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.WSSecSignature;

import javax.xml.crypto.dsig.Reference;
import java.util.List;

public class ImprovedWSSecSignature extends WSSecSignature {

    // Set to false to enable strict layout compatbility
    // Defaults to wss4j behavior
    private boolean prependSignature = true;

    public ImprovedWSSecSignature() {
        super();
    }

    public ImprovedWSSecSignature(WSSConfig config) {
        super(config);
    }

    @Override
    public void computeSignature(List<Reference> referenceList) throws WSSecurityException {
        computeSignature(referenceList, prependSignature, null);
    }

    public boolean getPrependSignature() {
        return prependSignature;
    }

    public void setPrependSignature(boolean prependSignature) {
        this.prependSignature = prependSignature;
    }
}
