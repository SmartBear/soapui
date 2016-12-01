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

package com.eviware.soapui.model.propertyexpansion;

import com.eviware.soapui.model.ModelItem;

import java.util.ArrayList;

public class PropertyExpansionsResult extends ArrayList<PropertyExpansion> {
    private final ModelItem modelItem;
    private final Object defaultTarget;

    public PropertyExpansionsResult(ModelItem modelItem) {
        this(modelItem, modelItem);
    }

    public PropertyExpansionsResult(ModelItem modelItem, Object defaultTarget) {
        this.modelItem = modelItem;
        this.defaultTarget = defaultTarget;
    }

    public boolean extractAndAddAll(Object target, String propertyName) {
        return addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, target, propertyName));
    }

    public boolean extractAndAddAll(String propertyName) {
        return addAll(PropertyExpansionUtils.extractPropertyExpansions(modelItem, defaultTarget, propertyName));
    }

    public PropertyExpansion[] toArray() {
        return toArray(new PropertyExpansion[size()]);
    }

    public void addAll(PropertyExpansion[] propertyExpansions) {
        if (propertyExpansions == null) {
            return;
        }

        for (PropertyExpansion pe : propertyExpansions) {
            add(pe);
        }
    }
}
