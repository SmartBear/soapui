package com.eviware.soapui.ui.navigator;

import com.eviware.soapui.model.tree.SoapUITreeModel;

import javax.swing.JTree;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;

class NavigatorTree extends JTree implements Autoscroll {
    public NavigatorTree(SoapUITreeModel treeModel) {
        super(treeModel);
    }

    private static final int AUTOSCROLL_MARGIN = 12;

    public void autoscroll(Point pt) {
        // Figure out which row we�re on.
        int nRow = getRowForLocation(pt.x, pt.y);

        // If we are not on a row then ignore this autoscroll request
        if (nRow < 0) {
            return;
        }

        Rectangle raOuter = getBounds();
        // Now decide if the row is at the top of the screen or at the
        // bottom. We do this to make the previous row (or the next
        // row) visible as appropriate. If we�re at the absolute top or
        // bottom, just return the first or last row respectively.

        nRow = (pt.y + raOuter.y <= AUTOSCROLL_MARGIN) // Is row at top of
                // screen?
                ? (nRow <= 0 ? 0 : nRow - 1) // Yes, scroll up one row
                : (nRow < getRowCount() - 1 ? nRow + 1 : nRow); // No,
        // scroll
        // down one
        // row

        scrollRowToVisible(nRow);
    }

    // Calculate the insets for the *JTREE*, not the viewport
    // the tree is in. This makes it a bit messy.
    public Insets getAutoscrollInsets() {
        Rectangle raOuter = getBounds();
        Rectangle raInner = getParent().getBounds();
        return new Insets(raInner.y - raOuter.y + AUTOSCROLL_MARGIN, raInner.x - raOuter.x + AUTOSCROLL_MARGIN,
                raOuter.height - raInner.height - raInner.y + raOuter.y + AUTOSCROLL_MARGIN, raOuter.width
                - raInner.width - raInner.x + raOuter.x + AUTOSCROLL_MARGIN);
    }
}