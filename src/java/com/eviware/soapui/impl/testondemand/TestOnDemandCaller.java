/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.testondemand;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import sun.misc.BASE64Encoder;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.settings.SSLSettings;
import com.eviware.soapui.support.xml.XmlUtils;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author Erik R. Yverling
 * 
 *         Calls the AlertSite API for running Test On Demand.
 */

// FIXME Make this an interface?
public class TestOnDemandCaller
{
	private static final String REDIRECT_URL_XPATH_EXPRESSION = "//RedirectURL";

	// FIXME Should this be in a configuration file instead?
	private static final String UPLOAD_URI = "https://10.0.48.172/restapi/v2/devices/upload/testondemand";

	// FIXME This should be removed when the AlertSite server is back up again!
	private static final String DUMMY_UPLOAD_RESPONSE = "<Response><Status>STATUS</Status><Message>MESSAGE</Message><RedirectURL>http://www.alertsite.com</RedirectURL></Response>";

	// FIXME This should be the soapUI version
	private static final String USER_AGENT = "SOAPUI_USER_AGENT";

	private final ExtendedPostMethod post = new ExtendedPostMethod();

	@NonNull
	public String sendProject( @NonNull WsdlTestCase testCase, @NonNull String locationCode ) throws Exception
	{
		post.setURI( new URI( UPLOAD_URI ) );
		String requestContent = createUploadRequestContents( testCase, locationCode );
		byte[] compressedRequestContent = CompressionSupport.compress( CompressionSupport.ALG_GZIP,
				requestContent.getBytes() );
		post.setEntity( new ByteArrayEntity( compressedRequestContent ) );

		// FIXME Uncomment this when the AlertSite server is back up again!
		//			HttpClientSupport.execute( post );
		//			byte[] responseBody = post.getResponseBody();
		//			SoapUI.log( "Got response from AlertSite:" );
		//			SoapUI.log( new String( responseBody ) );

		byte[] responseBody = DUMMY_UPLOAD_RESPONSE.getBytes();

		String reponseBodyAsString = new String( responseBody );
		Document responseDocument = XmlUtils.parseXml( reponseBodyAsString );
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		return ( String )xpath.evaluate( REDIRECT_URL_XPATH_EXPRESSION, responseDocument, XPathConstants.STRING );
	}

	private String createUploadRequestContents( WsdlTestCase testCase, String locationCode ) throws IOException
	{
		BASE64Encoder encoder = new BASE64Encoder();

		String encodedTestSuiteName = encoder.encode( testCase.getTestSuite().getName().getBytes() );
		String encodedTestCaseName = encoder.encode( testCase.getName().getBytes() );

		String projectFilePath = testCase.getTestSuite().getProject().getPath();
		byte[] projectFileData = getBytes( projectFilePath );
		byte[] zipedProjectFileData = zipBytes( testCase.getTestSuite().getProject().getName(), projectFileData );
		String encodedZipedProjectFile = encoder.encode( zipedProjectFileData );

		String projectPassword = testCase.getTestSuite().getProject().getShadowPassword();
		String encodedProjectPassword = encoder.encode( Strings.nullToEmpty( projectPassword ).getBytes() );

		String keystoreFilePath = SoapUI.getSettings().getString( SSLSettings.KEYSTORE, "" );
		byte[] keystoreFileData = getBytes( keystoreFilePath );
		String encodedKeystoreFile = encoder.encode( keystoreFileData );

		String encodedPassword = encoder.encode( SoapUI.getSettings().getString( SSLSettings.KEYSTORE_PASSWORD, "" )
				.getBytes() );

		//FIXME This should be made to an XMLObject 
		return "<Request api_version=\"2\"><Header><UserAgent>" + USER_AGENT
				+ "</UserAgent></Header><Body><Command><Name>TestOnDemand</Name><Parameters>test_location=" + locationCode
				+ "</Parameters></Command><Txn><TestSuite enctype=\"base64\">" + encodedTestSuiteName
				+ "</TestSuite><TestCase enctype=\"base64\">" + encodedTestCaseName
				+ "</TestCase><Content enctype=\"base64\" type=\"application/zip\">" + encodedZipedProjectFile
				+ "</Content><Password enctype=\"base64\"> " + encodedProjectPassword
				+ " </Password></Txn><Keystore><File enctype=\"base64\">" + encodedKeystoreFile
				+ "</File><Password enctype=\"base64\">" + encodedPassword + "</Password> </Keystore></Body></Request>";
	}

	private static byte[] getBytes( String filePath ) throws IOException
	{
		byte[] byteArray = new byte[0];
		if( !Strings.isNullOrEmpty( filePath ) )
		{
			File file = new File( filePath );
			FileInputStream inputStream = new FileInputStream( file );
			byteArray = ByteStreams.toByteArray( inputStream );
			inputStream.close();
		}
		return byteArray;
	}

	private static byte[] zipBytes( String filename, byte[] dataToBeZiped ) throws IOException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipOutputStream zipedOutputStream = new ZipOutputStream( outputStream );
		ZipEntry entry = new ZipEntry( filename );
		entry.setSize( dataToBeZiped.length );
		zipedOutputStream.putNextEntry( entry );
		zipedOutputStream.write( dataToBeZiped );
		zipedOutputStream.closeEntry();
		zipedOutputStream.close();
		return outputStream.toByteArray();
	}
}
