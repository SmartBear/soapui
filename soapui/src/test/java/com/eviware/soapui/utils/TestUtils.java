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

package com.eviware.soapui.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Erik R. Yverling
 *         <p/>
 *         Generic test utils and data
 */
public class TestUtils {
    public static final String SAMPLE_SOAP_MESSAGE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.app4test.eviware.com/\">"
            + "<soapenv:Header/>" + "<soapenv:Body>" + "<ws:getItems/>" + "</soapenv:Body>" + "</soapenv:Envelope>";

    private static final String NOW = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ").format(new Date());
    private static final String NOW_PLUS_10_MIN = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ").format(new Date(System.currentTimeMillis() + 10*60*1000));

    public static final String SAMPLE_SOAP_MESSAGE_CUSTOM_TOKEN = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.app4test.eviware.com/\"><soapenv:Header><wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"><saml1:Assertion AssertionID=\"Assertion-01\" Issuer=\"sts\" IssueInstant=\"" + NOW + "\" MajorVersion=\"1\" MinorVersion=\"1\" xmlns:saml1=\"urn:oasis:names:tc:SAML:1.0:assertion\"><saml1:Conditions NotBefore=\"" + NOW + "\" NotOnOrAfter=\"" + NOW_PLUS_10_MIN + "\"/><saml1:AuthenticationStatement AuthenticationInstant=\"" + NOW + "\" AuthenticationMethod=\"urn:oasis:names:tc:saml1:1.0:am:X509-PKI\"><saml1:Subject><saml1:NameIdentifier Format=\"urn:oasis:names:tc:SAML:1.1:nameidformat:X509SubjectName\">CN=Foo Bar,OU=Development,O=FooBar Inc.,L=Springfield,ST=CA,C=US</saml1:NameIdentifier><saml1:SubjectConfirmation><saml1:ConfirmationMethod>urn:oasis:names:tc:saml1:1.0:cm:holder-of-key</saml1:ConfirmationMethod><ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"><ds:X509Data><!-- subject's X.509 cert --><ds:X509Certificate>MIICVTCCAb6gAwIBAgIEUvXqfDANBgkqhkiG9w0BAQsFADBuMQswCQYDVQQGEwJVUzELMAkGA1UECAwCQ0ExFDASBgNVBAcMC1NwcmluZ2ZpZWxkMRQwEgYDVQQKDAtGb29CYXIgSW5jLjEUMBIGA1UECwwLRGV2ZWxvcG1lbnQxEDAOBgNVBAMMB0ZvbyBCYXIwIBcNMTQwMjA4MDgyODI1WhgPMjA2NDAyMDgwODI4MjVaMG4xCzAJBgNVBAYTAlVTMQswCQYDVQQIDAJDQTEUMBIGA1UEBwwLU3ByaW5nZmllbGQxFDASBgNVBAoMC0Zvb0JhciBJbmMuMRQwEgYDVQQLDAtEZXZlbG9wbWVudDEQMA4GA1UEAwwHRm9vIEJhcjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAlrVW+qjhPYP0W7z4Z6Vi8SLyawfW2pyDYkZ296+XetWiS37sFvt4VANPNlLnML/D7BIpUozOiV2Uz2zoVtGxrkvyRRk7aJ2dx40ZZmFbepphn56DlOHbZ5438Qvj6olygRmOdDF3nQuo6OO5gLHVffVYHX0iW0P8JoacNHhR2NUCAwEAATANBgkqhkiG9w0BAQsFAAOBgQAXSwsdDo0/hZmWIaezpUuO+sgk1rcAzhyyxtM/2dAikhGmV5bsWauZdHZXD0wkdmu7c/kxF9PJdjedieY80UzuApFYChOnZqCUhxTt7tkweCAPra+nYR/tnOqo/DPhRc9gYMIljIILPcOsYA6bqLoX4hoXVjUzi5tUosQZz+LdFw==</ds:X509Certificate></ds:X509Data></ds:KeyInfo></saml1:SubjectConfirmation></saml1:Subject></saml1:AuthenticationStatement></saml1:Assertion></wsse:Security></soapenv:Header><soapenv:Body><ws:getItems/></soapenv:Body></soapenv:Envelope>";

    public static final String SAMPLE_SAML_1_ASSERTION = "<saml1:Assertion AssertionID=\"AB98E061D5A2D3696C13234260487061\" "
            + "IssueInstant=\"2011-12-09T10:20:48.641Z\" MajorVersion=\"1\" MinorVersion=\"1\" xsi:type=\"saml1:AssertionType\" "
            + "xmlns:saml1=\"urn:oasis:names:tc:SAML:1.0:assertion\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
            + "<saml1:Conditions NotBefore=\"2011-12-09T10:20:48.715Z\" NotOnOrAfter=\"2011-12-09T10:25:48.715Z\"/>"
            + "<saml1:AuthenticationStatement AuthenticationInstant=\"2011-12-09T10:20:48.712Z\" AuthenticationMethod=\"urn:oasis:names:tc:SAML:1.0:am:password\" "
            + "xsi:type=\"saml1:AuthenticationStatementType\"><saml1:Subject>"
            + "<saml1:NameIdentifier Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\"/>"
            + "<saml1:SubjectConfirmation><saml1:ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:sender-vouches</saml1:ConfirmationMethod>"
            + "</saml1:SubjectConfirmation></saml1:Subject></saml1:AuthenticationStatement></saml1:Assertion>";
}
