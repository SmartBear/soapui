package com.smartbear.ready.ui;

import com.eviware.soapui.ui.navigator.state.NavigatorNodesExpandStateProvider;

public class UIManager {
    // it may be only single expand state provider.
    private static NavigatorNodesExpandStateProvider navigatorNodesExpandStateProvider = null;

    public static NavigatorNodesExpandStateProvider getNavigatorNodesExpandStateProvider() {
        return navigatorNodesExpandStateProvider;
    }

    public static void setNavigatorNodesExpandStateProvider(NavigatorNodesExpandStateProvider navigatorNodesExpandStateProvider) {
        UIManager.navigatorNodesExpandStateProvider = navigatorNodesExpandStateProvider;
    }

}
