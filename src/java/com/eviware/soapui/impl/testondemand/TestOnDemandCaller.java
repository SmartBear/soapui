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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.StringEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sun.misc.BASE64Encoder;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
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
// FIXME Move this to another package
// FIXME the getLocations and sendProject is very similar. Refactor these!
public class TestOnDemandCaller
{
	private static final String USER_AGENT = "soapUI-" + SoapUI.SOAPUI_VERSION;

	private final static String DEV_ENDPOINT = "10.0.48.172";
	private final static String PROD_ENDPOINT = "www.alertsite.com";
	private final static String PROTOCOL = "https://";
	private final static String testOnDemandEndpoint = getEndpoint();

	// FIXME Should these be in a configuration file instead?
	private static final String LOCATIONS_URI = testOnDemandEndpoint + "/restapi/v2/devices/list/locations";
	private static final String UPLOAD_URI = testOnDemandEndpoint + "/restapi/v2/devices/upload/testondemand";

	private static final String REDIRECT_URL_XPATH_EXPRESSION = "//RedirectURL";
	private static final String LOCATION_XPATH_EXPRESSION = "//Location";
	private static final String LOCATION_CODE_XPATH_EXPRESSION = "LocCode";
	private static final String LOCATION_NAME_XPATH_EXPRESSION = "LocName";

	@NonNull
	public List<Location> getLocations() throws Exception
	{
		final ExtendedPostMethod post = new ExtendedPostMethod();
		post.setURI( new URI( LOCATIONS_URI ) );

		String requestContent = "<Request api_version=\"2\"><Header><UserAgent>"
				+ USER_AGENT
				+ "</UserAgent></Header><Body><Command><Name>ListLocations</Name><Parameters>server_attrib=ITEST</Parameters></Command></Body></Request>";

		post.setEntity( new StringEntity( requestContent ) );

		SoapUI.log( "Sending request to  AlertSite:" );
		SoapUI.log( requestContent );

		HttpClientSupport.execute( post );

		byte[] responseBody = post.getResponseBody();
		SoapUI.log( "Got response from AlertSite:" );
		SoapUI.log( new String( responseBody ) );

		String reponseBodyAsString = new String( responseBody );
		Document responseDocument = XmlUtils.parseXml( reponseBodyAsString );
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		NodeList locationNodes = ( NodeList )xpath.evaluate( LOCATION_XPATH_EXPRESSION, responseDocument,
				XPathConstants.NODESET );

		List<Location> locations = new ArrayList<Location>();
		for( int i = 0; i < locationNodes.getLength(); i++ )
		{
			Node locationNode = locationNodes.item( i );
			String name = ( String )xpath.evaluate( LOCATION_NAME_XPATH_EXPRESSION, locationNode, XPathConstants.STRING );
			String code = ( String )xpath.evaluate( LOCATION_CODE_XPATH_EXPRESSION, locationNode, XPathConstants.STRING );
			locations.add( new Location( code, name ) );
		}

		return locations;
	}

	@NonNull
	public String sendProject( @NonNull WsdlTestCase testCase, @NonNull Location location, @NonNull File filename )
			throws Exception
	{
		final ExtendedPostMethod post = new ExtendedPostMethod();
		post.setURI( new URI( UPLOAD_URI ) );
		String locationCode = location.getCode();
		String requestContent = createUploadRequestContents( testCase, locationCode, filename );

		// FIXME The request compression is temporary disabled since the server throws an error when using it
		//		byte[] compressedRequestContent = CompressionSupport.compress( CompressionSupport.ALG_GZIP,
		//				requestContent.getBytes() );
		//		post.setEntity( new ByteArrayEntity( compressedRequestContent ) );

		post.setEntity( new StringEntity( requestContent ) );

		SoapUI.log( "Sending request to  AlertSite:" );
		SoapUI.log( requestContent );

		HttpClientSupport.execute( post );

		byte[] responseBody = post.getResponseBody();
		SoapUI.log( "Got response from AlertSite:" );
		SoapUI.log( new String( responseBody ) );

		String reponseBodyAsString = new String( responseBody );
		Document responseDocument = XmlUtils.parseXml( reponseBodyAsString );
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		return ( String )xpath.evaluate( REDIRECT_URL_XPATH_EXPRESSION, responseDocument, XPathConstants.STRING );
	}

	private String createUploadRequestContents( WsdlTestCase testCase, String locationCode, File filename )
			throws IOException
	{
		BASE64Encoder encoder = new BASE64Encoder();

		String encodedTestSuiteName = encoder.encode( testCase.getTestSuite().getName().getBytes() );
		String encodedTestCaseName = encoder.encode( testCase.getName().getBytes() );

		// String projectFilePath = testCase.getTestSuite().getProject().getPath();
		String projectFilePath = filename.getAbsolutePath();
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
				+ "</Content><Password enctype=\"base64\">" + encodedProjectPassword
				+ "</Password></Txn><Keystore><File enctype=\"base64\">" + encodedKeystoreFile
				+ "</File><Password enctype=\"base64\">" + encodedPassword + "</Password> </Keystore></Body></Request>";
	}

	private static byte[] getBytes( String filePath ) throws IOException
	{
		byte[] byteArray = new byte[0];
		if( !Strings.isNullOrEmpty( filePath ) )
		{
			File file = new File( filePath );
			FileInputStream inputStream = null;
			try
			{
				inputStream = new FileInputStream( file );
				byteArray = ByteStreams.toByteArray( inputStream );
			}
			finally
			{
				IOUtils.closeQuietly( inputStream );
			}
		}
		return byteArray;
	}

	private static byte[] zipBytes( String filename, byte[] dataToBeZiped ) throws IOException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipOutputStream zipedOutputStream = new ZipOutputStream( outputStream );
		ZipEntry entry = new ZipEntry( filename );
		entry.setSize( dataToBeZiped.length );
		try
		{
			zipedOutputStream.putNextEntry( entry );
			zipedOutputStream.write( dataToBeZiped );
		}
		finally
		{
			zipedOutputStream.closeEntry();
			zipedOutputStream.close();
		}
		return outputStream.toByteArray();
	}

	private static String getEndpoint()
	{
		String property = System.getProperty( "testOnDemandEndpoint" );

		if( property == null || "dev".equals( property ) )
		{
			return PROTOCOL + DEV_ENDPOINT;
		}
		else
		{
			return PROTOCOL + PROD_ENDPOINT;
		}
	}
}
