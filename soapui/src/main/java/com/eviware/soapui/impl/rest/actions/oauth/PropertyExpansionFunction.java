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

package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Guava function that expands properties in a string, using a ModelItem as context.
 */
public class PropertyExpansionFunction implements Function<String, String> {

    private ModelItem contextModelItem;

    /**
     * Constructs a function object
     *
     * @param contextModelItem the model item to be used as context
     */
    public PropertyExpansionFunction(ModelItem contextModelItem) {
        this.contextModelItem = contextModelItem;
    }

    @Nullable
    @Override
    public String apply(@Nullable String unexpandedString) {
        if (unexpandedString == null) {
            return null;
        }
        return PropertyExpander.expandProperties(contextModelItem, unexpandedString);
    }
}
