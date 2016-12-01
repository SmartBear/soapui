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

package com.eviware.soapui.ui.desktop;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.PropertyChangeNotifier;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * Behaviour for a SoapUI desktop panel
 *
 * @author Ole.Matzura
 */

public interface DesktopPanel extends PropertyChangeNotifier {
    public final static String TITLE_PROPERTY = DesktopPanel.class.getName() + "@title";
    public final static String ICON_PROPERTY = DesktopPanel.class.getName() + "@icon";

    /**
     * Gets the title for this desktop panel
     */

    public String getTitle();

    /**
     * Gets the description for this desktop panel.. may be used as tooltip,
     * etc..
     *
     * @return
     */

    public String getDescription();

    /**
     * Gets the model item associated with this desktop panel
     */

    public ModelItem getModelItem();

    /**
     * Called when a desktop panel is about to be closed, may be overriden
     * (depending on situation) by returning false if canCancel is set to true.
     */

    public boolean onClose(boolean canCancel);

    /**
     * Gets the component used to display this desktop panel
     */

    public JComponent getComponent();

    /**
     * Checks if this desktop panel depends on the existence of the specified
     * model item, used for closing relevant panels.
     *
     * @param modelItem
     */

    public boolean dependsOn(ModelItem modelItem);

    /**
     * Returns the icon for this panel
     */

    public Icon getIcon();
}
