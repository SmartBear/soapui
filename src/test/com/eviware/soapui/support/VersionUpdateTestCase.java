/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
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
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.eviware.soapui.SoapUI;

public class VersionUpdateTestCase
{

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( VersionUpdateTestCase.class );
	}

	@Test
	public void shouldParseVersionUpdateXml() throws Exception
	{
		SoapUIVersionUpdate versionUpdate = new SoapUIVersionUpdate();
		versionUpdate.getLatestVersionAvailable( SoapUI.class.getResource( "/soapui-latest-version.xml" ) );

		assertNotNull( versionUpdate.getLatestVersion() );
		assertNotNull( versionUpdate.getReleaseNotesCore() );
		assertNotNull( versionUpdate.getReleaseNotesPro() );
		assertNotNull( versionUpdate.getDownloadLinkCore() );
		assertNotNull( versionUpdate.getDownloadLinkPro() );
	}

}
