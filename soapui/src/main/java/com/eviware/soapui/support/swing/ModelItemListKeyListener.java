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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public abstract class ModelItemListKeyListener extends KeyAdapter {
    public void keyPressed(KeyEvent e) {
        int[] ix = ((JList) e.getSource()).getSelectedIndices();
        if (ix.length == 0) {
            return;
        }

        if (ix.length == 1) {
            ModelItem modelItem = getModelItemAt(ix[0]);
            ActionList actions = ActionListBuilder.buildActions(modelItem);
            if (actions != null) {
                actions.dispatchKeyEvent(e);
            }
        } else {
            ModelItem[] modelItems = new ModelItem[ix.length];

            for (int c = 0; c < ix.length; c++) {
                modelItems[c] = getModelItemAt(ix[c]);
            }

            ActionList actions = ActionListBuilder.buildMultiActions(modelItems);
            if (actions != null) {
                actions.dispatchKeyEvent(e);
            }
        }
    }

    public abstract ModelItem getModelItemAt(int ix);
}
