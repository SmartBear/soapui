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

package com.eviware.soapui.support;

import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.workspace.Workspace;
import org.apache.xmlbeans.XmlException;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Prakash
 */
public class ModelItemNamerTest {
    private static final String DEFAULT_PROJECT_NAME = "";
    private Workspace workspace;

    @Before
    public void setUp() throws IOException, XmlException {
        workspace = mock(Workspace.class);
    }

    @Test
    public void createsFirstRestProjectWithNameRESTProject1() throws Exception {
        mockWorkspaceProjects();
        String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 1";
        assertThat(ModelItemNamer.createName(DEFAULT_PROJECT_NAME, workspace.getProjectList()), Is.is(expectedFirstProjectName));
    }

    @Test
    public void createsProjectWithNameRESTProject2IfProjectWithNameRESTProject1AlreadyExists() throws Exception {
        mockWorkspaceProjects(DEFAULT_PROJECT_NAME + " 1");
        String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 2";
        assertThat(ModelItemNamer.createName(DEFAULT_PROJECT_NAME, workspace.getProjectList()), Is.is(expectedFirstProjectName));
    }

    @Test
    public void createsProjectWithNameRESTProject3IfProjectsWithNameRESTProject1AndRESTProject2AlreadyExist()
            throws Exception {
        mockWorkspaceProjects(DEFAULT_PROJECT_NAME + " 1", DEFAULT_PROJECT_NAME + " 2");
        String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 3";
        assertThat(ModelItemNamer.createName(DEFAULT_PROJECT_NAME, workspace.getProjectList()), Is.is(expectedFirstProjectName));
    }

    @Test
    public void createsProjectWithNameRESTProject4IfProjectsExistWithNameRESTProject1AndRESTProject3() throws Exception {
        mockWorkspaceProjects(DEFAULT_PROJECT_NAME + "1", DEFAULT_PROJECT_NAME + "3");  //REST Project1, REST Project3
        String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 4";
        assertThat(ModelItemNamer.createName(DEFAULT_PROJECT_NAME, workspace.getProjectList()), Is.is(expectedFirstProjectName));
    }

    @Test
    public void doesNotThrowAnExceptionIfProjectExistsWithNameRESTProject3x() throws Exception {
        mockWorkspaceProjects(DEFAULT_PROJECT_NAME + " 3x");
        String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 1";
        assertThat(ModelItemNamer.createName(DEFAULT_PROJECT_NAME, workspace.getProjectList()), Is.is(expectedFirstProjectName));
    }

    private void mockWorkspaceProjects(String... projectNames) {
        List projectList = new ArrayList<Project>();
        for (String projectName : projectNames) {
            Project project = Mockito.mock(Project.class);
            when(project.getName()).thenReturn(projectName);
            projectList.add(project);
        }
        when(workspace.getProjectList()).thenReturn(projectList);
    }
}
