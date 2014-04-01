/*
 * Copyright 2004-2014 SmartBear Software
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

/**
 * @author Erik R. Yverling
 * 
 *         Generic test utils and data
 */
public class TestUtils
{
	public static final String SAMPLE_SOAP_MESSAGE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.app4test.eviware.com/\">"
			+ "<soapenv:Header/>" + "<soapenv:Body>" + "<ws:getItems/>" + "</soapenv:Body>" + "</soapenv:Envelope>";

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
