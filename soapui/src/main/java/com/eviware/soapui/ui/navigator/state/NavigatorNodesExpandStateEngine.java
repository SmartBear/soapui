package com.eviware.soapui.ui.navigator.state;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.plugins.factories.navigator.NavigatorNodesExpandStateProvider;
import com.eviware.soapui.plugins.factories.navigator.NavigatroNodeExpandStateProviderFactory;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.List;

public class NavigatorNodesExpandStateEngine {
    private JTree navigatorTree;
    private NavigatorNodesExpandStateProvider navigatorNodesExpandStateProvider;
    private NavigatorTreeExpanedListener treeExpansionListener;

    /**
     * take "currentNode" and check historical state.
     * if it is expanded method will try to update "currentNode" and all its children nodes
     *
     * @param currentNode
     * @param soapUITreeModel
     */
    private void restoreNodeHistoricalState(SoapUITreeNode currentNode, SoapUITreeModel soapUITreeModel) {
        ModelItem modelItem = currentNode.getModelItem();
        boolean isExpanded = navigatorNodesExpandStateProvider.isExpanded(modelItem);
        if (!isExpanded) {
            return;
        }
        TreePath treePath = soapUITreeModel.getPath(currentNode);
        navigatorTree.expandPath(treePath);

        int childCount = currentNode.getChildCount();
        for (int i = 0; i < childCount; i++) {
            restoreNodeHistoricalState(currentNode.getChildNode(i), soapUITreeModel);
        }
    }

    /**
     * restore historical node state
     *
     * @param node
     */
    private void restoreNodeHistoricalState(SoapUITreeNode node) {
        SoapUITreeModel soapUITreeModel = (SoapUITreeModel) navigatorTree.getModel();
        restoreNodeHistoricalState(node, soapUITreeModel);
    }

    /**
     * take tree and restore it nodes state
     *
     * @param tree
     */
    private void restoreExpandedState(JTree tree) {
        TreeModel treeModel = tree.getModel();
        // second condition needed to use internal methods.
        if (treeModel == null || !(treeModel instanceof SoapUITreeModel)) {
            return;
        }
        SoapUITreeModel soapUITreeModel = (SoapUITreeModel) treeModel;
        Object rootObject = treeModel.getRoot();
        if (rootObject == null || !(rootObject instanceof SoapUITreeNode)) {
            return;
        }
        SoapUITreeNode rootNode = (SoapUITreeNode) rootObject;
        int childNodesAmount = rootNode.getChildCount();
        for (int i = 0; i < childNodesAmount; i++) {
            restoreNodeHistoricalState(rootNode.getChildNode(i), soapUITreeModel);
        }
    }

    public NavigatorNodesExpandStateEngine() {
    }

    /**
     * try to find first available provider instance
     *
     * @return
     */
    private NavigatorNodesExpandStateProvider getNavigatorNodesExpandStateProvider() {
        List<NavigatroNodeExpandStateProviderFactory> stateProviderFactoryList = SoapUI.getFactoryRegistry().getFactories(NavigatroNodeExpandStateProviderFactory.class);
        if (stateProviderFactoryList.size() == 0) {
            return null;
        }
        NavigatroNodeExpandStateProviderFactory providerFactory = stateProviderFactoryList.get(0);
        NavigatorNodesExpandStateProvider result = providerFactory.create();
        return result;
    }

    /**
     * get "navigatorTree" and initialize engine.
     * if state provider is empty nothing will happen
     *
     * @param navigatorTree
     */
    public void initialize(JTree navigatorTree) {
        navigatorNodesExpandStateProvider = getNavigatorNodesExpandStateProvider();
        if (navigatorNodesExpandStateProvider == null) {
            return;
        }
        this.navigatorTree = navigatorTree;
        restoreExpandedState(navigatorTree);

        treeExpansionListener = new NavigatorTreeExpanedListener(this);
        navigatorTree.addTreeExpansionListener(treeExpansionListener);
    }

    /**
     * set actual provider values for changed node
     *
     * @param node
     * @param expand
     */
    void setExpandedState(SoapUITreeNode node, boolean expand) {
        ModelItem modelItem = node.getModelItem();
        navigatorNodesExpandStateProvider.setExpandedState(modelItem, expand);
        if (expand) {
            restoreNodeHistoricalState(node);
        }
    }
}
