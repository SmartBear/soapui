package com.eviware.soapui.model.tree.nodes;

import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;
import com.eviware.soapui.support.UISupport;

public class PropertiesModelItem extends EmptyModelItem {
    private final TestPropertyHolder holder;

    public PropertiesModelItem(TestPropertyHolder holder) {
        super("Properties (" + holder.getPropertyNames().length + ")", UISupport
                .createImageIcon("/properties_step.png"));
        this.holder = holder;
    }

    public void updateName() {
        setName("Properties (" + holder.getPropertyNames().length + ")");
    }

    public TestPropertyHolder getHolder() {
        return holder;
    }
}
