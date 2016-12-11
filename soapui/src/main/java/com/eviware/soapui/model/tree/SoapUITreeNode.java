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

import javax.swing.JPopupMenu;
import javax.swing.tree.TreeNode;

/**
 * Behaviour for navigator tree nodes
 *
 * @author Ole.Matzura
 */

public interface SoapUITreeNode extends TreeNode {
    public int getChildCount();

    public int getIndexOfChild(Object child);

    public boolean valueChanged(Object newValue);

    public SoapUITreeNode getChildNode(int index);

    public boolean isLeaf();

    public JPopupMenu getPopup();

    public SoapUITreeNode getParentTreeNode();

    public void release();

    public ActionList getActions();

    public void reorder(boolean notify);

    public ModelItem getModelItem();
}
