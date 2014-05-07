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
package com.eviware.soapui.support;

import com.eviware.soapui.impl.wsdl.support.http.ProxyUtils;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class VersionUpdateTest {

    private static final String RELEASE_NOTES =
            "<version>\n" +
                    "<version-type>minor</version-type>\n" +
                    "<version-number>4.0.1</version-number>\n" +
                    "<release-notes-core>http://www.soapui.org/version/release-notes-core-4_0_1.html</release-notes-core>\n" +
                    "<release-notes-pro>http://www.soapui.org/version/release-notes-pro-4_0_1.html</release-notes-pro>\n" +
                    "<download-link-core>http://sourceforge.net/projects/soapui/files/soapui/4.0</download-link-core>\n" +
                    "<download-link-pro>http://www.eviware.com/Download-soapUI/download-soapui-pro.html</download-link-pro>\n" +
                    "</version>";


    @Test
    public void shouldParseVersionUpdateXml() throws Exception {
        ProxyUtils.setProxyEnabled(false);
        SoapUIVersionUpdate versionUpdate = new SoapUIVersionUpdate();

        versionUpdate.getLatestVersionAvailable(RELEASE_NOTES);

        assertThat(versionUpdate.getLatestVersion(), is("4.0.1"));
        assertThat(versionUpdate.getReleaseNotesCore(), is("http://www.soapui.org/version/release-notes-core-4_0_1.html"));
        assertThat(versionUpdate.getReleaseNotesPro(), is("http://www.soapui.org/version/release-notes-pro-4_0_1.html"));
        assertThat(versionUpdate.getDownloadLinkCore(), is("http://sourceforge.net/projects/soapui/files/soapui/4.0"));
        assertThat(versionUpdate.getDownloadLinkPro(), is("http://www.eviware.com/Download-soapUI/download-soapui-pro.html"));
    }

    @Test
    @Ignore
    public void updateUrlOnTheCommandLineShouldTakePrecedence() {
        System.setProperty(SoapUIVersionUpdate.VERSION_UPDATE_URL_SYS_PROP_KEY, "nop");

        assertEquals("Command line property for update url did not take precedence!", "nop", SoapUIVersionUpdate.LATEST_VERSION_XML_LOCATION);

        System.getProperties().remove(SoapUIVersionUpdate.VERSION_UPDATE_URL_SYS_PROP_KEY);

        assertNull("", System.getProperty(SoapUIVersionUpdate.VERSION_UPDATE_URL_SYS_PROP_KEY));
    }
}
