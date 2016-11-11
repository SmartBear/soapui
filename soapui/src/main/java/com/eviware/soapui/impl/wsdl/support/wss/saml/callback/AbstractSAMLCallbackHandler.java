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
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.saml.ext.SAMLCallback;
import org.apache.ws.security.saml.ext.bean.ActionBean;
import org.apache.ws.security.saml.ext.bean.AttributeBean;
import org.apache.ws.security.saml.ext.bean.AttributeStatementBean;
import org.apache.ws.security.saml.ext.bean.AuthDecisionStatementBean;
import org.apache.ws.security.saml.ext.bean.AuthenticationStatementBean;
import org.apache.ws.security.saml.ext.bean.KeyInfoBean;
import org.apache.ws.security.saml.ext.bean.KeyInfoBean.CERT_IDENTIFIER;
import org.apache.ws.security.saml.ext.bean.SubjectBean;
import org.apache.ws.security.saml.ext.bean.SubjectLocalityBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

/*
 * @author Erik R. Yverling
 * 
 * A base implementation of a Callback Handler for a SAML assertion. By
 * default it creates an authentication assertion.
 * 
 */
public abstract class AbstractSAMLCallbackHandler implements SAMLCallbackHandler {

    public enum Statement {
        AUTHN, ATTR, AUTHZ
    }

    ;

    protected String subjectName = null;
    protected String subjectQualifier = null;
    protected String confirmationMethod = null;
    protected X509Certificate[] certs;
    protected Statement statement = Statement.AUTHN;
    protected CERT_IDENTIFIER certIdentifier = CERT_IDENTIFIER.X509_CERT;
    protected byte[] ephemeralKey = null;
    protected String issuer = null;
    protected String subjectNameIDFormat = null;
    protected String subjectLocalityIpAddress = null;
    protected String subjectLocalityDnsAddress = null;
    protected String resource = null;
    protected List<?> customAttributeValues = null;
    private Crypto crypto;
    private String alias;
    private String customAttributeName;

    /**
     * Use this for signed assertion
     */
    public AbstractSAMLCallbackHandler(Crypto crypto, String alias, String assertionTypeFriendlyName,
                                       String confirmationMethodFriendlyName) {
        this.crypto = crypto;
        this.alias = alias;
        setStatement(assertionTypeFriendlyName);
        setConfirmationMethod(confirmationMethodFriendlyName);
    }

    /**
     * Use this is for unsigned assertions
     */
    public AbstractSAMLCallbackHandler(String assertionTypeFriendlyName, String confirmationMethodFriendlyName) {
        setStatement(assertionTypeFriendlyName);
        setConfirmationMethod(confirmationMethodFriendlyName);
    }

    @Override
    public void setCertIdentifier(CERT_IDENTIFIER certIdentifier) {
        this.certIdentifier = certIdentifier;
    }

    @Override
    public void setCerts(X509Certificate[] certs) {
        this.certs = certs;
    }

    @Override
    public byte[] getEphemeralKey() {
        return ephemeralKey;
    }

    @Override
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @Override
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    @Override
    public void setSubjectQualifier(String subjectQualifier) {
        this.subjectQualifier = subjectQualifier;
    }

    @Override
    public void setSubjectNameIDFormat(String subjectNameIDFormat) {
        this.subjectNameIDFormat = subjectNameIDFormat;
    }

    @Override
    public void setSubjectLocality(String ipAddress, String dnsAddress) {
        this.subjectLocalityIpAddress = ipAddress;
        this.subjectLocalityDnsAddress = dnsAddress;
    }

    @Override
    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public void setCustomAttributeName(String customAttributeName) {
        this.customAttributeName = customAttributeName;
    }

    @Override
    public void setCustomAttributeValues(List<?> customAttributeValues) {
        this.customAttributeValues = customAttributeValues;
    }

    @Override
    public Crypto getCrypto() {
        return crypto;
    }

    @Override
    public void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public void setStatement(String statement) {
        if (statement.equals(AutomaticSAMLEntry.AUTHENTICATION_ASSERTION_TYPE)) {
            this.statement = Statement.AUTHN;
        } else if (statement.equals(AutomaticSAMLEntry.ATTRIBUTE_ASSERTION_TYPE)) {
            this.statement = Statement.ATTR;
        } else if (statement.equals(AutomaticSAMLEntry.AUTHORIZATION_ASSERTION_TYPE)) {
            this.statement = Statement.AUTHZ;
        }
    }

    /**
     * Note that the SubjectBean parameter should be null for SAML2.0
     */
    protected void createAndSetStatement(SubjectBean subjectBean, SAMLCallback callback) {
        if (statement == Statement.AUTHN) {
            AuthenticationStatementBean authBean = new AuthenticationStatementBean();
            if (subjectBean != null) {
                authBean.setSubject(subjectBean);
            }
            if (subjectLocalityIpAddress != null || subjectLocalityDnsAddress != null) {
                SubjectLocalityBean subjectLocality = new SubjectLocalityBean();
                subjectLocality.setIpAddress(subjectLocalityIpAddress);
                subjectLocality.setDnsAddress(subjectLocalityDnsAddress);
                authBean.setSubjectLocality(subjectLocality);
            }
            authBean.setAuthenticationMethod("Password");
            callback.setAuthenticationStatementData(Collections.singletonList(authBean));
        } else if (statement == Statement.ATTR) {
            AttributeStatementBean attrBean = new AttributeStatementBean();
            if (subjectBean != null) {
                attrBean.setSubject(subjectBean);
            }
            AttributeBean attributeBean = new AttributeBean();
            attributeBean.setSimpleName(customAttributeName);
            if (customAttributeValues != null) {
                attributeBean.setCustomAttributeValues(customAttributeValues);
            }

            // TODO This should be removed
            else {
                attributeBean.setAttributeValues(Collections.singletonList("user"));
            }

            attrBean.setSamlAttributes(Collections.singletonList(attributeBean));
            callback.setAttributeStatementData(Collections.singletonList(attrBean));
        } else {
            AuthDecisionStatementBean authzBean = new AuthDecisionStatementBean();
            if (subjectBean != null) {
                authzBean.setSubject(subjectBean);
            }
            ActionBean actionBean = new ActionBean();
            actionBean.setContents("Read");
            authzBean.setActions(Collections.singletonList(actionBean));
            authzBean.setResource("endpoint");
            authzBean.setDecision(AuthDecisionStatementBean.Decision.PERMIT);
            authzBean.setResource(resource);
            callback.setAuthDecisionStatementData(Collections.singletonList(authzBean));
        }
    }

    protected KeyInfoBean createKeyInfo() throws Exception {
        KeyInfoBean keyInfo = new KeyInfoBean();
        if (statement == Statement.AUTHN) {
            keyInfo.setCertificate(certs[0]);
            keyInfo.setCertIdentifer(certIdentifier);
        } else if (statement == Statement.ATTR) {
            // Build a new Document
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // Create an Encrypted Key
            WSSecEncryptedKey encrKey = new WSSecEncryptedKey();
            encrKey.setKeyIdentifierType(WSConstants.X509_KEY_IDENTIFIER);
            encrKey.setUseThisCert(certs[0]);
            encrKey.prepare(doc, null);
            ephemeralKey = encrKey.getEphemeralKey();
            Element encryptedKeyElement = encrKey.getEncryptedKeyElement();

            // Append the EncryptedKey to a KeyInfo element
            Element keyInfoElement = doc.createElementNS(WSConstants.SIG_NS, WSConstants.SIG_PREFIX + ":"
                    + WSConstants.KEYINFO_LN);
            keyInfoElement.setAttributeNS(WSConstants.XMLNS_NS, "xmlns:" + WSConstants.SIG_PREFIX, WSConstants.SIG_NS);
            keyInfoElement.appendChild(encryptedKeyElement);

            keyInfo.setElement(keyInfoElement);
        }
        return keyInfo;
    }
}
