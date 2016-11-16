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

package com.eviware.soapui.impl.wsdl.support.wss.saml.callback;

import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.saml.ext.bean.KeyInfoBean.CERT_IDENTIFIER;

import javax.security.auth.callback.CallbackHandler;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @author Erik R. Yverling
 *         <p/>
 *         A generic SAML callback handler.
 */
public interface SAMLCallbackHandler extends CallbackHandler {

    public abstract void setAlias(String alias);

    public abstract String getAlias();

    public abstract void setCrypto(Crypto crypto);

    public abstract Crypto getCrypto();

    public abstract void setCustomAttributeValues(List<?> customAttributeValues);

    public abstract void setResource(String resource);

    public abstract void setSubjectLocality(String ipAddress, String dnsAddress);

    public abstract void setSubjectNameIDFormat(String subjectNameIDFormat);

    public abstract void setIssuer(String issuer);

    public void setSubjectName(String subjectName);

    public void setSubjectQualifier(String subjectQualifier);

    public abstract byte[] getEphemeralKey();

    public abstract void setCerts(X509Certificate[] certs);

    public abstract void setCertIdentifier(CERT_IDENTIFIER certIdentifier);

    public abstract void setStatement(String statement);

    public abstract void setConfirmationMethod(String confMethod);

    public abstract void setCustomAttributeName(String customAttributeName);

}
