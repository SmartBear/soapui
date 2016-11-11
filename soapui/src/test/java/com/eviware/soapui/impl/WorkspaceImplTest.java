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

package com.eviware.soapui.impl;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class WorkspaceImplTest {
    private static final String OUTPUT_FOLDER_PATH = WorkspaceImpl.class.getResource("/").getPath();
    private static final String TEST_WORKSPACE_FILE_PATH = OUTPUT_FOLDER_PATH + "test-workspace.xml";
    private static final String TEST_PROJECT_FILE_PATH = OUTPUT_FOLDER_PATH + "test-project.xml";
    private File workspaceFile;
    private File projectFile;
    private WorkspaceImpl workspace;

    @Before
    public void setUp() throws Exception {
        workspaceFile = new File(TEST_WORKSPACE_FILE_PATH);
        workspace = new WorkspaceImpl(workspaceFile.getAbsolutePath(), null);

        projectFile = new File(TEST_PROJECT_FILE_PATH);
        WsdlProject project = workspace.createProject("Test Project", null);
        project.saveAs(projectFile.getAbsolutePath());

        workspace.save(false);
    }

    @After
    public void tearDown() {
        if (workspaceFile.exists()) {
            workspaceFile.delete();
        }
        if (projectFile.exists()) {
            projectFile.delete();
        }
    }

    @Test
    public void testProjectRoot() throws Exception {
        workspace.setProjectRoot("${workspaceDir}");
        workspace.save(false);
        workspace.switchWorkspace(workspaceFile);

        assertThat(workspace.getProjectRoot(), is("${workspaceDir}"));
        assertThat(workspace.getProjectCount(), is(1));
        assertThat(workspace.getProjectAt(0).getName(), is("Test Project"));
    }

    @Test
    public void doesNotRemoveExternallyModifiedProjects() throws SoapUIException {
        projectFile.setLastModified(System.currentTimeMillis());
        workspace.save(false, true);

        workspace.switchWorkspace(workspaceFile);

        assertThat(workspace.getProjectCount(), is(1));
    }
}
