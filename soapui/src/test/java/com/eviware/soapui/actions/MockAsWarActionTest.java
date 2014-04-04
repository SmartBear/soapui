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
package com.eviware.soapui.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.x.dialogs.XFileDialogs;
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
import java.util.Random;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockAsWarActionTest {

    public static final String WAR_FILE_NAME = "./mock.war";
    private final String SOAPUI_HOME = "soapui.home";
    private WsdlProject project;
    private XFormDialog mockedDialog;
    private String soapuiOriginalHome;
    private File mockSoapHomeDir = null;

    @Before
    public void setUp() throws SoapUIException, URISyntaxException, IOException, XmlException {


        XFileDialogs xFileDialogs = mock(XFileDialogs.class);
        UISupport.setFileDialogs(xFileDialogs);

        mockSoapHomeDir = new File("soapuihometestdir");
        mockSoapHomeDir.mkdirs();
        mockSoapHomeDir.deleteOnExit();

        File lib = new File(mockSoapHomeDir, "lib");
        lib.mkdir();
        File sampleJarFile = new File(lib, "soapui.jar");
        FileUtils.touch(sampleJarFile);

        soapuiOriginalHome = System.getProperty(SOAPUI_HOME);
        System.setProperty(SOAPUI_HOME, lib.getPath());


        String fileName = SoapUI.class.getResource("/soapui-projects/BasicMock-soapui-4.6.3-Project.xml").toURI().toString();
        project = new WsdlProject(fileName);

        ModelItemFactory.makeRestMockService(project);

        mockedDialog = mock(XFormDialog.class);
        when(mockedDialog.show()).thenReturn(true);
        when(mockedDialog.getFormField(anyString())).thenReturn(any(XFormField.class));
        when(mockedDialog.getFormField(MockAsWarAction.MockAsWarDialog.SETTINGS_FILE)).thenReturn(new FileFormField("", XForm.FieldType.FILE, "setting.war"));
        FileFormField warFileFormField = new FileFormField("", XForm.FieldType.FILE, WAR_FILE_NAME);
        warFileFormField.setValue(WAR_FILE_NAME);
        when(mockedDialog.getFormField(MockAsWarAction.MockAsWarDialog.WAR_FILE)).thenReturn(warFileFormField);
        when(mockedDialog.getFormField(MockAsWarAction.MockAsWarDialog.WAR_DIRECTORY)).thenReturn(new FileFormField("", XForm.FieldType.FILE, "./tmp"));
        when(mockedDialog.getValue(MockAsWarAction.MockAsWarDialog.MOCKSERVICE_ENDPOINT)).thenReturn("http://localhost:8080");


    }

    @After
    public void tearDown() throws IOException {

        if (soapuiOriginalHome != null) {
            System.setProperty(SOAPUI_HOME, soapuiOriginalHome);
        }

        // TODO: remove all files
        FileUtils.deleteDirectory(mockSoapHomeDir);
    }

    @Test
    public void createMockAsWar() throws SoapUIException {

        MockAsWarAction action = new MockAsWarAction();

        action.setDialog(mockedDialog);
        action.perform(project, null);

        assertTrue(new File(WAR_FILE_NAME).exists());
    }

}
