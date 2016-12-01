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

package com.eviware.soapui.model.tree.nodes;

import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectTreeNodeTest {
    @Test
    public void shouldBeConstructedWithRestMockServices() {
        RestMockService restMockService = mock(RestMockService.class);
        when(restMockService.getPropertyNames()).thenReturn(new String[]{});

        Project project = mock(Project.class);
        when(project.getRestMockServiceCount()).thenReturn(1);
        when(project.getRestMockServiceAt(0)).thenReturn(restMockService);
        when(project.isOpen()).thenReturn(true);
        when(project.getPropertyNames()).thenReturn(new String[]{});

        WorkspaceTreeNode workspaceNode = mock(WorkspaceTreeNode.class);
        SoapUITreeModel soapUITreeModel = mock(SoapUITreeModel.class);
        when(workspaceNode.getTreeModel()).thenReturn(soapUITreeModel);

        ProjectTreeNode projectTreeNode = new ProjectTreeNode(project, workspaceNode);

        assertThat(projectTreeNode.getChildCount(), is(1));
        assertThat(projectTreeNode.getChildNode(0), instanceOf(MockServiceTreeNode.class));
    }
}
