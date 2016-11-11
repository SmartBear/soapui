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
