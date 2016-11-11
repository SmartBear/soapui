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

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import java.beans.PropertyChangeListener;

public interface Inspector {
    public final static String TITLE_PROPERTY = Inspector.class.getName() + "@title";
    public final static String ICON_PROPERTY = Inspector.class.getName() + "@icon";
    public final static String DESCRIPTION_PROPERTY = Inspector.class.getName() + "@description";
    public final static String ENABLED_PROPERTY = Inspector.class.getName() + "@enabled";

    public abstract String getTitle();

    public abstract ImageIcon getIcon();

    public abstract JComponent getComponent();

    public abstract String getDescription();

    public abstract boolean isEnabled();

    public abstract void addPropertyChangeListener(PropertyChangeListener listener);

    public abstract void removePropertyChangeListener(PropertyChangeListener listener);

    public abstract String getInspectorId();

    public abstract void release();

    public abstract void activate();

    public abstract void deactivate();
}
