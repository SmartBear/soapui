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

import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.settings.UISettings;

import java.util.ArrayList;
import java.util.List;

/**
 * SoapUITreeNode for Project implementations
 *
 * @author Ole.Matzura
 */

public class ProjectTreeNode extends AbstractModelItemTreeNode<Project> {
    private InternalProjectListener internalProjectListener;
    private List<InterfaceTreeNode> interfaceNodes = new ArrayList<InterfaceTreeNode>();
    private List<TestSuiteTreeNode> testSuiteNodes = new ArrayList<TestSuiteTreeNode>();
    private List<MockServiceTreeNode> mockServiceNodes = new ArrayList<MockServiceTreeNode>();
    private PropertiesTreeNode<?> propertiesTreeNode;

    public ProjectTreeNode(Project project, WorkspaceTreeNode workspaceNode) {
        super(project, workspaceNode.getWorkspace(), workspaceNode.getTreeModel());

        if (!project.isOpen()) {
            return;
        }

        internalProjectListener = new InternalProjectListener();
        project.addProjectListener(internalProjectListener);

        for (int c = 0; c < project.getInterfaceCount(); c++) {
            Interface iface = project.getInterfaceAt(c);
            interfaceNodes.add(new InterfaceTreeNode(iface, getTreeModel()));
        }

        for (int c = 0; c < project.getTestSuiteCount(); c++) {
            testSuiteNodes.add(new TestSuiteTreeNode(project.getTestSuiteAt(c), getTreeModel()));
        }

        for (int c = 0; c < project.getMockServiceCount(); c++) {
            mockServiceNodes.add(new MockServiceTreeNode(project.getMockServiceAt(c), getTreeModel()));
        }

        for (int c = 0; c < project.getRestMockServiceCount(); c++) {
            mockServiceNodes.add(new MockServiceTreeNode(project.getRestMockServiceAt(c), getTreeModel()));
        }

        initOrdering(interfaceNodes, UISettings.ORDER_SERVICES);

        getTreeModel().mapModelItems(interfaceNodes);
        getTreeModel().mapModelItems(testSuiteNodes);
        getTreeModel().mapModelItems(mockServiceNodes);

        propertiesTreeNode = PropertiesTreeNode.createDefaultPropertiesNode(project, getTreeModel());
        getTreeModel().mapModelItem(propertiesTreeNode);
    }

    public void release() {
        super.release();

        getProject().removeProjectListener(internalProjectListener);

        for (InterfaceTreeNode treeNode : interfaceNodes) {
            treeNode.release();
        }

        for (TestSuiteTreeNode treeNode : testSuiteNodes) {
            treeNode.release();
        }

        for (MockServiceTreeNode treeNode : mockServiceNodes) {
            treeNode.release();
        }

        if (propertiesTreeNode != null) {
            propertiesTreeNode.release();
        }
    }

    public int getChildCount() {
        if (propertiesTreeNode == null) {
            return 0;
        }

        int propMod = getTreeModel().isShowProperties() ? 1 : 0;

        return interfaceNodes.size() + testSuiteNodes.size() + mockServiceNodes.size() + propMod;
    }

    public int getIndexOfChild(Object child) {
        if (propertiesTreeNode == null) {
            return -1;
        }

        if (child == propertiesTreeNode) {
            return 0;
        }

        int propMod = getTreeModel().isShowProperties() ? 1 : 0;

        int index = interfaceNodes.indexOf(child);
        if (index >= 0) {
            return index + propMod;
        }

        index = testSuiteNodes.indexOf(child);
        if (index >= 0) {
            return index + interfaceNodes.size() + propMod;
        }

        index = mockServiceNodes.indexOf(child);
        if (index >= 0) {
            return index + interfaceNodes.size() + testSuiteNodes.size() + propMod;
        }

        return -1;
    }

    public SoapUITreeNode getChildNode(int index) {
        if (propertiesTreeNode == null) {
            return null;
        }

        int propMod = getTreeModel().isShowProperties() ? 1 : 0;

        if (propMod == 1 && index == 0) {
            return propertiesTreeNode;
        } else if (index < interfaceNodes.size() + propMod) {
            return interfaceNodes.get(index - propMod);
        } else if (index < testSuiteNodes.size() + interfaceNodes.size() + propMod) {
            return testSuiteNodes.get(index - interfaceNodes.size() - propMod);
        } else {
            return mockServiceNodes.get(index - interfaceNodes.size() - testSuiteNodes.size() - propMod);
        }
    }

    public Project getProject() {
        return (Project) getModelItem();
    }

    private class InternalProjectListener extends ProjectListenerAdapter {
        public void interfaceAdded(Interface iface) {
            InterfaceTreeNode interfaceTreeNode = new InterfaceTreeNode(iface, getTreeModel());
            interfaceNodes.add(interfaceTreeNode);
            reorder(false);
            getTreeModel().notifyNodeInserted(interfaceTreeNode);
        }

        public void interfaceRemoved(Interface iface) {
            SoapUITreeNode treeNode = getTreeModel().getTreeNode(iface);
            if (interfaceNodes.contains(treeNode)) {
                getTreeModel().notifyNodeRemoved(treeNode);
                interfaceNodes.remove(treeNode);
            } else {
                throw new RuntimeException("Removing unkown interface");
            }
        }

        public void testSuiteAdded(TestSuite testSuite) {
            TestSuiteTreeNode testSuiteNode = new TestSuiteTreeNode(testSuite, getTreeModel());
            testSuiteNodes.add(testSuite.getProject().getIndexOfTestSuite(testSuite), testSuiteNode);
            getTreeModel().notifyNodeInserted(testSuiteNode);
        }

        public void testSuiteRemoved(TestSuite testSuite) {
            SoapUITreeNode treeNode = getTreeModel().getTreeNode(testSuite);
            if (testSuiteNodes.contains(treeNode)) {
                getTreeModel().notifyNodeRemoved(treeNode);
                testSuiteNodes.remove(treeNode);
            } else {
                throw new RuntimeException("Removing unkown testSuite");
            }
        }

        @Override
        public void testSuiteMoved(TestSuite testSuite, int index, int offset) {
            testSuiteRemoved(testSuite);
            testSuiteAdded(testSuite);
        }

        public void mockServiceAdded(MockService mockService) {
            MockServiceTreeNode mockServiceNode = new MockServiceTreeNode(mockService, getTreeModel());
            mockServiceNodes.add(mockServiceNode);
            getTreeModel().notifyNodeInserted(mockServiceNode);
        }

        public void mockServiceRemoved(MockService mockService) {
            SoapUITreeNode treeNode = getTreeModel().getTreeNode(mockService);
            if (mockServiceNodes.contains(treeNode)) {
                getTreeModel().notifyNodeRemoved(treeNode);
                mockServiceNodes.remove(treeNode);
            } else {
                throw new RuntimeException("Removing unkown mockService");
            }
        }
    }

}
