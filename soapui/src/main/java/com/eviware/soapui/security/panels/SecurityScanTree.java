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

package com.eviware.soapui.security.panels;

import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class SecurityScanTree extends DefaultTreeModel {

    private SecurityTest securityTest;
    private SecurityTreeRootNode treeNode;

    public SecurityScanTree(SecurityTest securityTest, SecurityTreeRootNode treeNode) {
        super(treeNode);

        this.securityTest = securityTest;
        this.treeNode = treeNode;
    }

    public void insertNodeInto(TestStep testStep) {
        TestStepNode testStepNode = new TestStepNode((SecurityTreeRootNode) root, testStep, securityTest
                .getSecurityScansMap().get(testStep.getId()));
        insertNodeInto(testStepNode, (MutableTreeNode) root, root.getChildCount());
        nodeStructureChanged(root);
    }

    public void removeTestStep(TestStep testStep) {
        TestStepNode node = getTestStepNode(testStep);
        removeNodeFromParent(node);
    }

    /**
     * @param testStep
     * @return
     */
    protected TestStepNode getTestStepNode(TestStep testStep) {
        for (int cnt = 0; cnt < root.getChildCount(); cnt++) {
            TestStepNode node = (TestStepNode) root.getChildAt(cnt);
            if (node.getTestStep().getId().equals(testStep.getId())) {
                return node;
            }
        }
        return null;
    }

    protected SecurityScanNode getSecurityScanNode(SecurityScan securityCheck) {
        TestStepNode testStepNode = getTestStepNode(securityCheck.getTestStep());
        for (int cnt = 0; cnt < testStepNode.getChildCount(); cnt++) {
            SecurityScanNode node = (SecurityScanNode) testStepNode.getChildAt(cnt);
            if (node.getSecurityScan().getType().equals(securityCheck.getType())) {
                return node;
            }
        }
        return null;
    }

    public void addSecurityScanNode(JTree tree, SecurityScan securityCheck) {
        TestStepNode node = getTestStepNode(securityCheck.getTestStep());
        if (node != null) {
            SecurityScanNode newNode = new SecurityScanNode(securityCheck);
            insertNodeInto(newNode, node, node.getChildCount());
            nodeStructureChanged(node);
            for (int row = 0; row < tree.getRowCount(); row++) {
                tree.expandRow(row);
            }
            tree.setSelectionInterval(getIndexOfChild(node, newNode) + 1, getIndexOfChild(node, newNode) + 1);
        }
    }

    public void removeSecurityScanNode(SecurityScan securityCheck) {
        TestStepNode testStepNode = getTestStepNode(securityCheck.getTestStep());
        SecurityScanNode node = getSecurityScanNode(securityCheck);
        removeNodeFromParent(node);
        nodeStructureChanged(testStepNode);
    }

    /**
     * moves test step
     * <p/>
     * returns new index/row where test step is inserted
     *
     * @param testStep
     * @param index
     * @param offset
     * @return
     */
    public TreePath moveTestStepNode(TestStep testStep, int index, int offset) {
        TestStepNode node = getTestStepNode(testStep);
        int index2 = getIndexOfChild(root, node);
        removeNodeFromParent(node);
        insertNodeInto(node, (MutableTreeNode) root, index2 + offset);

        return new TreePath(node.getPath());
    }

    public void release() {
        if (securityTest != null) {
            securityTest.removePropertyChangeListener(treeNode);
        }
    }

}
