package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.impl.actions.DiscoveryMethod;
import com.eviware.soapui.impl.actions.DiscoveryMethodFactory;
import com.eviware.soapui.plugins.auto.PluginDiscoveryMethod;

/**
 * Created by ole on 15/06/14.
 */
public class AutoDiscoveryMethodFactory extends SimpleSoapUIFactory<DiscoveryMethod> implements DiscoveryMethodFactory {

    public AutoDiscoveryMethodFactory(PluginDiscoveryMethod annotation, Class<DiscoveryMethod> methodClass) {
        super(DiscoveryMethodFactory.class, methodClass);
    }

    @Override
    public DiscoveryMethod createNewDiscoveryMethod() {
        return create();
    }
}
