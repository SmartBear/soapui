/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.support;

import junit.framework.JUnit4TestAdapter;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class VersionUpdateTestCase
{

	private static final String RELEASE_NOTES =
			"<version>\n" +
					"<version-type>minor</version-type>\n" +
					"<version-number>4.0.1</version-number>\n" +
					"<release-notes-core>http://www.soapui.org/version/release-notes-core-4_0_1.html</release-notes-core>\n" +
					"<release-notes-pro>http://www.soapui.org/version/release-notes-pro-4_0_1.html</release-notes-pro>\n" +
					"<download-link-core>http://sourceforge.net/projects/soapui/files/soapui/4.0</download-link-core>\n" +
					"<download-link-pro>http://www.eviware.com/Download-soapUI/download-soapui-pro.html</download-link-pro>\n" +
					"</version>";

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( VersionUpdateTestCase.class );
	}

	@Test
	public void shouldParseVersionUpdateXml() throws Exception
	{
		SoapUIVersionUpdate versionUpdate = new SoapUIVersionUpdate();

		versionUpdate.getLatestVersionAvailable( RELEASE_NOTES );

		assertThat( versionUpdate.getLatestVersion(), is( "4.0.1" ) );
		assertThat( versionUpdate.getReleaseNotesCore(), is( "http://www.soapui.org/version/release-notes-core-4_0_1.html" ) );
		assertThat( versionUpdate.getReleaseNotesPro(), is( "http://www.soapui.org/version/release-notes-pro-4_0_1.html" ) );
		assertThat( versionUpdate.getDownloadLinkCore(), is( "http://sourceforge.net/projects/soapui/files/soapui/4.0" ) );
		assertThat( versionUpdate.getDownloadLinkPro(), is( "http://www.eviware.com/Download-soapUI/download-soapui-pro.html" ) );
	}

	@Test
	@Ignore
	public void updateUrlOnTheCommandLineShouldTakePrecedence()
	{
		System.setProperty( SoapUIVersionUpdate.VERSION_UPDATE_URL_SYS_PROP_KEY, "nop" );

		assertEquals( "Command line property for update url did not take precedence!", "nop", SoapUIVersionUpdate.LATEST_VERSION_XML_LOCATION );

		System.getProperties().remove( SoapUIVersionUpdate.VERSION_UPDATE_URL_SYS_PROP_KEY );

		assertNull( "", System.getProperty( SoapUIVersionUpdate.VERSION_UPDATE_URL_SYS_PROP_KEY ) );
	}
}
