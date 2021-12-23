package com.eviware.soapui.plugins.factories.navigator;

import com.eviware.soapui.model.ModelItem;

/**
 * interface to manage navigator nodes state.
 * only single instance is available to work.
 * Do not use this provider to perfect performance.
 * Provider contains needed functions to work.
 * How to use these functions and what to do with data depends on implementation.
 */
public interface NavigatorNodesExpandStateProvider {

    /**
     * called when certain modelItem node change its state
     *
     * @param modelItem
     * @param expanded
     */
    void setExpandedState(ModelItem modelItem, boolean expanded);

    /**
     * called to take historical modelItem node state
     * @param modelItem
     * @return
     */
    boolean isExpanded(ModelItem modelItem);
}

