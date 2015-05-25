package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.model.propertyexpansion.resolvers.DynamicPropertyResolver;
import com.eviware.soapui.plugins.auto.PluginValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoValueProviderFactory extends
        SimpleSoapUIFactory<DynamicPropertyResolver.ValueProvider> implements DynamicPropertyResolver.ValueProviderFactory {
    private String valueId;

    private final static Logger logger = LoggerFactory.getLogger(AutoValueProviderFactory.class);

    public AutoValueProviderFactory(PluginValueProvider annotation, Class<DynamicPropertyResolver.ValueProvider> valueProviderClass) {
        super(DynamicPropertyResolver.ValueProviderFactory.class, valueProviderClass);
        valueId = valueProviderClass.getAnnotation(PluginValueProvider.class).valueName();
        logger.debug("Added ValueProvider for [" + valueId + "]");
    }

    @Override
    public DynamicPropertyResolver.ValueProvider createValueProvider() {
        return create();
    }

    @Override
    public String getValueId() {
        return valueId;
    }
}
