/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.model.tree;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.tree.nodes.PropertiesTreeNode;
import com.eviware.soapui.model.tree.nodes.PropertyTreeNode;
import com.eviware.soapui.model.tree.nodes.WorkspaceTreeNode;
import com.eviware.soapui.model.workspace.Workspace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Navigator TreeModel
 *
 * @author Ole.Matzura
 */

public class SoapUITreeModel implements TreeModel {
    private Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();
    private SoapUITreeNode workspaceNode;
    private final static Logger logger = LogManager.getLogger(SoapUITreeModel.class);
    private Map<ModelItem, SoapUITreeNode> modelItemMap = new HashMap<ModelItem, SoapUITreeNode>();
    private boolean showProperties = false;

    public SoapUITreeModel(Workspace workspace) {
        workspaceNode = new WorkspaceTreeNode(workspace, this);
        mapModelItem(workspaceNode);
    }

    public Object getRoot() {
        return workspaceNode;
    }

    public Object getChild(Object parent, int index) {
        SoapUITreeNode treeNode = (SoapUITreeNode) parent;
        return treeNode.getChildNode(index);
    }

    public int getChildCount(Object parent) {
        SoapUITreeNode treeNode = (SoapUITreeNode) parent;
        return treeNode.getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        SoapUITreeNode treeNode = (SoapUITreeNode) node;
        return treeNode.isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        SoapUITreeNode treeNode = (SoapUITreeNode) path.getLastPathComponent();
        if (treeNode.valueChanged(newValue)) {
            // not implemented.. need to expose setName in ModelItem
        }
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        SoapUITreeNode treeNode = (SoapUITreeNode) parent;
        return treeNode.getIndexOfChild(child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    public void mapModelItem(SoapUITreeNode soapUITreeNode) {
        modelItemMap.put(soapUITreeNode.getModelItem(), soapUITreeNode);
    }

    public void unmapModelItem(ModelItem modelItem) {
        if (modelItemMap.containsKey(modelItem)) {
            modelItemMap.remove(modelItem);
        } else {
            logger.error("Failed to unmap model item [" + modelItem.getName() + "]");
            Thread.dumpStack();
        }
    }

    public void notifyNodesInserted(TreeModelEvent e) {
        Iterator<TreeModelListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().treeNodesInserted(e);
        }
    }

    public void notifyNodesRemoved(TreeModelEvent e) {
        Iterator<TreeModelListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().treeNodesRemoved(e);
        }
    }

    public void notifyStructureChanged(TreeModelEvent e) {
        Iterator<TreeModelListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().treeStructureChanged(e);
        }
    }

    public void notifyNodesChanged(TreeModelEvent e) {
        Iterator<TreeModelListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().treeNodesChanged(e);
        }
    }

    public TreePath getPath(SoapUITreeNode treeNode) {
        // SoapUITreeNode treeNode = modelItemMap.get( modelItem );
        // if( treeNode == null )
        // throw new RuntimeException( "Missing mapping for modelItem " +
        // modelItem.getName() );

        List<Object> nodes = new ArrayList<Object>();
        if (treeNode != null) {
            nodes.add(treeNode);

            treeNode = treeNode.getParentTreeNode();
            while (treeNode != null) {
                nodes.add(0, treeNode);
                treeNode = treeNode.getParentTreeNode();
            }
        }

        return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
    }

    public void notifyNodeChanged(SoapUITreeNode treeNode) {
        SoapUITreeNode parent = treeNode.getParentTreeNode();
        if (parent == null) {
            notifyNodesChanged(new TreeModelEvent(this, new Object[]{treeNode}));
            return;
        }

        int ix = parent.getIndexOfChild(treeNode);

        if (ix == -1) {
            if ((!(treeNode instanceof PropertyTreeNode) && !(treeNode instanceof PropertiesTreeNode))
                    || isShowProperties()) {
                logger.error("Changed node [" + treeNode + "] not found in parent [" + parent + "]");
            }

            return;
        }

        if (!(treeNode instanceof PropertyTreeNode) || showProperties) {
            notifyNodesChanged(new TreeModelEvent(this, getPath(parent), new int[]{ix},
                    new Object[]{parent.getChildNode(ix)}));
        }
    }

    public void notifyNodeInserted(SoapUITreeNode treeNode) {
        SoapUITreeNode parent = treeNode.getParentTreeNode();
        int ix = parent.getIndexOfChild(treeNode);

        if (ix == -1) {
            logger.error("Inserted node [" + treeNode + "] not found in parent [" + parent + "]");
            return;
        }

        mapModelItem(treeNode);

        if (!(treeNode instanceof PropertyTreeNode) || showProperties) {
            notifyNodesInserted(new TreeModelEvent(this, getPath(parent), new int[]{ix},
                    new Object[]{parent.getChildNode(ix)}));
        }
    }

    public void notifyNodeRemoved(SoapUITreeNode treeNode) {
        notifyNodeRemoved(treeNode, true);
    }

    public void notifyNodeRemoved(SoapUITreeNode treeNode, boolean release) {
        SoapUITreeNode parent = treeNode.getParentTreeNode();
        int ix = parent.getIndexOfChild(treeNode);

        if (ix == -1) {
            logger.error("Removed node [" + treeNode + "] not found in parent [" + parent + "]");
            return;
        }

        if (!(treeNode instanceof PropertyTreeNode) || showProperties) {
            notifyNodesRemoved(new TreeModelEvent(this, getPath(parent), new int[]{ix},
                    new Object[]{parent.getChildNode(ix)}));
        }

        if (release) {
            treeNode.release();
        }
    }

    public SoapUITreeNode getTreeNode(ModelItem parentItem) {
        return modelItemMap.get(parentItem);
    }

    public TreePath getPath(ModelItem modelItem) {
        return getPath(modelItemMap.get(modelItem));
    }

    public void mapModelItems(List<? extends SoapUITreeNode> treeNodes) {
        Iterator<? extends SoapUITreeNode> iterator = treeNodes.iterator();
        while (iterator.hasNext()) {
            SoapUITreeNode item = iterator.next();
            mapModelItem(item);
        }
    }

    public boolean isShowProperties() {
        return showProperties;
    }

    public void setShowProperties(boolean showProperties) {
        if (this.showProperties != showProperties) {
            this.showProperties = showProperties;
            notifyStructureChanged(new TreeModelEvent(this, getPath(workspaceNode)));
        }
    }
}
