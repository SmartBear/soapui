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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;

@SuppressWarnings("serial")
public class ModelItemListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof ModelItem) {
            ModelItem item = (ModelItem) value;
            setIcon(item.getIcon());
            setText(item.getName());
        }

        return result;
    }

}
