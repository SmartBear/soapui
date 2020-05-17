package com.eviware.soapui.model.tree.nodes.support;

import com.eviware.soapui.model.testsuite.TestCase;

import javax.swing.ImageIcon;

public class BaseTestsModelItem extends EmptyModelItem {
    protected TestCase testCase;

    public BaseTestsModelItem(TestCase testCase, String name, ImageIcon icon) {
        super(name, icon);
        this.testCase = testCase;
    }

    public TestCase getTestCase() {
        return testCase;
    }

}
