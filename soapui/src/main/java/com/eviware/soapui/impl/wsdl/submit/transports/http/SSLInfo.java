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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.security.Principal;
import java.security.cert.Certificate;

/**
 * Holder for SSL-Related details for a request/response interchange
 *
 * @author ole.matzura
 */

public class SSLInfo {
    private String cipherSuite;
    private Principal localPrincipal;
    private Certificate[] localCertificates;
    private Principal peerPrincipal;
    private Certificate[] peerCertificates;
    private boolean peerUnverified;

    public SSLInfo(SSLSession session) {
        cipherSuite = session.getCipherSuite();
        localPrincipal = session.getLocalPrincipal();
        localCertificates = session.getLocalCertificates();
        try {
            peerPrincipal = session.getPeerPrincipal();
            peerCertificates = session.getPeerCertificates();
        } catch (SSLPeerUnverifiedException e) {
            peerUnverified = true;
        }
    }

    public String getCipherSuite() {
        return cipherSuite;
    }

    public Certificate[] getLocalCertificates() {
        return localCertificates;
    }

    public Principal getLocalPrincipal() {
        return localPrincipal;
    }

    public Certificate[] getPeerCertificates() {
        return peerCertificates;
    }

    public Principal getPeerPrincipal() {
        return peerPrincipal;
    }

    public boolean isPeerUnverified() {
        return peerUnverified;
    }
}
