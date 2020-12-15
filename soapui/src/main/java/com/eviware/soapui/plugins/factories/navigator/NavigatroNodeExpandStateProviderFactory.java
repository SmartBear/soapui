package com.eviware.soapui.plugins.factories.navigator;

/**
 * factory to create NavigatorNodesExpandStateProvider
 */
public interface NavigatroNodeExpandStateProviderFactory {

    /**
     * @return provider to handle nodes state
     */
    NavigatorNodesExpandStateProvider create();
}
