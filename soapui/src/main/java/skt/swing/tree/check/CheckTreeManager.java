/**
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2005  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package skt.swing.tree.check;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Santhosh Kumar T
 * @email  santhosh@in.fiorano.com
 */
public class CheckTreeManager extends MouseAdapter implements TreeSelectionListener{
    private CheckTreeSelectionModel selectionModel;
    private TreePathSelectable selectable;
    protected JTree tree = new JTree();
    int hotspot = new JCheckBox().getPreferredSize().width;

    public CheckTreeManager(JTree tree, boolean dig, TreePathSelectable selectable){
        this.tree = tree;
        selectionModel = new CheckTreeSelectionModel(tree.getModel(), dig);
        this.selectable = selectable;

        // note: if largemodel is not set
        // then treenodes are getting truncated.
        // need to debug further to find the problem
        if(selectable!=null)
            tree.setLargeModel(true);

        tree.setCellRenderer(new CheckTreeCellRenderer(tree.getCellRenderer(), selectionModel, selectable));
        tree.addMouseListener(this);
        selectionModel.addTreeSelectionListener(this);
    }

    public TreePathSelectable getSelectable(TreePathSelectable selectable){
        return selectable;
    }

    public void mouseClicked(MouseEvent me){
        TreePath path = tree.getPathForLocation(me.getX(), me.getY());
        if(path==null)
            return;
        if(me.getX()>tree.getPathBounds(path).x+hotspot)
            return;

        if(selectable!=null && !selectable.isSelectable(path))
            return;

        boolean selected = selectionModel.isPathSelected(path, selectionModel.isDigged());
        selectionModel.removeTreeSelectionListener(this);

        try{
            if(selected)
                selectionModel.removeSelectionPath(path);
            else
                selectionModel.addSelectionPath(path);
        } finally{
            selectionModel.addTreeSelectionListener(this);
            tree.treeDidChange();
        }
    }

    public CheckTreeSelectionModel getSelectionModel(){
        return selectionModel;
    }

    public void valueChanged(TreeSelectionEvent e){
        tree.treeDidChange();
    }
}