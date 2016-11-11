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

package com.eviware.soapui.actions;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.impl.swing.FileFormField;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.eviware.soapui.utils.ResourceUtils.getFilePathFromResource;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockAsWarActionTest {

    private final String SOAPUI_HOME = "soapui.home";
    private WsdlProject project;
    private XFormDialog mockedDialog;
    private String soapuiOriginalHome;
    private final File warTestDir = new File("wartestdir");
    private final String warDirectoryPath = warTestDir.getPath() + File.separator + "wardirectory";
    private final String warFileName = warTestDir.getPath() + File.separator + "mock.war";

    @Before
    public void setUp() throws SoapUIException, URISyntaxException, IOException, XmlException {
        setUpTestDirectories();
        setUpProject();
        setUpFormDialog();
    }

    private void setUpFormDialog() throws IOException, URISyntaxException {
        mockedDialog = mock(XFormDialog.class);
        when(mockedDialog.show()).thenReturn(true);
        when(mockedDialog.getFormField(anyString())).thenReturn(any(XFormField.class));

        createFileformfield(getFilePathFromResource("/config/soapui-test-settings.xml"), MockAsWarAction.MockAsWarDialog.SETTINGS_FILE);
        createFileformfield(warFileName, MockAsWarAction.MockAsWarDialog.WAR_FILE);
        createFileformfield(warDirectoryPath, MockAsWarAction.MockAsWarDialog.WAR_DIRECTORY);

        when(mockedDialog.getValue(MockAsWarAction.MockAsWarDialog.MOCKSERVICE_ENDPOINT)).thenReturn("http://localhost:8080");
    }

    private void createFileformfield(String filePath, String fieldName) {
        String name = filePath;
        FileFormField fileFormField = new FileFormField("tooltip", XForm.FieldType.FILE, name);
        fileFormField.setValue(filePath);
        when(mockedDialog.getFormField(fieldName)).thenReturn(fileFormField);
    }

    private void setUpProject() throws URISyntaxException, XmlException, IOException, SoapUIException {
        String fileNameWithPath = getFilePathFromResource("/soapui-projects/BasicMock-soapui-4.6.3-Project.xml");
        project = new WsdlProject(fileNameWithPath);
        ModelItemFactory.makeRestMockService(project);
    }

    private void setUpTestDirectories() throws IOException {
        warTestDir.mkdirs();
        setSoapUiHomeDirectory();
    }

    private void setSoapUiHomeDirectory() throws IOException {
        File mockSoapHomeDir = new File(warTestDir, "soapuihometestdir");
        mockSoapHomeDir.mkdirs();

        File lib = new File(mockSoapHomeDir, "lib");
        lib.mkdir();
        File sampleJarFile = new File(lib, "soapui.jar");
        FileUtils.touch(sampleJarFile);

        soapuiOriginalHome = System.getProperty(SOAPUI_HOME);
        System.setProperty(SOAPUI_HOME, lib.getPath());
    }

    @After
    public void tearDown() throws IOException {

        if (soapuiOriginalHome == null) {
            System.getProperties().remove(SOAPUI_HOME);
        } else {
            System.setProperty(SOAPUI_HOME, soapuiOriginalHome);
        }

        FileUtils.deleteDirectory(warTestDir);
    }

    @Test
    public void createMockAsWar() throws SoapUIException, IOException {

        MockAsWarAction action = new MockAsWarAction();

        action.setDialog(mockedDialog);
        action.perform(project, null);

        assertTrue(new File(warFileName).exists());
        assertValidWarDirectory(warDirectoryPath);
        assertValidWarFile(warFileName);
    }

    private void assertValidWarFile(String warFileName) throws IOException {

        JarFile jarFile = new JarFile(warFileName);
        try {
            for (String fileName : getExpectedWarContents()) {
                JarEntry jarEntry = jarFile.getJarEntry(fileName);
                assertNotNull(jarEntry);
            }
        } finally {
            jarFile.close();
        }
    }

    private void assertValidWarDirectory(String warDirectoryPath) {

        for (String fileName : getExpectedWarContents()) {
            File file = new File(warDirectoryPath, fileName);
            assertTrue(file.exists());
        }
    }

    private String[] getExpectedWarContents() {

        return new String[]{
                "WEB-INF/web.xml",
                "WEB-INF/lib/soapui.jar",
                "WEB-INF/soapui/BasicMock-soapui-4.6.3-Project.xml"
        };

    }

}
