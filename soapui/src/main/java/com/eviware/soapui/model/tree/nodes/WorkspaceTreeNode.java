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

import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.WorkspaceListenerAdapter;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.settings.UISettings;

import javax.swing.event.TreeModelEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * SoapUITreeNode for Workspace implementations
 *
 * @author Ole.Matzura
 */

public class WorkspaceTreeNode extends AbstractModelItemTreeNode<Workspace> {
    private InternalWorkspaceListener workspaceListener = new InternalWorkspaceListener();
    private List<ProjectTreeNode> projectNodes = new ArrayList<ProjectTreeNode>();
    private ReorderPropertyChangeListener propertyChangeListener = new ReorderPropertyChangeListener();

    public WorkspaceTreeNode(Workspace workspace, SoapUITreeModel treeModel) {
        super(workspace, null, treeModel);

        workspace.addWorkspaceListener(workspaceListener);

        for (int c = 0; c < workspace.getProjectCount(); c++) {
            Project project = workspace.getProjectAt(c);
            project.addPropertyChangeListener(Project.NAME_PROPERTY, propertyChangeListener);
            projectNodes.add(new ProjectTreeNode(project, this));
        }

        initOrdering(projectNodes, UISettings.ORDER_PROJECTS);
        getTreeModel().mapModelItems(projectNodes);
    }

    public void release() {
        super.release();
        getWorkspace().removeWorkspaceListener(workspaceListener);

        for (ProjectTreeNode treeNode : projectNodes) {
            treeNode.getModelItem().removePropertyChangeListener(Project.NAME_PROPERTY, propertyChangeListener);
            treeNode.release();
        }
    }

    public Workspace getWorkspace() {
        return (Workspace) getModelItem();
    }

    private class InternalWorkspaceListener extends WorkspaceListenerAdapter {
        public void projectAdded(Project project) {
            ProjectTreeNode projectTreeNode = new ProjectTreeNode(project, WorkspaceTreeNode.this);
            projectNodes.add(projectTreeNode);
            project.addPropertyChangeListener(Project.NAME_PROPERTY, propertyChangeListener);
            reorder(false);
            getTreeModel().notifyNodeInserted(projectTreeNode);
        }

        public void projectRemoved(Project project) {
            SoapUITreeNode treeNode = getTreeModel().getTreeNode(project);
            if (projectNodes.contains(treeNode)) {
                getTreeModel().notifyNodeRemoved(treeNode);
                projectNodes.remove(treeNode);
                project.removePropertyChangeListener(propertyChangeListener);
            } else {
                throw new RuntimeException("Removing unkown project");
            }
        }

        public void projectChanged(Project project) {
            getTreeModel().notifyStructureChanged(
                    new TreeModelEvent(WorkspaceTreeNode.this, new Object[]{getTreeModel().getPath(
                            WorkspaceTreeNode.this)}));
        }
    }
}
