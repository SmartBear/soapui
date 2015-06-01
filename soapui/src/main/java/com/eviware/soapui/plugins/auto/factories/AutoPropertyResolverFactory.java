package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.model.propertyexpansion.resolvers.PropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.PropertyResolverFactory;
import com.eviware.soapui.plugins.auto.PluginPropertyResolver;

/**
 * Created by ole on 15/06/14.
 */
public class AutoPropertyResolverFactory extends SimpleSoapUIFactory<PropertyResolver> implements PropertyResolverFactory {

    public AutoPropertyResolverFactory(PluginPropertyResolver annotation, Class<PropertyResolver> propertyResolverClass) {
        super(PropertyResolverFactory.class, propertyResolverClass);
    }

    @Override
    public PropertyResolver createPropertyResolver() {
        return create();
    }
}
