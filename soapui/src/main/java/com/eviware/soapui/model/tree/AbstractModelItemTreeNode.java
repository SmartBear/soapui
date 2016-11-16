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

package com.eviware.soapui.model.tree;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Abstract base class for SoapUITreeNode implementations
 *
 * @author Ole.Matzura
 */

public abstract class AbstractModelItemTreeNode<T extends ModelItem> implements SoapUITreeNode, PropertyChangeListener {
    private final T modelItem;
    private final ModelItem parentItem;
    private final SoapUITreeModel treeModel;
    private List<? extends SoapUITreeNode> orderItems;
    private String orderSetting;
    private InternalSettingsListener internalSettingsListener;

    protected AbstractModelItemTreeNode(T modelItem, ModelItem parentItem, SoapUITreeModel treeModel) {
        this.modelItem = modelItem;
        this.parentItem = parentItem;
        this.treeModel = treeModel;

        modelItem.addPropertyChangeListener(this);
    }

    public SoapUITreeModel getTreeModel() {
        return treeModel;
    }

    public T getModelItem() {
        return modelItem;
    }

    public boolean valueChanged(Object newValue) {
        return false;
    }

    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    public int getChildCount() {
        return orderItems == null ? 0 : orderItems.size();
    }

    public SoapUITreeNode getChildNode(int index) {
        return orderItems == null ? null : orderItems.get(index);
    }

    public int getIndexOfChild(Object child) {
        return orderItems == null ? -1 : orderItems.indexOf(child);
    }

    public String toString() {
        if (modelItem instanceof TestStep) {
            return ((TestStep) modelItem).getLabel();
        } else if (modelItem instanceof TestCase) {
            return ((TestCase) modelItem).getLabel();
        }

        return modelItem.getName();
    }

    public JPopupMenu getPopup() {
        return ActionSupport.buildPopup(getActions());
    }

    public ActionList getActions() {
        return ActionListBuilder.buildActions(modelItem);
    }

    public SoapUITreeNode getParentTreeNode() {
        return treeModel.getTreeNode(parentItem);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (propertyName.equals(ModelItem.NAME_PROPERTY) || propertyName.equals(ModelItem.LABEL_PROPERTY)) {
            // use this since length has probably changed
            getTreeModel().notifyNodeChanged(this);
        } else if (propertyName.equals(ModelItem.ICON_PROPERTY)) {
            // hack to improve rendering performance
            JTree mainTree = SoapUI.getNavigator().getMainTree();
            TreePath nodePath = getTreeModel().getPath(this);
            Rectangle rowBounds = mainTree.getPathBounds(nodePath);
            if (rowBounds != null) {
                mainTree.repaint(rowBounds);
            }
        }
    }

    public void release() {
        modelItem.removePropertyChangeListener(this);

        if (internalSettingsListener != null) {
            SoapUI.getSettings().removeSettingsListener(internalSettingsListener);
        }

        getTreeModel().unmapModelItem(modelItem);
    }

    public <T2 extends SoapUITreeNode> void initOrdering(List<T2> items, String setting) {
        this.orderItems = items;
        this.orderSetting = setting;

        internalSettingsListener = new InternalSettingsListener(this, setting);
        SoapUI.getSettings().addSettingsListener(internalSettingsListener);
        sortModelItems(items, setting);
    }

    private final class InternalSettingsListener implements SettingsListener {
        private final AbstractModelItemTreeNode<?> node;
        private final String setting;

        public InternalSettingsListener(AbstractModelItemTreeNode<?> node, String setting) {
            this.node = node;
            this.setting = setting;
        }

        public void settingChanged(String name, String newValue, String oldValue) {
            if (name.equals(setting)) {
                if (oldValue == null) {
                    oldValue = "false";
                }

                if (newValue == null) {
                    newValue = "false";
                }

                if (!oldValue.equals(newValue)) {
                    TreePath path = getTreeModel().getPath(AbstractModelItemTreeNode.this);
                    node.reorder(SoapUI.getNavigator().isVisible(path) && SoapUI.getNavigator().isExpanded(path));
                }
            }
        }

        @Override
        public void settingsReloaded() {
            // TODO Auto-generated method stub
        }
    }

    public void reorder(boolean notify) {
        if (orderItems != null) {
            sortModelItems(orderItems, orderSetting);

            if (notify) {
                getTreeModel().notifyStructureChanged(new TreeModelEvent(this, getTreeModel().getPath(this)));
            }
        }
    }

    public <T2 extends SoapUITreeNode> void sortModelItems(List<T2> modelItems, final String setting) {
        Collections.sort(modelItems, new Comparator<T2>() {
            public int compare(T2 o1, T2 o2) {
                String name1 = o1.getModelItem().getName();
                String name2 = o2.getModelItem().getName();

                if (name1 == null && name2 == null) {
                    return 0;
                } else if (name1 == null) {
                    return -1;
                } else if (name2 == null) {
                    return 1;
                } else if (setting != null && SoapUI.getSettings().getBoolean(setting)) {
                    return name1.compareToIgnoreCase(name2);
                } else {
                    return name1.compareTo(name2);
                }
            }
        });
    }

    public Enumeration<?> children() {
        Vector<TreeNode> children = new Vector<TreeNode>();
        for (int c = 0; c < getChildCount(); c++) {
            children.add(getChildAt(c));
        }

        return children.elements();
    }

    public boolean getAllowsChildren() {
        return !isLeaf();
    }

    public TreeNode getChildAt(int childIndex) {
        return getChildNode(childIndex);
    }

    public int getIndex(TreeNode node) {
        return getIndexOfChild(node);
    }

    public TreeNode getParent() {
        return getParentTreeNode();
    }

    public class ReorderPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent arg0) {
            reorder(true);
            SoapUI.getNavigator().selectModelItem((ModelItem) arg0.getSource());
        }
    }
}
