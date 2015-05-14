package com.eviware.soapui.ui.toolbar;

import javax.swing.JComponent;

public interface ToolbarItem {
    JComponent getComponent();

    /**
     * Order where it should be displayed in the toolbar group. Starts from 0.
     *
     * @return index in the toolbar
     */
    int getToolbarIndex();
}
