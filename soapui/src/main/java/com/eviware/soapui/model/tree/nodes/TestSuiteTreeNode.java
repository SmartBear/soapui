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

import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteListener;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.security.SecurityTest;

import java.util.ArrayList;
import java.util.List;

/**
 * SoapUITreeNode for TestSuite implementations
 *
 * @author Ole.Matzura
 */

public class TestSuiteTreeNode extends AbstractModelItemTreeNode<TestSuite> {
    private InternalTestSuiteListener internalTestSuiteListener = new InternalTestSuiteListener();
    ;
    private ReorderPropertyChangeListener propertyChangeListener = new ReorderPropertyChangeListener();
    private List<TestCaseTreeNode> testCaseNodes = new ArrayList<TestCaseTreeNode>();
    private PropertiesTreeNode<?> propertiesTreeNode;

    public TestSuiteTreeNode(TestSuite testSuite, SoapUITreeModel treeModel) {
        super(testSuite, testSuite.getProject(), treeModel);

        testSuite.addTestSuiteListener(internalTestSuiteListener);

        for (int c = 0; c < testSuite.getTestCaseCount(); c++) {
            TestCase testCase = testSuite.getTestCaseAt(c);
            testCase.addPropertyChangeListener(TestCase.NAME_PROPERTY, propertyChangeListener);
            testCaseNodes.add(new TestCaseTreeNode(testCase, getTreeModel()));
        }

        getTreeModel().mapModelItems(testCaseNodes);

        propertiesTreeNode = PropertiesTreeNode.createDefaultPropertiesNode(testSuite, getTreeModel());
        getTreeModel().mapModelItem(propertiesTreeNode);
    }

    public void release() {
        super.release();

        getTestSuite().removeTestSuiteListener(internalTestSuiteListener);

        for (TestCaseTreeNode treeNode : testCaseNodes) {
            treeNode.getModelItem().removePropertyChangeListener(TestCase.NAME_PROPERTY, propertyChangeListener);
            treeNode.release();
        }

        propertiesTreeNode.release();
    }

    @Override
    public int getChildCount() {
        int propMod = getTreeModel().isShowProperties() ? 1 : 0;
        return testCaseNodes.size() + propMod;
    }

    @Override
    public SoapUITreeNode getChildNode(int index) {
        int propMod = getTreeModel().isShowProperties() ? 1 : 0;
        return index == 0 && propMod == 1 ? propertiesTreeNode : testCaseNodes.get(index - propMod);
    }

    @Override
    public int getIndexOfChild(Object child) {
        int propMod = getTreeModel().isShowProperties() ? 1 : 0;
        if (propMod == 1 && child == propertiesTreeNode) {
            return 0;
        }

        int ix = testCaseNodes.indexOf(child);
        return ix == -1 ? ix : ix + propMod;
    }

    public TestSuite getTestSuite() {
        return (TestSuite) getModelItem();
    }

    private class InternalTestSuiteListener implements TestSuiteListener {
        public void testCaseAdded(TestCase testCase) {
            TestCaseTreeNode testCaseTreeNode = new TestCaseTreeNode(testCase, getTreeModel());
            testCaseNodes.add(testCase.getTestSuite().getIndexOfTestCase(testCase), testCaseTreeNode);

            testCase.addPropertyChangeListener(TestCase.NAME_PROPERTY, propertyChangeListener);
            getTreeModel().notifyNodeInserted(testCaseTreeNode);
        }

        public void testCaseRemoved(TestCase testCase) {
            SoapUITreeNode treeNode = getTreeModel().getTreeNode(testCase);
            if (testCaseNodes.contains(treeNode)) {
                getTreeModel().notifyNodeRemoved(treeNode);
                testCaseNodes.remove(treeNode);
                testCase.removePropertyChangeListener(propertyChangeListener);
            } else {
                throw new RuntimeException("Removing unknown TestCase");
            }
        }

        public void testStepAdded(TestStep testStep, int index) {
            TestCaseTreeNode testCaseTreeNode = (TestCaseTreeNode) getTreeModel().getTreeNode(testStep.getTestCase());
            if (testCaseTreeNode != null) {
                testCaseTreeNode.testStepInserted(testStep, index);
            }
        }

        public void testStepRemoved(TestStep testStep, int index) {
            TestCaseTreeNode testCaseTreeNode = (TestCaseTreeNode) getTreeModel().getTreeNode(testStep.getTestCase());
            if (testCaseTreeNode != null) {
                testCaseTreeNode.testStepRemoved(testStep, index);
            }
        }

        public void loadTestAdded(LoadTest loadTest) {
            TestCaseTreeNode testCaseTreeNode = (TestCaseTreeNode) getTreeModel().getTreeNode(loadTest.getTestCase());
            if (testCaseTreeNode != null) {
                testCaseTreeNode.loadTestInserted(loadTest);
            }
        }

        public void loadTestRemoved(LoadTest loadTest) {
            TestCaseTreeNode testCaseTreeNode = (TestCaseTreeNode) getTreeModel().getTreeNode(loadTest.getTestCase());
            if (testCaseTreeNode != null) {
                testCaseTreeNode.loadTestRemoved(loadTest);
            }
        }

        public void testStepMoved(TestStep testStep, int fromIndex, int offset) {
            TestCaseTreeNode testCaseTreeNode = (TestCaseTreeNode) getTreeModel().getTreeNode(testStep.getTestCase());
            if (testCaseTreeNode != null) {
                testCaseTreeNode.testStepMoved(testStep, fromIndex, offset);
            }
        }

        public void testCaseMoved(TestCase testCase, int index, int offset) {
            testCaseRemoved(testCase);
            testCaseAdded(testCase);
        }

        @Override
        public void securityTestAdded(SecurityTest securityTest) {
            TestCaseTreeNode testCaseTreeNode = (TestCaseTreeNode) getTreeModel()
                    .getTreeNode(securityTest.getTestCase());
            if (testCaseTreeNode != null) {
                testCaseTreeNode.securityTestInserted(securityTest);
            }

        }

        @Override
        public void securityTestRemoved(SecurityTest securityTest) {
            TestCaseTreeNode testCaseTreeNode = (TestCaseTreeNode) getTreeModel()
                    .getTreeNode(securityTest.getTestCase());
            if (testCaseTreeNode != null) {
                testCaseTreeNode.securityTestRemoved(securityTest);
            }

        }
    }
}
