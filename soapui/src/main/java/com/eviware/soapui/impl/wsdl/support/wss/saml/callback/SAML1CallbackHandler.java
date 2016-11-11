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

import com.eviware.soapui.impl.wsdl.support.wss.entries.AutomaticSAMLEntry;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoType;
import org.apache.ws.security.saml.ext.SAMLCallback;
import org.apache.ws.security.saml.ext.bean.KeyInfoBean;
import org.apache.ws.security.saml.ext.bean.SubjectBean;
import org.apache.ws.security.saml.ext.builder.SAML1Constants;
import org.opensaml.common.SAMLVersion;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * @author Erik R. Yverling
 *         <p/>
 *         A Callback Handler implementation for a SAML 1.1 assertion. By
 *         default it creates an authentication assertion using Sender Vouches.
 */
public class SAML1CallbackHandler extends AbstractSAMLCallbackHandler {

    /**
     * Use this for signed assertion
     */
    public SAML1CallbackHandler(Crypto crypto, String alias, String assertionTypeFriendlyName, String confirmationMethodFriendlyName)
            throws Exception {
        super(crypto, alias, assertionTypeFriendlyName, confirmationMethodFriendlyName);

        if (certs == null) {
            CryptoType cryptoType = new CryptoType(CryptoType.TYPE.ALIAS);
            cryptoType.setAlias(alias);
            certs = crypto.getX509Certificates(cryptoType);
        }
    }

    /**
     * Use this is for unsigned assertions
     */
    public SAML1CallbackHandler(String assertionTypeFriendlyName, String confirmationMethodFriendlyName) {
        super(assertionTypeFriendlyName, confirmationMethodFriendlyName);
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof SAMLCallback) {
                SAMLCallback callback = (SAMLCallback) callbacks[i];
                callback.setSamlVersion(SAMLVersion.VERSION_11);
                callback.setIssuer(issuer);
                SubjectBean subjectBean = new SubjectBean(subjectName, subjectQualifier, confirmationMethod);
                if (subjectNameIDFormat != null) {
                    subjectBean.setSubjectNameIDFormat(subjectNameIDFormat);
                }
                if (SAML1Constants.CONF_HOLDER_KEY.equals(confirmationMethod)) {
                    try {
                        KeyInfoBean keyInfo = createKeyInfo();
                        subjectBean.setKeyInfo(keyInfo);
                    } catch (Exception ex) {
                        throw new IOException("Problem creating KeyInfo: " + ex.getMessage());
                    }
                }
                createAndSetStatement(subjectBean, callback);
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }

    @Override
    public void setConfirmationMethod(String confirmationMethodFriendlyName) {
        if (confirmationMethodFriendlyName.equals(AutomaticSAMLEntry.HOLDER_OF_KEY_CONFIRMATION_METHOD)) {
            confirmationMethod = SAML1Constants.CONF_HOLDER_KEY;
        } else if (confirmationMethodFriendlyName.equals(AutomaticSAMLEntry.SENDER_VOUCHES_CONFIRMATION_METHOD)) {
            confirmationMethod = SAML1Constants.CONF_SENDER_VOUCHES;
        }
    }
}
