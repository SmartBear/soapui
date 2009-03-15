package com.eviware.soapui.impl.wsdl.support.soap;

import junit.framework.TestCase;

public class SoapUtilsTestCase extends TestCase
{
   public void testTransferSoapHeaders1() throws Exception
   {
   	String newStr = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
   						 "<soapenv:Header/><soapenv:Body/></soapenv:Envelope>";

   	String oldStr = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
		 "<soapenv:Header><test/></soapenv:Header><soapenv:Body/></soapenv:Envelope>";

   	assertEquals( oldStr, SoapUtils.transferSoapHeaders(oldStr, newStr, SoapVersion.Soap11));
   }

   public void testTransferSoapHeaders2() throws Exception
   {
   	String newStr = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
   						 "<soapenv:Body/></soapenv:Envelope>";

   	String oldStr = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
		 "<soapenv:Header><test/></soapenv:Header><soapenv:Body/></soapenv:Envelope>";

   	assertEquals( oldStr, SoapUtils.transferSoapHeaders(oldStr, newStr, SoapVersion.Soap11));
   }

   public void testTransferSoapHeaders3() throws Exception
   {
   	String newStr = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
   						 "<soapenv:Header><test/></soapenv:Header><soapenv:Body/></soapenv:Envelope>";

   	String oldStr = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
		 "<soapenv:Header><test/></soapenv:Header><soapenv:Body/></soapenv:Envelope>";

   	assertEquals( "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
   			 "<soapenv:Header><test/><test/></soapenv:Header><soapenv:Body/></soapenv:Envelope>"
   			, SoapUtils.transferSoapHeaders(oldStr, newStr, SoapVersion.Soap11));
   }

}
