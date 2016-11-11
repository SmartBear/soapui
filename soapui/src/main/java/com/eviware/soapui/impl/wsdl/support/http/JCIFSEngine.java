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

package com.eviware.soapui.impl.wsdl.support.http;

import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;
import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMEngineException;

import java.io.IOException;

public class JCIFSEngine implements NTLMEngine {

    public String generateType1Msg(String domain, String workstation) throws NTLMEngineException {
        Type1Message t1m = new Type1Message(Type1Message.getDefaultFlags(), domain, workstation);
        return Base64.encode(t1m.toByteArray());
    }

    public String generateType3Msg(String username, String password, String domain, String workstation, String challenge)
            throws NTLMEngineException {
        Type2Message t2m;
        try {
            t2m = new Type2Message(Base64.decode(challenge));
        } catch (IOException ex) {
            throw new NTLMEngineException("Invalid Type2 message", ex);
        }
        Type3Message t3m = new Type3Message(t2m, password, domain, username, workstation);
        return Base64.encode(t3m.toByteArray());
    }

}
