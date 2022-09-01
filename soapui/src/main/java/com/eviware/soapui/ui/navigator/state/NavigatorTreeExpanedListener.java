package com.eviware.soapui.ui.navigator.state;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.tree.SoapUITreeNode;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;

/**
 * helper class to handle node state engine behaviour
 */
class NavigatorTreeExpanedListener implements TreeExpansionListener {
    private NavigatorNodesExpandStateEngine navigatorNodesExpandStateEngine;

    NavigatorTreeExpanedListener(NavigatorNodesExpandStateEngine navigatorNodesExpandStateEngine) {
        this.navigatorNodesExpandStateEngine = navigatorNodesExpandStateEngine;
    }

    void switchNodeState(TreeExpansionEvent event, boolean expand) {
        TreePath path = event.getPath();
        Object currentNode = path.getLastPathComponent();
        if (!(currentNode instanceof SoapUITreeNode)) {
            return;
        }
        SoapUITreeNode soapUITreeNode = (SoapUITreeNode) currentNode;
        navigatorNodesExpandStateEngine.setExpandedState(soapUITreeNode, expand);
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        switchNodeState(event, true);
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        switchNodeState(event, false);
    }
}
