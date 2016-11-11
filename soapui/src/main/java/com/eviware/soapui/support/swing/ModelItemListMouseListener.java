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

package com.eviware.soapui.support.swing;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;

import javax.swing.JList;

/**
 * ListMouseListener for ModelItems
 *
 * @author ole.matzura
 */

public class ModelItemListMouseListener extends AbstractListMouseListener {
    public ModelItemListMouseListener() {
        this(true);
    }

    public ModelItemListMouseListener(boolean enablePopup) {
        super(enablePopup);
    }

    @Override
    protected ActionList getActionsForRow(JList list, int row) {
        ModelItem item = (ModelItem) list.getModel().getElementAt(row);
        try {
            return item == null ? null : ActionListBuilder.buildActions(item);
        } catch (Exception e) {
            return null;
        }
    }
}
