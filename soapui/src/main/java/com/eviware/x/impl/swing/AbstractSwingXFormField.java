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

package com.eviware.x.impl.swing;

import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.AbstractXFormField;

import javax.swing.JComponent;
import java.awt.Dimension;

public abstract class AbstractSwingXFormField<T extends JComponent> extends AbstractXFormField<T> {
    private T component;

    public AbstractSwingXFormField(T component) {
        this.component = component;
    }

    public T getComponent() {
        return component;
    }

    public void setToolTip(String tooltip) {
        component.setToolTipText(tooltip);
        component.getAccessibleContext().setAccessibleDescription(tooltip);
    }

    public boolean isEnabled() {
        return component.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        component.setEnabled(enabled);
    }

    public void setProperty(String name, Object value) {
        if (name.equals("dimension")) {
            UISupport.setFixedSize(getComponent(), (Dimension) value);
        }
    }
}
