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

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;

import javax.swing.JPopupMenu;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Base implementation of SoapUITreeNode interface
 *
 * @author Ole.Matzura
 */

public abstract class AbstractTreeNode<T extends ModelItem> implements SoapUITreeNode {
    private T modelItem;

    public AbstractTreeNode(T modelItem) {
        this.modelItem = modelItem;
    }

    public boolean valueChanged(Object newValue) {
        return false;
    }

    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    public JPopupMenu getPopup() {
        return ActionSupport.buildPopup(getActions());
    }

    public ActionList getActions() {
        return ActionListBuilder.buildActions(modelItem);
    }

    public T getModelItem() {
        return modelItem;
    }

    public String toString() {
        return modelItem.getName();
    }

    public void reorder(boolean notify) {
    }

    public Enumeration children() {
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

}
