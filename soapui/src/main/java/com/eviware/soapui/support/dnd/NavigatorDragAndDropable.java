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

package com.eviware.soapui.support.dnd;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.tree.SoapUITreeNode;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.awt.Component;

public class NavigatorDragAndDropable extends JTreeDragAndDropable<ModelItem> {
    public NavigatorDragAndDropable(JTree tree) {
        super(tree);
    }

    @Override
    public ModelItem getModelItemAtRow(int row) {
        TreePath pathForRow = getTree().getPathForRow(row);
        return pathForRow == null ? null : ((SoapUITreeNode) pathForRow.getLastPathComponent()).getModelItem();
    }

    @Override
    public int getRowForModelItem(ModelItem modelItem) {
        if (modelItem == null) {
            return -1;
        }

        TreePath treePath = SoapUI.getNavigator().getTreePath(modelItem);
        return getTree().getRowForPath(treePath);
    }

    public Component getRenderer(ModelItem modelItem) {
        TreePath treePath = SoapUI.getNavigator().getTreePath(modelItem);
        int row = getTree().getRowForPath(treePath);
        SoapUITreeNode treeNode = (SoapUITreeNode) treePath.getLastPathComponent();

        return getTree().getCellRenderer().getTreeCellRendererComponent(getTree(), treeNode, true,
                getTree().isExpanded(row), treeNode.isLeaf(), row, true);
    }

    @Override
    public void toggleExpansion(ModelItem last) {
        if (last == SoapUI.getWorkspace()) {
            return;
        }

        super.toggleExpansion(last);
    }
}
