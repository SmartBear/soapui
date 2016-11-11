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

package com.eviware.soapui.impl.wsdl.actions.iface;

import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Utility class for creating Swing Actions for ModelItems
 *
 * @author ole.matzura
 */

public abstract class AbstractSwingAction<T extends Object> extends AbstractAction {
    private T modelItem;
    private final String name;

    public AbstractSwingAction(String name, String description) {
        super(name);
        this.name = name;
        this.modelItem = null;

        putValue(Action.SHORT_DESCRIPTION, description);
    }

    public AbstractSwingAction(String name, String description, T modelItem) {
        super(name);
        this.name = name;
        this.modelItem = modelItem;

        putValue(Action.SHORT_DESCRIPTION, description);
    }

    public AbstractSwingAction(String name, String description, String iconUrl) {
        super(name);
        this.name = name;
        this.modelItem = null;

        putValue(Action.SHORT_DESCRIPTION, description);
        putValue(Action.SMALL_ICON, UISupport.createImageIcon(iconUrl));
    }

    public AbstractSwingAction(String name, String description, String iconUrl, T modelItem) {
        super(name);
        this.name = name;
        this.modelItem = modelItem;

        putValue(Action.SHORT_DESCRIPTION, description);
        putValue(Action.SMALL_ICON, UISupport.createImageIcon(iconUrl));
    }

    public void actionPerformed(ActionEvent arg0) {
        actionPerformed(arg0, modelItem);
    }

    public String getName() {
        return name;
    }

    public abstract void actionPerformed(ActionEvent arg0, T modelItem2);

    public T getModelItem() {
        return modelItem;
    }
}
