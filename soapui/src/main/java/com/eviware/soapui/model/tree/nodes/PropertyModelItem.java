package com.eviware.soapui.model.tree.nodes;

import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;
import com.eviware.soapui.support.UISupport;

public class PropertyModelItem extends EmptyModelItem {
    private final TestProperty property;
    private String xpath;

    public PropertyModelItem(TestProperty property, boolean readOnly) {
        super(PropertyTreeNode.buildName(property), readOnly ? UISupport.createImageIcon("/bullet_black.gif") : UISupport
                .createImageIcon("/bullet_green.gif"));

        this.property = property;
    }

    public TestProperty getProperty() {
        return property;
    }

    public String getXPath() {
        return xpath;
    }

    public void setXPath(String xpath) {
        this.xpath = xpath;
    }
}