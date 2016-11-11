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

package com.eviware.soapui.support.components;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;

/**
 * A wrapper class to accociate
 */
public final class PropertyComponent {
    @Nullable
    private final String property;
    @Nonnull
    private final JComponent component;

    public PropertyComponent(JComponent component) {
        this(null, component);
    }

    public PropertyComponent(String property, JComponent component) {
        this.property = property;

        Preconditions.checkNotNull("You must provide a component", component);
        this.component = component;
    }

    public String getProperty() {
        return property;
    }

    public JComponent getComponent() {
        return component;
    }

    public boolean hasProperty() {
        return property != null;
    }
}
