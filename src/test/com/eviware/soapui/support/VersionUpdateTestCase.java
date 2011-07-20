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
package com.eviware.soapui.support;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class VersionUpdateTestCase
{
	Document doc = null;
	FileInputStream in = null;
	File file = null;

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( VersionUpdateTestCase.class );
	}

	@Before
	public void instantiateVersionDocument() throws ParserConfigurationException, SAXException, IOException
	{
		file = new File( "src" + File.separator + "test-resources" + File.separator + "soapui-latest-version.xml" );
		in = new FileInputStream( file );
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		doc = db.parse( in );
	}

	@Test
	public void shouldParseVersionUpdateXml() throws Exception
	{
		SoapUIVersionUpdate versionUpdate = new SoapUIVersionUpdate();
		versionUpdate.getLatestVersionAvailable( doc );

		assertNotNull( versionUpdate.getLatestVersion() );
		assertNotNull( versionUpdate.getReleaseNotes() );
		assertNotNull( versionUpdate.getCoreDownloadLink() );
		assertNotNull( versionUpdate.getProDownloadLink() );
		assertNotNull( versionUpdate.getVersionType() );
	}

	@After
	public void clear() throws IOException
	{
		in.close();
	}

}
