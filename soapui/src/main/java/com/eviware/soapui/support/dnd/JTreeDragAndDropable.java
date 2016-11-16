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

import javax.swing.JComponent;
import javax.swing.JTree;
import java.awt.Component;
import java.awt.Rectangle;

public abstract class JTreeDragAndDropable<T> implements SoapUIDragAndDropable<T> {
    private JTree tree;

    public JTreeDragAndDropable(JTree tree) {
        this.tree = tree;
    }

    public JTree getTree() {
        return tree;
    }

    public Component getCellRendererComponent(Object lastPathComponent, boolean b, boolean object, boolean object2,
                                              int i, boolean c) {
        return tree.getCellRenderer().getTreeCellRendererComponent(tree, lastPathComponent, b, object, object2, i, c);
    }

    public JComponent getComponent() {
        return tree;
    }

    public void setDragInfo(String dropInfo) {
        tree.setToolTipText(dropInfo);
    }

    public Rectangle getModelItemBounds(T path) {
        return tree.getRowBounds(getRowForModelItem(path));
    }

    public T getModelItemForLocation(int x, int y) {
        int rowForLocation = tree.getRowForLocation(x, y);
        if (rowForLocation == -1) {
            rowForLocation = tree.getClosestRowForLocation(x, y);
        }

        return getModelItemAtRow(rowForLocation);
    }

    public void selectModelItem(T path) {
        int row = getRowForModelItem(path);
        tree.setSelectionRow(row);
    }

    public void toggleExpansion(T last) {
        int row = getRowForModelItem(last);
        if (tree.isExpanded(row)) {
            tree.collapseRow(row);
        } else {
            tree.expandRow(row);
        }
    }

    public abstract int getRowForModelItem(T modelItem);

    public abstract T getModelItemAtRow(int row);

}
